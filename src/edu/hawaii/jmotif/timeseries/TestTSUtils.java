package edu.hawaii.jmotif.timeseries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import edu.hawaii.jmotif.sax.alphabet.Alphabet;
import edu.hawaii.jmotif.sax.alphabet.BuildTelemetryAlphabet;
import edu.hawaii.jmotif.sax.alphabet.NormalAlphabet;
import edu.hawaii.jmotif.sax.alphabet.UniversalTelemetryAlphabet;

/**
 * Test the TSUtils.
 * 
 * @author Pavel Senin.
 * 
 * 
 * 
 * 
 */
public class TestTSUtils {

  private static final String ts1File = "RCode/data/timeseries01.csv";
  private static final String ts2File = "RCode/data/timeseries02.csv";

  private static final String ts1NormFile = "RCode/data/timeseries01.norm.csv";
  private static final String ts2NormFile = "RCode/data/timeseries02.norm.csv";

  private static final String ts1PAAFile = "RCode/data/timeseries01.PAA10.csv";
  private static final String ts2PAAFile = "RCode/data/timeseries02.PAA10.csv";

  private static final String attribute = "value0";

  private static final int length = 15;
  private static final int PAAlength = 10;
  private static final double delta = 0.000001;

  private static final double ts1Max = 9.2;
  private static final double ts1Min = 1.34;
  private static final double ts2Max = 8.83;
  private static final double ts2Min = 0.5;

  private static final Alphabet normalA = new NormalAlphabet();
  private static final Alphabet buildA = new BuildTelemetryAlphabet();
  private static final Alphabet universalA = new UniversalTelemetryAlphabet();

  private static final double[] tsWithNaNs = { 1.0, 4.0, 7.0, 10.0, 2.0, 5.0, 8.0, 11.0, 3.0, 6.0,
      9.0, 12 };

  Timeseries ts1, ts2, ts1Norm, ts2Norm, ts1PAA10, ts2PAA10;

  /**
   * Test set-up.
   * 
   * @throws Exception if error occurs.
   */
  @Before
  public void setUp() throws Exception {
    ts1 = TSUtils.readTS(ts1File, length);
    ts2 = TSUtils.readTS(ts2File, length);
  }

  /**
   * Test the extremum calculations.
   */
  @Test
  public void testExtremum() {
    assertEquals("max", ts1Max, TSUtils.max(ts1), delta);
    assertEquals("max", ts2Max, TSUtils.max(ts2), delta);
    assertEquals("min", ts1Min, TSUtils.min(ts1), delta);
    assertEquals("min", ts2Min, TSUtils.min(ts2), delta);
    //
    // now set the value of the second element as NaN
    ts1.elementAt(1).setValue(Double.NaN);
    ts2.elementAt(2).setValue(Double.NaN);
    assertEquals("max", ts1Max, TSUtils.max(ts1), delta);
    assertEquals("max", ts2Max, TSUtils.max(ts2), delta);
    assertEquals("min", ts1Min, TSUtils.min(ts1), delta);
    assertEquals("min", ts2Min, TSUtils.min(ts2), delta);
  }

  /**
   * Test the mean calculation.
   */
  @Test
  public void testMean() {
    assertEquals("mean", 4.606667, TSUtils.mean(ts1), delta);
    assertEquals("mean", 4.606667, TSUtils.mean(ts1.values()), delta);

    assertEquals("mean", 4.01, TSUtils.mean(ts2), delta);
    assertEquals("mean", 4.01, TSUtils.mean(ts2.values()), delta);
    //
    // now set the value of the second element as NaN
    ts1.elementAt(0).setValue(Double.NaN);
    ts2.elementAt(0).setValue(Double.NaN);

    assertEquals("mean", 4.791429, TSUtils.mean(ts1), delta);
    assertEquals("mean", 4.791429, TSUtils.mean(ts1.values()), delta);

    assertEquals("mean", 4.260714, TSUtils.mean(ts2), delta);
    assertEquals("mean", 4.260714, TSUtils.mean(ts2.values()), delta);
  }

  /**
   * Test the variance calculation.
   */
  @Test
  public void testVar() {
    assertEquals("variance", 6.971267, TSUtils.var(ts1), delta);
    assertEquals("variance", 6.971267, TSUtils.var(ts1.values()), delta);
    assertEquals("variance", 7.409971, TSUtils.var(ts2), delta);
    assertEquals("variance", 7.409971, TSUtils.var(ts2.values()), delta);
    //
    // now set the value of the second element as NaN
    ts1.elementAt(0).setValue(Double.NaN);
    ts2.elementAt(0).setValue(Double.NaN);

    assertEquals("variance", 6.956075, TSUtils.var(ts1), delta);
    assertEquals("variance", 6.956075, TSUtils.var(ts1.values()), delta);
    assertEquals("variance", 6.964576, TSUtils.var(ts2), delta);
    assertEquals("variance", 6.964576, TSUtils.var(ts2.values()), delta);

  }

