/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.hawaii.jmotif.sampler;

import edu.hawaii.jmotif.text.SAXCollectionStrategy;

/**
 * A <code>Function</code> is an object that can be queried about its value at
 * a certain point and about its dimension.
 *
 * @author ytoh
 */
public interface Function {

    /**
     * Function value at the given point.
     *
     * @param point where a function value is evaluated
     * @return function value at the given point
     */
    double valueAt(Point point);

    /**
     * Function dimension.
     *
     * @return function dimension
     */
    int getDimension();

    SAXCollectionStrategy getSAXSamplingStrategy();
}
