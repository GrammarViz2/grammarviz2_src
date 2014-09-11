/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.hawaii.jmotif.sampler;

/**
 * A method for finding a function optimum based on certain stop conditions.
 *
 * @author ytoh
 */
public interface OptimizationMethod<T extends Telemetry> extends Producer<T> {
    
    /**
     * Initialization of the optimization method. Called before the main
     * optimization method.
     *
     * @param function
     */
    void init(ObjectiveFunction function);

    /**
     * Return stop conditions used by the solver.
     *
     * @return an array of preconfigured stop conditions
     */
    StopCondition[] getStopConditions();

    /**
     * Main cycle of the optimization method. This method is called repeatedly
     * by the solver until atleast one of the specified stop conditions is met.
     * There fore this method should contain an optimization step.
     *
     * @throws OptimizationException if the optimization process encountered
     * a problem
     */
    void optimize();
}
