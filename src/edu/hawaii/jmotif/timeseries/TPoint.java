package edu.hawaii.jmotif.timeseries;

import java.io.Serializable;

/**
 * The singular timeseries data point implementation.
 *
 * @author Pavel Senin.
 *
 */
public final class TPoint implements Comparable<TPoint>, Serializable {

  private static final long serialVersionUID = 7526471155622776149L;

  /** The point value. */
  private double value;

  /** The timestamp in milliseconds. */
  private long tstamp;

  /**
   * Constructor.
   *
   * @param value The timepoint value.
   * @param tstamp The timepoint timestamp.
   */
  public TPoint(double value, long tstamp) {
    this.value = value;
    this.tstamp = tstamp;
  }

  /**
   * Get the value.
   *
   * @return the value.
   */
  public double value() {
    return this.value;
  }

  /**
   * Set the value.
   *
   * @param newValue The value to set.
   */
  public void setValue(double newValue) {
    this.value = newValue;
  }

  /**
   * Get the time stamp.
   *
   * @return the timestamp.
   */
  public long tstamp() {
    return this.tstamp;
  }

  /**
   * Set the timestamp.
   *
   * @param newTstamp The new tstamp to set.
   */
  public void setTstamp(long newTstamp) {
    this.tstamp = newTstamp;
  }

  /**
   * {@inheritDoc}
   */
  public int hashCode() {
    int hash = 7;
    int num0 = 0;
    if (this.tstamp > Integer.MAX_VALUE) {
      num0 = (int) (this.tstamp ^ (this.tstamp >>> 32));
    }
    else {
      num0 = (int) this.tstamp;
    }
    long bits = Double.doubleToLongBits(this.value);
    int num1 = (int) (bits ^ (bits >>> 32));
    hash = num0 + hash * num1;
    return hash;
  }

  /**
   * {@inheritDoc}
   */
  public boolean equals(Object o) {
    if (o instanceof TPoint) {
      TPoint tp = (TPoint) o;
      if ((this.value == tp.value()) && (this.tstamp == tp.tstamp())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Compares the TPoint object with other TPoint using timestamps first: i.e. by the timestamp
   * values, if they are equal, the TPoint values used.
   *
   * @param o the TPoint to compare with.
   *
   * @return the standard compareTo result.
   */
  public int compareTo(TPoint o) {
    if ((this.value == o.value()) && (this.tstamp == o.tstamp())) {
      return 0;
    }
    else if (this.tstamp > o.tstamp) {
      return 1;
    }
    else if (this.tstamp < o.tstamp) {
      return -1;
    }
    else if (this.value > o.value) {
      return 1;
    }
    return -1;
  }

}