  /**
   * Test the standard deviation calculation.
   */
  @Test
  public void testStdev() {
    assertEquals("stdev", 2.640316, TSUtils.stDev(ts1), delta);
    assertEquals("stdev", 2.640316, TSUtils.stDev(ts1.values()), delta);

    ts1.elementAt(0).setValue(Double.NaN);
    assertEquals("stdev", 2.637437, TSUtils.stDev(ts1), delta);
    assertEquals("stdev", 2.637437, TSUtils.stDev(ts1.values()), delta);

    assertEquals("stdev", 2.722126, TSUtils.stDev(ts2), delta);
    assertEquals("stdev", 2.722126, TSUtils.stDev(ts2.values()), delta);
  }

  /**
   * Test the normalize routine.
   * 
   * @throws Exception if error occurs.
   */
  @Test
  public void testNormalize() throws Exception {
    // read the normalized data
    ts1Norm = TSUtils.readTS(ts1NormFile, length);
    ts2Norm = TSUtils.readTS(ts2NormFile, length);

    // get the normal data through the code
    Timeseries ts1NormTest = TSUtils.zNormalize(ts1);
    double[] ts1NormDoubleTest = TSUtils.zNormalize(ts1.values());

    Timeseries ts2NormTest = TSUtils.zNormalize(ts2);

    // get data for testing as arrays
    double[] ts1TrueValues = ts1Norm.values();
    double[] ts1TestValues = ts1NormTest.values();

    assertEquals("normalization", ts1TrueValues.length, ts1TestValues.length);
    for (int i = 0; i < ts1TrueValues.length; i++) {
      assertEquals("normalization", ts1TrueValues[i], ts1TestValues[i], delta);
      assertEquals("normalization", ts1TrueValues[i], ts1NormDoubleTest[i], delta);
    }

    double[] ts2Values = new double[ts2Norm.size()];
    double[] ts2TestValues = new double[ts2NormTest.size()];
    assertEquals("normalization", ts2Values.length, ts2TestValues.length);
    for (int i = 0; i < ts2Values.length; i++) {
      assertEquals("normalization", ts2Values[i], ts2TestValues[i], delta);
    }
  }

  /**
   * Test the normalize routine.
   * 
   * @throws Exception if error occurs.
   */
  @Test
  public void testNormalizeWithNaN1() throws Exception {
    //
    // test for single value within the timeseries.

    // length of the ts1 is 14, let's keep only value at the position 5
    for (int i = 0; i < ts1.size(); i++) {
      if (4 == i) {
        continue;
      }
      ts1.elementAt(i).setValue(Double.NaN);
    }

    ts1Norm = TSUtils.zNormalize(ts1);
    assertEquals("Test normalize", 1.0, ts1Norm.elementAt(4).value(), 0.00001D);
    assertTrue("Test normalize", Double.isNaN(ts1Norm.elementAt(2).value()));
    assertTrue("Test normalize", Double.isNaN(ts1Norm.elementAt(6).value()));

    double[] ts1Norm1 = TSUtils.zNormalize(ts1.values());
    assertEquals("Test normalize", 1.0, ts1Norm1[4], 0.00001D);
    assertTrue("Test normalize", Double.isNaN(ts1Norm1[2]));
    assertTrue("Test normalize", Double.isNaN(ts1Norm1[6]));
  }

  /**
   * Test the normalize routine.
   * 
   * @throws Exception if error occurs.
   */
  @Test
  public void testNormalizeWithNaN2() throws Exception {
    //
    // test for all NaN's

    // length of the ts1 is 14, let's keep only value at the position 5
    for (int i = 0; i < ts1.size(); i++) {
      ts1.elementAt(i).setValue(Double.NaN);
    }

    ts1Norm = TSUtils.zNormalize(ts1);
    assertTrue("Test normalize", Double.isNaN(ts1Norm.elementAt(2).value()));
    assertTrue("Test normalize", Double.isNaN(ts1Norm.elementAt(6).value()));

    double[] ts1Norm1 = TSUtils.zNormalize(ts1.values());
    assertTrue("Test normalize", Double.isNaN(ts1Norm1[2]));
    assertTrue("Test normalize", Double.isNaN(ts1Norm1[6]));
  }

