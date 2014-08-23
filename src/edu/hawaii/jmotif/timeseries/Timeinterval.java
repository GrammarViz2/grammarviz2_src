package edu.hawaii.jmotif.timeseries;

/**
 * The timeinterval implementation. Timeinterval is a strech of time with an asiigned value.
 * 
 * @author Pavel Senin
 * 
 */
public final class Timeinterval implements Comparable<Timeinterval> {

  private double value;
  private long start;
  private long end;

  /**
   * Constructor.
   * 
   * @param value The interval value.
   * @param start The interval start timestamp.
   * @param end The interval end timestamp.
   */
  public Timeinterval(double value, long start, long end) {
    this.value = value;
    this.start = start;
    this.end = end;
  }

  /**
   * Get the value.
   * 
   * @return the value.
   */
  public double getValue() {
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
   * Get the start time stamp.
   * 
   * @return the timestamp.
   */
  public long getStart() {
    return this.start;
  }

  /**
   * Set the start time stamp.
   * 
   * @param newStart The new tstamp to set.
   */
  public void setStart(long newStart) {
    this.start = newStart;
  }

  /**
   * Get the end time stamp.
   * 
   * @return the end timestamp.
   */
  public long getEnd() {
    return this.end;
  }

  /**
   * Set the end time stamp.
   * 
   * @param newEnd The new end tstamp to set.
   */
  public void setEnd(long newEnd) {
    this.end = newEnd;
  }

  /**
   * {@inheritDoc}
   */
  public int hashCode() {

    int hash = 7;

    int num0 = 0;
    int num1 = 0;

    if (this.start > Integer.MAX_VALUE) {
      num0 = (int) (this.start ^ (this.start >>> 32));
    }
    else {
      num0 = (int) this.start;
    }

    if (this.end > Integer.MAX_VALUE) {
      num0 = (int) (this.end ^ (this.end >>> 32));
    }
    else {
      num0 = (int) this.end;
    }

    long bits = Double.doubleToLongBits(this.value);
    int num2 = (int) (bits ^ (bits >>> 32));

    hash = num0 + hash * (num1 + num2);
    return hash;
  }

  /**
   * {@inheritDoc}
   */
  public boolean equals(Object o) {
    if (o instanceof Timeinterval) {
      Timeinterval ti = (Timeinterval) o;
      if ((this.value == ti.getValue()) && (this.start == ti.getStart())
          && (this.end == ti.getEnd())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Compares the TimeInterval object with other TimeInterval using START timestamps first: i.e. by
   * the timestamp values, if they are equal, the END values or VALUE used.
   * 
   * @param o the TimeInterval to compare with.
   * 
   * @return the standard compareTo result.
   */
  public int compareTo(Timeinterval o) {
    if ((this.value == o.getValue()) && (this.start == o.getStart()) && (this.end == o.getEnd())) {
      return 0;
    }
    else if (this.start > o.start) {
      return 1;
    }
    else if (this.start < o.start) {
      return -1;
    }
    else if (this.end > o.end) {
      return 1;
    }
    else if (this.end < o.end) {
      return -1;
    }
    else if (this.value > o.value) {
      return 1;
    }
    return -1;
  }
}