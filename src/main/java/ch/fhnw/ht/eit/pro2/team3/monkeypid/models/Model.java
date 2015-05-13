package ch.fhnw.ht.eit.pro2.team3.monkeypid.models;

import ch.fhnw.ht.eit.pro2.team3.monkeypid.listeners.IClosedLoopListener;
import ch.fhnw.ht.eit.pro2.team3.monkeypid.listeners.IModelListener;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This is the heart of all data related operations. All simulations, calculations and events are orchestrated here.
 *
 * First, some terminology:
 *   - A *calculation* is the process of calculating a controller, creating a closed loop, and calculating the step
 *     response of that closed loop. There are usually multiple calculations in every simulation.
 *   - A *simulation* is the process of executing all calculations.
 *
 * There are multiple settings which can be configured for simulations:
 *   - The plant can be set with Tu, Tg, and Ks.
 *   - The controller type can be set to either I, PI, or PID.
 *   - The parasitic time constant factor can be set. This influences how Tp is calculated.
 *   - The phase margin for Zellweger calculations can be set.
 *
 * When a simulation is initiated, a list of controller calculators is generated based on what has been configured.
 * These are executed in parallel, and each produce a controller to fit the configured plant. Once that is done, the
 * list of resulting controllers are used to create a closed loop system with the plant, and a step response is
 * calculated. When all step responses complete, the end of the simulation is notified.
 *
 * @author Alex Murray
 */
public class Model implements IClosedLoopListener {

    /**
     * Is thrown when an unknown regulator string is passed to setRegulatorType().
     */
    public class UnknownRegulatorTypeException extends RuntimeException {
        UnknownRegulatorTypeException(String message) {
            super(message);
        }
    }

    /**
     * Is thrown when the user tries to simulate PID calculations with an order of n=2.
     * See issue #31.
     */
    public class InvalidPlantForPIDSimulationException extends RuntimeException {
        InvalidPlantForPIDSimulationException(String message) {
            super(message);
        }
    }

    private class CalculationCycle implements Runnable {
        private AbstractControllerCalculator controllerCalculator;
        private IClosedLoopListener resultListener;

        CalculationCycle(AbstractControllerCalculator controllerCalculator, IClosedLoopListener resultListener) {
            this.controllerCalculator = controllerCalculator;
            this.resultListener = resultListener;
        }

        public AbstractControllerCalculator getControllerCalculator() {
            return controllerCalculator;
        }

        @Override
        public void run() {
            controllerCalculator.run();
            ClosedLoop closedLoop = new ClosedLoop(plant, controllerCalculator.getController());
            closedLoop.registerListener(resultListener);
            closedLoop.setTableRowIndex(controllerCalculator.getTableRowIndex());
            closedLoop.calculateStepResponse(8 * 1024);
        }
    }

    private ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    // have the model own the sani curves, so they don't have to be reloaded from
    // disk every time a new calculation is performed.
    private SaniCurves sani = new SaniCurves();

    // current plant to use for controller calculations
    private Plant plant = null;

    // phase margin for Zellweger
    private double phaseMargin = 0.0;

    // regulator types to calculate when user simulates
    private enum RegulatorType {
        I,
        PI,
        PID
    }
    private RegulatorType regulatorType;

    private double parasiticTimeConstantFactor;

    // list of closed loops to be displayed on the graph
    ClosedLoop selectedCalculation = null;
    private ArrayList<ClosedLoop> closedLoops = new ArrayList<>();

    // list of Model listeners
    private ArrayList<IModelListener> listeners = new ArrayList<>();

    // -----------------------------------------------------------------------------------------------------------------
    // Public methods
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Updates the plant to be used for all calculations.
     * @param tu Plant value Tu.
     * @param tg Plant value Ks.
     * @param ks Plant value Ks.
     */
    public final void setPlant(double tu, double tg, double ks) {
        this.plant = new Plant(tu, tg, ks, sani);
        notifySetPlant(plant);
    }

    /**
     * Select the regulator types to be simulated. We currently support I, PI, and PID type regulators. When the
     * simulation is initiated, only calculators matching the selected type will be calculated.
     * @param regulatorTypeName A string containing either "I", "IP", or "PID".
     */
    public final void setRegulatorType(String regulatorTypeName) throws UnknownRegulatorTypeException {
        switch (regulatorTypeName) {
            case "I":
                regulatorType = RegulatorType.I;
                break;
            case "PI":
                regulatorType = RegulatorType.PI;
                break;
            case "PID":
                regulatorType = RegulatorType.PID;
                break;
            default:
                throw new UnknownRegulatorTypeException("Unknown regulator \"" + regulatorTypeName + "\"");
        }
    }

