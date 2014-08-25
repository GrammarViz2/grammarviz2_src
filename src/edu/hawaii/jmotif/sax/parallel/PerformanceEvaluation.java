package edu.hawaii.jmotif.sax.parallel;

import java.io.IOException;
import edu.hawaii.jmotif.sax.NumerosityReductionStrategy;
import edu.hawaii.jmotif.sax.SAXFactory;
import edu.hawaii.jmotif.sax.alphabet.NormalAlphabet;
import edu.hawaii.jmotif.sax.datastructures.SAXFrequencyData;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.timeseries.TSUtils;
import edu.hawaii.jmotif.timeseries.Timeseries;

public class PerformanceEvaluation {

  private static final Integer NRUNS = 10;

  public static void main(String[] args) throws TSException, CloneNotSupportedException,
      NumberFormatException, IOException {

    String dataFileName = args[0];
    Integer slidingWindowSize = Integer.valueOf(args[1]);
    Integer paaSize = Integer.valueOf(args[2]);
    Integer alphabetSize = Integer.valueOf(args[3]);

    double[] ts = TSUtils.readFileColumn(dataFileName, 0, 0);
    System.out.println("data file: " + dataFileName);
    System.out.println("data size: " + ts.length);
    System.out.println("SAX parameters: sliding window size " + slidingWindowSize + ", PAA size "
        + paaSize + ", alphabet size " + alphabetSize);

    System.out.println("Performing " + NRUNS + " SAX runs for each algorithm implementation ... ");

    // conventional
    //
    SAXFrequencyData sequentialRes = null;
    long tstamp1 = System.currentTimeMillis();
    for (int i = 0; i < NRUNS; i++) {
      sequentialRes = SAXFactory.ts2saxZNorm(new Timeseries(ts), slidingWindowSize, paaSize,
          new NormalAlphabet(), alphabetSize);
    }
    String str = sequentialRes.getSAXString(" ");
    long tstamp2 = System.currentTimeMillis();
    System.out.println("conventional conversion " + SAXFactory.timeToString(tstamp1, tstamp2));

    // conventional 2
    //
    SAXFrequencyData sequentialRes2 = null;
    tstamp1 = System.currentTimeMillis();
    for (int i = 0; i < NRUNS; i++) {
      sequentialRes2 = SAXFactory.data2sax(ts, slidingWindowSize, paaSize, alphabetSize);
    }
    tstamp2 = System.currentTimeMillis();
    str = sequentialRes2.getSAXString(" ");
    System.out
        .println("conversion with optimized PAA " + SAXFactory.timeToString(tstamp1, tstamp2));

    SAXRecords parallelRes = null;
    for (int threadsNum = 2; threadsNum < 11; threadsNum++) {
      tstamp1 = System.currentTimeMillis();
      for (int i = 0; i < NRUNS; i++) {
        ParallelSAXImplementation ps = new ParallelSAXImplementation();
        parallelRes = ps.process(ts, threadsNum, slidingWindowSize, paaSize, alphabetSize,
            NumerosityReductionStrategy.EXACT, 0.5);
      }
      tstamp2 = System.currentTimeMillis();
      System.out.println("parallel conversion using " + threadsNum + " threads: "
          + SAXFactory.timeToString(tstamp1, tstamp2));
    }
  }

}
