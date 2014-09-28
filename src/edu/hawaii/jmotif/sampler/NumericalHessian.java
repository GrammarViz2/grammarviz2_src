/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.hawaii.jmotif.sampler;

import edu.hawaii.jmotif.direct.Point;

/**
 * Interface defining a method returning a numerical hessian of a function.
 * (Strategy pattern)
 *
 * @author ytoh
 */
public interface NumericalHessian {

    /**
     * Method calculating the numerical hessian of a given function
     * at a given point.
     *
     * @param function
     * @param point
     * @return a Hessian object instanciated with the calculated hessian array
     */
    Hessian hessianAt(ObjectiveFunction function, Point point);
}
