package edu.hawaii.jmotif.experimentation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.hawaii.jmotif.gi.GrammarRuleRecord;
import edu.hawaii.jmotif.gi.GrammarRules;
import edu.hawaii.jmotif.gi.sequitur.SequiturFactory;
import edu.hawaii.jmotif.logic.Interval;
import edu.hawaii.jmotif.logic.RuleInterval;
import edu.hawaii.jmotif.sax.NumerosityReductionStrategy;
import edu.hawaii.jmotif.sax.SAXFactory;
import edu.hawaii.jmotif.sax.datastructures.DiscordRecords;
import edu.hawaii.jmotif.timeseries.TSUtils;
import edu.hawaii.jmotif.util.StackTrace;

public class ParamsSearchExperiment2 {

  // locale, charset, etc
  //
  final static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
  private static final Level LOGGING_LEVEL = Level.INFO;
  private static final String DATA_FILENAME = "data/ecg0606_1.csv";
  private static final String OUT_FILENAME = "RCode/ecg0606_res.txt";

  private static final double NORMALIZATION_THRESHOLD = 0.08;
  private static final NumerosityReductionStrategy NUMEROSITY_REDUCTION_STRATEGY = NumerosityReductionStrategy.EXACT;

  private static final int MIN_WINDOW_SIZE = 30;
  private static final int MAX_WINDOW_SIZE = 500;
  private static final int WINDOW_INCREMENT = 5;

  private static final int MIN_PAA_SIZE = 3;
  private static final int MAX_PAA_SIZE = 20;
  private static final int PAA_INCREMENT = 1;

  private static final int MIN_A_SIZE = 3;
  private static final int MAX_A_SIZE = 12;
  private static final int A_INCREMENT = 1;

  private static Logger consoleLogger;

  private static double[] ts;

