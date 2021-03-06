package ch.fhnw.ht.eit.pro2.team3.monkeypid.listeners;

import ch.fhnw.ht.eit.pro2.team3.monkeypid.models.ClosedLoop;
import ch.fhnw.ht.eit.pro2.team3.monkeypid.models.Plant;

/**
 * Allows classes to register as listeners to the Model class. Model is part of the MVC pattern.
 * @author Alex Murray
 */
public interface IModelListener {

	/**
	 * This is called when the model has calculated a new plant object.
	 * @param plant The new plant object.
	 */
	void onNewPlant(Plant plant);
	
	/**
	 * This is called when the model has finished calculating a closed system and its respective step response, and
	 * has added it to its internal list. A single simulation usually has multiple calculations, and thus, will call
	 * this method multiple times with different closed loop objects.
	 * @param closedLoop The closed loop object holding the step response information as well as the controller and
	 *                   plant.
	 *  @param visible The visibility of the closedLoop which has to be added. If true, the stepResponse should be
	 *                 visible, else it should be added but invisible
	 */
	void onAddCalculation(ClosedLoop closedLoop, boolean visible);

	/**
	 * This is called when the model is about to remove a calculation from its internal list.
	 * @param closedLoop The closed loop object being removed.
	 */
	void onRemoveCalculation(ClosedLoop closedLoop);

	/**
	 * This is called when the model has updated/recalculated an existing calculation.
	 * @param closedLoop The closed loop object that has been recalculated.
	 */
	void onUpdateCalculation(ClosedLoop closedLoop);

	/**
	 * This is called right before a simulation is initiated. A single simulation will usually have multiple
	 * calculations. The exact number of calculations is passed with this method.
	 * @param numberOfStepResponses The total number of calculations that will be executed.
	 */
	void onSimulationBegin(int numberOfStepResponses);

	/**
	 * This is called when all calculations have completed.
	 */
	void onSimulationComplete();

	/**
	 * This is called when a calculation should be hidden, such as when the user clicks on one of the show/hide
	 * checkboxes.
	 * @param closedLoop The closed loop object being hidden.
	 */
	void onHideCalculation(ClosedLoop closedLoop);

	/**
	 * This is called when a calculation should be made visible, such as when the user clicks on one of the show/hide
	 * checkboxes.
	 * @param closedLoop The closed loop object being made visible.
	 */
	void onShowCalculation(ClosedLoop closedLoop);
}
