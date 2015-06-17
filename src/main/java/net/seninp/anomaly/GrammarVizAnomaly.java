package net.seninp.anomaly;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import net.seninp.gi.GrammarRuleRecord;
import net.seninp.gi.GrammarRules;
import net.seninp.gi.RuleInterval;
import net.seninp.gi.sequitur.SequiturFactory;
import net.seninp.jmotif.distance.EuclideanDistance;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.discord.DiscordRecords;
import net.seninp.util.StackTrace;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.hawaii.jmotif.grammarviz.logic.CoverageCountStrategy;

/**
 * Main executable wrapping all the discord discovery methods.
 * 
 * @author psenin
 * 
 */
public class GrammarVizAnomaly {

  // locale, charset, etc
  //
  final static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
  private static final String CR = "\n";

  // logging stuff
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.INFO;

  // the global timeseries variable
  //
  private static double[] ts;

  // the parameters - also global
  //
  private static String argsString;

  private static String dataFName;

  private static Integer algorithm;

  private static Integer windowSize;
  private static Integer paaSize;
  private static Integer alphabetSize;
  private static Integer discordsToReport;
  private static boolean outputRRAresults;

  // workers
  //
  private static TSProcessor tp = new TSProcessor();
  private static SAXProcessor sp = new SAXProcessor();

