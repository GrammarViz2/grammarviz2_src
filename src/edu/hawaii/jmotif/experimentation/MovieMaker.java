package edu.hawaii.jmotif.experimentation;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.hawaii.jmotif.gi.GrammarRuleRecord;
import edu.hawaii.jmotif.gi.GrammarRules;
import edu.hawaii.jmotif.gi.sequitur.SAXRule;
import edu.hawaii.jmotif.gi.sequitur.SAXTerminal;
import edu.hawaii.jmotif.gi.sequitur.SequiturFactory;
import edu.hawaii.jmotif.logic.RuleInterval;
import edu.hawaii.jmotif.sax.alphabet.NormalAlphabet;
import edu.hawaii.jmotif.sax.datastructures.SAXRecords;
import edu.hawaii.jmotif.timeseries.TSUtils;

public class MovieMaker {

  // locale, charset, logger, etc
  //
  final static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
  private static final Level LOGGING_LEVEL = Level.INFO;
  private static Logger consoleLogger;

  // data file
  //
  private static final String DATA_FILENAME = "data/dutch_power_demand.txt";
  private static final String OUT_PREFIX = "movie/density";

  // params
  //
  private static final int WINDOW_SIZE = 750;
  private static final int PAA_SIZE = 10;
  private static final int A_SIZE = 4;
  private static final double NORMALIZATION_THRESHOLD = 0.5D;

  private static final NormalAlphabet normalA = new NormalAlphabet();

  // data
  //
  private static double[] ts;

  // static block - we instantiate the logger
  //
  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(MovieMaker.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  // the main runnable
  //
  public static void main(String[] args) throws Exception {

    // load the data
    //
    ts = ExperimentUtils.loadData(DATA_FILENAME);

    // we keep discretized data here
    //
    SAXRecords saxFrequencyData = new SAXRecords();

    // getting ready
    //
    SAXRule.reset();
    SAXRule grammar = new SAXRule();
    String previousString = "";

    // scan across the time series extract sub sequences, and convert
    // them to strings
    int stringPosCounter = 0;
    int saveFileCounter = 0;
    for (int i = 0; i < ts.length - (WINDOW_SIZE - 1); i++) {

      if (i % (1000) == 0) {
        consoleLogger.info("processing position " + i + " out of " + ts.length);
      }

      // fix the current subsection
      double[] subSection = Arrays.copyOfRange(ts, i, i + WINDOW_SIZE);

      // Z normalize it
      subSection = TSUtils.optimizedZNorm(subSection, NORMALIZATION_THRESHOLD);

      // perform PAA conversion if needed
      double[] paa = TSUtils.optimizedPaa(subSection, PAA_SIZE);

      // Convert the PAA to a string.
      char[] currentString = TSUtils.ts2String(paa, normalA.getCuts(A_SIZE));

      // NumerosityReduction
      if (!previousString.isEmpty()
          && previousString.equalsIgnoreCase(String.valueOf(currentString))) {
        continue;
      }
      previousString = String.valueOf(currentString);

      // add a terminal to the Sequitur
      //
      grammar.last().insertAfter(new SAXTerminal(String.valueOf(currentString), stringPosCounter));
      grammar.last().p.check();

      // add the word to frequency data structure
      //
      saxFrequencyData.add(currentString, i);

      // save the current rule density curve
      //
      if (i >= WINDOW_SIZE && i < ts.length - WINDOW_SIZE * 2) {

        // index sax words
        //
        saxFrequencyData.buildIndex();

        // convert the grammar to a simple data structure
        //
        GrammarRules rules = grammar.toGrammarRulesData();

        // and populate the coverage
        //
        SequiturFactory.updateRuleIntervals(rules, saxFrequencyData, true,
            Arrays.copyOfRange(ts, i, i + WINDOW_SIZE), WINDOW_SIZE, PAA_SIZE);

        // collect the coverage
        //
        int[] coverageArray = new int[i + WINDOW_SIZE];
        for (GrammarRuleRecord r : rules) {
          if (0 == r.ruleNumber()) {
            continue;
          }
          ArrayList<RuleInterval> arrPos = r.getRuleIntervals();
          for (RuleInterval saxPos : arrPos) {
            int startPos = saxPos.getStartPos();
            int endPos = saxPos.getEndPos();
            for (int j = startPos; j < endPos; j++) {
              coverageArray[j] = coverageArray[j] + 1;
            }
          }
        }

        String outFname = OUT_PREFIX + String.format("%04d", saveFileCounter) + ".csv";
        ExperimentUtils.saveColumn(coverageArray, outFname);

      }

      // moving on...
      //
      stringPosCounter++;
      saveFileCounter++;
    }

    // String cmdLine = "Rscript RCode/movie_plotter.R " + DATA_FILENAME + " " + outFname
    // + " movie/" + String.format("%04d", counter) + ".jpg";
    // consoleLogger.info(cmdLine);
    // Runtime r = Runtime.getRuntime();
    // Process p = r.exec(cmdLine);
    // p.waitFor();
    // BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
    // String line = "";
    //
    // while ((line = b.readLine()) != null) {
    // System.out.println(line);
    // }
    //
    // b.close();

  }
}
