/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.hawaii.jmotif.sampler;

import java.util.ArrayList;
import java.util.List;

/**
 * Instances of <code>OptimizationResults</code> hold the solution of
 * the optimization process as well as optmization {@link Statistics}
 * and {@link StopCondition}s which caused the optimization process to stop.
 *
 * @author ytoh
 */
public final class OptimizationResults {

    // solution of the optimization process
    private final Telemetry   solution;
    // value evaluation statistics
    private final Statistics  statistics;
    // terminating stop conditions
    private final List<StopCondition> metConditions;
    // number of iterations it took to stop the optimization
    private final int numberOfIterations;

    /**
     * Creates an instance of <code>OptimizationResults</code> with the given
     * solution, statistics and terminating stop conditions.
     *
     * @param solution the solution of the optimization process
     * @param statistics the evaluation statistics
     * @param numberOfIterations number of optimization steps it took to stop
     * @param metConditions stop conditions causing the optimization process to stop
     */
    public OptimizationResults(Telemetry solution, Statistics statistics, int numberOfIterations, List<StopCondition> metConditions) {
        this.solution           = solution;
        this.statistics         = statistics;
        this.numberOfIterations = numberOfIterations;
        // defensive copy
        this.metConditions      = new ArrayList<StopCondition>(metConditions);
    }

    /**
     * Retrieve the solution of the optimization process.
     *
     * @return solution represented by the telemetry after the optimization process terminated
     */
    public Telemetry getSolution() {
        return solution;
    }

    /**
     * Retrieve the statistics of the optimization process.
     *
     * @return function value evaluation statistics gathered dring optimization process.
     */
    public Statistics getStatistics() {
        return statistics;
    }

    /**
     * Retrieve the number of iterations it took to stop the optimization process
     * on one of the stop conditions.
     *
     * @return iteration count
     */
    public int getNumberOfIterations() {
        return numberOfIterations;
    }

    /**
     * Retrieve a list of stop conditions that caused the optimization process to stop.
     *
     * @return list an <strong>unmodifiable</strong> list of stop conditions
     */
    public List<StopCondition> getMetConditions() {
        return new ArrayList<StopCondition>(metConditions);
    }
}
