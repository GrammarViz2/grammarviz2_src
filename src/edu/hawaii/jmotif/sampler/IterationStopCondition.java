/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.hawaii.jmotif.sampler;

/**
 *
 * @author ytoh
 */
public class IterationStopCondition implements StopCondition {
    private int iterations;
    private int maxIterations;

    public IterationStopCondition(int maxIterations) {
        this.iterations     = 0;
        this.maxIterations  = maxIterations;
    }

    public void nextIteration() {
        iterations++;
    }

    public int getIterations() {
        return iterations;
    }

    public boolean isConditionMet() {
        return iterations > maxIterations;
    }

    @Override
    public String toString() {
        return "Max numer of iterations reached (" + maxIterations + ").";
    }
}
