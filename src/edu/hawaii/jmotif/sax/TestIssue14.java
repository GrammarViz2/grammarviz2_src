package edu.hawaii.jmotif.sax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;
import edu.hawaii.jmotif.sax.alphabet.NormalAlphabet;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.timeseries.TSUtils;

/**
 * Test for the issue #14.
 * 
 * @author psenin
 * 
 */
public class TestIssue14 {

  // the raw data
  //
  private static final double[] series1 = { -0.5, -1.0, -0.538, 1.0, -0.083 };
  private static final double[] series2 = { -0.2, -0.6, -1.0, 1, 0.2 };

  private static final char[] testStr1 = { 'a', 'a', 'a', 'a', 'a' };
  private static final char[] testStr2 = { 'a', 'b', 'c', 'd', 'e' };

  @Test
  public void testDistance() {

    try {

      NormalAlphabet alphabet = new NormalAlphabet();

      char[] str1 = TSUtils.ts2String(series1, alphabet.getCuts(6));

      char[] str2 = TSUtils.ts2String(series2, alphabet.getCuts(6));

      assertEquals("Testing the Str distance", 0, SAXFactory.strSAXMinDistance(str1, str2));
      assertEquals("Testing the Str distance", 4, SAXFactory.strDistance(str1, str2));

      assertEquals("Testing the Str distance", 9, SAXFactory.strSAXMinDistance(testStr1, testStr2));
      assertEquals("Testing the Str distance", 10, SAXFactory.strDistance(testStr1, testStr2));

    }
    catch (TSException e) {
      fail("Exception shall not be thrown!");
    }

  }

}
