package edu.hawaii.jmotif.timeseries;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;
import edu.hawaii.jmotif.util.StackTrace;

/**
 * Implements time-series. It's important to know how it handles NaN values. If input data has NaN's
 * or INFINITY's as entries it will be kept same, but if you specify particular numeric value as
 * NaN, (say a 0 for missing values) it'll be converted into Double.NaN internally and you have to
 * convert it back to your number later.
 * 
 * @author Pavel Senin.
 * 
 */
public final class Timeseries implements Iterable<TPoint>, Cloneable, Serializable {

  private static final String COMMA = ", ";

  private final Vector<TPoint> series = new Vector<TPoint>();

  private static final long serialVersionUID = 7526471155622776148L;

  /**
   * Constructor.
   * 
   */
  public Timeseries() {
    assert true;
  }

  /**
   * Constructor.
   * 
   * @param values the timeseries values.
   * @param tstamps the timestamps.
   * @throws TSException if error occurs.
   */
  public Timeseries(double[] values, long[] tstamps) throws TSException {
    if (values.length == tstamps.length) {
      for (int i = 0; i < values.length; i++) {
        this.series.add(new TPoint(values[i], tstamps[i]));
      }
    }
    else {
      throw new TSException("The lengths of the values and timestamps arrays are not equal!");
    }
  }

  /**
   * Constructor.
   * 
   * @param values the timeseries values.
   * @param tstamps the timestamps.
   * @param nanValue the Not a Number value for this timeseries.
   * @throws TSException if error occurs.
   */
  public Timeseries(double[] values, long[] tstamps, double nanValue) throws TSException {
    if (values.length == tstamps.length) {
      for (int i = 0; i < values.length; i++) {
        if (nanValue == values[i]) {
          this.series.add(new TPoint(Double.NaN, tstamps[i]));
        }
        else {
          this.series.add(new TPoint(values[i], tstamps[i]));
        }
      }
    }
    else {
      throw new TSException("The lengths of the values and timestamps arrays are not equal!");
    }
  }

  /**
   * Constructor.
   * 
   * @param values the timeseries values.
   */
  public Timeseries(double[] values) {
    for (int i = 0; i < values.length; i++) {
      this.series.add(new TPoint(values[i], i));
    }
  }

  /**
   * Add the point at the end of the timeseries. No consistency check is conducted.
   * 
   * @param p The point to add.
   */
  public void add(TPoint p) {
    this.series.add(p);
  }

  /**
   * Add the point to the timeseries, inserting it at the position specified by the timestamp.
   * 
   * @param p The point to add.
   */
  public void addByTime(TPoint p) {
    if (this.series.isEmpty()) {
      this.series.add(p);
      return;
    }
    else if (p.tstamp() > this.series.lastElement().tstamp()) {
      this.series.add(p);
      return;
    }
    else if (p.tstamp() < this.series.firstElement().tstamp()) {
      this.series.insertElementAt(p, 0);
      return;
    }
    int pos2insert = findPositions(p);
    this.series.insertElementAt(p, pos2insert);
  }

  /**
   * Finds the position for the new point insertion.
   * 
   * @param p The point to add to the timeseries.
   * @return position found.
   */
  private int findPositions(TPoint p) {
    int i = 0;
    int j = this.series.size();
    int k = 0;
    while ((j - i) > 1) {
      k = i + (j - i) / 2;
      if (p.tstamp() == this.series.elementAt(k).tstamp()) {
        return k;
      }
      else if (p.tstamp() < this.series.elementAt(k).tstamp()) {
        j = k;
      }
      else {
        i = k;
      }
    }
    if ((j - i) == 1) {
      return j;
    }
    return -1;
  }

  /**
   * Remove an element from the timeseries.
   * 
   * @param pos The position of the element to remove.
   * @throws TSException if wrong position specified.
   */
  public void removeAt(int pos) throws TSException {
    if (pos >= 0 && pos < this.series.size()) {
      this.series.remove(pos);
    }
    else {
      throw new TSException("Illegal position specified for removal: " + pos + ", series length: "
          + this.size());
    }
  }

