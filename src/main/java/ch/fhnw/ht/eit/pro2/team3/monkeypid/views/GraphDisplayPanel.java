package ch.fhnw.ht.eit.pro2.team3.monkeypid.views;

import ch.fhnw.ht.eit.pro2.team3.monkeypid.controllers.Controller;
import ch.fhnw.ht.eit.pro2.team3.monkeypid.listeners.IModelListener;
import ch.fhnw.ht.eit.pro2.team3.monkeypid.models.CalculatorNames;
import ch.fhnw.ht.eit.pro2.team3.monkeypid.models.ClosedLoop;
import ch.fhnw.ht.eit.pro2.team3.monkeypid.models.Plant;

import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * GraphDisplayPanel is a panel which includes checkBoxes with names of all
 * simulated graphs. If a checkBox is set the appendant graph is displayed else
 * it isn't displayed anymore.
 * @author Josua Stierli
 */
public class GraphDisplayPanel extends JPanel implements ActionListener, IModelListener {

	private Controller controller;
	private HashMap<String, JCheckBox> checkBoxes = new HashMap<>();
	private View view;
	private boolean curvesDisplayOn = true;
	
	/**
	 * Constructor of GraphDisplayPanel set layout and commit controller and view
	 */
	public GraphDisplayPanel(Controller controller, View view) {
		// set layout to wrapLayout, (FlowLayout doesn't wrap after resize)
		super(new WrapLayout(WrapLayout.LEFT));
		this.controller = controller;
		this.view = view;
	}