  // static block - we instantiate the logger
  //
  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(GrammarVizAnomaly.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  /**
   * The main executable.
   * 
   * @param args The command-line params.
   * @throws Exception If error occurs.
   */
  public static void main(String[] args) throws Exception {

    // System.in.read(); // this is used for proper performance evaluation using visual jvm
    argsString = Arrays.toString(args);

    consoleLogger.info("Parameters string: \"" + Arrays.toString(args) + "\"");

    // parameters check section
    //
    if (args.length != 6) {
      //
      // four arguments are used for discords using brute force
      //
      if (args.length == 4) {
        try {
          algorithm = Integer.valueOf(args[0]);
          dataFName = args[1];
          ts = TSProcessor.readFileColumn(dataFName, 0, 0);
          windowSize = Integer.valueOf(args[2]);
          consoleLogger.info("Starting brute force search for " + discordsToReport
              + " discords with settings: algorithm " + algorithm + ", data \"" + dataFName + "\""
              + ", window " + windowSize);
        }
        catch (Exception e) {
          System.err.println("error occured while parsing parameters:\n" + StackTrace.toString(e));
          System.exit(-1);
        }
      }
      //
      // this goes to RRA - 6/7 parameters
      //
      else if ((args.length == 7 && 3 == Integer.valueOf(args[0]))
          || (args.length == 6 && 3 == Integer.valueOf(args[0]))) {
        try {
          // the parsing will work anyway
          setParameters(args);
          // check the true/false parameter
          outputRRAresults = false;
          if (args.length == 7) {
            String yesNoParam = args[6];
            if (yesNoParam.equalsIgnoreCase("true") || yesNoParam.equalsIgnoreCase("t")
                || yesNoParam.equalsIgnoreCase("yes") || yesNoParam.equalsIgnoreCase("1")) {
              outputRRAresults = true;
            }
          }
        }
        catch (Exception e) {
          System.err.println("error occured while parsing parameters:\n" + StackTrace.toString(e));
          System.exit(-1);
        }
      }
      else {
        System.err.println(getHelp());
        System.exit(-1);
      }
    }
    else {
      setParameters(args);
    }

    if (1 == algorithm) {
      findBruteForce();
    }
    else if (2 == algorithm) {
      findHotSax();
    }
    else if (3 == algorithm) {
      findRRA(null);
    }
    else if (4 == algorithm) {
      findHotSaxWithHash();
    }

  }

  /**
   * This set parameters globally.
   * 
   * @param args
   */
  private static void setParameters(String[] args) {
    try {

      consoleLogger.info("Parsing param string \"" + Arrays.toString(args) + "\"");

      algorithm = Integer.valueOf(args[0]);

      dataFName = args[1];
      ts = TSProcessor.readFileColumn(dataFName, 0, 0);

      windowSize = Integer.valueOf(args[2]);
      paaSize = Integer.valueOf(args[3]);
      alphabetSize = Integer.valueOf(args[4]);

      discordsToReport = Integer.valueOf(args[5]);

      consoleLogger.info("Starting discords search with settings: algorithm " + algorithm
          + ", data \"" + dataFName + "\"" + ", window " + windowSize + ", PAA " + paaSize
          + ", alphabet " + alphabetSize + ", reporting " + discordsToReport + " discords.");

    }
    catch (Exception e) {
      System.err.println("error occured while parsing parameters:\n" + StackTrace.toString(e));
      System.exit(-1);
    }

  }

  private static void findRRA(CoverageCountStrategy strategy) throws Exception {

    consoleLogger.info("running RRA algorithm...");
    Date start = new Date();

    GrammarRules rules = SequiturFactory.series2SequiturRules(ts, windowSize, paaSize,
        alphabetSize, NumerosityReductionStrategy.EXACT, 0.01D);

    ArrayList<RuleInterval> intervals = new ArrayList<RuleInterval>();

    // populate all intervals with their frequency
    //
    for (GrammarRuleRecord rule : rules) {
      //
      // TODO: do we care about long rules?
      //
      // if (0 == rule.ruleNumber() || rule.getRuleYield() > 2) {
      if (0 == rule.ruleNumber()) {
        continue;
      }
      for (RuleInterval ri : rule.getRuleIntervals()) {
        ri.setCoverage(rule.getRuleIntervals().size());
        ri.setId(rule.ruleNumber());
        intervals.add(ri);
      }
    }

    // get the coverage array
    //
    int[] coverageArray = new int[ts.length];
    for (GrammarRuleRecord rule : rules) {
      if (0 == rule.ruleNumber()) {
        continue;
      }
      ArrayList<RuleInterval> arrPos = rule.getRuleIntervals();
      for (RuleInterval saxPos : arrPos) {
        int startPos = saxPos.getStartPos();
        int endPos = saxPos.getEndPos();
        for (int j = startPos; j < endPos; j++) {
          coverageArray[j] = coverageArray[j] + 1;
        }
      }
    }

    // look for zero-covered intervals and add those to the list
    //
    List<RuleInterval> zeros = getZeroIntervals(coverageArray);
    if (zeros.size() > 0) {
      consoleLogger.info("found " + zeros.size() + " intervals not covered by rules: "
          + intervalsToString(zeros));
      intervals.addAll(getZeroIntervals(coverageArray));
    }
    else {
      consoleLogger.info("Whole timeseries covered by rule intervals ...");
    }

    // run HOTSAX with this intervals set
    //
    DiscordRecords discords = SAXFactory.series2RRAAnomalies(ts, discordsToReport, intervals);
    Date end = new Date();
    System.out.println("params: " + argsString);
    System.out.println(discords.toString());
    System.out.println("Discords found in "
        + SAXProcessor.timeToString(start.getTime(), end.getTime()));

    // THE DISCORD SEARCH IS DONE RIGHT HERE
    // BELOW IS THE CODE WHICH WRITES THE CURVE AND THE DISTANCE FILE ON FILESYSTEM
    //
    if (!outputRRAresults) {
      System.exit(10);
    }

    // write the coverage array
    //
    String currentPath = new File(".").getCanonicalPath();
    BufferedWriter bw = new BufferedWriter(new FileWriter(new File(currentPath + File.separator
        + "coverage.txt")));
    for (int i : coverageArray) {
      bw.write(i + "\n");
    }
    bw.close();

    Collections.sort(intervals, new Comparator<RuleInterval>() {
      public int compare(RuleInterval c1, RuleInterval c2) {
        if (c1.getStartPos() > c2.getStartPos()) {
          return 1;
        }
        else if (c1.getStartPos() < c2.getStartPos()) {
          return -1;
        }
        return 0;
      }
    });

    // now lets find all the distances to non-self match
    //
    double[] distances = new double[ts.length];
    double[] widths = new double[ts.length];

    for (RuleInterval ri : intervals) {

      int ruleStart = ri.getStartPos();
      int ruleEnd = ruleStart + ri.getLength();
      int window = ruleEnd - ruleStart;

      double[] cw = tp.subseriesByCopy(ts, ruleStart, ruleStart + window);

      double cwNNDist = Double.MAX_VALUE;

      // this effectively finds the furthest hit
      //
      for (int j = 0; j < ts.length - window - 1; j++) {
        if (Math.abs(ruleStart - j) > window) {
          double[] currentSubsequence = tp.subseriesByCopy(ts, j, j + window);
          double dist = EuclideanDistance.distance(cw, currentSubsequence);
          if (dist < cwNNDist) {
            cwNNDist = dist;
          }
        }
      }

      distances[ruleStart] = cwNNDist;
      widths[ruleStart] = ri.getLength();
    }

    bw = new BufferedWriter(
        new FileWriter(new File(currentPath + File.separator + "distances.txt")));
    for (int i = 0; i < distances.length; i++) {
      bw.write(i + "," + distances[i] + "," + widths[i] + "\n");
    }
    bw.close();

  }

  /**
   * Run a quick scan along the timeseries coverage to find a zeroed intervals.
   * 
   * @param coverageArray the coverage to analyze.
   * @return set of zeroed intervals (if found).
   */
  private static List<RuleInterval> getZeroIntervals(int[] coverageArray) {
    ArrayList<RuleInterval> res = new ArrayList<RuleInterval>();
    int start = -1;
    boolean inInterval = false;
    int intervalsCounter = -1;
    for (int i = 0; i < coverageArray.length; i++) {
      if (0 == coverageArray[i] && !inInterval) {
        start = i;
        inInterval = true;
      }
      if (coverageArray[i] > 0 && inInterval) {
        res.add(new RuleInterval(intervalsCounter, start, i, 0));
        inInterval = false;
        intervalsCounter--;
      }
    }
    return res;
  }

  /**
   * Makes a zeroed interval to appear nicely in output.
   * 
   * @param zeros the list of zeros.
   * @return the intervals list as a string.
   */
  private static String intervalsToString(List<RuleInterval> zeros) {
    StringBuilder sb = new StringBuilder();
    for (RuleInterval i : zeros) {
      sb.append(i.toString()).append(",");
    }
    return sb.toString();
  }

  /**
   * Finds discords in classic manner (i.e., using a trie).
   * 
   * @throws TrieException if error occurs.
   * @throws Exception if error occurs.
   */
  private static void findHotSax() throws TrieException, Exception {
    consoleLogger.info("running HOT SAX Trie-based algorithm...");

    Date start = new Date();
    DiscordRecords discords = SAXFactory.series2Discords(ts, windowSize, alphabetSize,
        discordsToReport, new LargeWindowAlgorithm());
    Date end = new Date();

    System.out.println(discords.toString());
    System.out.println("Discords found in "
        + SAXFactory.timeToString(start.getTime(), end.getTime()));

  }

  /**
   * Finds discords using a hash-backed magic array.
   * 
   * @throws Exception if error occurs.
   */
  private static void findHotSaxWithHash() throws TrieException, Exception {
    consoleLogger.info("running HOT SAX Hash-based algorithm...");

    Date start = new Date();
    DiscordRecords discords = SAXFactory.series2DiscordsWithHash(ts, windowSize, paaSize,
        alphabetSize, discordsToReport, new LargeWindowAlgorithm());
    Date end = new Date();

    System.out.println(discords.toString());
    System.out.println("Discords found in "
        + SAXFactory.timeToString(start.getTime(), end.getTime()));

  }

  /**
   * Procedure of finding brute-force discords.
   * 
   * @throws Exception
   */
  private static void findBruteForce() throws Exception {

    consoleLogger.info("running brute force algorithm...");

    Date start = new Date();
    DiscordRecords discords = TSUtils.series2BruteForceDiscords(ts, windowSize, discordsToReport,
        new LargeWindowAlgorithm());
    Date end = new Date();

    System.out.println(discords.toString());

    System.out.println(discords.getSize() + " discords found in "
        + SAXFactory.timeToString(start.getTime(), end.getTime()));
  }

  /**
   * Prints the command-line help.
   * 
   * @return The help string.
   */
  private static String getHelp() {

    StringBuffer sb = new StringBuffer();

    sb.append("GrammarViz2 v. 1.0 release, contact: seninp@gmail.com").append(CR);

    sb.append("Expected parameters: ").append(CR);

    sb.append(" [1] algorithm to use: 1 - brute force, 2 - HOT SAX, backed by a Trie").append(CR);
    sb.append("                       3 - RRA algorithm, 4 - HOT SAX backed by a Hash").append(CR);
    sb.append(
        "     *** for algorithm 2, PAA size will be equal to Alphabet size due to the *trie* design")
        .append(CR);
    sb.append("         use algorithm 4 so PAA and Alphabet sizes may differ").append(CR);
    sb.append("     *** for brute force only sliding window parameter is expected ").append(CR);

    sb.append(" [2] dataset input file; ").append(CR);

    sb.append(" [3] window size; ").append(CR);

    sb.append(" [4] paa size; ").append(CR);

    sb.append(" [5] alphabet size; ").append(CR);

    sb.append(" [6] discords number to report; ").append(CR);

    sb.append(" [7] indicate true/false for RRA algorithm auxiliary output; ").append(CR);

    return sb.toString();
  }

}
