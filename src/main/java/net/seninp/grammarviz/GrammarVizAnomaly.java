package net.seninp.grammarviz;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.beust.jcommander.JCommander;
import net.seninp.gi.GIAlgorithm;
import net.seninp.gi.logic.GrammarRuleRecord;
import net.seninp.gi.logic.GrammarRules;
import net.seninp.gi.logic.RuleInterval;
import net.seninp.gi.repair.RePairFactory;
import net.seninp.gi.repair.RePairGrammar;
import net.seninp.gi.rulepruner.ReducedGrammarSizeSorter;
import net.seninp.gi.rulepruner.ReductionSorter;
import net.seninp.gi.rulepruner.RulePruner;
import net.seninp.gi.rulepruner.RulePrunerFactory;
import net.seninp.gi.rulepruner.SampledPoint;
import net.seninp.gi.sequitur.SequiturFactory;
import net.seninp.grammarviz.anomaly.AnomalyAlgorithm;
import net.seninp.grammarviz.anomaly.RRAImplementation;
import net.seninp.grammarviz.anomaly.RRAIntervalBuilder;
import net.seninp.jmotif.distance.EuclideanDistance;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.datastructure.SAXRecords;
import net.seninp.jmotif.sax.discord.BruteForceDiscordImplementation;
import net.seninp.jmotif.sax.discord.DiscordRecords;
import net.seninp.jmotif.sax.discord.HOTSAXImplementation;
import net.seninp.jmotif.sax.parallel.ParallelSAXImplementation;
import net.seninp.jmotif.sax.registry.LargeWindowAlgorithm;

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

  // workers
  //
  private static TSProcessor tp = new TSProcessor();
  private static EuclideanDistance ed = new EuclideanDistance();

  // static block - we instantiate the logger
  //
  private static final Logger LOGGER = LoggerFactory.getLogger(GrammarVizAnomaly.class);

  /**
   * The main executable.
   * 
   * @param args The command-line params.
   * @throws Exception If error occurs.
   */
  public static void main(String[] args) throws Exception {

    GrammarVizAnomalyParameters params = new GrammarVizAnomalyParameters();
    JCommander jct = new JCommander(params);

    if (0 == args.length) {
      jct.usage();
    }
    else {
      
      jct.parse(args);
      // get params printed
      //
      StringBuffer sb = new StringBuffer(1024);
      sb.append(CR).append("GrammarViz2 CLI anomaly discovery").append(CR);
      sb.append("parameters:").append(CR);

      sb.append(" input file:                  ").append(GrammarVizAnomalyParameters.IN_FILE)
          .append(CR);
      sb.append(" output files prefix:         ").append(GrammarVizAnomalyParameters.OUT_FILE)
          .append(CR);

      sb.append(" Algorithm implementation:    ").append(GrammarVizAnomalyParameters.ALGORITHM)
          .append(CR);
      sb.append(" Num. of discords to report:  ").append(GrammarVizAnomalyParameters.DISCORDS_NUM)
          .append(CR);

      if (!(AnomalyAlgorithm.RRASAMPLED.equals(GrammarVizAnomalyParameters.ALGORITHM)
          || AnomalyAlgorithm.EXPERIMENT.equals(GrammarVizAnomalyParameters.ALGORITHM))) {
        sb.append(" SAX sliding window size:     ")
            .append(GrammarVizAnomalyParameters.SAX_WINDOW_SIZE).append(CR);
      }

      if (!(AnomalyAlgorithm.BRUTEFORCE.equals(GrammarVizAnomalyParameters.ALGORITHM))) {
        if (!(AnomalyAlgorithm.RRASAMPLED.equals(GrammarVizAnomalyParameters.ALGORITHM)
            || AnomalyAlgorithm.EXPERIMENT.equals(GrammarVizAnomalyParameters.ALGORITHM))) {
          sb.append(" SAX PAA size:                ")
              .append(GrammarVizAnomalyParameters.SAX_PAA_SIZE).append(CR);
          sb.append(" SAX alphabet size:           ")
              .append(GrammarVizAnomalyParameters.SAX_ALPHABET_SIZE).append(CR);
        }
        sb.append(" SAX numerosity reduction:    ")
            .append(GrammarVizAnomalyParameters.SAX_NR_STRATEGY).append(CR);
        sb.append(" SAX normalization threshold: ")
            .append(GrammarVizAnomalyParameters.SAX_NORM_THRESHOLD).append(CR);
      }

      if (AnomalyAlgorithm.RRASAMPLED.equals(GrammarVizAnomalyParameters.ALGORITHM)
          || AnomalyAlgorithm.RRA.equals(GrammarVizAnomalyParameters.ALGORITHM)
          || AnomalyAlgorithm.RRAPRUNED.equals(GrammarVizAnomalyParameters.ALGORITHM)
          || AnomalyAlgorithm.EXPERIMENT.equals(GrammarVizAnomalyParameters.ALGORITHM)) {
        sb.append(" GI Algorithm:                ")
            .append(GrammarVizAnomalyParameters.GI_ALGORITHM_IMPLEMENTATION).append(CR);
      }

      if (AnomalyAlgorithm.RRASAMPLED.equals(GrammarVizAnomalyParameters.ALGORITHM)
          || AnomalyAlgorithm.EXPERIMENT.equals(GrammarVizAnomalyParameters.ALGORITHM)) {
        sb.append(" Grid boundaries:             ")
            .append(GrammarVizAnomalyParameters.GRID_BOUNDARIES).append(CR);
      }

      if ((AnomalyAlgorithm.RRASAMPLED.equals(GrammarVizAnomalyParameters.ALGORITHM)
          || AnomalyAlgorithm.EXPERIMENT.equals(GrammarVizAnomalyParameters.ALGORITHM))
          && !(Double.isNaN(GrammarVizAnomalyParameters.SUBSAMPLING_FRACTION))) {
        sb.append(" Subsampling fraction:        ")
            .append(GrammarVizAnomalyParameters.SUBSAMPLING_FRACTION).append(CR);
      }

      System.out.println(sb.toString());

      // read the file
      //
      LOGGER.info("Reading data ...");
      double[] series = tp.readTS(GrammarVizAnomalyParameters.IN_FILE, 0);
      LOGGER.info("read " + series.length + " points from " + GrammarVizAnomalyParameters.IN_FILE);

      // switch logic according to the algorithm selection
      //
      if (AnomalyAlgorithm.BRUTEFORCE.equals(GrammarVizAnomalyParameters.ALGORITHM)) {
        findBruteForce(series, GrammarVizAnomalyParameters.SAX_WINDOW_SIZE,
            GrammarVizAnomalyParameters.DISCORDS_NUM,
            GrammarVizAnomalyParameters.SAX_NORM_THRESHOLD);
      }
      else if (AnomalyAlgorithm.HOTSAX.equals(GrammarVizAnomalyParameters.ALGORITHM)) {
        findHotSax(series, GrammarVizAnomalyParameters.DISCORDS_NUM,
            GrammarVizAnomalyParameters.SAX_WINDOW_SIZE, GrammarVizAnomalyParameters.SAX_PAA_SIZE,
            GrammarVizAnomalyParameters.SAX_ALPHABET_SIZE,
            GrammarVizAnomalyParameters.SAX_NR_STRATEGY,
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
      else if (AnomalyAlgorithm.EXPERIMENT.equals(GrammarVizAnomalyParameters.ALGORITHM)) {
        findRRAExperiment(series, GrammarVizAnomalyParameters.GRID_BOUNDARIES,
            GrammarVizAnomalyParameters.SAX_NR_STRATEGY, GrammarVizAnomalyParameters.DISCORDS_NUM,
            GrammarVizAnomalyParameters.GI_ALGORITHM_IMPLEMENTATION,
            GrammarVizAnomalyParameters.OUT_FILE, GrammarVizAnomalyParameters.SAX_NORM_THRESHOLD);
      }
    }
  }

  private static void findRRAExperiment(double[] ts, String boundaries,
      NumerosityReductionStrategy saxNRStrategy, int discordsToReport, GIAlgorithm giImplementation,
      String outputPrefix, double normalizationThreshold) throws Exception {

    LOGGER.info("running RRA with experiment sampling algorithm...");
    // Date start = new Date();

    // parse the boundaries params
    int[] bounds = toBoundaries(GrammarVizAnomalyParameters.GRID_BOUNDARIES);

    ArrayList<SampledPoint> res = new ArrayList<SampledPoint>();

    // we need to use this in the loop
    RulePruner rp;
    if (GrammarVizAnomalyParameters.SUBSAMPLING_FRACTION.isNaN()) {
      LOGGER.info("sampling on full time series length");
      rp = new RulePruner(ts);
    }
    else {
      int sampleIntervalStart = 0;
      int sampleIntervalEnd = (int) Math
          .round(ts.length * GrammarVizAnomalyParameters.SUBSAMPLING_FRACTION);
      LOGGER.info("sampling parameters on interval [" + sampleIntervalStart + ", "
          + sampleIntervalEnd + "]");
      rp = new RulePruner(Arrays.copyOfRange(ts, sampleIntervalStart, sampleIntervalEnd));
    }

    // iterate over the grid evaluating the grammar
    //
    for (int WINDOW_SIZE = bounds[0]; WINDOW_SIZE < bounds[1]; WINDOW_SIZE += bounds[2]) {
      for (int PAA_SIZE = bounds[3]; PAA_SIZE < bounds[4]; PAA_SIZE += bounds[5]) {
        // check for invalid cases
        if (PAA_SIZE > WINDOW_SIZE) {
          continue;
        }
        for (int ALPHABET_SIZE = bounds[6]; ALPHABET_SIZE < bounds[7]; ALPHABET_SIZE += bounds[8]) {
          SampledPoint p = rp.sample(WINDOW_SIZE, PAA_SIZE, ALPHABET_SIZE, GIAlgorithm.REPAIR,
              GrammarVizAnomalyParameters.SAX_NR_STRATEGY,
              GrammarVizAnomalyParameters.SAX_NORM_THRESHOLD);
          res.add(p);
          ///
          ///
          ///
          ///
          // GrammarRules rules;
          // if (GIAlgorithm.SEQUITUR.equals(giImplementation)) {
          // rules = SequiturFactory.series2SequiturRules(ts, WINDOW_SIZE, PAA_SIZE, ALPHABET_SIZE,
          // GrammarVizAnomalyParameters.SAX_NR_STRATEGY,
          // GrammarVizAnomalyParameters.SAX_NORM_THRESHOLD);
          // }
          // else {
          // ParallelSAXImplementation ps = new ParallelSAXImplementation();
          // SAXRecords parallelRes = ps.process(ts, 2, WINDOW_SIZE, PAA_SIZE, ALPHABET_SIZE,
          // GrammarVizAnomalyParameters.SAX_NR_STRATEGY,
          // GrammarVizAnomalyParameters.SAX_NORM_THRESHOLD);
          // RePairGrammar rePairGrammar = RePairFactory.buildGrammar(parallelRes);
          // rePairGrammar.expandRules();
          // rePairGrammar.buildIntervals(parallelRes, ts, WINDOW_SIZE);
          // rules = rePairGrammar.toGrammarRulesData();
          // }
          //
          // // prune grammar' rules
          // GrammarRules prunedRulesSet = RulePrunerFactory.performPruning(ts, rules);
          //
          // // pruned intervals
          // ArrayList<RuleInterval> prunedIntervals = new ArrayList<RuleInterval>();
          //
          // // coverage intervals
          // int[] coverageArray = new int[ts.length];
          //
          // // populate all intervals with their frequency
          // for (GrammarRuleRecord rule : prunedRulesSet) {
          // if (0 == rule.ruleNumber()) {
          // continue;
          // }
          // for (RuleInterval ri : rule.getRuleIntervals()) {
          // ri.setCoverage(rule.getRuleIntervals().size());
          // ri.setId(rule.ruleNumber());
          // prunedIntervals.add(ri);
          // //
          // int startPos = ri.getStartPos();
          // int endPos = ri.getEndPos();
          // for (int j = startPos; j < endPos; j++) {
          // coverageArray[j] = coverageArray[j] + 1;
          // }
          // }
          // }
          //
          // // look for zero-covered intervals and add those to the list
          // List<RuleInterval> zeros = getZeroIntervals(coverageArray);
          // if (zeros.size() > 0) {
          // prunedIntervals.addAll(zeros);
          // }

          // // run HOTSAX with this intervals set
          // DiscordRecords discords = RRAImplementation.series2RRAAnomalies(ts, 1,
          // prunedIntervals);
          //
          // if (discords.getSize() > 0) {
          // // if the discord(s) found
          LOGGER.info("# " + WINDOW_SIZE + "," + PAA_SIZE + "," + ALPHABET_SIZE + ","
              + p.getApproxDist() + "," + p.getGrammarSize() + "," + p.getCompressedGrammarSize()
              + "," + p.getGrammarRules() + "," + p.getPrunedRules() + "," + p.getCoverage() + ","
              + p.getMaxFrequency());
          // }
          // else {
          // // no discords were discovered
          // // need to increase the granularity of discretization
          // LOGGER.info("# " + WINDOW_SIZE + "," + PAA_SIZE + "," + ALPHABET_SIZE + ","
          // + p.getApproxDist() + "," + p.getGrammarSize() + "," + p.getCompressedGrammarSize()
          // + "," + p.getCoverage() + ",-1,-1");
          // }
          ///
          ///
        }
      }
    }

    // Collections.sort(res, new ReductionSorter());
    Collections.sort(res, new GrammarSizeSorter());

    if (!hasSampledPoints(res)) {
      LOGGER.error("experiment grid produced no valid parameter samples; skipping RRA runs");
      return;
    }

    System.out.println(CR + "# GLOBALLY MIN GRAMMAR size is " + res.get(0).toString() + CR
        + "Running RRAPruned ..." + CR);

    int windowSize = res.get(0).getWindow();
    int paaSize = res.get(0).getPAA();
    int alphabetSize = res.get(0).getAlphabet();

    findRRAPruned(ts, windowSize, paaSize, alphabetSize, saxNRStrategy, discordsToReport,
        giImplementation, outputPrefix, normalizationThreshold);

    Collections.sort(res, new ReducedGrammarSizeSorter());

    System.out.println(CR + "# GLOBALLY MIN PRUNED grammar size: " + res.get(0).toString() + CR
        + "Running RRAPruned ..." + CR);

    windowSize = res.get(0).getWindow();
    paaSize = res.get(0).getPAA();
    alphabetSize = res.get(0).getAlphabet();

    findRRAPruned(ts, windowSize, paaSize, alphabetSize, saxNRStrategy, discordsToReport,
        giImplementation, outputPrefix, normalizationThreshold);

    double threshold = 0.99;
    ArrayList<SampledPoint> resCovered = poolAtCoverageThreshold(res, threshold);

    // Collections.sort(resCovered, new ReductionSorter());
    Collections.sort(resCovered, new GrammarSizeSorter());

    System.out.println(CR + "# COVERED ABOVE THRESHOLD MIN GRAMMAR parameters are "
        + resCovered.get(0).toString() + CR + "Running RRAPruned ..." + CR);

    windowSize = resCovered.get(0).getWindow();
    paaSize = resCovered.get(0).getPAA();
    alphabetSize = resCovered.get(0).getAlphabet();

    findRRAPruned(ts, windowSize, paaSize, alphabetSize, saxNRStrategy, discordsToReport,
        giImplementation, outputPrefix, normalizationThreshold);

    Collections.sort(resCovered, new ReducedGrammarSizeSorter());

    System.out.println(CR + "# COVERED ABOVE THRESHOLD MIN PRUNED GRAMMAR : "
        + resCovered.get(0).toString() + CR + "Running RRAPruned ..." + CR);

    windowSize = resCovered.get(0).getWindow();
    paaSize = resCovered.get(0).getPAA();
    alphabetSize = resCovered.get(0).getAlphabet();

    findRRAPruned(ts, windowSize, paaSize, alphabetSize, saxNRStrategy, discordsToReport,
        giImplementation, outputPrefix, normalizationThreshold);

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

    LOGGER.info("running RRA with sampling algorithm...");
    // Date start = new Date();

    // parse the boundaries params
    int[] bounds = toBoundaries(GrammarVizAnomalyParameters.GRID_BOUNDARIES);

    // create the output file
    // BufferedWriter bw = new BufferedWriter(
    // new FileWriter(new File(GrammarVizAnomalyParameters.OUT_FILE)));
    // bw.write(OUTPUT_HEADER);

    ArrayList<SampledPoint> res = new ArrayList<SampledPoint>();

    // we need to use this in the loop
    RulePruner rp;
    if (GrammarVizAnomalyParameters.SUBSAMPLING_FRACTION.isNaN()) {
      LOGGER.info("sampling on full time series length");
      rp = new RulePruner(ts);
    }
    else {
      int sampleIntervalStart = 0;
      int sampleIntervalEnd = (int) Math
          .round(ts.length * GrammarVizAnomalyParameters.SUBSAMPLING_FRACTION);
      LOGGER.info("sampling parameters on interval [" + sampleIntervalStart + ", "
          + sampleIntervalEnd + "]");
      rp = new RulePruner(Arrays.copyOfRange(ts, sampleIntervalStart, sampleIntervalEnd));
    }

    // iterate over the grid evaluating the grammar
    //
    for (int WINDOW_SIZE = bounds[0]; WINDOW_SIZE < bounds[1]; WINDOW_SIZE += bounds[2]) {
      for (int PAA_SIZE = bounds[3]; PAA_SIZE < bounds[4]; PAA_SIZE += bounds[5]) {
        // check for invalid cases
        if (PAA_SIZE > WINDOW_SIZE) {
          continue;
        }
        for (int ALPHABET_SIZE = bounds[6]; ALPHABET_SIZE < bounds[7]; ALPHABET_SIZE += bounds[8]) {
          SampledPoint p = rp.sample(WINDOW_SIZE, PAA_SIZE, ALPHABET_SIZE, GIAlgorithm.REPAIR,
              GrammarVizAnomalyParameters.SAX_NR_STRATEGY,
              GrammarVizAnomalyParameters.SAX_NORM_THRESHOLD);
          res.add(p);
        }
      }
    }

    Collections.sort(res, new ReductionSorter());

    if (!hasSampledPoints(res)) {
      LOGGER.error("sampled grid produced no valid parameter samples; skipping RRA run");
      return;
    }

    System.out.println(CR + "Apparently, the best parameters are " + res.get(0).toString() + CR
        + "Running RRAPRUNED..." + CR);

    int windowSize = res.get(0).getWindow();
    int paaSize = res.get(0).getPAA();
    int alphabetSize = res.get(0).getAlphabet();

    findRRAPruned(ts, windowSize, paaSize, alphabetSize, saxNRStrategy, discordsToReport,
        giImplementation, outputPrefix, normalizationThreshold);

  }

  /**
   * Finds discords in classic manner (i.e., using a trie).
   * 
   * @param ts the dataset.
   * @param windowSize SAX int windowSize = res.get(0).getWindow(); int paaSize =
   * res.get(0).getPAA(); int alphabetSize = res.get(0).getAlphabet();
   * 
   * GrammarRules rules;
   * 
   * if (GIAlgorithm.SEQUITUR.equals(giImplementation)) { rules =
   * SequiturFactory.series2SequiturRules(ts, windowSize, paaSize, alphabetSize, saxNRStrategy,
   * normalizationThreshold); } else { ParallelSAXImplementation ps = new
   * ParallelSAXImplementation(); SAXRecords parallelRes = ps.process(ts, 2, windowSize, paaSize,
   * alphabetSize, NumerosityReductionStrategy.EXACT, normalizationThreshold); RePairGrammar
   * rePairGrammar = RePairFactory.buildGrammar(parallelRes); rePairGrammar.expandRules();
   * rePairGrammar.buildIntervals(parallelRes, ts, windowSize); rules =
   * rePairGrammar.toGrammarRulesData(); }
   * 
   * ArrayList<RuleInterval> intervals = new ArrayList<RuleInterval>();
   * 
   * // populate all intervals with their frequency // for (GrammarRuleRecord rule : rules) { // //
   * TODO: do we care about long rules? // if (0 == rule.ruleNumber() || rule.getRuleYield() > 2) {
   * if (0 == rule.ruleNumber()) { continue; } for (RuleInterval ri : rule.getRuleIntervals()) {
   * ri.setCoverage(rule.getRuleIntervals().size()); ri.setId(rule.ruleNumber()); intervals.add(ri);
   * } }
   * 
   * // get the coverage array // int[] coverageArray = new int[ts.length]; for (GrammarRuleRecord
   * rule : rules) { if (0 == rule.ruleNumber()) { continue; } ArrayList<RuleInterval> arrPos =
   * rule.getRuleIntervals(); for (RuleInterval saxPos : arrPos) { int startPos =
   * saxPos.getStartPos(); int endPos = saxPos.getEndPos(); for (int j = startPos; j < endPos; j++)
   * { coverageArray[j] = coverageArray[j] + 1; } } }
   * 
   * // look for zero-covered intervals and add those to the list // List<RuleInterval> zeros =
   * getZeroIntervals(coverageArray); if (zeros.size() > 0) { LOGGER.info( "found " + zeros.size() +
   * " intervals not covered by rules: " + intervalsToString(zeros)); intervals.addAll(zeros); }
   * else { LOGGER.info( "the whole timeseries is covered by rule intervals ..."); }
   * 
   * // run HOTSAX with this intervals set // DiscordRecords discords =
   * RRAImplementation.series2RRAAnomalies(ts, discordsToReport, intervals); Date end = new Date();
   * 
   * System.out.println(discords.toString() + CR + "Discords found in " +
   * SAXProcessor.timeToString(start.getTime(), end.getTime()) + CR);
   * 
   * // THE DISCORD SEARCH IS DONE RIGHT HERE // BELOW IS THE CODE WHICH WRITES THE CURVE AND THE
   * DISTANCE FILE ON FILESYSTEM // if (!(outputPrefix.isEmpty())) {
   * 
   * // write the coverage array // String currentPath = new File(".").getCanonicalPath();
   * BufferedWriter bw = new BufferedWriter( new FileWriter(new File(currentPath + File.separator +
   * outputPrefix + "_coverage.txt"))); for (int i : coverageArray) { bw.write(i + "\n"); }
   * bw.close();
   * 
   * Collections.sort(intervals, new Comparator<RuleInterval>() { public int compare(RuleInterval
   * c1, RuleInterval c2) { if (c1.getStartPos() > c2.getStartPos()) { return 1; } else if
   * (c1.getStartPos() < c2.getStartPos()) { return -1; } return 0; } });
   * 
   * // now lets find all the distances to non-self match // double[] distances = new
   * double[ts.length]; double[] widths = new double[ts.length];
   * 
   * for (RuleInterval ri : intervals) {
   * 
   * int ruleStart = ri.getStartPos(); int ruleEnd = ruleStart + ri.getLength(); int window =
   * ruleEnd - ruleStart;
   * 
   * double[] cw = tp.subseriesByCopy(ts, ruleStart, ruleStart + window);
   * 
   * double cwNNDist = Double.MAX_VALUE;
   * 
   * // this effectively finds the furthest hit // for (int j = 0; j < ts.length - window - 1; j++)
   * { if (Math.abs(ruleStart - j) > window) { double[] currentSubsequence = tp.subseriesByCopy(ts,
   * j, j + window); double dist = ed.distance(cw, currentSubsequence); if (dist < cwNNDist) {
   * cwNNDist = dist; } } }
   * 
   * distances[ruleStart] = cwNNDist; widths[ruleStart] = ri.getLength(); }
   * 
   * bw = new BufferedWriter( new FileWriter(new File(currentPath + File.separator + outputPrefix +
   * "_distances.txt"))); for (int i = 0; i < distances.length; i++) { bw.write(i + "," +
   * distances[i] + "," + widths[i] + "\n"); } bw.close(); } sliding window size.
   * @param paaSize SAX PAA size.
   * @param alphabetSize SAX alphabet size.
   * @param saxNRStrategy the NR strategy to use.
   * @param discordsToReport SAX sliding window size.
   * @param giImplementation the GI algorithm to use.
   * @param outputPrefix the output prefix.
   * @param normalizationThreshold SAX normalization threshold.
   * @throws Exception if error occurs.
   */
  private static void findRRAPruned(double[] ts, int windowSize, int paaSize, int alphabetSize,
      NumerosityReductionStrategy saxNRStrategy, int discordsToReport, GIAlgorithm giImplementation,
      String outputPrefix, double normalizationThreshold) throws Exception {

    LOGGER.info("running RRA with pruning algorithm, building the grammar ...");
    Date start = new Date();

    GrammarRules rules;

    if (GIAlgorithm.SEQUITUR.equals(giImplementation)) {
      rules = SequiturFactory.series2SequiturRules(ts, windowSize, paaSize, alphabetSize,
          saxNRStrategy, normalizationThreshold);
    }
    else {
      ParallelSAXImplementation ps = new ParallelSAXImplementation();
      SAXRecords parallelRes = ps.process(ts, 2, windowSize, paaSize, alphabetSize, saxNRStrategy,
          normalizationThreshold);
      RePairGrammar rePairGrammar = RePairFactory.buildGrammar(parallelRes);
      rePairGrammar.expandRules();
      rePairGrammar.buildIntervals(parallelRes, ts, windowSize);
      rules = rePairGrammar.toGrammarRulesData();
    }
    LOGGER.info(rules.size() + " rules inferred in "
        + SAXProcessor.timeToString(start.getTime(), new Date().getTime()) + ", pruning ...");

    // prune grammar' rules
    //
    GrammarRules prunedRulesSet = RulePrunerFactory.performPruning(ts, rules);
    LOGGER.info(
        "finished pruning in " + SAXProcessor.timeToString(start.getTime(), new Date().getTime())
            + ", keeping " + prunedRulesSet.size() + " rules for anomaly discovery ...");

    ArrayList<RuleInterval> intervals = RRAIntervalBuilder.fromGrammarRules(prunedRulesSet, ts.length,
        paaSize);
    int[] coverageArray = RRAIntervalBuilder.computePointCoverage(prunedRulesSet, ts.length);

    List<RuleInterval> zeros = filterZeroIntervalsForAnomalySearch(getZeroIntervals(coverageArray),
        paaSize);
    if (zeros.size() > 0) {
      LOGGER.info(
          "found " + zeros.size() + " intervals not covered by rules: " + intervalsToString(zeros));
    }
    else {
      LOGGER.info("the whole timeseries is covered by rule intervals ...");
    }

    // run RRA with this intervals set
    DiscordRecords discords = RRAImplementation.series2RRAAnomalies(ts, discordsToReport, intervals,
        normalizationThreshold);
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
      BufferedWriter bw = new BufferedWriter( // NOPMD - CloseResource; file frozen pending prior uncommitted fix
          new FileWriter(new File(currentPath + File.separator + outputPrefix + "_coverage.txt")));
      for (int i : coverageArray) {
        bw.write(i + "\n");
      }
      bw.close();

      Collections.sort(intervals, new Comparator<RuleInterval>() {
        public int compare(RuleInterval c1, RuleInterval c2) {
          if (c1.getStart() > c2.getStart()) {
            return 1;
          }
          else if (c1.getStart() < c2.getStart()) {
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

        int ruleStart = ri.getStart();
        int ruleEnd = ruleStart + ri.getLength();
        int window = ruleEnd - ruleStart;

        double[] cw = tp.subseriesByCopy(ts, ruleStart, ruleStart + window);

        double cwNNDist = Double.MAX_VALUE;

        // this effectively finds the furthest hit
        //
        for (int j = 0; j <= ts.length - window; j++) {
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
  private static void findRRA(double[] ts, int windowSize, int paaSize, int alphabetSize,
      NumerosityReductionStrategy saxNRStrategy, int discordsToReport, GIAlgorithm giImplementation,
      String outputPrefix, double normalizationThreshold) throws Exception {

    LOGGER.info("running RRA algorithm...");
    Date start = new Date();

    // [1] get the grammar induced
    //
    GrammarRules rules;

    if (GIAlgorithm.SEQUITUR.equals(giImplementation)) {
      rules = SequiturFactory.series2SequiturRules(ts, windowSize, paaSize, alphabetSize,
          saxNRStrategy, normalizationThreshold);
      Date end = new Date();
      LOGGER.info(rules.size() + " Sequitur rules inferred in "
          + SAXProcessor.timeToString(start.getTime(), end.getTime()));
    }
    else {
      ParallelSAXImplementation ps = new ParallelSAXImplementation();
      SAXRecords parallelRes = ps.process(ts, 2, windowSize, paaSize, alphabetSize, saxNRStrategy,
          normalizationThreshold);
      RePairGrammar rePairGrammar = RePairFactory.buildGrammar(parallelRes);
      rePairGrammar.expandRules();
      rePairGrammar.buildIntervals(parallelRes, ts, windowSize);
      rules = rePairGrammar.toGrammarRulesData();
      Date end = new Date();
      LOGGER.info(rules.size() + " RePair rules inferred in "
          + SAXProcessor.timeToString(start.getTime(), end.getTime()));
    }

    ArrayList<RuleInterval> intervals = RRAIntervalBuilder.fromGrammarRules(rules, ts.length,
        paaSize);
    int[] coverageArray = RRAIntervalBuilder.computePointCoverage(rules, ts.length);

    List<RuleInterval> zeros = filterZeroIntervalsForAnomalySearch(getZeroIntervals(coverageArray),
        paaSize);
    if (zeros.size() > 0) {
      LOGGER.info(
          "found " + zeros.size() + " intervals not covered by rules: " + intervalsToString(zeros));
    }
    else {
      LOGGER.info("the whole timeseries is covered by rule intervals ...");
    }

    // run RRA with this intervals set
    DiscordRecords discords = RRAImplementation.series2RRAAnomalies(ts, discordsToReport, intervals,
        normalizationThreshold);
    Date end = new Date();

    System.out.println(discords.toString() + CR + discords.getSize() + " discords found in "
        + SAXProcessor.timeToString(start.getTime(), end.getTime()) + CR);

    // THE DISCORD SEARCH IS DONE RIGHT HERE
    // BELOW IS THE CODE WHICH WRITES THE CURVE AND THE DISTANCE FILE ON FILESYSTEM
    //
    if (!(outputPrefix.isEmpty())) {

      // write the coverage array
      //
      String currentPath = new File(".").getCanonicalPath();
      BufferedWriter bw = new BufferedWriter( // NOPMD - CloseResource; file frozen pending prior uncommitted fix
          new FileWriter(new File(currentPath + File.separator + outputPrefix + "_coverage.txt")));
      for (int i : coverageArray) {
        bw.write(i + "\n");
      }
      bw.close();

      Collections.sort(intervals, new Comparator<RuleInterval>() {
        public int compare(RuleInterval c1, RuleInterval c2) {
          if (c1.getStart() > c2.getStart()) {
            return 1;
          }
          else if (c1.getStart() < c2.getStart()) {
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

        int ruleStart = ri.getStart();
        int ruleEnd = ruleStart + ri.getLength();
        int window = ruleEnd - ruleStart;

        double[] cw = tp.subseriesByCopy(ts, ruleStart, ruleStart + window);

        double cwNNDist = Double.MAX_VALUE;

        // this effectively finds the furthest hit
        //
        for (int j = 0; j <= ts.length - window; j++) {
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
   * @param nThreshold the z-Normlization threshold value.
   * @throws Exception if error occurs.
   */
  private static void findBruteForce(double[] ts, int windowSize, int discordsToReport,
      double nThreshold) throws Exception {

    LOGGER.info("running brute force algorithm...");

    Date start = new Date();
    DiscordRecords discords = BruteForceDiscordImplementation.series2BruteForceDiscords(ts,
        windowSize, discordsToReport, new LargeWindowAlgorithm(), nThreshold);
    Date end = new Date();

    System.out.println(CR + discords.toString() + CR + discords.getSize() + " discords found in "
        + SAXProcessor.timeToString(start.getTime(), end.getTime()) + CR);
  }

  /**
   * Finds discords using a hash-backed magic array.
   * 
   * @param ts the dataset.
   * @param discordsToReport SAX sliding window size.
   * @param windowSize SAX sliding window size.
   * @param paaSize SAX PAA size.
   * @param alphabetSize SAX alphabet size. * @param saxNRStrategy the NR strategy to use.
   * @param normalizationThreshold SAX normalization threshold.
   * @throws Exception if error occurs.
   */
  private static void findHotSax(double[] ts, int discordsToReport, int windowSize, int paaSize,
      int alphabetSize, NumerosityReductionStrategy saxNRStrategy, double normalizationThreshold)
      throws Exception {

    LOGGER.info("running HOT SAX hashtable-based algorithm...");

    Date start = new Date();
    DiscordRecords discords = HOTSAXImplementation.series2Discords(ts, discordsToReport, windowSize,
        paaSize, alphabetSize, saxNRStrategy, normalizationThreshold);
    Date end = new Date();

    System.out.println(CR + discords.toString() + CR + discords.getSize() + " discords found in "
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

  /** Minimum subsequence length for RRA anomaly candidates (shorter intervals cannot be z-normalized). */
  public static final int MIN_ANOMALY_CANDIDATE_LENGTH = 2;

  /**
   * Returns {@code true} when the sampled-parameter list contains at least one grid point.
   *
   * @param res sampled grid results.
   * @return whether RRA CLI paths can safely read {@code res.get(0)}.
   */
  static boolean hasSampledPoints(ArrayList<SampledPoint> res) {
    return res != null && !res.isEmpty();
  }

  /**
   * Returns sampled points whose pruned coverage meets {@code threshold}. When none qualify,
   * returns a copy of the full pool (same fallback as the GUI parameter guesser).
   *
   * @param res all sampled grid points.
   * @param threshold minimal pruned coverage (typically 0.99).
   * @return filtered pool, never {@code null}; may be empty only when {@code res} is empty.
   */
  static ArrayList<SampledPoint> poolAtCoverageThreshold(ArrayList<SampledPoint> res,
      double threshold) {
    if (!hasSampledPoints(res)) {
      return res;
    }
    ArrayList<SampledPoint> covered = new ArrayList<SampledPoint>();
    for (SampledPoint p : res) {
      if (p.getCoverage() >= threshold) {
        covered.add(p);
      }
    }
    if (covered.isEmpty()) {
      LOGGER.warn("no parameter set reached cover threshold " + threshold + " over " + res.size()
          + " samples; falling back to full sample pool");
      return new ArrayList<SampledPoint>(res);
    }
    return covered;
  }

  /**
   * Returns {@code true} when the interval id marks an uncovered gap (negative sentinel ids).
   *
   * @param interval the candidate interval.
   * @return whether this interval represents uncovered time series.
   */
  public static boolean isUncoveredInterval(RuleInterval interval) {
    return interval.getId() < 0;
  }

  /**
   * Returns {@code true} when the interval is long enough for meaningful distance computation.
   *
   * @param interval the candidate interval.
   * @return whether the interval should participate in RRA search.
   */
  public static boolean isViableAnomalyCandidate(RuleInterval interval) {
    return interval.getLength() >= MIN_ANOMALY_CANDIDATE_LENGTH;
  }

  /**
   * Minimum uncovered-gap length for RRA (matches saxpy / jmotif-R {@code max(2, paa_size)}).
   *
   * @param paaSize PAA size from the SAX discretization.
   * @return minimum run length for synthetic zero-coverage candidates.
   */
  public static int minUncoveredGapLength(int paaSize) {
    return Math.max(MIN_ANOMALY_CANDIDATE_LENGTH, paaSize);
  }

  /**
   * Formats a grammar rule id for GUI display; negative ids are uncovered-gap sentinels.
   *
   * @param ruleId the rule id stored in a discord record.
   * @return a human-readable label.
   */
  public static String formatRuleIdForDisplay(int ruleId) {
    if (ruleId < 0) {
      return "uncovered gap #" + (-ruleId);
    }
    return String.valueOf(ruleId);
  }

  /**
   * Drops uncovered gaps that are too short to analyze (typically one-point boundary artifacts).
   *
   * @param zeroIntervals intervals returned by {@link #getZeroIntervals(int[])}.
   * @return filtered list suitable for RRA.
   */
  public static List<RuleInterval> filterZeroIntervalsForAnomalySearch(List<RuleInterval> zeroIntervals) {
    return filterZeroIntervalsForAnomalySearch(zeroIntervals, MIN_ANOMALY_CANDIDATE_LENGTH);
  }

  /**
   * Drops uncovered gaps shorter than {@link #minUncoveredGapLength(int)}.
   *
   * @param zeroIntervals intervals returned by {@link #getZeroIntervals(int[])}.
   * @param paaSize PAA size from the SAX discretization.
   * @return filtered list suitable for RRA.
   */
  public static List<RuleInterval> filterZeroIntervalsForAnomalySearch(List<RuleInterval> zeroIntervals,
      int paaSize) {
    int minLen = minUncoveredGapLength(paaSize);
    ArrayList<RuleInterval> res = new ArrayList<RuleInterval>(zeroIntervals.size());
    for (RuleInterval interval : zeroIntervals) {
      if (interval.getLength() >= minLen) {
        res.add(interval);
      }
    }
    return res;
  }

  /**
   * Run a quick scan along the timeseries coverage to find a zeroed intervals.
   * 
   * @param coverageArray the coverage to analyze.
   * @return set of zeroed intervals (if found).
   */
  public static List<RuleInterval> getZeroIntervals(int[] coverageArray) {
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
    if (inInterval) {
      res.add(new RuleInterval(intervalsCounter, start, coverageArray.length, 0));
    }
    return res;
  }

  /**
   * Converts a param string to boundaries array.
   * 
   * @param str
   * @return
   */
  static int[] toBoundaries(String str) {
    if (str == null) {
      throw new IllegalArgumentException(
          "the grid boundaries must be 9 whitespace-separated integers, got null");
    }
    String[] split = str.trim().split("\\s+");
    if (split.length != 9) {
      throw new IllegalArgumentException(
          "the grid boundaries must contain exactly 9 whitespace-separated integers, got "
              + split.length + ": \"" + str + "\"");
    }
    int[] res = new int[9];
    for (int i = 0; i < 9; i++) {
      try {
        res[i] = Integer.parseInt(split[i]);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException(
            "grid boundary #" + (i + 1) + " is not an integer: \"" + split[i] + "\"", e);
      }
    }
    return res;
  }
}
