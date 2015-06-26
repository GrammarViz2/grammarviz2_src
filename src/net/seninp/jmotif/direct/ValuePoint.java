package net.seninp.jmotif.direct;

/**
 * An immutable point wrapper to represent the value of a function at a specified point. The
 * immutability makes this class threadsafe.
 * 
 * @author ytoh, seninp
 */
public class ValuePoint implements Comparable<ValuePoint> {
  private final Point point;
  private final double value;

  /** */
  private static final ValuePoint DEFAULT = new ValuePoint(Point.getDefault(), Double.NaN);

  /**
   * 
   * @param point
   * @param value
   */
  private ValuePoint(Point point, double value) {
    this.point = point;
    this.value = value;
  }

  /**
   * 
   * @return
   */
  public static ValuePoint getDefault() {
    return DEFAULT;
  }

  /**
   * 
   * @param p
   * @param value
   * @return
   */
  public static ValuePoint at(Point p, double value) {
    return new ValuePoint(p, value);
  }

  /**
   * 
   * @return
   */
  public double getValue() {
    return value;
  }

  /**
   * 
   * @return
   */
  public Point getPoint() {
    return point;
  }

  @Override
  public String toString() {
    return value + "@" + point;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ValuePoint other = (ValuePoint) obj;
    if (this.point != other.point && (this.point == null || !this.point.equals(other.point))) {
      return false;
    }
    if (this.value != other.value) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 17 * hash + (this.point != null ? this.point.hashCode() : 0);
    hash = 17
        * hash
        + (int) (Double.doubleToLongBits(this.value) ^ (Double.doubleToLongBits(this.value) >>> 32));
    return hash;
  }

  public int compareTo(ValuePoint o) {
    // return (int) (o.getValue() - this.getValue());

    if (this.getValue() > o.getValue())
      return 1;

    if (this.getValue() < o.getValue())
      return -1;

    return 0;
  }
}