    /**
     * This is used to calculate Tp.
     * Updates the parasitic time constant factor to use in all simulations. Zellweger methods will multiply this
     * with Tvk to get Tp (Tp = factor * Tvk). Fist formulas will multiply it with Tv to get Tp (Tp = factor * Tv)
     * @param parasiticTimeConstantFactor The factor to use. The value should be absolute, not in percent.
     */
    public final void setParasiticTimeConstantFactor(double parasiticTimeConstantFactor) {
        this.parasiticTimeConstantFactor = parasiticTimeConstantFactor;
    }

    /**
     * Updates the phase margin used in Zellweger-based calculations.
     * @param phaseMargin A positive angle in degrees, usually in the range of 45° and 76.3°
     */
    public final void setPhaseMargin(double phaseMargin) {
        this.phaseMargin = phaseMargin;
    }

    /**
     * Clears, then calculates all controllers and their closed loop step responses. If a simulation is still active,
     * this method will not do anything.
     */
    public final void simulateAll() {

        if(threadPool.getActiveCount() > 0)
            return;

        // see issue #31 - disallow orders n=2 for PID simulations
        validatePlantIsPIDCompliant();

        // clean up from last simulation
        clearSimulation();

        // get all calculators and notify simulation begin
        ArrayList<CalculationCycle> calculators = getCalculators();
        notifySimulationBegin(calculators.size());

        // dispatch all calculators
        calculators.forEach(threadPool::submit);

        try {
            threadPool.awaitTermination(0, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            notifySimulationComplete();
        }
    }

    /**
     * Selects an active calculation. After selecting a calculation, it's possible to manipulate it with other methods,
     * such as hideSelectedCalculation() or showSelectedCalculation().
     * @param name The unique name of the calculation to select.
     */
    public final void selectCalculation(String name) {
        for(ClosedLoop loop : closedLoops) {
            if(loop.getName().equals(name)) {
                selectedCalculation = loop;
                return;
            }
        }
    }

    /**
     * Hides the currently selected calculation. This sends an onHideCalculation() event to all model listeners.
     */
    public final void hideSelectedCalculation() {
        if(selectedCalculation == null) {
            return;
        }

        notifyHideCalculation(selectedCalculation);
    }

    /**
     * Shows the currently selected calculation. This sends an onShowCalculation() event to all model listeners.
     */
    public final void showSelectedCalculation() {
        if(selectedCalculation == null) {
            return;
        }

        notifyShowCalculation(selectedCalculation);
    }

    /**
     * Registers a model listener.
     * @param listener An object implementing IModelListener.
     */
    public final void registerListener(IModelListener listener) {
        listeners.add(listener);
    }

    /**
     * Unregisters a model listener.
     * @param listener An object that previously called registerListener.
     */
    public final void unregisterListener(IModelListener listener) {
        listeners.remove(listener);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Private methods
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Clears all existing calculations. The graph, table, and checkboxes will be cleared along with all closed loops.
     * NOTE: This should only be called if there isn't an active simulation, because the calculations are threaded.
     */
    private void clearSimulation() {

        // deselect calculation
        selectedCalculation = null;

        // notify all listeners that we're removing all closed loops
        closedLoops.forEach(this::notifyRemoveCalculation);
        closedLoops = new ArrayList<>();
    }

    /**
     * PID simulations with order n=2 don't make much sense, so throw an exception if that happens.
     * Request from Peter Niklaus, see issue #31.
     */
    private void validatePlantIsPIDCompliant() {
        // see issue #31 - disallow orders n=2 for PID simulations
        if(regulatorType == RegulatorType.PID) {
            double ratio = plant.getTu() / plant.getTg();
            if(sani.lookupOrder(ratio) == 2) {
                throw new InvalidPlantForPIDSimulationException("Die Strecke ist n=2. Eine PID Simulation ist Sinnlos");
            }
        }
    }

    /**
     * Creates a list of controller calculators, filtered to the currently selected controller type (I, PI, or PID).
     * The calculators are configured with the currently set plant, and the Zellweger calculators are configured with
     * the currently selected phase margin. Additionally, each calculator is given a unique index. This is so their
     * order isn't undefined, even when they are computed in parallel.
     * @return An ArrayList of calculators. See issue #29
     */
    private ArrayList<CalculationCycle> getCalculators() {

        ArrayList<CalculationCycle> calculators = new ArrayList<>();

        // generate a list of all calculators matching the currently selected controller type
        switch(regulatorType) {
            case PID:
                calculators.add(new CalculationCycle(new FistFormulaOppeltPID(plant), this));
                calculators.add(new CalculationCycle(new FistFormulaReswickStoerPID0(plant), this));
                calculators.add(new CalculationCycle(new FistFormulaReswickStoerPID20(plant), this));
                calculators.add(new CalculationCycle(new FistFormulaReswickFuehrungPID0(plant), this));
                calculators.add(new CalculationCycle(new FistFormulaReswickFuehrungPID20(plant), this));
                calculators.add(new CalculationCycle(new FistFormulaRosenbergPID(plant), this));
                calculators.add(new CalculationCycle(new ZellwegerPID(plant, phaseMargin), this));
                break;

            case PI:
                calculators.add(new CalculationCycle(new FistFormulaOppeltPI(plant), this));
                calculators.add(new CalculationCycle(new FistFormulaReswickStoerPI0(plant), this));
                calculators.add(new CalculationCycle(new FistFormulaReswickStoerPI20(plant), this));
                calculators.add(new CalculationCycle(new FistFormulaReswickFuehrungPI0(plant), this));
                calculators.add(new CalculationCycle(new FistFormulaReswickFuehrungPI20(plant), this));
                calculators.add(new CalculationCycle(new FistFormulaRosenbergPI(plant), this));
                calculators.add(new CalculationCycle(new ZellwegerPI(plant, phaseMargin), this));
                break;

            case I:
                calculators.add(new CalculationCycle(new ZellwegerI(plant), this));

            default:
                break;
        }

        // set parasitic time constant
        // set table row indices of calculator - See issue #29
        int i = 0;
        for(CalculationCycle calculator : calculators) {
            calculator.getControllerCalculator().setParasiticTimeConstantFactor(parasiticTimeConstantFactor);
            calculator.getControllerCalculator().setTableRowIndex(i);
            i++;
        }

        return calculators;
    }

    /**
     * Call this to notify that a new completed calculation was added to the internal list.
     * @param loop The closed loop that was added.
     */
    private void notifyAddCalculation(ClosedLoop loop) {
        for(IModelListener listener : listeners) {
            listener.onAddCalculation(loop);
        }
    }

    /**
     * Call this to notify that a calculation was removed from the internal list.
     * @param loop The closed loop that was removed.
     */
    private void notifyRemoveCalculation(ClosedLoop loop) {
        for (IModelListener listener : listeners) {
            listener.onRemoveCalculation(loop);
        }
    }

    /**
     * Call this to notify that a new simulation is about to begin.
     * @param numberOfCalculators The number of calculators that will be executed.
     */
    private void notifySimulationBegin(int numberOfCalculators) {
        for(IModelListener listener : listeners) {
            listener.onSimulationBegin(numberOfCalculators);
        }
    }

    /**
     * Call this to notify that a simulation has completed.
     */
    private void notifySimulationComplete() {
        listeners.forEach(IModelListener::onSimulationComplete);
    }

    /**
     * Call this to notify that a calculation was hidden. This should cause the curve in the plot to be hidden.
     * @param closedLoop The closed loop being hidden.
     */
    private void notifyHideCalculation(ClosedLoop closedLoop) {
        for(IModelListener listener : listeners) {
            listener.onHideCalculation(closedLoop);
        }
    }

    /**
     * Call this to notify that a calculation was made visible. This should cause the curve in the plot to be made
     * visible.
     * @param closedLoop The closed loop to make visible.
     */
    private void notifyShowCalculation(ClosedLoop closedLoop) {
        for(IModelListener listener : listeners) {
            listener.onShowCalculation(closedLoop);
        }
    }
    
    private void notifySetPlant(Plant plant){
    	for(IModelListener listener : listeners){
    		listener.onSetPlant(plant);
    	}
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Derived methods
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Called when a step response calculation completes.
     * @param closedLoop The closed loop object holding the results of the step response.
     */
    @Override
    public final synchronized void onStepResponseCalculationComplete(ClosedLoop closedLoop) {
        closedLoops.add(closedLoop);
        notifyAddCalculation(closedLoop);
    }
}
