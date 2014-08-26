package edu.hawaii.jmotif.gi.repair;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.junit.Test;
import edu.hawaii.jmotif.sax.NumerosityReductionStrategy;
import edu.hawaii.jmotif.sax.SAXFactory;
import edu.hawaii.jmotif.sax.parallel.ParallelSAXImplementation;
import edu.hawaii.jmotif.sax.parallel.SAXRecords;
import edu.hawaii.jmotif.timeseries.TSUtils;

public class TestRePairImplementation {

  // private static final String filenameTEK14 =
  // "/media/Stock/SDM15/mitdbx_mitdbx_108/mitdbx_mitdbx_108_trace1.csv";
  private static final String filenameTEK14 = "/media/Stock/SDM15/300/300_signal1.txt";

  private static final Integer WINDOW_SIZE = 2220;
  private static final Integer PAA_SIZE = 10;
  private static final Integer ALPHABET_SIZE = 4;

  private double[] ts1;

  /**
   * Test the simple SAX conversion.
   * 
   * @throws Exception if error occurs.
   */
  @Test
  public void testRePairImplementation() throws Exception {

    ts1 = TSUtils.readFileColumn(filenameTEK14, 0, 0);

    ParallelSAXImplementation ps = new ParallelSAXImplementation();
    SAXRecords saxData = ps.process(ts1, 3, WINDOW_SIZE, PAA_SIZE, ALPHABET_SIZE,
        NumerosityReductionStrategy.EXACT, 0.05);

    String inputString = saxData.getSAXString(" ");
    // System.out.println("Input string:\n" + inputString);
    saxData.buildIndex();

    Date start = new Date();

    RePairRule grammar = RePairFactory.buildGrammar(saxData);
    Date grammarEnd = new Date();

    RePairRule.expandRules();
    Date expandEnd = new Date();

    String recoveredString = RePairRule.recoverString();

    // System.out.println("RePair grammar:\n" + RePairRule.toGrammarRules());

    // System.out.println("Recovered string:\n" + recoveredString);

    System.out.println("Grammar built in  "
        + SAXFactory.timeToString(start.getTime(), grammarEnd.getTime()));

    System.out.println("Rules exanded in "
        + SAXFactory.timeToString(grammarEnd.getTime(), expandEnd.getTime()));
    
    assertNotNull(grammar);
    assertTrue(inputString.trim().equalsIgnoreCase(recoveredString.trim()));


  }

}
