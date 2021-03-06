package ch.fhnw.ht.eit.pro2.team3.monkeypid.views;

import ch.fhnw.ht.eit.pro2.team3.monkeypid.controllers.Controller;
import ch.fhnw.ht.eit.pro2.team3.monkeypid.listeners.IModelListener;
import ch.fhnw.ht.eit.pro2.team3.monkeypid.models.ClosedLoop;
import ch.fhnw.ht.eit.pro2.team3.monkeypid.models.Plant;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Creates a panel which includes the input fields for Tu, Tg, Ks, Tp, a
 * comboBox to select the regulator, a comboBox to select the overshoot, the
 * buttons and the table with the results of the simulation.
 * 
 * @author Josua
 *
 */
public class OutputPanel extends JPanel implements IModelListener,
		ChangeListener {

	private static final long serialVersionUID = 1L;
	private Controller controller;

	// dummy label to get height of label font
	private JLabel lbDummyGetHeight = new JLabel(" ");

	// number of regulators for size of table
	private int maxNumberOfRegulators = 7;

	// table and table model
	NonEditableTableModel tableModel = new NonEditableTableModel();
	JTable table = new JTable(tableModel);

	// adjustment slider
	private JLabel lbTrimmSlider = new JLabel("Trimm für Zellwegermethode");
	private JSlider slTrimmSlider = new JSlider(JSlider.HORIZONTAL, -90, 90, 0);

	/**
	 * The constuctor of Leftpanel set the layout to GridBagLayout and adds all
	 * the components to the panel. Furthermore it creates the table for the
	 * results and the buttons listen to the ActionListener
	 * @param controller The controller part of the MVC pattern.
	 */
	public OutputPanel(Controller controller) {
		super(new GridBagLayout());
		this.controller = controller;

		// add columns to the table
		tableModel.addColumn("Name");
		tableModel.addColumn("Kr");
		tableModel.addColumn("Tn");
		tableModel.addColumn("Tv");
		tableModel.addColumn("Tp");
		tableModel.addColumn("<html><left>Über-<br>schwingen");

		// get font height of a label from GUI
		FontMetrics fm = lbDummyGetHeight.getFontMetrics(lbDummyGetHeight
				.getFont());
		int fontHeight = fm.getHeight();

		// set size of first column
		table.getColumnModel().getColumn(0)
				.setMinWidth((int) (5.5 * fontHeight));
		table.getColumnModel().getColumn(0)
				.setMaxWidth((int) (5.5 * fontHeight));
		table.getColumnModel().getColumn(0)
				.setPreferredWidth((int) (5.5 * fontHeight));

		table.getColumnModel().getColumn(5)
				.setMinWidth((int) (4.6 * fontHeight));
		table.getColumnModel().getColumn(5)
				.setMaxWidth((int) (4.6 * fontHeight));
		table.getColumnModel().getColumn(5)
				.setPreferredWidth((int) (4.6 * fontHeight));

		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment(DefaultTableCellRenderer.RIGHT);
		table.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);

		// set size of the rest columns
		for (int i = 1; i < table.getColumnCount() - 1; i++) {
			TableColumn col;
			col = table.getColumnModel().getColumn(i);
			col.setMinWidth((int) (4.6 * fontHeight));
			col.setMaxWidth((int) (4.6 * fontHeight));
			col.setPreferredWidth((int) (4.6 * fontHeight));
		}

		// allocate all rows with empty strings
		for (int i = 0; i < maxNumberOfRegulators; i++) {
			tableModel.addRow(new String[] { "", "", "", "", "", "" });
		}
		// set preferred size of table
		table.getTableHeader().setPreferredSize(
				new Dimension((int) (28.5 * fontHeight),
						(int) (2.5 * fontHeight)));

		// set minimum size of table
		table.getTableHeader().setMinimumSize(
				new Dimension((int) (28.5 * fontHeight),
						(int) (2.5 * fontHeight)));

		// disable autoResize of table
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		// disable mouse resize icon
		table.getTableHeader().setResizingAllowed(false);

		// disable user column dragging
		table.getTableHeader().setReorderingAllowed(false);

		// add header of table to GridBagLaout
		add(table.getTableHeader(), new GridBagConstraints(0, 0, 7, 1, 0.0,
				0.0, GridBagConstraints.FIRST_LINE_START,
				GridBagConstraints.NONE, new Insets(10, 10, 0, 10), 0, 0));
		// add table to GridBagLayout
		add(table, new GridBagConstraints(0, 1, 7, 1, 0.0, 0.0,
				GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE,
				new Insets(0, 10, 10, 10), 0, 0));

		// add title for trimm slider to GridbagLayout
		add(lbTrimmSlider, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0,
				GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE,
				new Insets(10, 10, 0, 10), 0, 0));

		// add the trimm slider to GridbagLayout
		add(slTrimmSlider, new GridBagConstraints(0, 3, 7, 1, 0.0, 0.0,
				GridBagConstraints.FIRST_LINE_START,
				GridBagConstraints.HORIZONTAL, new Insets(0, 10, 10, 10), 0, 0));

		// add vertical dummy to GridbagLayout
		add(new JLabel(), new GridBagConstraints(0, 4, 1, 1, 0.0, 1.0,
				GridBagConstraints.FIRST_LINE_START,
				GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));

		// add ActionListener to trimmer
		slTrimmSlider.addChangeListener(this);
	}

	/**
	 * Sets the elements to visible or invisible. It depends on which version
	 * (mini or normal) is selected in the menu.
	 * @param miniVersionSelected If true, the mini version is activated. If false,
	 *                            the normal version is activated.
	 */
	public void setMiniVersion(boolean miniVersionSelected) {
		// set all changing components to in- or visible
		lbTrimmSlider.setVisible(miniVersionSelected);
		slTrimmSlider.setVisible(miniVersionSelected);
	}

	/**
	 * Sets the Slider to its default Value = 0 This is in the middle of the
	 * slider This is called (from the controller) after the user clicks onto
	 * the Simulate-Button
	 */
	public void setSliderDefaultValue() {
		slTrimmSlider.setValue(0);
	}

	/**
	 * This is called when a calculation completes. It will add the controller parameters
	 * to the output table in the GUI.
	 */
	@Override
	public void onAddCalculation(ClosedLoop closedLoop, boolean visible) {
		SwingUtilities.invokeLater(() -> {

			// do we have a row allocated for this closed loop?
				if (closedLoop.getTableRowIndex() > -1
						&& closedLoop.getTableRowIndex() < tableModel
								.getRowCount()) {
					String[] tableRowStrings = closedLoop.getTableRowStrings();

					// trimm string to get only the name of the regulator
					tableRowStrings[0] = tableRowStrings[0].split(" ")[0];

					// get rgbColor from closedLoop and convert it to string
					String hexColor = String.format("#%02x%02x%02x", closedLoop
							.getColor().getRed(), closedLoop.getColor()
							.getGreen(), closedLoop.getColor().getBlue());

					// adds row with colored dot before name
				for (int i = 0; i < tableRowStrings.length; i++) {
					if (i == 0) {
						tableModel.setValueAt(
								"<html><font style=\"font-family: unicode \"color="
										+ hexColor + ">" + "\u25CF"
										+ "<font color=#000000>"
										+ tableRowStrings[i],
								closedLoop.getTableRowIndex(), i);
					} else {
						tableModel.setValueAt(tableRowStrings[i],
								closedLoop.getTableRowIndex(), i);
					}

				}
			} else {

				// we don't have space allocated, so just append it to the
				// end
				tableModel.addRow(closedLoop.getTableRowStrings());
			}
		});
	}

	/**
	 * This is called when a calculation is being removed. This will remove the
	 * corresponding controller parameters from the output table.
	 */
	@Override
	public void onRemoveCalculation(ClosedLoop closedLoop) {
		SwingUtilities.invokeLater(() -> {
			// remove from table
				for (int row = 0; row < tableModel.getRowCount(); row++) {
					// name is stored in column 0
					String controllerName = (String) tableModel.getValueAt(row,
							0);
					if (controllerName.endsWith(closedLoop.getName())) {
						tableModel.removeRow(row);
						return;
					}
				}
				// view.validate();
			});
	}

	@Override
	public void onUpdateCalculation(ClosedLoop closedLoop) {}

	/**
	 * This is called when a new simulation begins. The table is filled with placeholder
	 * text which is overwritten when each calculation completes.
	 * @param numberOfStepResponses The total number of calculations that will be executed.
	 */
	@Override
	public void onSimulationBegin(int numberOfStepResponses) {
		SwingUtilities.invokeLater(() -> {
			// clear the table
				while (tableModel.getRowCount() > 0) {
					tableModel.removeRow(0);
				}

				// allocate rows with strings
				for (int i = 0; i < numberOfStepResponses; i++) {
					tableModel.addRow(new String[] { "calculating...", "", "",
							"", "", "" });
				}
				// allocate the rest of rows with empty strings
				for (int j = numberOfStepResponses; j < maxNumberOfRegulators; j++) {
					tableModel.addRow(new String[] { "", "", "", "", "", "" });
				}
			});
	}

	@Override
	public void onSimulationComplete() {
	}

	@Override
	public void onHideCalculation(ClosedLoop closedLoop) {
	}

	@Override
	public void onShowCalculation(ClosedLoop closedLoop) {
	}

	@Override
	public void onNewPlant(Plant plant) {
	}

	/**
	 * This is called when the user adjusts the Zellweger slider.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		controller.angleOfInflectionOffsetChanged(slTrimmSlider.getValue());
	}
}