/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.hawaii.jmotif.sampler;

/**
 * An implementation of the {@link StopCondition} interface stoping when
 * the optimization process is no longer lowering the current value.
 *
 * @author ytoh
 */
//@Component(
//    name="Epsilon stop condition",
//    description="Tolerance based stop condition"
//)
public class SimpleStopCondition implements StopCondition {
//    @Property(name="Use")
    private boolean use = true;

    public boolean isUse() {
        return use;
    }

    public void setUse(boolean use) {
        this.use = use;
    }
    // flag signifying if the optimization process should stop
    private boolean isMet = false;
    // last function value
    private double previousValue;
    private double epsilon;
    private double tolerance;

//    @Property(name = "max repeats of the same best value")
//    @Range(from = 1, to = Integer.MAX_VALUE)
    private int maxRepeats = 1;
    private int repeats;
    
    /**
     * Initialize the stop condition.
     *
     * @param initialValue
     * @param epsilon
     * @param tolerance
     * @param maxRepeats
     */
    public void init(double initialValue, double epsilon, double tolerance, int maxRepeats) {
        this.previousValue = initialValue;
        this.epsilon = epsilon;
        this.tolerance = tolerance;
        this.maxRepeats = maxRepeats;
        this.repeats = 0;
        this.isMet = false;
    }

    /**
     * Initialize the stop condition.
     *
     * @param initialValue
     * @param tolerance
     * @param maxRepeats
     */
    public void init(double initialValue, double tolerance, int maxRepeats) {
        this.init(initialValue, 1.0e-10, tolerance, maxRepeats);
    }

    /**
     * Set the current value of the optimization process and calculate whether
     * the optimization should stop.
     *
     * @param value current value of the optimization process
     */
    public void setValue(double value) {
        boolean t = 2.0 * Math.abs(value - previousValue) <= tolerance * (Math.abs(value) + Math.abs(previousValue) + epsilon);
        if (t) {
            if (repeats == maxRepeats) {
                isMet = true;
            } else {
                repeats++;
                previousValue = value;
            }
        } else {
            repeats = 0;
            previousValue = value;
        }
        previousValue = value;
    }

    public boolean isConditionMet() {
        return use && isMet;
    }

    public int getMaxRepeats() {
        return maxRepeats;
    }

    public void setMaxRepeats(int maxRepeats) {
        this.maxRepeats = maxRepeats;
    }

    public void setInitialValue(double initialValue){
        this.previousValue = initialValue;
        this.isMet = false;
        this.repeats = 0;
    }

    @Override
    public String toString() {
        return "Convergence detected at " + previousValue;
    }
}