  /**
   * Test the normalize routine.
   * 
   * @throws Exception if error occurs.
   */
  @Test
  public void testNormalizeWithNaN3() throws Exception {
    //
    // test for standard deviation = 0

    // length of the ts1 is 14, let's keep only value at the position 5
    for (int i = 0; i < ts1.size(); i++) {
      if (3 == i || 4 == i) {
        ts1.elementAt(i).setValue(3.0D);
      }
      else {
        ts1.elementAt(i).setValue(Double.NaN);
      }
    }

    ts1Norm = TSUtils.zNormalize(ts1);
    assertTrue("Test normalize", Double.isNaN(ts1Norm.elementAt(2).value()));
    assertEquals("Test normalize", 0.1D, ts1Norm.elementAt(3).value(), 0.00001D);
    assertEquals("Test normalize", 0.1D, ts1Norm.elementAt(4).value(), 0.00001D);
    assertTrue("Test normalize", Double.isNaN(ts1Norm.elementAt(5).value()));

    double[] ts1Norm1 = TSUtils.zNormalize(ts1.values());
    assertTrue("Test normalize", Double.isNaN(ts1Norm1[2]));
    assertEquals("Test normalize", 0.1D, ts1Norm1[3], 0.00001D);
    assertEquals("Test normalize", 0.1D, ts1Norm1[4], 0.00001D);
    assertTrue("Test normalize", Double.isNaN(ts1Norm1[5]));

  }

  /**
   * Test the PAA routine.
   * 
   * @throws Exception if error occurs.
   */
  @Test
  public void testPAA() throws Exception {
    // read the normalized data
    ts1Norm = TSUtils.readTS(ts1NormFile, length);
    ts2Norm = TSUtils.readTS(ts2NormFile, length);

    // read the PAA data
    ts1PAA10 = TSUtils.readTS(ts1PAAFile, PAAlength);
    ts2PAA10 = TSUtils.readTS(ts2PAAFile, PAAlength);

    // get the normal data through the code
    Timeseries ts1PAATest = TSUtils.paa(ts1Norm, PAAlength);
    Timeseries ts2PAATest = TSUtils.paa(ts2Norm, PAAlength);

    // get data for testing as arrays
    double[] ts1TrueValues = ts1PAA10.values();
    double[] ts1TestValues = ts1PAATest.values();

    assertEquals("PAA", ts1TrueValues.length, ts1TestValues.length);
    for (int i = 0; i < ts1TrueValues.length; i++) {
      assertEquals("PAA", ts1TrueValues[i], ts1TestValues[i], delta);
    }

    // get data for testing as arrays
    double[] ts2TrueValues = ts2PAA10.values();
    double[] ts2TestValues = ts2PAATest.values();

    assertEquals("PAA", ts2TrueValues.length, ts2TestValues.length);
    for (int i = 0; i < ts2TrueValues.length; i++) {
      assertEquals("PAA", ts2TrueValues[i], ts2TestValues[i], delta);
    }
  }

  /**
   * Test the normalize routine with NaN's.
   * 
   * @throws Exception if error occurs.
   */
  @Test
  public void testPAAWithNaNs() throws Exception {
    // get the time vector
    long[] time = new long[tsWithNaNs.length];
    for (int i = 0; i < tsWithNaNs.length; i++) {
      time[i] = Long.valueOf(i);
    }

    // do PAA without NaN's
    Timeseries paa5 = TSUtils.paa(new Timeseries(tsWithNaNs, time), 5);
    Timeseries paa10 = TSUtils.paa(new Timeseries(tsWithNaNs, time), 10);

    // do PAA with NaN's
    tsWithNaNs[0] = Double.NaN;
    tsWithNaNs[1] = Double.NaN;

    Timeseries paa5nan = TSUtils.paa(new Timeseries(tsWithNaNs, time), 5);
    assertFalse("Test proper NaN's handling", Double.isNaN(paa5nan.elementAt(0).value()));
    assertEquals("Test proper NaN's handling", 7.0, paa5nan.elementAt(0).value(), 0.0000001);
    assertEquals("Test proper NaN's handling", paa5.elementAt(1).value(), paa5nan.elementAt(1)
        .value(), 0.0001);

    Timeseries paa10nan = TSUtils.paa(new Timeseries(tsWithNaNs, time), 10);
    assertTrue("Test proper NaN's handling", Double.isNaN(paa10nan.elementAt(0).value()));
    assertEquals("Test proper NaN's handling", paa10.elementAt(2).value(), paa10nan.elementAt(2)
        .value(), 0.0001);
  }

