package edu.hawaii.jmotif.gi;

import edu.hawaii.jmotif.gi.sequitur.SAXSymbol;
import edu.hawaii.jmotif.gi.sequitur.SequiturFactory;
import edu.hawaii.jmotif.sax.NumerosityReductionStrategy;
import edu.hawaii.jmotif.timeseries.TSUtils;

public class MemoryLeakTester {

  private static final String INPUT_FNAME = "data/300_signal1.txt";

  private static final int SAX_WIN_SIZE = 100;
  private static final int SAX_PAA_SIZE = 6;
  private static final int SAX_A_SIZE = 5;

  private static final double SAX_NORM_THRESHOLD = 0.001;

  public static void main(String[] args) throws Exception {

    try {
      Thread.sleep(10000); // 1000 milliseconds is one second.
    }
    catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }

    double[] ts = TSUtils.readFileColumn(INPUT_FNAME, 0, 0);
    System.out.println("Read " + ts.length + " points from " + INPUT_FNAME);

    try {
      Thread.sleep(10000); // 1000 milliseconds is one second.
    }
    catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }

    for (int i = 0; i < 20; i++) {

      System.out.println("Iteration " + i);
      System.gc();
      GrammarRules g = SequiturFactory.series2SequiturRules(ts, SAX_WIN_SIZE, SAX_PAA_SIZE,
          SAX_A_SIZE, NumerosityReductionStrategy.EXACT, SAX_NORM_THRESHOLD);
      System.out.println("Inferred " + g.size() + " rules.");

      try {
        Thread.sleep(10000); // 1000 milliseconds is one second.
      }
      catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }

    }

  }

}
