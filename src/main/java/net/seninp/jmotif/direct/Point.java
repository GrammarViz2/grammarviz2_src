package net.seninp.jmotif.direct;

import java.util.Arrays;

/**
 * Wrapper class around a point (or input vector) array. This class is immutable making it
 * threadsafe. It is not extendible because it has no public constructor. Creation is handled by a
 * static factory method an ideal point for caching or hashcode precomputation.
 * 
 * Added value: <li>more abstraction</li> <li>default inplementations of equals + hashcode</li> <li>
 * can be used in collections</li> <li>caching</li>
 * 
 * @author ytoh
 */
public class Point {

  // internal coordinate representation
  private final double[] array;

  /** */
  private String toString;

  /** */
  private static final Point DEFAULT = new Point(0);

  /**
   * Creates and initializes an instance of <code>Point</code> using the specified values.
   * 
   * @param array to use to initialize the created instance
   */
  private Point(double[] array) {
    this(array.length);
    System.arraycopy(array, 0, this.array, 0, this.array.length);

    StringBuilder builder = new StringBuilder();
    builder.append("[");
    for (int i = 0; i < array.length; i++) {
      builder.append(array[i]);
      if (i < array.length - 1) {
        builder.append(",");
      }
    }

    this.toString = builder.append("]").toString();
  }

  /**
   * Creates a default instance of <code>Point</code>.
   * 
   * @param dimension representing the length of the <code>Point</code>
   */
  private Point(int dimension) {
    this.array = new double[dimension];
  }

  /**
   * Factory method for creating <code>Point</code> instances out of arrays of values. (Factory
   * method pattern)
   * 
   * @param array representing a point
   * @return a reference to an instance of <code>Point</code>
   */
  public static final Point at(double... array) {
    return new Point(array);
  }

  public static final Point random(int dimension) {
    return Point.random(dimension, -Double.MAX_VALUE, Double.MAX_VALUE);
  }

  public static final Point random(int dimension, double min, double max) {
    double[] array = new double[dimension];

    for (int i = 0; i < array.length; i++)
      array[i] = Math.random() * (max - min) + min;

    return new Point(array);
  }

  /**
   * 
   * @return
   */
  public static final Point getDefault() {
    return DEFAULT;
  }

  /**
   * Returns the internal representation of this <code>Point</code> object. The returned value is a
   * copy of the internal immutable state.
   * 
   * @return internal state as an array
   */
  public double[] toArray() {
    double[] copy = new double[array.length];
    System.arraycopy(array, 0, copy, 0, copy.length);
    return copy;
  }

  @Override
  public String toString() {
    return toString;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Point other = (Point) obj;
    if (this.array != other.array
        && (this.array == null || !Arrays.equals(this.array, other.array))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 89 * hash + (this.array != null ? this.array.hashCode() : 0);
    return hash;
  }

  public int[] toIntArray() {
    int[] res = new int[array.length];
    for (int i = 0; i < array.length; i++) {
      res[i] = (int) Math.round(array[i]);
    }
    return res;
  }

  public String toLogString() {
    return toString.replaceAll("\\[|\\]", "").replace(",", "\t");
  }
}