  // static block - we instantiate the logger
  //
  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(ParamsSearchExperiment2.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  public static void main(String[] args) throws Exception {

    ts = loadData(DATA_FILENAME);

    BufferedWriter bw = new BufferedWriter(new FileWriter(new File(OUT_FILENAME)));
    bw.write("winSize, paaSize, aSize, approximationDistance, "
        + "minObservedCoverage, maxObservedCoverage, meanCoverage, numZeroRuns, maxZeroRunLength, "
        + "grammarSize, zeroCoverageLength, r1, r2, r3, r4, r5 \n");

    for (int winSize = MIN_WINDOW_SIZE; winSize < MAX_WINDOW_SIZE; winSize = winSize
        + WINDOW_INCREMENT) {
      for (int paaSize = MIN_PAA_SIZE; paaSize < MAX_PAA_SIZE; paaSize = paaSize + PAA_INCREMENT) {
        if (paaSize > winSize) {
          continue;
        }
        for (int aSize = MIN_A_SIZE; aSize < MAX_A_SIZE; aSize = aSize + A_INCREMENT) {

          // get the TS converted into the rule Intervals
          //
          GrammarRules rules = SequiturFactory.series2SequiturRules(ts, winSize, paaSize, aSize,
              NUMEROSITY_REDUCTION_STRATEGY, NORMALIZATION_THRESHOLD);

          // get the grammar size out
          // The size of a grammar is the sum of the sizes of its production rules, where the size
          // of a rule is one plus the length of its right-hand side.
          int grammarSize = 0;
          for (GrammarRuleRecord r : rules) {
            String rStr = r.getRuleString();
            int size = countSpaces(rStr) + 1;
            grammarSize = grammarSize + size;
          }

          // populate all intervals with their coverage
          //
          ArrayList<RuleInterval> intervals = new ArrayList<RuleInterval>();
          for (GrammarRuleRecord rule : rules) {
            if (rule.getRuleYield() > 2) {
              continue;
            }
            for (RuleInterval ri : rule.getRuleIntervals()) {
              ri.setCoverage(rule.getRuleIntervals().size());
              ri.setId(rule.ruleNumber());
              intervals.add(ri);
            }
          }

          // get the average coverage
          //
          int[] coverageArray = new int[ts.length];

          for (GrammarRuleRecord r : rules) {
            if (0 == r.ruleNumber()) {
              continue;
            }
            ArrayList<RuleInterval> arrPos = r.getRuleIntervals();
            for (RuleInterval saxPos : arrPos) {
              int start = saxPos.getStartPos();
              int end = saxPos.getEndPos();
              for (int j = start; j < end; j++) {
                coverageArray[j] = coverageArray[j] + 1;
              }
            }
          }

          // find the rule density value
          int zeroCoverageLength = 0;
          int maxObservedCoverage = 0;
          int minObservedCoverage = Integer.MAX_VALUE;
          for (int i = winSize; i < coverageArray.length - winSize; i++) {
            // update the min and max coverage values
            if (maxObservedCoverage < coverageArray[i]) {
              maxObservedCoverage = coverageArray[i];
            }
            if (minObservedCoverage > coverageArray[i]) {
              minObservedCoverage = coverageArray[i];
            }
            if (0 == coverageArray[i]) {
              zeroCoverageLength++;
            }
          }

          double meanCoverage = TSUtils.mean(coverageArray);

          // find the longest continous zero run
          //
          int maxZeroRunLength = 0;
          int numZeroRuns = 0;

          boolean isRun = false;
          int currentRun = 0;
          for (int i = winSize; i < coverageArray.length - winSize; i++) {
            if (0 == coverageArray[i]) {
              if (false == isRun) {
                isRun = true;
                numZeroRuns++;
              }
              currentRun++;
            }
            else if (true == isRun) {
              isRun = false;
              if (currentRun > maxZeroRunLength) {
                maxZeroRunLength = currentRun;
              }
              currentRun = 0;
            }
          }

          // find out if global minima hit anomaly
          //
          boolean anomalyHit = false;
          boolean outsideAnomalyHit = false;

          boolean anomalyZeroHit = false;
          boolean outsideAnomalyZeroHit = false;

          for (int i = winSize; i < coverageArray.length - winSize; i++) {
            if (minObservedCoverage == coverageArray[i]) {
              if ((480 - winSize) < i && i < 480) {
                anomalyHit = true;
              }
              else {
                outsideAnomalyHit = true;
              }
            }
            if (0 == coverageArray[i]) {
              if ((480 - winSize) < i && i < 480) {
                anomalyZeroHit = true;
              }
              else {
                outsideAnomalyZeroHit = true;
              }
            }
          }

          // get the approximation distance computed
          //
          double approximationDistance = SAXFactory.approximationDistance(ts, winSize, paaSize,
              aSize, NumerosityReductionStrategy.EXACT, NORMALIZATION_THRESHOLD);

          // *******************************
          // populate all intervals with their frequency
          //
          intervals = new ArrayList<RuleInterval>();
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
          coverageArray = new int[ts.length];
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
            intervals.addAll(getZeroIntervals(coverageArray));
          }

          // run HOTSAX with this intervals set
          //
          DiscordRecords discords = SAXFactory.series2RRAAnomalies(ts, 1, intervals);
          int discordPos = -1;

          Interval interval = null;
          Interval trueDiscordInterval = new Interval(480 - winSize / 2, 480 + winSize / 2, 0);
          if (discords.getSize() > 0) {
            discordPos = discords.get(0).getPosition();
            interval = new Interval(discordPos, discordPos + discords.get(0).getLength(), 1);
          }

          boolean discordHit = false;
          if (null != interval && trueDiscordInterval.overlaps(interval)) {
            discordHit = true;

          }

          // Output
          //
          consoleLogger.info(winSize + ", " + paaSize + ", " + aSize + ", " + anomalyHit + ", "
              + discordHit);
          bw.write(winSize + ", " + paaSize + ", " + aSize + ", " + approximationDistance + ", "
              + minObservedCoverage + ", " + maxObservedCoverage + ", " + meanCoverage + ", "
              + numZeroRuns + ", " + maxZeroRunLength + ", " + grammarSize + ", "
              + zeroCoverageLength + ", " + anomalyHit + ", " + outsideAnomalyHit + ", "
              + anomalyZeroHit + ", " + outsideAnomalyZeroHit + ", " + discordHit + "\n");

        }
      }
    }

    bw.close();

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

  /**
   * Counts spaces in the string.
   * 
   * @param str the string to process.
   * @return number of spaces found.
   */
  @SuppressWarnings("unused")
  private static int countSpaces(String str) {
    if (null == str) {
      return -1;
    }
    int counter = 0;
    for (int i = 0; i < str.length(); i++) {
      if (str.charAt(i) == ' ') {
        counter++;
      }
    }
    return counter;
  }

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

}
