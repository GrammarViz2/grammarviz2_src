/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.hawaii.jmotif.sampler;

import java.util.Arrays;

/**
 * Wrapper class around a gradient array.
 * 
 * Added value:
 * <li>more abstraction</li>
 * <li>default inplementations of equals + hashcode</li>
 * <li>can be used in collections</li>
 * <li>casching</li>
 *
 * @author ytoh
 */
public class Gradient {

    // internal coordinate representation
    private final double[] array;

    /**
     * Creates and initializes an instance of <code>Gradient</code> using
     * the specified values.
     *
     * @param array to use to initialize the created instance
     */
    private Gradient(double[] array) {
        this(array.length);
        System.arraycopy(array, 0, this.array, 0, this.array.length);
    }

    /**
     * Creates a default instance of <code>Gradient</code>.
     *
     * @param dimenstion representing the length of the <code>Gradient</code>
     */
    private Gradient(int dimenstion) {
        this.array = new double[dimenstion];
    }

    /**
     * Returns the internal representation of this <code>Gradient</code> object.
     * The returned value is a copy of the internal immutable state.
     *
     * @return internal state as an array
     */
    public double[] toArray() {
        double[] copy = new double[array.length];
        System.arraycopy(array, 0, copy, 0, copy.length);
        return copy;
    }

    /**
     * Factory method for creating <code>Gradient</code> instances out of arrays
     * of values.
     * (Factory method pattern)
     *
     * @param array representing a function gradient
     * @return a reference to an instance of <code>Gradient</code>
     */
    public static final Gradient valueOf(double... array) {
        return new Gradient(array);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Gradient other = (Gradient) obj;
        if (this.array != other.array && (this.array == null || !Arrays.equals(this.array, other.array))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + (this.array != null ? this.array.hashCode() : 0);
        return hash;
    }
}
