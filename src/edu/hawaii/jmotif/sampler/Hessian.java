/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.hawaii.jmotif.sampler;

import java.util.Arrays;

/**
 * Wrapper class around a hessian array.
 * 
 * Added value:
 * <li>more abstraction</li>
 * <li>default inplementations of equals + hashcode</li>
 * <li>can be used in collections</li>
 * <li>casching</li>
 *
 * @author ytoh
 */
public class Hessian {

    // internal representation
    private double[][] array;

    /**
     * Creates and initializes an instance of <code>Hessian</code> with
     * the specified values.
     *
     * @param array to use to initialize the created instance
     */
    private Hessian(double[][] array) {
        this(array.length);
        for (int i = 0; i < array.length; i++) {
            System.arraycopy(array[i], 0, this.array[i], 0, this.array[i].length);
        }
    }

    /**
     * Creates a default instance of <code>Hessian</code>.
     *
     * @param dimenstion representing the lengths of the <code>Hessian</code>
     */
    private Hessian(int dimension) {
        this.array = new double[dimension][dimension];
    }

    /**
     * Returns the internal representation of this <code>Hessian</code> object.
     * The returned value is a copy of the internal immutable state.
     *
     * @return internal state as an array
     */
    public double[][] toArray() {
        double[][] copy = new double[array.length][array.length];
        for (int i = 0; i < array.length; i++) {
            System.arraycopy(array[i], 0, copy[i], 0, copy[i].length);
        }
        return copy;
    }

    /**
     * Factory method for creating <code>Hessian</code> instances out of arrays
     * of values.
     * (Factory method pattern)
     *
     * @param array representing a function hessian
     * @return a reference to an instance of <code>Hessian</code>
     */
    public static final Hessian valueOf(double[][] array) {
        return new Hessian(array);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Hessian other = (Hessian) obj;
        if (this.array != other.array && (this.array == null || !Arrays.equals(this.array,other.array))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + (this.array != null ? this.array.hashCode() : 0);
        return hash;
    }
}
