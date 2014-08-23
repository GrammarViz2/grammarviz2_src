package edu.hawaii.jmotif.distance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import edu.hawaii.jmotif.timeseries.TSException;

/**
 * Tests the Euclidean distance.
 * 
 * @author Pavel Senin.
 * 
 */
public class TestEuclideanDistance {

  // 1D points for the test
  private static final double[] testPoint1D1 = { 0.545 };
  private static final double[] testPoint1D2 = { 0.845 };

  // 3D points for the test
  private static final double[] testPoint3D1 = { 0.545, 0.856, 0.856 };
  private static final double[] testPoint3D2 = { 0.845, 0.654, 0.986 };

  // 2d series for the test
  private static final double[][] testSeries1 = { { 0.2, 0.6 }, { 0.3, 0.5 }, { 0.4, 0.4 },
      { 0.5, 0.3 }, { 0.6, 0.2 } };

  private static final double[][] testSeries2 = { { 1.0, 1.8 }, { 1.2, 1.6 }, { 1.4, 1.4 },
      { 1.6, 1.2 }, { 1.8, 1.0 } };

  private static final double[][] testSeries3 = { { 1.0, 1.8 }, { 1.2, 1.6 }, { 1.4, 1.4 },
      { 1.6, 1.2 } };

  /**
   * Test the distance between single points.
   * 
   */
  @Test
  public void testPointDistance() {

    try {
      //
      // test the distance between points
      assertEquals("test 1D distance", EuclideanDistance.distance(testPoint1D1, testPoint1D2), Math
          .abs(testPoint1D2[0] - testPoint1D1[0]), 0.01D);
      assertEquals("test 1D distance",
          EuclideanDistance.distance(testPoint1D1[0], testPoint1D2[0]), Math.abs(testPoint1D2[0]
              - testPoint1D1[0]), 0.01D);

      //
      // compute the test value right here
      double dist = 0D;
      for (int i = 0; i < 3; i++) {
        dist += (testPoint3D1[i] - testPoint3D2[i]) * (testPoint3D1[i] - testPoint3D2[i]);
      }
      dist = Math.sqrt(dist);

      //
      // multi-dimensional points which in fact are series
      assertEquals("test 1D distance", EuclideanDistance.distance(testPoint3D1, testPoint3D2),
          dist, 0.01D);
      assertEquals("test series distance", EuclideanDistance.seriesDistance(testPoint3D1,
          testPoint3D2), dist, 0.01D);
    }
    catch (TSException e) {
      fail("Shouldn't throw any exception");
    }

    //
    // test for exception
    try {
      @SuppressWarnings("unused")
      double distance = EuclideanDistance.distance(testPoint1D1, testPoint3D1);
      fail("Exception is not thrown!");
    }
    catch (TSException e) {
      assert true;
    }

    //
    // test for exception
    try {
      @SuppressWarnings("unused")
      double distance = EuclideanDistance.seriesDistance(testPoint1D1, testPoint3D1);
      fail("Exception is not thrown!");
    }
    catch (TSException e) {
      assert true;
    }

  }

  /**
   * Test the distance between two series.
   * 
   */
  @Test
  public void testSeriesDistance() {
    try {
      Double dist = EuclideanDistance.seriesDistance(testSeries1, testSeries2);
      assertEquals("testing distance, ", 3.193743885, dist, 0.01D);
    }
    catch (TSException e) {
      fail("Should not throw any exception here.");
    }

    //
    // test for exception
    try {
      @SuppressWarnings("unused")
      Double dist = EuclideanDistance.seriesDistance(testSeries1, testSeries3);
      fail("Should throw exception here.");
    }
    catch (TSException e) {
      assert true;
    }

  }

}
