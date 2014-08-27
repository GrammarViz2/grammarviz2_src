package edu.hawaii.jmotif.distance;

import com.dtw.TimeWarpInfo;
import com.timeseries.TimeSeries;
import com.timeseries.TimeSeriesPoint;
import com.util.DistanceFunction;
import com.util.DistanceFunctionFactory;
import edu.hawaii.jmotif.timeseries.TSException;

/**
 * The Euclidean distance implementation for a variety of datatypes.
 * 
 * @author Pavel Senin
 * 
 */
public final class EuclideanDistance {

  /**
   * Constructor.
   */
  private EuclideanDistance() {
    super();
  }

  /**
   * Calculates the square of the Euclidean distance between two 1D points represented by real
   * values.
   * 
   * @param p1 The first point.
   * @param p2 The second point.
   * @return The Square of Euclidean distance.
   */
  private static double distance2(double p1, double p2) {
    return (p1 - p2) * (p1 - p2);
  }

  /**
   * Calculates the square of the Euclidean distance between two multidimensional points represented
   * by the real vectors.
   * 
   * @param point1 The first point.
   * @param point2 The second point.
   * @return The Euclidean distance.
   * @throws TSException In the case of error.
   */
  private static double distance2(double[] point1, double[] point2) throws TSException {
    if (point1.length == point2.length) {
      Double sum = 0D;
      for (int i = 0; i < point1.length; i++) {
        double tmp = point2[i] - point1[i];
        sum = sum + tmp * tmp;
        // sum = sum + Math.pow(point2[i] - point1[i], 2.0);
      }
      return sum;
    }
    else {
      throw new TSException("Exception in Euclidean distance: array lengths are not equal");
    }
  }

  /**
   * Calculates the square of the Euclidean distance between two multidimensional points represented
   * by integer vectors.
   * 
   * @param point1 The first point.
   * @param point2 The second point.
   * @return The Euclidean distance.
   * @throws TSException In the case of error.
   */
  private static double distance2(int[] point1, int[] point2) throws TSException {
    if (point1.length == point2.length) {
      Double sum = 0D;
      for (int i = 0; i < point1.length; i++) {
        sum = sum
            + (Integer.valueOf(point2[i]).doubleValue() - Integer.valueOf(point1[i]).doubleValue())
            * (Integer.valueOf(point2[i]).doubleValue() - Integer.valueOf(point1[i]).doubleValue());
      }
      return sum;
    }
    else {
      throw new TSException("Exception in Euclidean distance: array lengths are not equal");
    }
  }

  /**
   * Calculates the Euclidean distance between two points.
   * 
   * @param p1 The first point.
   * @param p2 The second point.
   * @return The Euclidean distance.
   */
  public static double distance(double p1, double p2) {
    double d = (p1 - p2) * (p1 - p2);
    return Math.sqrt(d);
  }

  /**
   * Calculates the Euclidean distance between two points.
   * 
   * @param point1 The first point.
   * @param point2 The second point.
   * @return The Euclidean distance.
   * @throws TSException In the case of error.
   */
  public static double distance(double[] point1, double[] point2) throws TSException {
    return Math.sqrt(distance2(point1, point2));
  }

  /**
   * Calculates the Normalized Euclidean distance between two points.
   * 
   * @param point1 The first point.
   * @param point2 The second point.
   * @return The Euclidean distance.
   * @throws TSException In the case of error.
   */
  public static double normalizedDistance(double[] point1, double[] point2) throws TSException {
    return Math.sqrt(distance2(point1, point2)) / point1.length;
  }

  /**
   * Calculates the Euclidean distance between two points.
   * 
   * @param point1 The first point.
   * @param point2 The second point.
   * @return The Euclidean distance.
   * @throws TSException In the case of error.
   */
  public static double distance(int[] point1, int[] point2) throws TSException {
    return Math.sqrt(distance2(point1, point2));
  }

  /**
   * Calculates euclidean distance between two one-dimensional time-series of equal length.
   * 
   * @param series1 The first series.
   * @param series2 The second series.
   * @return The eclidean distance.
   * @throws TSException if error occures.
   */
  public static double seriesDistance(double[] series1, double[] series2) throws TSException {
    if (series1.length == series2.length) {
      Double res = 0D;
      for (int i = 0; i < series1.length; i++) {
        res = res + distance2(series1[i], series2[i]);
      }
      return Math.sqrt(res);
    }
    else {
      throw new TSException("Exception in Euclidean distance: array lengths are not equal");
    }
  }

  /**
   * Calculates euclidean distance between two multi-dimensional time-series of equal length.
   * 
   * @param series1 The first series.
   * @param series2 The second series.
   * @return The eclidean distance.
   * @throws TSException if error occures.
   */
  public static double seriesDistance(double[][] series1, double[][] series2) throws TSException {
    if (series1.length == series2.length) {
      Double res = 0D;
      for (int i = 0; i < series1.length; i++) {
        res = res + distance2(series1[i], series2[i]);
      }
      return Math.sqrt(res);
    }
    else {
      throw new TSException("Exception in Euclidean distance: array lengths are not equal");
    }
  }

  /**
   * Implements Euclidean distance with early abandoning.
   * 
   * @param series1 the first series.
   * @param series2 the second series.
   * @param cutoff the cut-off threshold
   * @return the distance if it is less than cutoff or Double.NAN if it is above.
   * 
   * @throws TSException if error occurs.
   */
  public static Double earlyAbandonedDistance(double[] series1, double[] series2, double cutoff)
      throws TSException {
    double cutOff2 = cutoff * cutoff;
    if (series1.length == series2.length) {
      Double res = 0D;
      for (int i = 0; i < series1.length; i++) {
        res = res + distance2(series1[i], series2[i]);
        if (res > cutOff2) {
          return Double.NaN;
        }
      }
      return Math.sqrt(res);
    }
    else {
      throw new TSException("Exception in Euclidean distance: array lengths are not equal");
    }
  }

  /**
   * This is DTW distance which we get using the FastDTW library.
   * 
   * @param series1 the series A.
   * @param series2 the series B.
   * @return the DTW distance value.
   */
  public static double getDTWDist(double[] series1, double[] series2) {

    // final TimeSeries tsI = new TimeSeries(args[0], false, false, ',');
    final TimeSeries tsI = new TimeSeries(1);
    for (int i = 0; i < series1.length; i++) {
      tsI.addLast(i, new TimeSeriesPoint(new double[] { series1[i] }));
    }

    // final TimeSeries tsJ = new TimeSeries(args[1], false, false, ',');
    final TimeSeries tsJ = new TimeSeries(1);
    for (int i = 0; i < series2.length; i++) {
      tsJ.addLast(i, new TimeSeriesPoint(new double[] { series2[i] }));
    }

    DistanceFunction distFn = DistanceFunctionFactory.getDistFnByName("EuclideanDistance");

    final TimeWarpInfo info = com.dtw.FastDTW.getWarpInfoBetween(tsI, tsJ, 15, distFn);

    return info.getDistance();
  }

}
