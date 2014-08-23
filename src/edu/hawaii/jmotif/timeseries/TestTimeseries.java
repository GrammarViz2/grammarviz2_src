package edu.hawaii.jmotif.timeseries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the timeseries class.
 *
 * @author Pavel Senin.
 *
 */
public class TestTimeseries {

  private Timeseries seriesA;
  private static final double[] testSeriesAValues = { 0.22, 0.23, 0.24, 0.50, 0.83 };
  private static final long[] testSeriesATstamps = { 22L, 23L, 24L, 50L, 83L };
  private static final Integer tsaSize = 5;

  private Timeseries seriesB;
  private static final double[] testSeriesBValues = { 0.82, 0.63, 0.54, 0.70, 0.33 };
  private static final long[] testSeriesBTstamps = { 82L, 83L, 84L, 85L, 86L };
  private static final Integer tsbSize = 5;

  private Timeseries seriesC;
  private static final double[] testSeriesCValues = { 0.82, Double.POSITIVE_INFINITY, Double.NaN,
      Double.NEGATIVE_INFINITY, 0.33 };
  private static final long[] testSeriesCTstamps = { 82L, 83L, 84L, 85L, 86L };

  private static final double delta = 0.000001;

  /**
   * Set up testing environment.
   *
   * @throws Exception if error occurs.
   */
  @Before
  public void setUp() throws Exception {
    seriesA = new Timeseries(testSeriesAValues, testSeriesATstamps);
    seriesB = new Timeseries(testSeriesBValues, testSeriesBTstamps);
    seriesC = new Timeseries(testSeriesCValues, testSeriesCTstamps);
  }

  /**
   * Test insert methods.
   *
   * @throws TSException if error occurs.
   */
  @Test
  public void testInsert() throws TSException {
    // private static final double[] testSeriesAValues = { 0.22, 0.23, 0.24, 0.50, 0.83 };
    // private static final long[] testSeriesATstamps = { 22L, 23L, 24L, 50L, 83L };
    Timeseries seriesA1 = new Timeseries(testSeriesAValues, testSeriesATstamps);

    seriesA1.addByTime(new TPoint(0.11D, 11L));
    assertEquals("Testing insert code", 6, seriesA1.size());
    assertEquals("Testing insert code", 0.11D, seriesA1.elementAt(0).value(), 0.00001);

    seriesA1.addByTime(new TPoint(1.01D, 101L));
    assertEquals("Testing insert code", 7, seriesA1.size());
    assertEquals("Testing insert code", 1.01D, seriesA1.elementAt(6).value(), 0.00001);

    seriesA1.addByTime(new TPoint(0.31D, 31L));
    assertEquals("Testing insert code", 8, seriesA1.size());
    assertEquals("Testing insert code", 0.31D, seriesA1.elementAt(4).value(), 0.00001);

    seriesA1.addByTime(new TPoint(0.12D, 12L));
    assertEquals("Testing insert code", 9, seriesA1.size());
    assertEquals("Testing insert code", 0.12D, seriesA1.elementAt(1).value(), 0.00001);
    double val = seriesA1.elementAt(2).value();
    long tstamp = seriesA1.elementAt(2).tstamp();

    seriesA1.addByTime(new TPoint(1.0D, 100L));
    assertEquals("Testing insert code", 10, seriesA1.size());
    assertEquals("Testing insert code", 1.0D, seriesA1.elementAt(8).value(), 0.00001);

    seriesA1.removeAt(1);
    assertEquals("Testing insert code", 9, seriesA1.size());
    assertEquals("Testing insert code", val, seriesA1.elementAt(1).value(), 0.00001);
    assertEquals("Testing insert code", tstamp, seriesA1.elementAt(1).tstamp());

  }

  /**
   * Test equals and hashCode methods.
   *
   * @throws TSException if error occurs.
   */
  @Test
  public void testEquals() throws TSException {
    Timeseries seriesA1 = new Timeseries(testSeriesAValues, testSeriesATstamps);
    assertEquals("Testing hash code", seriesA1.hashCode(), seriesA.hashCode());
    assertNotSame("Testing hash code", seriesA1.hashCode(), seriesB.hashCode());
    assertNotSame("Testing hash code", seriesA.hashCode(), seriesB.hashCode());
    assertEquals("Testing equals", seriesA1, seriesA);

    assertFalse("Testing equals", seriesA1.equals(((Integer) 25)));
  }

  /**
   * Test timeseries.
   */
  @Test
  public void testTS() {
    assertEquals("Testing TS", tsaSize.intValue(), seriesA.size());
    assertEquals("Testing TS", tsbSize.intValue(), seriesB.size());
    assertEquals("Testing TS", (Double) testSeriesAValues[0],
        (Double) seriesA.elementAt(0).value());
    assertEquals("Testing TS", (Double) testSeriesAValues[2],
        (Double) seriesA.elementAt(2).value());
    assertEquals("Testing TS", (Double) testSeriesAValues[4],
        (Double) seriesA.elementAt(4).value());

    double[] values = testSeriesAValues;
    long[] tstamps = new long[testSeriesATstamps.length - 1];
    for (int i = 0; i < testSeriesATstamps.length - 1; i++) {
      tstamps[i] = testSeriesATstamps[i];
    }

    //
    // test constructor exception when size is not equal, part 1
    try {
      @SuppressWarnings("unused")
      Timeseries tsFail = new Timeseries(values, tstamps);
      fail("Exception wasnt thrown!");
    }
    catch (TSException e) {
      assertTrue("Testing exception", e.getMessage().contains(
          "The lengths of the values " + "and timestamps arrays are not equal!"));
      assert true;
    }

    //
    // test constructor exception when size is not equal, part 2
    try {
      @SuppressWarnings("unused")
      Timeseries tsFail = new Timeseries(values, tstamps, -1.0);
      fail("Exception wasnt thrown!");
    }
    catch (TSException e) {
      assertTrue("Testing exception", e.getMessage().contains(
          "The lengths of the values " + "and timestamps arrays are not equal!"));
      assert true;
    }
  }

