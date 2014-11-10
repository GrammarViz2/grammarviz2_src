package edu.hawaii.jmotif.gi.sequitur;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.hawaii.jmotif.gi.GrammarRuleRecord;
import edu.hawaii.jmotif.gi.GrammarRules;
import edu.hawaii.jmotif.logic.RuleInterval;
import edu.hawaii.jmotif.sax.NumerosityReductionStrategy;
import edu.hawaii.jmotif.sax.SAXFactory;
import edu.hawaii.jmotif.sax.alphabet.NormalAlphabet;
import edu.hawaii.jmotif.sax.datastructures.SAXRecords;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.timeseries.TSUtils;

/**
 * Sort of a stand-alone factory to digesting strings with Sequitur.
 * 
 * @author psenin
 * 
 */
public final class SequiturFactory {

  /** Chunking/Sliding switch action key. */
  protected static final String USE_SLIDING_WINDOW_ACTION_KEY = "sliding_window_key";

  private static final double NORMALIZATION_THRESHOLD = 0.5D;

  private static final NormalAlphabet normalA = new NormalAlphabet();

  // logging stuff
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.INFO;
  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(SequiturFactory.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  /**
   * Disabling the constructor.
   */
  private SequiturFactory() {
    assert true;
  }

  /**
   * Digests a string of symbols separated by space.
   * 
   * @param inputString The string to digest. Symbols expected to be separated by space.
   * 
   * @return The top rule handler.
   * @throws TSException
   */
  public static SAXRule runSequitur(String inputString) throws TSException {

    consoleLogger.trace("digesting the string " + inputString);

    // clear global collections
    //
    SAXRule.numRules = new AtomicInteger(0);
    SAXSymbol.theDigrams.clear();
    SAXSymbol.theSubstituteTable.clear();

    // init the top-level rule
    //
    SAXRule resRule = new SAXRule();

    // tokenize the input string
    //
    StringTokenizer st = new StringTokenizer(inputString, " ");

    // while there are tokens
    int currentPosition = 0;
    while (st.hasMoreTokens()) {

      String token = st.nextToken();
      // System.out.println("  processing the token " + token);

      // extract next token
      SAXTerminal symbol = new SAXTerminal(token, currentPosition);

      // append to the end of the current sequitur string
      // ... As each new input symbol is observed, append it to rule S....
      resRule.last().insertAfter(symbol);

      // once appended, check if the resulting digram is new or recurrent
      //
      // ... Each time a link is made between two symbols if the new digram is repeated elsewhere
      // and the repetitions do not overlap, if the other occurrence is a complete rule,
      // replace the new digram with the non-terminal symbol that heads the rule,
      // otherwise,form a new rule and replace both digrams with the new non-terminal symbol
      // otherwise, insert the digram into the index...
      resRule.last().p.check();

      currentPosition++;

      // consoleLogger.debug("Current grammar:\n" + SAXRule.getRules());
    }

    return resRule;
  }

  /**
   * Digests a string of symbols separated by space.
   * 
   * @param string The string to digest. Symbols expected to be separated by space.
   * 
   * @return The top rule handler.
   * @throws TSException
   */
  public static SAXRule runSequiturWithEditDistanceThreshold(String string, Integer alphabetSize,
      Integer threshold) throws TSException {

    consoleLogger.trace("digesting the string " + string);

    // clear global collections
    //
    SAXRule.numRules = new AtomicInteger(0);
    SAXSymbol.theDigrams.clear();
    SAXSymbol.theSubstituteTable.clear();

    // init the top-level rule
    //
    SAXRule resRule = new SAXRule();

    // tokenize the input string
    //
    StringTokenizer st = new StringTokenizer(string, " ");

    // while there are tokens
    int currentPosition = 0;
    while (st.hasMoreTokens()) {

      String token = st.nextToken();
      // System.out.println("  processing the token " + token);

      if (null != alphabetSize && null != threshold) {
        //
        boolean merged = false;
        for (String str : SAXSymbol.theSubstituteTable.keySet()) {
          double dist = SAXFactory.saxMinDist(str.toCharArray(), token.toCharArray(),
              normalA.getDistanceMatrix(alphabetSize));
          if (dist < threshold) {
            merged = true;
            SAXSymbol.theSubstituteTable.get(str).put(token.substring(0), currentPosition);
            token = str;
          }
        }
        if (!(merged)) {
          SAXSymbol.theSubstituteTable.put(token, new Hashtable<String, Integer>());
        }
      }

      // extract next token
      SAXTerminal symbol = new SAXTerminal(token, currentPosition);

      // append to the end of the current sequitur string
      // ... As each new input symbol is observed, append it to rule S....
      resRule.last().insertAfter(symbol);

      // once appended, check if the resulting digram is new or recurrent
      //
      // ... Each time a link is made between two symbols if the new digram is repeated elsewhere
      // and the repetitions do not overlap, if the other occurrence is a complete rule,
      // replace the new digram with the non-terminal symbol that heads the rule,
      // otherwise,form a new rule and replace both digrams with the new non-terminal symbol
      // otherwise, insert the digram into the index...
      resRule.last().p.check();
      currentPosition++;

      consoleLogger.trace("Current grammar:\n" + SAXRule.getRules());
    }

    return resRule;
  }

  /**
   * Recovers start and stop coordinates of a rule subsequences.
   * 
   * @param ruleIdx The rule index.
   * @return The array of all intervals corresponding to this rule.
   */
  public static ArrayList<RuleInterval> getRulePositionsByRuleNum(int ruleIdx, SAXRule grammar,
      SAXRecords saxFrequencyData, double[] originalTimeSeries, int saxWindowSize) {

    // this will be the result
    ArrayList<RuleInterval> resultIntervals = new ArrayList<RuleInterval>();

    // the rule container
    GrammarRuleRecord ruleContainer = grammar.getRuleRecords().get(ruleIdx);

    // the original indexes of all SAX words
    ArrayList<Integer> saxWordsIndexes = new ArrayList<Integer>(saxFrequencyData.getAllIndices());

    // debug printout
    consoleLogger.trace("Expanded rule: \"" + ruleContainer.getExpandedRuleString() + '\"');
    consoleLogger.trace("Indexes: " + ruleContainer.getOccurrences());

    // array of all words of this expanded rule
    String[] expandedRuleSplit = ruleContainer.getExpandedRuleString().trim().split(" ");

    for (Integer currentIndex : ruleContainer.getOccurrences()) {

      // System.out.println("Index: " + currentIndex);
      String extractedStr = "";
      int[] extractedPositions = new int[expandedRuleSplit.length];
      for (int i = 0; i < expandedRuleSplit.length; i++) {
        consoleLogger.trace("currentIndex " + currentIndex + ", i: " + i);
        extractedStr = extractedStr.concat(" ").concat(
            String.valueOf(saxFrequencyData.getByIndex(saxWordsIndexes.get(currentIndex + i))
                .getPayload()));
        extractedPositions[i] = saxWordsIndexes.get(currentIndex + i);
      }
      // System.out.println("Recovered string: " + extractedStr);
      // System.out.println("Recovered positions: " + Arrays.toString(extractedPositions));

      int start = saxWordsIndexes.get(currentIndex);
      int end = -1;
      // need to care about bouncing beyond the all SAX words index array
      if ((currentIndex + expandedRuleSplit.length) >= saxWordsIndexes.size()) {
        // if we at the last index - then it's easy - end is the timeseries end
        end = originalTimeSeries.length - 1;
      }
      else {
        // if we OK with indexes, the Rule subsequence end is the start of the very next SAX word
        // after the kast in this expanded rule
        end = saxWordsIndexes.get(currentIndex + expandedRuleSplit.length) - 1 + saxWindowSize;
      }
      // save it
      resultIntervals.add(new RuleInterval(start, end));
    }

    return resultIntervals;
  }

  public static int[] series2RulesDensity(double[] originalTimeSeries, int saxWindowSize,
      int saxPaaSize, int saxAlphabetSize) throws TSException, IOException {

    SAXRecords saxFrequencyData = new SAXRecords();

    SAXRule.numRules = new AtomicInteger(0);
    SAXSymbol.theDigrams.clear();
    SAXSymbol.theSubstituteTable.clear();
    SAXRule.arrRuleRecords = new ArrayList<GrammarRuleRecord>();

    SAXRule grammar = new SAXRule();

    String previousString = "";

    // scan across the time series extract sub sequences, and convert
    // them to strings
    int stringPosCounter = 0;
    for (int i = 0; i < originalTimeSeries.length - (saxWindowSize - 1); i++) {

      // fix the current subsection
      double[] subSection = Arrays.copyOfRange(originalTimeSeries, i, i + saxWindowSize);

      // Z normalize it
      subSection = TSUtils.optimizedZNorm(subSection, NORMALIZATION_THRESHOLD);

      // perform PAA conversion if needed
      double[] paa = TSUtils.optimizedPaa(subSection, saxPaaSize);

      // Convert the PAA to a string.
      char[] currentString = TSUtils.ts2String(paa, normalA.getCuts(saxAlphabetSize));

      // NumerosityReduction
      if (!previousString.isEmpty()
          && previousString.equalsIgnoreCase(String.valueOf(currentString))) {
        continue;
      }

      previousString = String.valueOf(currentString);

      grammar.last().insertAfter(new SAXTerminal(String.valueOf(currentString), stringPosCounter));
      grammar.last().p.check();

      saxFrequencyData.add(currentString, i);

      stringPosCounter++;

    }

    saxFrequencyData.buildIndex();

    GrammarRules rules = grammar.toGrammarRulesData();

    SequiturFactory.updateRuleIntervals(rules, saxFrequencyData, true, originalTimeSeries,
        saxWindowSize, saxPaaSize);

    int[] coverageArray = new int[originalTimeSeries.length];

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

    return coverageArray;
  }

  /**
   * Translates the time series into the set of rules.
   * 
   * @param timeseries
   * @param saxWindowSize
   * @param saxPAASize
   * @param saxAlphabetSize
   * @param numerosityReductionStrategy
   * @param normalizationThreshold
   * @return
   * @throws TSException
   * @throws IOException
   */
  public static GrammarRules series2SequiturRules(double[] timeseries, int saxWindowSize, int saxPAASize,
      int saxAlphabetSize, NumerosityReductionStrategy numerosityReductionStrategy,
      double normalizationThreshold) throws TSException, IOException {

    consoleLogger.debug("Discretizing time series...");
    SAXRecords saxFrequencyData = discretize(timeseries, numerosityReductionStrategy,
        saxWindowSize, saxPAASize, saxAlphabetSize, normalizationThreshold);

    consoleLogger.debug("Inferring the grammar...");
    // this is a string we are about to feed into Sequitur
    //
    String saxDisplayString = saxFrequencyData.getSAXString(" ");
    // String[] split = saxDisplayString.split(" ");
    // System.out.println("*** " + split[21] + " " + split[22] + " " + split[23]);

    // BufferedWriter bw = new BufferedWriter(new FileWriter(new File("rules_stat.txt")));

    // reset Sequitur structures
    SAXRule.numRules = new AtomicInteger(0);
    SAXSymbol.theDigrams.clear();

    SAXRule grammar = new SAXRule();
    SAXRule.arrRuleRecords = new ArrayList<GrammarRuleRecord>();

    StringTokenizer st = new StringTokenizer(saxDisplayString, " ");
    int currentPosition = 0;
    while (st.hasMoreTokens()) {
      grammar.last().insertAfter(new SAXTerminal(st.nextToken(), currentPosition));
      grammar.last().p.check();
      // bw.write(currentPosition + "," + SAXSymbol.theDigrams.size() + "," +
      // SAXRule.theRules.size()
      // + "\n");
      currentPosition++;
    }

    // bw.close();
    consoleLogger.debug("Collecting the grammar rules statistics and expanding the rules...");
    GrammarRules rules = grammar.toGrammarRulesData();

    consoleLogger.debug("Mapping expanded rules to time-series intervals...");
    SequiturFactory.updateRuleIntervals(rules, saxFrequencyData, true, timeseries, saxWindowSize,
        saxPAASize);

    return rules;

  }

  public static GrammarRules series2RulesWithLog(double[] timeseries, int saxWindowSize,
      int saxPAASize, int saxAlphabetSize, double normalizationThreshold, String prefix)
      throws TSException, IOException {

    consoleLogger.debug("Discretizing time series...");
    SAXRecords saxFrequencyData = discretize(timeseries, NumerosityReductionStrategy.EXACT,
        saxWindowSize, saxPAASize, saxAlphabetSize, normalizationThreshold);

    consoleLogger.debug("Inferring the grammar...");
    // this is a string we are about to feed into Sequitur
    //
    String saxDisplayString = saxFrequencyData.getSAXString(" ");
    // String[] split = saxDisplayString.split(" ");
    // System.out.println("*** " + split[21] + " " + split[22] + " " + split[23]);

    BufferedWriter bw = new BufferedWriter(new FileWriter(new File(prefix + "_rules_stat.txt")));

    // reset Sequitur structures
    SAXRule.numRules = new AtomicInteger(0);
    SAXSymbol.theDigrams.clear();

    SAXRule grammar = new SAXRule();
    SAXRule.arrRuleRecords = new ArrayList<GrammarRuleRecord>();

    StringTokenizer st = new StringTokenizer(saxDisplayString, " ");
    int currentPosition = 0;
    while (st.hasMoreTokens()) {
      grammar.last().insertAfter(new SAXTerminal(st.nextToken(), currentPosition));
      grammar.last().p.check();
      bw.write(currentPosition + "," + SAXSymbol.theDigrams.size() + "," + SAXRule.theRules.size()
          + "\n");
      currentPosition++;
    }
    bw.close();

    consoleLogger.debug("Collecting the grammar rules statistics and expanding the rules...");
    GrammarRules rules = grammar.toGrammarRulesData();

    consoleLogger.debug("Mapping expanded rules to time-series intervals...");
    SequiturFactory.updateRuleIntervals(rules, saxFrequencyData, true, timeseries, saxWindowSize,
        saxPAASize);

    return rules;

  }

  /**
   * Performs discretization.
   * 
   * @param timeseries
   * @param numerosityReductionStrategy
   * @param saxWindowSize
   * @param saxPAASize
   * @param saxAlphabetSize
   * @param normalizationThreshold
   * @return
   * @throws TSException
   */
  public static SAXRecords discretize(double[] timeseries,
      NumerosityReductionStrategy numerosityReductionStrategy, int saxWindowSize, int saxPAASize,
      int saxAlphabetSize, double normalizationThreshold) throws TSException {

    SAXRecords saxFrequencyData = new SAXRecords();

    // scan across the time series extract sub sequences, and convert
    // them to strings`
    char[] previousString = null;
    for (int i = 0; i < timeseries.length - (saxWindowSize - 1); i++) {

      // fix the current subsection
      double[] subSection = Arrays.copyOfRange(timeseries, i, i + saxWindowSize);

      // Z normalize it
      subSection = TSUtils.optimizedZNorm(subSection, normalizationThreshold);

      // perform PAA conversion if needed
      double[] paa = TSUtils.optimizedPaa(subSection, saxPAASize);

      // Convert the PAA to a string.
      char[] currentString = TSUtils.ts2String(paa, normalA.getCuts(saxAlphabetSize));

      if (NumerosityReductionStrategy.EXACT.equals(numerosityReductionStrategy)
          && Arrays.equals(previousString, currentString)) {
        // NumerosityReduction
        continue;
      }
      else if ((null != previousString)
          && NumerosityReductionStrategy.MINDIST.equals(numerosityReductionStrategy)) {
        double dist = SAXFactory.saxMinDist(previousString, currentString,
            normalA.getDistanceMatrix(saxAlphabetSize));
        if (0.0D == dist) {
          continue;
        }
      }

      previousString = currentString;

      saxFrequencyData.add(currentString, i);
    }

    return saxFrequencyData;
  }

  public static void updateRuleIntervals(GrammarRules rules, SAXRecords saxFrequencyData,
      boolean slidingWindowOn, double[] originalTimeSeries, int saxWindowSize, int saxPAASize) {

    // the original indexes of all SAX words
    ArrayList<Integer> saxWordsIndexes = new ArrayList<Integer>(saxFrequencyData.getAllIndices());

    for (GrammarRuleRecord ruleContainer : rules) {

      // here we construct the array of rule intervals
      ArrayList<RuleInterval> resultIntervals = new ArrayList<RuleInterval>();

      // array of all words of this rule expanded form
      // String[] expandedRuleSplit = ruleContainer.getExpandedRuleString().trim().split(" ");
      int expandedRuleLength = countSpaces(ruleContainer.getExpandedRuleString());

      // the auxiliary array that keeps lengths of all rule occurrences
      int[] lengths = new int[ruleContainer.getOccurrences().size()];
      int lengthCounter = 0;

      // iterate over all occurrences of this rule
      // the currentIndex here is the position of the rule in the input string
      //
      for (Integer currentIndex : ruleContainer.getOccurrences()) {

        // System.out.println("Index: " + currentIndex);
        // String extractedStr = "";

        // what we do here is to extract the positions of sax words in the real time-series
        // by using their positions at the input string
        //
        // int[] extractedPositions = new int[expandedRuleSplit.length];
        // for (int i = 0; i < expandedRuleSplit.length; i++) {
        // extractedStr = extractedStr.concat(" ").concat(
        // saxWordsToIndexesMap.get(saxWordsIndexes.get(currentIndex + i)));
        // extractedPositions[i] = saxWordsIndexes.get(currentIndex + i);
        // }

        int startPos = saxWordsIndexes.get(currentIndex);
        int endPos = -1;
        if ((currentIndex + expandedRuleLength) >= saxWordsIndexes.size()) {
          endPos = originalTimeSeries.length - 1;
        }
        else {
          if (slidingWindowOn) {
            endPos = saxWordsIndexes.get(currentIndex + expandedRuleLength) + saxWindowSize - 1;
          }
          else {
            double step = (double) originalTimeSeries.length / (double) saxPAASize;
            endPos = Long.valueOf(
                Math.round(saxWordsIndexes.get(currentIndex + expandedRuleLength) * step))
                .intValue();
          }
        }

        resultIntervals.add(new RuleInterval(startPos, endPos));

        lengths[lengthCounter] = endPos - startPos;
        lengthCounter++;
      }
      if (0 == ruleContainer.getRuleNumber()) {
        resultIntervals.add(new RuleInterval(0, originalTimeSeries.length - 1));
        lengths = new int[1];
        lengths[0] = originalTimeSeries.length;
      }
      ruleContainer.setRuleIntervals(resultIntervals);
      ruleContainer.setMeanLength(TSUtils.mean(lengths));
      ruleContainer.setMinMaxLength(lengths);
    }

  }

  public static void updateRuleIntervals(GrammarRules rules, SAXRecords saxFrequencyData,
      boolean slidingWindowOn, double[] originalTimeSeries, Integer saxWindowSize,
      Integer saxPAASize) {

    // the original indexes of all SAX words
    ArrayList<Integer> saxWordsIndexes = new ArrayList<Integer>(saxFrequencyData.getIndexes()
        .size());
    saxWordsIndexes.addAll(saxFrequencyData.getIndexes());

    for (GrammarRuleRecord ruleContainer : rules) {

      // here we construct the array of rule intervals
      ArrayList<RuleInterval> resultIntervals = new ArrayList<RuleInterval>();

      // array of all words of this rule expanded form
      // String[] expandedRuleSplit = ruleContainer.getExpandedRuleString().trim().split(" ");
      int expandedRuleLength = countSpaces(ruleContainer.getExpandedRuleString());

      // the auxiliary array that keeps lengths of all rule occurrences
      int[] lengths = new int[ruleContainer.getOccurrences().size()];
      int lengthCounter = 0;

      // iterate over all occurrences of this rule
      // the currentIndex here is the position of the rule in the input string
      //
      for (Integer currentIndex : ruleContainer.getOccurrences()) {

        int startPos = saxWordsIndexes.get(currentIndex);
        int endPos = -1;
        if ((currentIndex + expandedRuleLength) >= saxWordsIndexes.size()) {
          endPos = originalTimeSeries.length - 1;
        }
        else {
          if (slidingWindowOn) {
            endPos = saxWordsIndexes.get(currentIndex + expandedRuleLength) + saxWindowSize - 1;
          }
          else {
            double step = (double) originalTimeSeries.length / (double) saxPAASize;
            endPos = Long.valueOf(
                Math.round(saxWordsIndexes.get(currentIndex + expandedRuleLength) * step))
                .intValue();
          }
        }

        resultIntervals.add(new RuleInterval(startPos, endPos));

        lengths[lengthCounter] = endPos - startPos;
        lengthCounter++;
      }
      if (0 == ruleContainer.getRuleNumber()) {
        resultIntervals.add(new RuleInterval(0, originalTimeSeries.length - 1));
        lengths = new int[1];
        lengths[0] = originalTimeSeries.length;
      }
      ruleContainer.setRuleIntervals(resultIntervals);
      ruleContainer.setMeanLength(TSUtils.mean(lengths));
      ruleContainer.setMinMaxLength(lengths);
    }

  }

  /**
   * Counts spaces in the string.
   * 
   * @param str The string.
   * @return The number of spaces.
   */
  private static int countSpaces(String str) {
    int counter = 0;
    for (int i = 0; i < str.length(); i++) {
      if (str.charAt(i) == ' ') {
        counter++;
      }
    }
    return counter;
  }

}