	/**
	 * This is called, when the user clicks onto the check-boxes of the graph-curves
	 * then the curves are hidden/showed by the model
	 */
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
    	//check each checkbox, if it was the actionEvent
        for(Map.Entry<String, JCheckBox> entry : checkBoxes.entrySet()) {
            if (actionEvent.getSource() == entry.getValue()) {
                if (entry.getValue().isSelected()) {
                    controller.cbCheckAction(entry.getKey());
                } else {
                    controller.cbUncheckAction(entry.getKey());
                }
            }
        }
    }
    
    /**
     * This is called, when the user clicks onto the all-curves-display-toggle-button
     * This toggles the visibility of the curves (over the Controller and the model)
     * Toggles also the corresponding checkboxes (of the curves)
     */
    public void toggleDisplayAllCurves(){
    	//toggle mechanism 
    	if(curvesDisplayOn){
    		curvesDisplayOn = false;
			for(Map.Entry<String, JCheckBox> entry : checkBoxes.entrySet()) {
				controller.cbUncheckAction(entry.getKey());
				entry.getValue().setSelected(false); //uncheck the checkboxes
			}	
		}
		else{
			curvesDisplayOn = true;
			for(Map.Entry<String, JCheckBox> entry : checkBoxes.entrySet()) {
				controller.cbCheckAction(entry.getKey());
				entry.getValue().setSelected(true); //check the checkboxes
			}
		}
    }
    
    /**
     * This is called, when the user clicks onto the fistformula-curve-display-toggle-button
     * This toggles the visibility of the curves (over the Controller and the model)
     * Toggles also the corresponding checkboxes (of the curves)
     */
    public void toggleDisplayFistCurves() {
    	//toggle mechanism
    	if(curvesDisplayOn){
    		curvesDisplayOn = false;
			for(Map.Entry<String, JCheckBox> entry : checkBoxes.entrySet()) {
				//toggle only, if not a Zellweger-Curve
				if(!entry.getKey().equals(CalculatorNames.ZELLWEGER_I)) {
					controller.cbUncheckAction(entry.getKey());
					entry.getValue().setSelected(false); //uncheck the checkboxes
				}
			}	
		}
		else{
			curvesDisplayOn = true;
			for(Map.Entry<String, JCheckBox> entry : checkBoxes.entrySet()) {
				//toggle only, if not a Zellweger-Curve
				if(!entry.getKey().equals(CalculatorNames.ZELLWEGER_I)) {
					controller.cbCheckAction(entry.getKey());
					entry.getValue().setSelected(true); //check the checkboxes
				}
			}
		}
	}

    /**
     * Adds a the name for the new calculated stepResponse to a checkbox of this panel
     * The checkboxes are generated onSimulationBegin, so this method here has to find the 
     * corresponding checkbox for each Closed-Loop and has to connect it with the calculated
     * Closed-Loop
     */
	@Override
	public void onAddCalculation(ClosedLoop closedLoop, boolean visible) {
		SwingUtilities.invokeLater(() -> {

			// The checkboxes were already created and are stored using the table row index as the key.
			// This is a hack and relies on onAddCalculation only being called once for every calculation.
			JCheckBox cb = checkBoxes.get("" + closedLoop.getTableRowIndex());

			// since we're using this hack, we'll have to re-write the key if it was found to match
			// the name of the closed loop
			if(cb != null) {
				checkBoxes.remove("" + closedLoop.getTableRowIndex());
				checkBoxes.put(closedLoop.getName(), cb);
			}

			// If the key was not found (for instance, if the table row index was -1)
			// create a new checkbox
			if(cb == null) {
				cb = new JCheckBox();
			}

			//get rgbColor from closedLoop and convert it to string
			String hexColor = String.format("#%02x%02x%02x", closedLoop.getColor()
					.getRed(), closedLoop.getColor().getGreen(), closedLoop
					.getColor().getBlue());

			//set checkBox content: colored dot and name of closedLoop
			cb.setText("<html><font style=\"font-family: unicode \"color=" + hexColor
					+ ">" + "\u25CF" + "<font color=#000000>"
					+ closedLoop.getName());

			//set the checkbox selected dependent of the param visibles
			cb.setSelected(visible);
			cb.setVisible(false);

			//add actionListener to checkbox
			cb.addActionListener(this);
		 });
	}

	/**
	 * Removes the checkBoxes from the panel
	 */
	@Override
	public void onRemoveCalculation(ClosedLoop closedLoop) {
        SwingUtilities.invokeLater(() -> {
        	JCheckBox c = checkBoxes.remove(closedLoop.getName());
            remove(c);
            view.validate();	//triggers repaint of the GUI
        });
	}

	@Override
	public void onUpdateCalculation(ClosedLoop closedLoop) {
	}

	/**
	 * Creates number numberOfStepResponses dummyCheckboxes as place-holder, that the order
	 * of the curve-checkboxes is for each simulation the same
	 * The checkboxes will be set visible after all simulations are finished
	 * When each simulation is finished, the name of the checkbox is replaced by the real name of the
	 * Step-Response
	 */
    @Override
    public void onSimulationBegin(int numberOfStepResponses) {
        SwingUtilities.invokeLater(() -> {
        	//remove old checkboxes, if they are not removed before
            for(Map.Entry<String, JCheckBox> entry : checkBoxes.entrySet()) {
            	remove(entry.getValue());
            }
            checkBoxes.clear();
            view.validate();	//triggers repaint of the GUI

			//create the dummyCheckboxes
			for (int i = 0; i < numberOfStepResponses; i++) {
				JCheckBox cb = new JCheckBox();
				cb.setVisible(false);
				add(cb);
				// HACK: We don't know any of the names at this point, but we have to somehow store
				// the checkbox and be able to reference it later. The only unique identifier we
				// have is the table row index. When the calculation is added in onAddCalculation,
				// the key is replaced with the name of the calculation.
				checkBoxes.put(""+i, cb);
			}
		});
	}

	@Override
	public void onSimulationComplete() {
		SwingUtilities.invokeLater(() -> {
			//if all step-responses were calculated and all checkboxes updated, show them to the user
			//(make them visible)
			for (Map.Entry<String, JCheckBox> entry : checkBoxes.entrySet()) {
				entry.getValue().setVisible(true);
			}
			view.validate();    //triggers repaint of the GUI
		});
	}

    @Override
    public void onHideCalculation(ClosedLoop closedLoop) {}

    @Override
    public void onShowCalculation(ClosedLoop closedLoop) {}

	@Override
	public void onNewPlant(Plant plant) {}
}