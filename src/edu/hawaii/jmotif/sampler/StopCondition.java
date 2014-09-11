/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.hawaii.jmotif.sampler;

/**
 * A stop condition is checked every optimization calculation cycle for 
 * an indication of computation termination.
 * A met condition can mean two things:
 * <li>the optimization method has reached its goal</li>
 * or
 * <li>the optimization method should be stopped for certain reasons
 * e.g. too much time since the start of computations has passed</li>
 *
 * @author ytoh
 */
public interface StopCondition {
    
    /**
     * Evaluates if the optimization calculation should be stopped or not.
     *
     * @return true if condition was met, false otherwise
     */
    boolean isConditionMet();
}
