package net.seninp.tinker;

public class Interval {

  private int start;
  private int end;

  public Interval(int start, int end) {
    this.start = start;
    this.end = end;
  }

  /**
   * Returns true if this interval intersects the specified interval.
   *
   * @param that the other interval
   * @return <tt>true</tt> if this interval intersects the argument interval; <tt>false</tt>
   * otherwise
   */
  public boolean intersects(Interval that) {
    if (this.end < that.start) {
      return false;
    }
    if (that.end < this.start) {
      return false;
    }
    return true;
  }

  /**
   * Returns true if this interval contains the specified value.
   *
   * @param x the value
   * @return <tt>true</tt> if this interval contains the value <tt>x</tt>; <tt>false</tt> otherwise
   */
  public boolean contains(int x) {
    return (start <= x) && (x <= end);
  }

  /**
   * Returns the length of this interval.
   *
   * @return the length of this interval (max - min)
   */
  public double length() {
    return this.end - this.start;
  }

}