  /**
   * Get the length of the timeseries.
   * 
   * @return the length of the timeseries.
   */
  public int size() {
    return this.series.size();
  }

  /**
   * Return the element at the position.
   * 
   * @param i the position of the element.
   * @return the element at the position i.
   */
  public TPoint elementAt(int i) {
    return this.series.get(i);
  }

  /**
   * Return the array of values.
   * 
   * @return the array of values.
   */
  public double[] values() {
    double[] res = new double[this.series.size()];
    for (int i = 0; i < this.series.size(); i++) {
      res[i] = this.series.get(i).value();
    }
    return res;
  }

  /**
   * Return the one-row matrix of values.
   * 
   * @return the one-row matrix of values.
   */
  public double[][] valuesAsMatrix() {
    double[][] res = new double[1][this.series.size()];
    for (int i = 0; i < this.series.size(); i++) {
      res[0][i] = this.series.get(i).value();
    }
    return res;
  }

  /**
   * Return the array of timestamps.
   * 
   * @return the array of timestamps.
   */
  public long[] tstamps() {
    long[] res = new long[this.series.size()];
    for (int i = 0; i < this.series.size(); i++) {
      res[i] = this.series.get(i).tstamp();
    }
    return res;
  }

  /**
   * Extract interval from timeseries. It is inclusive, i.e. subsection(0,5) will include 6 elements
   * from ts[0] to ts[5].
   * 
   * @param start the interval start.
   * @param end the interval end.
   * @return the timeseries interval.
   * @throws TSException if error occurs.
   */
  public Timeseries subsection(int start, int end) throws TSException {
    if (start >= 0 && end < this.series.size()) {
      int len = end - start + 1;
      double[] val = new double[len];
      long[] ts = new long[len];

      for (int i = start; i <= end; i++) {
        TPoint tp = this.series.get(i);
        val[i - start] = tp.value();
        ts[i - start] = tp.tstamp();
      }

      return new Timeseries(val, ts);

    }
    else {
      throw new TSException("Invalid interval specified: timeseries size " + this.series.size()
          + " interval [" + start + ", " + end + "]");
    }
  }

  /**
   * {@inheritDoc}
   */
  public int hashCode() {
    int hash = 7;
    if (this.series.size() > 0) {
      hash = hash + 31 * this.series.get(0).hashCode();
    }
    for (int i = 1; i < this.series.size(); i++) {
      hash = hash + this.series.get(i).hashCode();
    }
    return hash;
  }

  /**
   * {@inheritDoc}
   */
  public boolean equals(Object o) {
    if (o instanceof Timeseries) {
      Timeseries ot = (Timeseries) o;
      if (this.size() == ot.size()) {
        for (int i = 0; i < this.series.size(); i++) {
          if (this.series.get(i).equals(ot.elementAt(i))) {
            continue;
          }
          else {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<TPoint> iterator() {
    return this.series.iterator();
  }

  /**
   * {@inheritDoc}
   */
  public Timeseries clone() throws CloneNotSupportedException {
    int len = this.series.size();
    double[] val = new double[len];
    long[] ts = new long[len];

    for (int i = 0; i < len; i++) {
      TPoint tp = this.series.get(i);
      val[i] = tp.value();
      ts[i] = tp.tstamp();
    }

    try {
      return new Timeseries(val, ts);
    }
    catch (TSException e) {
      throw new CloneNotSupportedException("Exception thrown! " + StackTrace.toString(e));
    }
  }

  /**
   * {@inheritDoc}
   */
  public String toString() {
    StringBuffer res = new StringBuffer(500);
    for (TPoint tp : this.series) {
      res.append(tp.value() + COMMA);
    }
    return res.substring(0, res.length() - 2);
  }

}
