package ch.fhnw.ht.eit.pro2.team3.monkeypid.views;

import ch.fhnw.ht.eit.pro2.team3.monkeypid.controllers.Controller;
import ch.fhnw.ht.eit.pro2.team3.monkeypid.listeners.IModelListener;
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
public class GraphDisplayPanel extends JPanel implements ActionListener,
		IModelListener {

	private Controller controller;
	private HashMap<String, JCheckBox> checkBoxes = new HashMap<>();
	private View view;
	private boolean CurvesDisplayOn = true;
	//private JPanel dummyCheckBoxPn;
	//private String[] closedLoopNames;
	private int numberOfStepResponses;
	
	/**
	 * Constructor of GraphDisplayPanel adds
	 */
	public GraphDisplayPanel(Controller controller, View view) {
		// super(new FlowLayout(FlowLayout.LEADING));
		super(new WrapLayout(WrapLayout.LEFT));
		this.controller = controller;
		this.view = view;
	}

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
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
    
    public void toggleDisplayAllCurves(){
    	if(CurvesDisplayOn){
    		CurvesDisplayOn = false;
			for(Map.Entry<String, JCheckBox> entry : checkBoxes.entrySet()) {
				controller.cbUncheckAction(entry.getKey());
				entry.getValue().setSelected(false);
			}	
		}
		else{
			CurvesDisplayOn = true;
			for(Map.Entry<String, JCheckBox> entry : checkBoxes.entrySet()) {
				controller.cbCheckAction(entry.getKey());
				entry.getValue().setSelected(true);
			}
		}
    }
    
    public void toggleDisplayFistCurves() {
    	if(CurvesDisplayOn){
    		CurvesDisplayOn = false;
			for(Map.Entry<String, JCheckBox> entry : checkBoxes.entrySet()) {
				if(entry.getKey() != "Zellweger"){
					controller.cbUncheckAction(entry.getKey());
					entry.getValue().setSelected(false);
				}
			}	
		}
		else{
			CurvesDisplayOn = true;
			for(Map.Entry<String, JCheckBox> entry : checkBoxes.entrySet()) {
				if(entry.getKey() != "Zellweger"){
					controller.cbCheckAction(entry.getKey());
					entry.getValue().setSelected(true);
				}
			}
		}
	}

	@Override
	public void onAddCalculation(ClosedLoop closedLoop, boolean visible) {
        SwingUtilities.invokeLater(() -> {

            //get rgbColor from closedLoop and convert it to string
            String hexColor = String.format("#%02x%02x%02x", closedLoop.getColor()
                    .getRed(), closedLoop.getColor().getGreen(), closedLoop
                    .getColor().getBlue());
            
            //save closedLoop names in correct order
        	//closedLoopNames[closedLoop.getTableRowIndex()] = closedLoop.getName();
            
            JCheckBox cb = new JCheckBox();
            
            for(Map.Entry<String, JCheckBox> entry : checkBoxes.entrySet()) {
            	//get the entry which matches the index of the given closed-Loop
            	try{
            		int key = Integer.parseInt(entry.getKey());
	            	if(key == closedLoop.getTableRowIndex()){
	            		//replace the old key, wich was the index number as string with the name of the closedLoop
	            		//because the key can't be renamed, remove the old has and insert the value of the old hash 
	            		//with a new key, the new key is the name of the closedLoop
	            		checkBoxes.put(closedLoop.getName(), checkBoxes.remove(""+closedLoop.getTableRowIndex()));
	            		//get the checkBox of this closedLoop
	            		cb = checkBoxes.get(closedLoop.getName());
	            		break;
	            	}
            	}
            	catch(NumberFormatException exc){
            		// entry-key was no number -> try next entry
            	}
            }
            //set checkBox content: colored dot and name of closedLoop
    		cb.setText( "<html><font style=\"font-family: unicode \"color=" + hexColor
                    + ">" + "\u25CF" + "<font color=#000000>"
                    + closedLoop.getName());
            
    		//set checkBox visible, before checkBox was only a space-holder (invisible)
    		//cb.setVisible(false);
    		System.out.println("on add");
    		
            //set the checkbox selected dependent of the param visibles
            cb.setSelected(visible);
            
            //add actionListener to checkbox
            cb.addActionListener(this);
    		
            numberOfStepResponses--;
            if(numberOfStepResponses == 0){
            	for(Map.Entry<String, JCheckBox> entry : checkBoxes.entrySet()) {
    				JCheckBox cb2 = entry.getValue();
    				cb2.setVisible(true);
    			}
            	view.validate();
            }
        });
	}

	@Override
	public void onRemoveCalculation(ClosedLoop closedLoop) {
        SwingUtilities.invokeLater(() -> {
            JCheckBox c = checkBoxes.remove(closedLoop.getName());
            remove(c);
            view.validate();
        });
	}

    @Override
    public void onSimulationBegin(int numberOfStepResponses) {
        SwingUtilities.invokeLater(() -> {
            for(Map.Entry<String, JCheckBox> entry : checkBoxes.entrySet()) {
            	remove(entry.getValue());
            }
            checkBoxes.clear();
            view.validate();
            this.numberOfStepResponses = numberOfStepResponses;
        	//closedLoopNames = new String[numberOfStepResponses];
            //dummyCheckBoxPn = new JPanel();
            for (int i = 0; i < numberOfStepResponses; i++) {
            	//closedLoopNames[i] = new String("");
            	JCheckBox cb = new JCheckBox();
            	cb.setVisible(false);
            	//dummyCheckBoxPn.add(cb);
            	add(cb);
            	//cb.setVisible(false);
            	checkBoxes.put(""+i, cb); //set key temporary to the index-number of the closedLoops
			}
            
        });
    }

	@Override
	public void onSimulationComplete() {
		SwingUtilities.invokeLater(() -> {
			/*
			for(Map.Entry<String, JCheckBox> entry : checkBoxes.entrySet()) {
				JCheckBox cb = entry.getValue();
				cb.setVisible(true);
			}
			*/
			//System.out.println("on complete");
			/*
			for (int i = 0; i < closedLoopNames.length; i++) {
				for(Map.Entry<String, JCheckBox> entry : checkBoxes.entrySet()) {
					if(closedLoopNames[i] == entry.getKey()){
						add(entry.getValue());
						entry.getValue().setVisible(true);
						break;
					}
				}
			}
			*/
			//view.validate();
		});
	}

    @Override
    public void onHideCalculation(ClosedLoop closedLoop) {}

    @Override
    public void onShowCalculation(ClosedLoop closedLoop) {}

	@Override
	public void onSetPlant(Plant plant) {}
}