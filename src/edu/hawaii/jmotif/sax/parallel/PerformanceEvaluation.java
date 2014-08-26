package edu.hawaii.jmotif.sax.parallel;

import edu.hawaii.jmotif.sax.NumerosityReductionStrategy;
import edu.hawaii.jmotif.sax.SAXFactory;
import edu.hawaii.jmotif.sax.datastructures.SAXFrequencyData;
import edu.hawaii.jmotif.timeseries.TSUtils;

/**
 * This runs the performance evaluation code - we are looking on the speedup.
 * 
 * @author psenin
 * 
 */
public class PerformanceEvaluation {

  private static final Integer NRUNS = 10;

  private static final Integer MIN_CPUS = 2;

  private static final Integer MAX_CPUS = 16;

  /**
   * Runs the evaluation.
   * 
   * @param args some accepted, see the code.
   * @throws Exception thrown if an error occured.
   */
  public static void main(String[] args) throws Exception {

    String dataFileName = args[0];
    Integer slidingWindowSize = Integer.valueOf(args[1]);
    Integer paaSize = Integer.valueOf(args[2]);
    Integer alphabetSize = Integer.valueOf(args[3]);

    double[] ts = TSUtils.readFileColumn(dataFileName, 0, 0);
    System.out.println("data file: " + dataFileName);
    System.out.println("data size: " + ts.length);
    System.out.println("SAX parameters: sliding window size " + slidingWindowSize + ", PAA size "
        + paaSize + ", alphabet size " + alphabetSize);

    System.out.println("Will be performing " + NRUNS
        + " SAX runs for each algorithm implementation ... ");

    // conventional
    //
    long tstamp1 = System.currentTimeMillis();
    for (int i = 0; i < NRUNS; i++) {
      @SuppressWarnings("unused")
      SAXFrequencyData sequentialRes2 = SAXFactory.data2sax(ts, slidingWindowSize, paaSize,
          alphabetSize);
    }
    long tstamp2 = System.currentTimeMillis();
    System.out
        .println("conversion with optimized PAA " + SAXFactory.timeToString(tstamp1, tstamp2));

    // parallel
    for (int threadsNum = MIN_CPUS; threadsNum < MAX_CPUS; threadsNum++) {
      tstamp1 = System.currentTimeMillis();
      for (int i = 0; i < NRUNS; i++) {
        ParallelSAXImplementation ps = new ParallelSAXImplementation();
        @SuppressWarnings("unused")
        SAXRecords parallelRes = ps.process(ts, threadsNum, slidingWindowSize, paaSize,
            alphabetSize, NumerosityReductionStrategy.EXACT, 0.005);
      }
      tstamp2 = System.currentTimeMillis();
      System.out.println("parallel conversion using " + threadsNum + " threads: "
          + SAXFactory.timeToString(tstamp1, tstamp2));
    }
  }

}
