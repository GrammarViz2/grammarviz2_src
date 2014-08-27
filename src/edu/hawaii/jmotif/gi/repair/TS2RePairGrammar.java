package edu.hawaii.jmotif.gi.repair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map.Entry;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.hawaii.jmotif.logic.RuleInterval;
import edu.hawaii.jmotif.sax.NumerosityReductionStrategy;
import edu.hawaii.jmotif.sax.SAXFactory;
import edu.hawaii.jmotif.sax.datastructures.SAXRecords;
import edu.hawaii.jmotif.sax.parallel.ParallelSAXImplementation;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.util.StackTrace;

public class TS2RePairGrammar {

  private final static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  /** The data filename. */
  private static String dataFileName;

  private static double[] originalTimeSeries;

  private static Integer saxWindowSize;

  private static Integer saxPaaSize;

  private static Integer saxAlphabetSize;

  private static String outputPrefix;

  // the logger business
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.INFO;

  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(TS2RePairGrammar.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  public static void main(String[] args) throws TSException {

    // System.in.read(); // this is used for proper performance evaluation using visual jvm

    if (args.length == 5) {
      try {
        consoleLogger.info("Parsing param string \"" + Arrays.toString(args) + "\"");

        dataFileName = args[0];
        originalTimeSeries = loadData(dataFileName);

        saxWindowSize = Integer.valueOf(args[1]);
        saxPaaSize = Integer.valueOf(args[2]);
        saxAlphabetSize = Integer.valueOf(args[3]);

        outputPrefix = args[4];

        consoleLogger.info("Starting conversion " + dataFileName + " with settings: window "
            + saxWindowSize + ", paa " + saxPaaSize + ", alphabet " + saxAlphabetSize
            + ", out prefix " + outputPrefix);

      }
      catch (Exception e) {
        System.err.println("error occured while parsing parameters:\n" + StackTrace.toString(e));
        System.exit(-1);
      }
      // end parsing brute-force parameters
      //
    }
    else {
      System.err.println("expected 5 parameters");
    }

    Date fullStart = new Date();
    ParallelSAXImplementation ps = new ParallelSAXImplementation();
    SAXRecords parallelRes = ps.process(originalTimeSeries, 2, saxWindowSize, saxPaaSize,
        saxAlphabetSize, NumerosityReductionStrategy.EXACT, 0.5);
    Date end = new Date();

    System.out.println("Discretized timeseries using SAXRecords into string in "
        + String.valueOf(end.getTime() - fullStart.getTime()) + " ms, "
        + SAXFactory.timeToString(fullStart.getTime(), end.getTime()));

    Date start = new Date();
    RePairRule rePairGrammar = RePairFactory.buildGrammar(parallelRes);
    end = new Date();

    System.out.println("Inferred grammar with RE-PAIR in  "
        + String.valueOf(end.getTime() - start.getTime()) + " ms, "
        + SAXFactory.timeToString(start.getTime(), end.getTime()));

    start = new Date();
    RePairRule.expandRules();
    RePairRule.buildIntervals(parallelRes, originalTimeSeries, saxWindowSize);
    end = new Date();

    System.out.println("Expanded rules and computed intervals  in  "
        + String.valueOf(end.getTime() - start.getTime()) + " ms, "
        + SAXFactory.timeToString(start.getTime(), end.getTime()));

    start = new Date();
    int[] coverageArray = new int[originalTimeSeries.length];
    for (Entry<Integer, RePairRule> e : rePairGrammar.getRules().entrySet()) {
      RePairRule r = e.getValue();
      for (RuleInterval ri : r.getRuleIntervals()) {
        for (int k = ri.getStartPos(); k < ri.getEndPos(); k++) {
          coverageArray[k] = coverageArray[k] + 1;
        }
      }
    }
    end = new Date();
    System.out.println("Computed rule coverage in  "
        + String.valueOf(end.getTime() - start.getTime()) + " ms, "
        + SAXFactory.timeToString(start.getTime(), end.getTime()));

    System.out.println("Total runtime  " + String.valueOf(end.getTime() - fullStart.getTime())
        + " ms, " + SAXFactory.timeToString(fullStart.getTime(), end.getTime()));

    // write down the coverage array
    //
    try {
      String currentPath = new File(".").getCanonicalPath();
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
          currentPath + File.separator + outputPrefix + "_REPAIR_density_curve.txt"), "UTF-8"));
      for (int c : coverageArray) {
        bw.write(String.valueOf(c) + "\n");
      }
      bw.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }

  }

  /**
   * This reads the data
   * 
   * @param fname The filename.
   * @return
   */
  private static double[] loadData(String fname) {

    consoleLogger.info("reading from " + fname);

    long lineCounter = 0;
    double ts[] = new double[1];

    Path path = Paths.get(fname);

    ArrayList<Double> data = new ArrayList<Double>();

    try {

      BufferedReader reader = Files.newBufferedReader(path, DEFAULT_CHARSET);

      String line = null;
      while ((line = reader.readLine()) != null) {
        String[] lineSplit = line.trim().split("\\s+");
        for (int i = 0; i < lineSplit.length; i++) {
          double value = new BigDecimal(lineSplit[i]).doubleValue();
          data.add(value);
        }
        lineCounter++;
      }
      reader.close();
    }
    catch (Exception e) {
      System.err.println(StackTrace.toString(e));
    }
    finally {
      assert true;
    }

    if (!(data.isEmpty())) {
      ts = new double[data.size()];
      for (int i = 0; i < data.size(); i++) {
        ts[i] = data.get(i);
      }
    }

    consoleLogger.info("loaded " + data.size() + " points from " + lineCounter + " lines in "
        + fname);
    return ts;

  }

}