  /**
   * Test clone.
   *
   * @throws CloneNotSupportedException if error occurs.
   */
  @Test
  public void testTSClone() throws CloneNotSupportedException {
    Timeseries cloneA = seriesA.clone();
    Timeseries cloneB = seriesB.clone();
    assertEquals("Testing TS", seriesA, cloneA);
    assertTrue("Testing TS", seriesA.equals(cloneA));
    assertFalse("Testing TS", cloneA.equals(cloneB));
  }

  /**
   * Test subsection.
   *
   * @throws TSException if error occurs.
   */
  @Test
  public void testTSSubsection() throws TSException {
    Timeseries prefix = seriesA.subsection(0, 1);
    assertEquals("Testing subsection", 2, prefix.size());
    assertEquals("Testing subsection", prefix.elementAt(0).value(), seriesA.elementAt(0).value(),
        delta);
    assertEquals("Testing subsection", prefix.elementAt(1).value(), seriesA.elementAt(1).value(),
        delta);

    seriesA.elementAt(1).setValue(Double.NaN);
    seriesA.elementAt(2).setValue(Double.NaN);

    prefix = seriesA.subsection(0, 4);
    assertEquals("Testing subsection", 5, prefix.size());
    assertEquals("Testing subsection", prefix.elementAt(0).value(), seriesA.elementAt(0).value(),
        delta);
    assertEquals("Testing subsection", prefix.elementAt(1).value(), Double.NaN, delta);

    Timeseries suffix = seriesA.subsection(2, 4);
    assertEquals("Testing subsection", 3, suffix.size());
    assertEquals("Testing subsection", suffix.elementAt(2).value(), seriesA.elementAt(4).value(),
        delta);
    assertEquals("Testing subsection", suffix.elementAt(1).value(), seriesA.elementAt(3).value(),
        delta);
    assertEquals("Testing subsection", suffix.elementAt(1).tstamp(), seriesA.elementAt(3).tstamp());
    assertEquals("Testing subsection", suffix.elementAt(0).value(), seriesA.elementAt(2).value(),
        delta);

    //
    // test exception when index is out of boundaries, part 1
    try {
      @SuppressWarnings("unused")
      Timeseries tsFail = seriesA.subsection(-2, seriesA.size() - 1);
      fail("Exception wasnt thrown!");
    }
    catch (TSException e) {
      assertTrue("Testing exception", e.getMessage().contains("Invalid interval specified"));
      assert true;
    }

    //
    // test exception when index is out of boundaries, part 1
    try {
      @SuppressWarnings("unused")
      Timeseries tsFail = seriesA.subsection(0, seriesA.size() + 1);
      fail("Exception wasnt thrown!");
    }
    catch (TSException e) {
      assertTrue("Testing exception", e.getMessage().contains("Invalid interval specified"));
      assert true;
    }

  }

  /**
   * Test the data conversion.
   *
   * @throws TSException If error occurs.
   */
  @Test
  public void testTypeConversion() throws TSException {
    double[] valsAsVector = seriesA.values();
    for (int i = 0; i < valsAsVector.length; i++) {
      assertEquals(testSeriesAValues[i], valsAsVector[i], 0.01);
    }

    double[][] valsAsMatix = seriesA.valuesAsMatrix();
    for (int i = 0; i < valsAsMatix[0].length; i++) {
      assertEquals(testSeriesAValues[i], valsAsMatix[0][i], 0.01);
    }
  }

  /**
   * Test the NaN features in timeseries.
   *
   * @throws TSException If error occurs.
   */
  @Test
  public void testTSNAFeatures() throws TSException {
    assertTrue("Testing NaN values", ((Double) seriesC.elementAt(1).value()).isInfinite());
    assertTrue("Testing NaN values", ((Double) seriesC.elementAt(2).value()).isNaN());
    assertTrue("Testing NaN values", ((Double) seriesC.elementAt(3).value()).isInfinite());

    seriesC = new Timeseries(testSeriesCValues, testSeriesCTstamps);
    assertFalse("Testing NaN values", Double.isNaN(seriesC.elementAt(0).value()));
    seriesC = new Timeseries(testSeriesCValues, testSeriesCTstamps, testSeriesCValues[0]);
    assertTrue("Testing NaN values", Double.isNaN(seriesC.elementAt(0).value()));
  }

  /**
   * Test toString staff.
   *
   * @throws TSException If error occurs.
   */
  @Test
  public void testToString() throws TSException {
    String tsA = seriesA.toString();
    assertTrue("Testing to string method", tsA.startsWith("0.22, 0.23,"));
  }
}
