/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.hawaii.jmotif.sampler;

/**
 * Interface defining a method returning a numerical gradient of a function.
 * (Strategy pattern)
 *
 * @author ytoh
 */
public interface NumericalGradient {

    /**
     * Method calculating the numerical gradient of a given function
     * at a given point.
     *
     * @param function
     * @param point
     * @return a Gradient object instanciated with the calculated gradient array
     */
    Gradient gradientAt(ObjectiveFunction function, Point point);
}
