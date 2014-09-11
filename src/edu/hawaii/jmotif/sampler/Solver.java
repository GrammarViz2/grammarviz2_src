/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.hawaii.jmotif.sampler;

/**
 * A solver uses an optimization method as a script or algorithm to find 
 * an optimum or optima of the given function.
 *
 * @author ytoh
 */
public interface Solver extends Producer<Synchronization> {

    /**
     * Performs the necessary solver initializations.
     *
     * @param function to optimize
     * @param method the find the optimum of the given function
     */
    void init(Function function, OptimizationMethod method) throws Exception;

    /**
     * Includes aditional stop condition to the solver main loop.
     *
     * @param condition
     * stop condition to be added to other solver checked stop conditions
     */
    void addSystemStopCondition(StopCondition condition);

    /**
     * Main method using the given optimization method to find the given
     * function's optimum.
     */
    void solve() throws Exception;

    /**
     * Returns the optimization solution along with statistics about 
     * the calculation and its termination.
     *
     * @return optimization results
     */
    OptimizationResults getResults();
}
