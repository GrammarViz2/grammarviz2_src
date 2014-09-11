/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.hawaii.jmotif.sampler;

/**
 * Objects implementing <code>FunctionGradient</code> can be used to calculate
 * a gradient of a function at a certain point.
 *
 * @author ytoh
 */
public interface FunctionGradient {

    /**
     * Gradient vector at the given point.
     *
     * @param point where the gradient is evaluated
     * @return the calculated gradient
     */
    Gradient gradientAt(Point point);
}
