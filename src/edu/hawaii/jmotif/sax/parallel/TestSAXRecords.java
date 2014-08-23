package edu.hawaii.jmotif.sax.parallel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import edu.hawaii.jmotif.sax.NumerosityReductionStrategy;
import edu.hawaii.jmotif.timeseries.TSUtils;

public class TestSAXRecords {

  private static final String filenameTEK14 = "data/TEK/TEK14.txt";

  private double[] ts1;

  @Before
  public void setUp() throws Exception {
  }

  /**
   * Test the simple SAX conversion.
   * 
   * @throws Exception if error occurs.
   */
  @Test
  public void testProperIndexing() throws Exception {

    ts1 = TSUtils.readFileColumn(filenameTEK14, 0, 0);

    ParallelSAXImplementation ps = new ParallelSAXImplementation();
    SAXRecords parallelRes = ps.process(ts1, 2, 400, 6, 3, NumerosityReductionStrategy.EXACT, 0.01);

    String str = parallelRes.getSAXString(" ");

    parallelRes.buildIndex();

    String str1 = new String();

    for (int i = 11; i < 47; i++) {
      SaxRecord r = parallelRes.getByIndex(parallelRes.mapStringIndexToTSPosition(i));
      str1 = str1.concat(String.valueOf(r.getPayload()) + " ");
    }

    assertTrue("Asserting substring existence", str.indexOf(str1) > 0);

    assertEquals("Asserting substring the index", 11 * 6 + 11 * 1, str.indexOf(str1));

  }

}
