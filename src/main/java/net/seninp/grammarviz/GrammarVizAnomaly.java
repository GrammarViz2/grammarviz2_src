package net.seninp.grammarviz;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.jfree.util.Log;
import org.slf4j.LoggerFactory;
import com.beust.jcommander.JCommander;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import net.seninp.gi.GIAlgorithm;
import net.seninp.gi.GrammarRuleRecord;
import net.seninp.gi.GrammarRules;
import net.seninp.gi.RuleInterval;
import net.seninp.gi.repair.RePairFactory;
import net.seninp.gi.repair.RePairGrammar;
import net.seninp.gi.rulepruner.ReductionSorter;
import net.seninp.gi.rulepruner.RulePruner;
import net.seninp.gi.rulepruner.RulePrunerFactory;
import net.seninp.gi.rulepruner.RulePrunerParameters;
import net.seninp.gi.rulepruner.SampledPoint;
import net.seninp.gi.sequitur.SequiturFactory;
import net.seninp.grammarviz.anomaly.AnomalyAlgorithm;
import net.seninp.grammarviz.anomaly.RRAImplementation;
import net.seninp.jmotif.distance.EuclideanDistance;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.datastructures.SAXRecords;
import net.seninp.jmotif.sax.discord.BruteForceDiscordImplementation;
import net.seninp.jmotif.sax.discord.DiscordRecords;
import net.seninp.jmotif.sax.discord.HOTSAXImplementation;
import net.seninp.jmotif.sax.parallel.ParallelSAXImplementation;
import net.seninp.jmotif.sax.registry.LargeWindowAlgorithm;
import net.seninp.jmotif.sax.trie.TrieException;

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

  // workers
  //
  private static TSProcessor tp = new TSProcessor();
  private static EuclideanDistance ed = new EuclideanDistance();

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

    GrammarVizAnomalyParameters params = new GrammarVizAnomalyParameters();
    JCommander jct = new JCommander(params, args);

    if (0 == args.length) {
      jct.usage();
    }
    else {
      // get params printed
      //
      StringBuffer sb = new StringBuffer(1024);
      sb.append("GrammarViz2 CLI anomaly discovery").append(CR);
      sb.append("parameters:").append(CR);

      sb.append(" input file:                  ").append(GrammarVizAnomalyParameters.IN_FILE).append(CR);
      sb.append(" output files prefix:         ").append(GrammarVizAnomalyParameters.OUT_FILE).append(CR);
      
      sb.append(" Algorithm implementation:    ").append(GrammarVizAnomalyParameters.ALGORITHM).append(CR);
      sb.append(" Num. of discords to report:  ").append(GrammarVizAnomalyParameters.DISCORDS_NUM).append(CR); 
      
      if (!(AnomalyAlgorithm.RRASAMPLED.equals(GrammarVizAnomalyParameters.ALGORITHM))){
        sb.append(" SAX sliding window size:     ").append(GrammarVizAnomalyParameters.SAX_WINDOW_SIZE).append(CR);
      }
      
      if (!(AnomalyAlgorithm.BRUTEFORCE.equals(GrammarVizAnomalyParameters.ALGORITHM))) {
        if (!(AnomalyAlgorithm.RRASAMPLED.equals(GrammarVizAnomalyParameters.ALGORITHM))) {
          if (AnomalyAlgorithm.HOTSAXTRIE.equals(GrammarVizAnomalyParameters.ALGORITHM)) {
            GrammarVizAnomalyParameters.SAX_PAA_SIZE = GrammarVizAnomalyParameters.SAX_ALPHABET_SIZE;
          }
          sb.append(" SAX PAA size:                ").append(GrammarVizAnomalyParameters.SAX_PAA_SIZE).append(CR);
          sb.append(" SAX alphabet size:           ").append(GrammarVizAnomalyParameters.SAX_ALPHABET_SIZE).append(CR);
        }
        sb.append(" SAX numerosity reduction:    ").append(GrammarVizAnomalyParameters.SAX_NR_STRATEGY).append(CR);
        sb.append(" SAX normalization threshold: ").append(GrammarVizAnomalyParameters.SAX_NORM_THRESHOLD).append(CR);
      }

      if (AnomalyAlgorithm.RRASAMPLED.equals(GrammarVizAnomalyParameters.ALGORITHM) || 
          AnomalyAlgorithm.RRA.equals(GrammarVizAnomalyParameters.ALGORITHM)) {
        sb.append(" GI Algorithm:                ").append(GrammarVizAnomalyParameters.GI_ALGORITHM_IMPLEMENTATION).append(CR);
      }
      
      if (AnomalyAlgorithm.RRASAMPLED.equals(GrammarVizAnomalyParameters.ALGORITHM)) {
        sb.append(" Grid boundaries:             ").append(RulePrunerParameters.GRID_BOUNDARIES).append(CR);
      }
      
      if (AnomalyAlgorithm.RRASAMPLED.equals(GrammarVizAnomalyParameters.ALGORITHM) && 
          !(Double.isNaN(RulePrunerParameters.SUBSAMPLING_FRACTION))) {
          sb.append("  Subsampling fraction:     ").append(RulePrunerParameters.SUBSAMPLING_FRACTION).append(CR);
      }

      sb.append(CR);
      System.out.println(sb.toString());

      // read the file
      //
      consoleLogger.info("Reading data ...");
      double[] series = tp.readTS(GrammarVizAnomalyParameters.IN_FILE, 0);
      consoleLogger
          .info("read " + series.length + " points from " + GrammarVizAnomalyParameters.IN_FILE);

      // switch logic according to the algorithm selection
      //
      if (AnomalyAlgorithm.BRUTEFORCE.equals(GrammarVizAnomalyParameters.ALGORITHM)) {
        findBruteForce(series, GrammarVizAnomalyParameters.SAX_WINDOW_SIZE,
            GrammarVizAnomalyParameters.DISCORDS_NUM);
      }
      else if (AnomalyAlgorithm.HOTSAXTRIE.equals(GrammarVizAnomalyParameters.ALGORITHM)) {
        findHotSax(series, GrammarVizAnomalyParameters.SAX_WINDOW_SIZE,
            GrammarVizAnomalyParameters.SAX_ALPHABET_SIZE, GrammarVizAnomalyParameters.DISCORDS_NUM,
            GrammarVizAnomalyParameters.SAX_NORM_THRESHOLD);
      }
      else if (AnomalyAlgorithm.HOTSAX.equals(GrammarVizAnomalyParameters.ALGORITHM)) {
        findHotSaxWithHash(series, GrammarVizAnomalyParameters.SAX_WINDOW_SIZE,
            GrammarVizAnomalyParameters.SAX_PAA_SIZE, GrammarVizAnomalyParameters.SAX_ALPHABET_SIZE,
            GrammarVizAnomalyParameters.DISCORDS_NUM,
            GrammarVizAnomalyParameters.SAX_NORM_THRESHOLD);
      }
      else if (AnomalyAlgorithm.RRA.equals(GrammarVizAnomalyParameters.ALGORITHM)) {
        findRRA(series, GrammarVizAnomalyParameters.SAX_WINDOW_SIZE,
            GrammarVizAnomalyParameters.SAX_PAA_SIZE, GrammarVizAnomalyParameters.SAX_ALPHABET_SIZE,
            GrammarVizAnomalyParameters.SAX_NR_STRATEGY, GrammarVizAnomalyParameters.DISCORDS_NUM,
            GrammarVizAnomalyParameters.GI_ALGORITHM_IMPLEMENTATION,
            GrammarVizAnomalyParameters.OUT_FILE, GrammarVizAnomalyParameters.SAX_NORM_THRESHOLD);
      }
      else if (AnomalyAlgorithm.RRAPRUNED.equals(GrammarVizAnomalyParameters.ALGORITHM)) {
        findRRAPruned(series, GrammarVizAnomalyParameters.SAX_WINDOW_SIZE,
            GrammarVizAnomalyParameters.SAX_PAA_SIZE, GrammarVizAnomalyParameters.SAX_ALPHABET_SIZE,
            GrammarVizAnomalyParameters.SAX_NR_STRATEGY, GrammarVizAnomalyParameters.DISCORDS_NUM,
            GrammarVizAnomalyParameters.GI_ALGORITHM_IMPLEMENTATION,
            GrammarVizAnomalyParameters.OUT_FILE, GrammarVizAnomalyParameters.SAX_NORM_THRESHOLD);
      }
      else if (AnomalyAlgorithm.RRASAMPLED.equals(GrammarVizAnomalyParameters.ALGORITHM)) {
        findRRASampled(series, GrammarVizAnomalyParameters.GRID_BOUNDARIES,
            GrammarVizAnomalyParameters.SAX_NR_STRATEGY, GrammarVizAnomalyParameters.DISCORDS_NUM,
            GrammarVizAnomalyParameters.GI_ALGORITHM_IMPLEMENTATION,
            GrammarVizAnomalyParameters.OUT_FILE, GrammarVizAnomalyParameters.SAX_NORM_THRESHOLD);
      }
    }
  }

  /**
   * Finds discords in classic manner (i.e., using a trie).
   * 
   * @param ts the dataset.
   * @param boundaries the sampling boundaries.
   * @param saxNRStrategy the NR strategy to use.
   * @param discordsToReport SAX sliding window size.
   * @param giImplementation the GI algorithm to use.
   * @param outputPrefix the output prefix.
   * @param normalizationThreshold SAX normalization threshold.
   * @throws Exception if error occurs.
   */
  private static void findRRASampled(double[] ts, String boundaries,
      NumerosityReductionStrategy saxNRStrategy, int discordsToReport, GIAlgorithm giImplementation,
      String outputPrefix, double normalizationThreshold) throws Exception {

    consoleLogger.info("running RRA with sampling algorithm...");
    Date start = new Date();

    // parse the boundaries params
    int[] bounds = toBoundaries(RulePrunerParameters.GRID_BOUNDARIES);

    // create the output file
    // BufferedWriter bw = new BufferedWriter(
    // new FileWriter(new File(RulePrunerParameters.OUT_FILE)));
    // bw.write(OUTPUT_HEADER);

    ArrayList<SampledPoint> res = new ArrayList<SampledPoint>();

    // we need to use this in the loop
    RulePruner rp = new RulePruner(ts);

    // iterate over the grid evaluating the grammar
    //
    for (int WINDOW_SIZE = bounds[0]; WINDOW_SIZE < bounds[1]; WINDOW_SIZE += bounds[2]) {
      for (int PAA_SIZE = bounds[3]; PAA_SIZE < bounds[4]; PAA_SIZE += bounds[5]) {
        // check for invalid cases
        if (PAA_SIZE > WINDOW_SIZE) {
          continue;
        }
        for (int ALPHABET_SIZE = bounds[6]; ALPHABET_SIZE < bounds[7]; ALPHABET_SIZE += bounds[8]) {
          SampledPoint p = rp.sample(WINDOW_SIZE, PAA_SIZE, ALPHABET_SIZE,
              RulePrunerParameters.SAX_NR_STRATEGY, RulePrunerParameters.SAX_NORM_THRESHOLD);
          res.add(p);
        }
      }
    }

    Collections.sort(res, new ReductionSorter());

    System.out.println("\nApparently, the best parameters are " + res.get(0).toString());

    int windowSize = res.get(0).getWindow();
    int paaSize = res.get(0).getPAA();
    int alphabetSize = res.get(0).getAlphabet();

    GrammarRules rules;

    if (GIAlgorithm.SEQUITUR.equals(giImplementation)) {
      rules = SequiturFactory.series2SequiturRules(ts, windowSize, paaSize, alphabetSize,
          saxNRStrategy, normalizationThreshold);
    }
    else {
      ParallelSAXImplementation ps = new ParallelSAXImplementation();
      SAXRecords parallelRes = ps.process(ts, 2, windowSize, paaSize, alphabetSize,
          NumerosityReductionStrategy.EXACT, normalizationThreshold);
      RePairGrammar rePairGrammar = RePairFactory.buildGrammar(parallelRes);
      rePairGrammar.expandRules();
      rePairGrammar.buildIntervals(parallelRes, ts, windowSize);
      rules = rePairGrammar.toGrammarRulesData();
    }

    ArrayList<RuleInterval> intervals = new ArrayList<RuleInterval>();

    // populate all intervals with their frequency
    //
    for (GrammarRuleRecord rule : rules) {
      //
      // TODO: do we care about long rules?
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
      consoleLogger.info(
          "found " + zeros.size() + " intervals not covered by rules: " + intervalsToString(zeros));
      intervals.addAll(zeros);
    }
    else {
      consoleLogger.info("Whole timeseries covered by rule intervals ...");
    }

    // run HOTSAX with this intervals set
    //
    DiscordRecords discords = RRAImplementation.series2RRAAnomalies(ts, discordsToReport,
        intervals);
    Date end = new Date();

    System.out.println(discords.toString() + CR + "Discords found in "
        + SAXProcessor.timeToString(start.getTime(), end.getTime()) + CR);

    // THE DISCORD SEARCH IS DONE RIGHT HERE
    // BELOW IS THE CODE WHICH WRITES THE CURVE AND THE DISTANCE FILE ON FILESYSTEM
    //
    if (!(outputPrefix.isEmpty())) {

      // write the coverage array
      //
      String currentPath = new File(".").getCanonicalPath();
      BufferedWriter bw = new BufferedWriter(
          new FileWriter(new File(currentPath + File.separator + outputPrefix + "_coverage.txt")));
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
            double dist = ed.distance(cw, currentSubsequence);
            if (dist < cwNNDist) {
              cwNNDist = dist;
            }
          }
        }

        distances[ruleStart] = cwNNDist;
        widths[ruleStart] = ri.getLength();
      }

      bw = new BufferedWriter(
          new FileWriter(new File(currentPath + File.separator + outputPrefix + "_distances.txt")));
      for (int i = 0; i < distances.length; i++) {
        bw.write(i + "," + distances[i] + "," + widths[i] + "\n");
      }
      bw.close();
    }
  }

  /**
   * Finds discords in classic manner (i.e., using a trie).
   * 
   * @param ts the dataset.
   * @param windowSize SAX sliding window size.
   * @param paaSize SAX PAA size.
   * @param alphabetSize SAX alphabet size.
   * @param saxNRStrategy the NR strategy to use.
   * @param discordsToReport SAX sliding window size.
   * @param giImplementation the GI algorithm to use.
   * @param outputPrefix the output prefix.
   * @param normalizationThreshold SAX normalization threshold.
   * @throws Exception if error occurs.
   */
  private static void findRRAPruned(double[] ts, int windowSize, int alphabetSize, int paaSize,
      NumerosityReductionStrategy saxNRStrategy, int discordsToReport, GIAlgorithm giImplementation,
      String outputPrefix, double normalizationThreshold) throws Exception {

    consoleLogger.info("running RRA with pruning algorithm...");
    Date start = new Date();

    GrammarRules rules;

    if (GIAlgorithm.SEQUITUR.equals(giImplementation)) {
      rules = SequiturFactory.series2SequiturRules(ts, windowSize, paaSize, alphabetSize,
          saxNRStrategy, normalizationThreshold);
    }
    else {
      ParallelSAXImplementation ps = new ParallelSAXImplementation();
      SAXRecords parallelRes = ps.process(ts, 2, windowSize, paaSize, alphabetSize,
          NumerosityReductionStrategy.EXACT, normalizationThreshold);
      RePairGrammar rePairGrammar = RePairFactory.buildGrammar(parallelRes);
      rePairGrammar.expandRules();
      rePairGrammar.buildIntervals(parallelRes, ts, windowSize);
      rules = rePairGrammar.toGrammarRulesData();
    }

    // prune grammar' rules
    //
    GrammarRules prunedRulesSet = RulePrunerFactory.performPruning(ts, rules);
    
    ArrayList<RuleInterval> intervals = new ArrayList<RuleInterval>();

    // populate all intervals with their frequency
    //
    for (GrammarRuleRecord rule : prunedRulesSet) {
      //
      // TODO: do we care about long rules?
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
    for (GrammarRuleRecord rule : prunedRulesSet) {
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
      consoleLogger.info(
          "found " + zeros.size() + " intervals not covered by rules: " + intervalsToString(zeros));
      intervals.addAll(zeros);
    }
    else {
      consoleLogger.info("Whole timeseries covered by rule intervals ...");
    }

    // run HOTSAX with this intervals set
    //
    DiscordRecords discords = RRAImplementation.series2RRAAnomalies(ts, discordsToReport,
        intervals);
    Date end = new Date();

    System.out.println(discords.toString() + CR + "Discords found in "
        + SAXProcessor.timeToString(start.getTime(), end.getTime()) + CR);

    // THE DISCORD SEARCH IS DONE RIGHT HERE
    // BELOW IS THE CODE WHICH WRITES THE CURVE AND THE DISTANCE FILE ON FILESYSTEM
    //
    if (!(outputPrefix.isEmpty())) {

      // write the coverage array
      //
      String currentPath = new File(".").getCanonicalPath();
      BufferedWriter bw = new BufferedWriter(
          new FileWriter(new File(currentPath + File.separator + outputPrefix + "_coverage.txt")));
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
            double dist = ed.distance(cw, currentSubsequence);
            if (dist < cwNNDist) {
              cwNNDist = dist;
            }
          }
        }

        distances[ruleStart] = cwNNDist;
        widths[ruleStart] = ri.getLength();
      }

      bw = new BufferedWriter(
          new FileWriter(new File(currentPath + File.separator + outputPrefix + "_distances.txt")));
      for (int i = 0; i < distances.length; i++) {
        bw.write(i + "," + distances[i] + "," + widths[i] + "\n");
      }
      bw.close();
    }
  }

  /**
   * Finds discords in classic manner (i.e., using a trie).
   * 
   * @param ts the dataset.
   * @param windowSize SAX sliding window size.
   * @param paaSize SAX PAA size.
   * @param alphabetSize SAX alphabet size.
   * @param saxNRStrategy the NR strategy to use.
   * @param discordsToReport SAX sliding window size.
   * @param giImplementation the GI algorithm to use.
   * @param outputPrefix the output prefix.
   * @param normalizationThreshold SAX normalization threshold.
   * @throws Exception if error occurs.
   */
  private static void findRRA(double[] ts, int windowSize, int alphabetSize, int paaSize,
      NumerosityReductionStrategy saxNRStrategy, int discordsToReport, GIAlgorithm giImplementation,
      String outputPrefix, double normalizationThreshold) throws Exception {

    consoleLogger.info("running RRA algorithm...");
    Date start = new Date();

    GrammarRules rules;

    if (GIAlgorithm.SEQUITUR.equals(giImplementation)) {
      rules = SequiturFactory.series2SequiturRules(ts, windowSize, paaSize, alphabetSize,
          saxNRStrategy, normalizationThreshold);
    }
    else {
      ParallelSAXImplementation ps = new ParallelSAXImplementation();
      SAXRecords parallelRes = ps.process(ts, 2, windowSize, paaSize, alphabetSize,
          NumerosityReductionStrategy.EXACT, normalizationThreshold);
      RePairGrammar rePairGrammar = RePairFactory.buildGrammar(parallelRes);
      rePairGrammar.expandRules();
      rePairGrammar.buildIntervals(parallelRes, ts, windowSize);
      rules = rePairGrammar.toGrammarRulesData();
    }

    ArrayList<RuleInterval> intervals = new ArrayList<RuleInterval>();

    // populate all intervals with their frequency
    //
    for (GrammarRuleRecord rule : rules) {
      //
      // TODO: do we care about long rules?
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
      consoleLogger.info(
          "found " + zeros.size() + " intervals not covered by rules: " + intervalsToString(zeros));
      intervals.addAll(zeros);
    }
    else {
      consoleLogger.info("Whole timeseries covered by rule intervals ...");
    }

    // run HOTSAX with this intervals set
    //
    DiscordRecords discords = RRAImplementation.series2RRAAnomalies(ts, discordsToReport,
        intervals);
    Date end = new Date();

    System.out.println(discords.toString() + CR + "Discords found in "
        + SAXProcessor.timeToString(start.getTime(), end.getTime()) + CR);

    // THE DISCORD SEARCH IS DONE RIGHT HERE
    // BELOW IS THE CODE WHICH WRITES THE CURVE AND THE DISTANCE FILE ON FILESYSTEM
    //
    if (!(outputPrefix.isEmpty())) {

      // write the coverage array
      //
      String currentPath = new File(".").getCanonicalPath();
      BufferedWriter bw = new BufferedWriter(
          new FileWriter(new File(currentPath + File.separator + outputPrefix + "_coverage.txt")));
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
            double dist = ed.distance(cw, currentSubsequence);
            if (dist < cwNNDist) {
              cwNNDist = dist;
            }
          }
        }

        distances[ruleStart] = cwNNDist;
        widths[ruleStart] = ri.getLength();
      }

      bw = new BufferedWriter(
          new FileWriter(new File(currentPath + File.separator + outputPrefix + "_distances.txt")));
      for (int i = 0; i < distances.length; i++) {
        bw.write(i + "," + distances[i] + "," + widths[i] + "\n");
      }
      bw.close();
    }
  }

  /**
   * Procedure of finding brute-force discords.
   * 
   * @param ts timeseries to use
   * @param windowSize the sliding window size.
   * @param discordsToReport num of discords to report.
   * @throws Exception if error occurs.
   */
  private static void findBruteForce(double[] ts, int windowSize, int discordsToReport)
      throws Exception {

    consoleLogger.info("running brute force algorithm...");

    Date start = new Date();
    DiscordRecords discords = BruteForceDiscordImplementation.series2BruteForceDiscords(ts,
        windowSize, discordsToReport, new LargeWindowAlgorithm());
    Date end = new Date();

    System.out.println(CR + discords.toString() + CR + discords.getSize() + " discords found in "
        + SAXProcessor.timeToString(start.getTime(), end.getTime()) + CR);
  }

  /**
   * Finds discords in classic manner (i.e., using a trie).
   * 
   * @param ts the dataset.
   * @param windowSize SAX sliding window size.
   * @param alphabetSize SAX alphabet size.
   * @param discordsToReport SAX sliding window size.
   * @param normalizationThreshold SAX normalization threshold.
   * @throws Exception if error occurs.
   */
  private static void findHotSax(double[] ts, int windowSize, int alphabetSize,
      int discordsToReport, double normalizationThreshold) throws Exception {

    consoleLogger.info("running HOT SAX Trie-based algorithm...");

    Date start = new Date();
    DiscordRecords discords = HOTSAXImplementation.series2Discords(ts, windowSize, alphabetSize,
        discordsToReport, new LargeWindowAlgorithm(), normalizationThreshold);
    Date end = new Date();

    System.out.println(CR + discords.toString() + CR + "Discords found in "
        + SAXProcessor.timeToString(start.getTime(), end.getTime()) + CR);

  }

  /**
   * Finds discords using a hash-backed magic array.
   * 
   * @param ts the dataset.
   * @param windowSize SAX sliding window size.
   * @param paaSize SAX PAA size.
   * @param alphabetSize SAX alphabet size.
   * @param discordsToReport SAX sliding window size.
   * @param normalizationThreshold SAX normalization threshold.
   * @throws Exception if error occurs.
   */
  private static void findHotSaxWithHash(double[] ts, int windowSize, int paaSize, int alphabetSize,
      int discordsToReport, double normalizationThreshold) throws TrieException, Exception {

    consoleLogger.info("running HOT SAX Hash-based algorithm...");

    Date start = new Date();
    DiscordRecords discords = HOTSAXImplementation.series2DiscordsWithHash(ts, windowSize, paaSize,
        alphabetSize, discordsToReport, new LargeWindowAlgorithm(), normalizationThreshold);
    Date end = new Date();

    System.out.println(CR + discords.toString() + CR + "Discords found in "
        + SAXProcessor.timeToString(start.getTime(), end.getTime()) + CR);

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
   * Converts a param string to boundaries array.
   * 
   * @param str
   * @return
   */
  private static int[] toBoundaries(String str) {
    int[] res = new int[9];
    String[] split = str.split("\\s+");
    for (int i = 0; i < 9; i++) {
      res[i] = Integer.valueOf(split[i]);
    }
    return res;
  }
}