  /**
   * Test the SAX conversion.
   * 
   * @throws TSException if error occurs.
   */
  @Test
  public void testNum2Char() throws TSException {
    // private static final double[] case2 = { 0 };
    assertEquals("test num2char", 'a', TSUtils.num2char(-0.5, normalA.getCuts(2)));
    assertEquals("test num2char", 'b', TSUtils.num2char(0.5, normalA.getCuts(2)));
    assertEquals("test num2char", 'b', TSUtils.num2char(0.0, normalA.getCuts(2)));
    double[] ts0 = { -0.5, 0.5, 0.0 };
    long[] t = { 1L, 2L, 3L };
    assertTrue("test num2char",
        "abb".equalsIgnoreCase(new String(TSUtils.ts2String(new Timeseries(ts0, t), normalA, 2))));
    // private static final double[] case7 = { -1.07, -0.57, -0.18, 0.18, 0.57, 1.07 };
    assertEquals("test num2char", 'd', TSUtils.num2char(-0.179, normalA.getCuts(7)));
    assertEquals("test num2char", 'd', TSUtils.num2char(-0.18, normalA.getCuts(7)));
    assertEquals("test num2char", 'c', TSUtils.num2char(-0.1801, normalA.getCuts(7)));
    double[] ts1 = { -0.179, -0.18, -0.1801 };
    assertTrue("test num2char",
        "ddc".equalsIgnoreCase(new String(TSUtils.ts2String(new Timeseries(ts1, t), normalA, 7))));

    // private static final double[] case2 = { -0.31629 };
    assertEquals("test num2char", 'a', TSUtils.num2char(-0.32, buildA.getCuts(2)));
    assertEquals("test num2char", 'b', TSUtils.num2char(-0.315, buildA.getCuts(2)));
    assertEquals("test num2char", 'b', TSUtils.num2char(0.0, buildA.getCuts(2)));
    double[] ts2 = { -0.32, -0.315, 0.0 };
    assertTrue("test num2char",
        "abb".equalsIgnoreCase(new String(TSUtils.ts2String(new Timeseries(ts2, t), buildA, 2))));
    // private static final double[] case7 = { -0.682417, -0.48938550000000003, -0.3782755,
    // -0.189843, 0.0532246, 0.67971 };
    assertEquals("test num2char", 'd', TSUtils.num2char(-0.375, buildA.getCuts(7)));
    assertEquals("test num2char", 'd', TSUtils.num2char(-0.1899, buildA.getCuts(7)));
    assertEquals("test num2char", 'c', TSUtils.num2char(-0.3787, buildA.getCuts(7)));
    double[] ts3 = { -0.375, -0.1899, -0.3787 };
    assertTrue("test num2char",
        "ddc".equalsIgnoreCase(new String(TSUtils.ts2String(new Timeseries(ts3, t), buildA, 7))));

    // private static final double[] case2 = { -0.098181 };
    assertEquals("test num2char", 'a', TSUtils.num2char(-0.099, universalA.getCuts(2)));
    assertEquals("test num2char", 'b', TSUtils.num2char(-0.0980, universalA.getCuts(2)));
    assertEquals("test num2char", 'b', TSUtils.num2char(0.0, universalA.getCuts(2)));
    double[] ts4 = { -0.099, -0.098, 0.0 };
    assertTrue("test num2char", "abb".equalsIgnoreCase(new String(TSUtils.ts2String(new Timeseries(
        ts4, t), universalA, 2))));
    // private static final double[] case7 = { -0.795985, -0.443235, -0.165274, 0.0, 0.399261,
    // 0.77940 };
    assertEquals("test num2char", 'd', TSUtils.num2char(-0.001, universalA.getCuts(7)));
    assertEquals("test num2char", 'd', TSUtils.num2char(-0.164, universalA.getCuts(7)));
    assertEquals("test num2char", 'c', TSUtils.num2char(-0.166, universalA.getCuts(7)));
    double[] ts5 = { -0.001, -0.164, -0.166 };
    assertTrue("test num2char", "ddc".equalsIgnoreCase(new String(TSUtils.ts2String(new Timeseries(
        ts5, t), universalA, 7))));

    double[] ts6 = { -0.001, Double.NEGATIVE_INFINITY, -0.166 };
    assertTrue("test num2char", "d_c".equalsIgnoreCase(new String(TSUtils.ts2StringWithNaN(
        new Timeseries(ts6, t), universalA, 7))));

  }

  /**
   * Test the SAX conversion.
   * 
   * @throws Exception if error occurs.
   */
  @Test
  public void testTS2Index() throws Exception {
    // read the PAA data
    ts1PAA10 = TSUtils.readTS(ts1PAAFile, PAAlength);

    int[] idx1 = TSUtils.ts2Index(ts1PAA10, normalA, 10);

    assertEquals("Testing ts2index", Integer.valueOf(idx1[1]), Integer.valueOf(2));
    assertEquals("Testing ts2index", Integer.valueOf(idx1[3]), Integer.valueOf(9));
    assertEquals("Testing ts2index", Integer.valueOf(idx1[7]), Integer.valueOf(4));
  }

  /**
   * Test the data conversion.
   * 
   * @throws TSException If error occurs.
   */
  @Test
  public void testTypeConversion() throws TSException {
    double[][] valsAsMatix = ts2.valuesAsMatrix();
    for (int i = 0; i < valsAsMatix.length; i++) {
      assertEquals(ts2.elementAt(i).value(), valsAsMatix[0][i], 0.01);
    }
  }

}
