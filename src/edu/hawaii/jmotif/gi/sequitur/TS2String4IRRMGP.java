package edu.hawaii.jmotif.gi.sequitur;

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
import java.util.HashMap;
import java.util.Map.Entry;
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
import edu.hawaii.jmotif.sax.alphabet.Alphabet;
import edu.hawaii.jmotif.sax.alphabet.NormalAlphabet;
import edu.hawaii.jmotif.sax.datastructures.SAXRecords;
import edu.hawaii.jmotif.sequitur.model.SequiturModel;
import edu.hawaii.jmotif.timeseries.TSUtils;
import edu.hawaii.jmotif.util.StackTrace;

public class TS2String4IRRMGP {

  private final static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  private static final double NORMALIZATION_THRESHOLD = 0.5;

  private static final NumerosityReductionStrategy numerosityReductionStrategy = NumerosityReductionStrategy.EXACT;

  private static final Object CR = "\n";

  /** The data filename. */
  private static String dataFileName;

  // the logger business
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.DEBUG;

  private static SAXRecords saxFrequencyData;

  private static double[] originalTimeSeries;

  private static Integer saxWindowSize;

  private static Integer saxPaaSize;

  private static Integer saxAlphabetSize;

  private static Alphabet normalA = new NormalAlphabet();

  private static String outputPrefix;

  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(SequiturModel.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  public static void main(String[] args) {

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

    /**
     * Builds (1) SAX frequency data structure respecting all the parameters, (2) runs Sequitur.
     */

    saxFrequencyData = new SAXRecords();

    try {

      String previousString = "";

      // scan across the time series extract sub sequences, and convert
      // them to strings
      for (int i = 0; i < originalTimeSeries.length - (saxWindowSize - 1); i++) {

        // fix the current subsection
        double[] subSection = Arrays.copyOfRange(originalTimeSeries, i, i + saxWindowSize);

        // Z normalize it
        if (TSUtils.optimizedStDev(subSection) > NORMALIZATION_THRESHOLD) {
          subSection = TSUtils.optimizedZNorm(subSection, NORMALIZATION_THRESHOLD);
        }

        // perform PAA conversion if needed
        double[] paa = TSUtils.optimizedPaa(subSection, saxPaaSize);

        // Convert the PAA to a string.
        char[] currentString = TSUtils.ts2String(paa, normalA.getCuts(saxAlphabetSize));

        // NumerosityReduction
        if (!previousString.isEmpty()) {

          if ((NumerosityReductionStrategy.MINDIST == numerosityReductionStrategy)
              && (0.0 == SAXFactory.saxMinDist(previousString.toCharArray(), currentString,
                  normalA.getDistanceMatrix(saxAlphabetSize)))) {
            continue;
          }
          else if ((NumerosityReductionStrategy.EXACT == numerosityReductionStrategy)
              && previousString.equalsIgnoreCase(new String(currentString))) {
            continue;
          }
        }

        previousString = new String(currentString);
        saxFrequencyData.add(currentString, i);
      }

      // get a whole series representation as SAX words
      //
      String saxDisplayString = saxFrequencyData.getSAXString(" ");

      String currentPath = new File(".").getCanonicalPath();
      BufferedWriter bw = new BufferedWriter(new FileWriter(new File(currentPath + File.separator
          + outputPrefix + "_sequitur_str.txt")));
      StringTokenizer st = new StringTokenizer(saxDisplayString, " ");
      int currentNum = 1;
      HashMap<String, Integer> dictionary = new HashMap<String, Integer>();
      while (st.hasMoreTokens()) {
        String token = st.nextToken();
        Integer key = dictionary.get(token);
        if (null == key) {
          key = currentNum;
          dictionary.put(token, currentNum);
          currentNum = currentNum + 1;
        }
        bw.write(key + " ");
      }
      bw.close();

      bw = new BufferedWriter(new FileWriter(new File(currentPath + File.separator + outputPrefix
          + "_sequitur_dictionary.txt")));
      for (Entry<String, Integer> e : dictionary.entrySet()) {
        bw.write(e.getKey() + " " + e.getValue() + "\n");
      }
      bw.close();

      // writing the frequency data
      //
      bw = new BufferedWriter(new FileWriter(new File(currentPath + File.separator + outputPrefix
          + "_frequencyData.ser")));
      // TODO: not implemented
      // saxFrequencyData.save(bw);
      bw.close();

      // running sequitur
      //
      //
      SAXRule.numRules = new AtomicInteger(0);
      SAXSymbol.theDigrams.clear();
      SAXSymbol.theSubstituteTable.clear();

      SAXRule grammar = new SAXRule();
      // SAXRule.arrayRuleStrings = new ArrayList<String>();
      SAXRule.arrRuleRecords = new ArrayList<GrammarRuleRecord>();

      StringTokenizer tokenizer = new StringTokenizer(saxDisplayString, " ");

      int currentPosition = 0;
      while (tokenizer.hasMoreTokens()) {

        String token = tokenizer.nextToken();

        grammar.last().insertAfter(new SAXTerminal(token, currentPosition));
        grammar.last().p.check();
        currentPosition++;
      }

      collectMotifStats(grammar);

    }
    catch (Exception ex) {
      ex.printStackTrace();
    }

  }

  private static String collectMotifStats(SAXRule grammar) throws IOException {

    // start collecting stats
    //
    consoleLogger.info("Collecting stats:");
    String currentPath = new File(".").getCanonicalPath();
    String fname = currentPath + File.separator + outputPrefix + "_sequitur_grammar_stat.txt";

    boolean fileOpen = false;
    BufferedWriter bw = null;
    try {
      bw = new BufferedWriter(new FileWriter(new File(fname)));
      StringBuffer sb = new StringBuffer();
      sb.append("# filename: ").append(fname).append(CR);
      sb.append("# sliding window: ").append(saxWindowSize).append(CR);
      sb.append("# paa size: ").append(saxPaaSize).append(CR);
      sb.append("# alphabet size: ").append(saxAlphabetSize).append(CR);
      bw.write(sb.toString());
      fileOpen = true;
    }
    catch (IOException e) {
      System.err.print("Encountered an error while writing stats file: \n" + StackTrace.toString(e)
          + "\n");
    }

    GrammarRules rules = grammar.toGrammarRulesData();

    SequiturFactory.updateRuleIntervals(rules, saxFrequencyData, true, originalTimeSeries,
        saxWindowSize, saxPaaSize);

    for (GrammarRuleRecord ruleRecord : rules) {

      StringBuffer sb = new StringBuffer();
      sb.append("/// ").append(ruleRecord.getRuleName()).append(CR);
      sb.append(ruleRecord.getRuleName()).append(" -> \'")
          .append(ruleRecord.getRuleString().trim()).append("\', expanded rule string: \'")
          .append(ruleRecord.getExpandedRuleString()).append("\'").append(CR);

      if (!ruleRecord.getOccurrences().isEmpty()) {
        sb.append("subsequences starts: ")
            .append(
                Arrays.toString(ruleRecord.getOccurrences().toArray(
                    new Integer[ruleRecord.getOccurrences().size()]))).append(CR);
        int[] lengths = new int[ruleRecord.getRuleIntervals().size()];
        int i = 0;
        for (RuleInterval r : ruleRecord.getRuleIntervals()) {
          lengths[i] = r.getEndPos() - r.getStartPos();
          i++;
        }
        sb.append("subsequences lengths: ").append(Arrays.toString(lengths)).append(CR);
      }

      sb.append("rule occurrence frequency ").append(ruleRecord.getOccurrences().size()).append(CR);
      sb.append("rule use frequency ").append(ruleRecord.getRuleUseFrequency()).append(CR);
      sb.append("min length ").append(ruleRecord.minMaxLengthAsString().split(" - ")[0]).append(CR);
      sb.append("max length ").append(ruleRecord.minMaxLengthAsString().split(" - ")[1]).append(CR);
      sb.append("mean length ").append(ruleRecord.getMeanLength()).append(CR);

      if (fileOpen) {
        try {
          bw.write(sb.toString());
        }
        catch (IOException e) {
          System.err.print("Encountered an error while writing stats file: \n"
              + StackTrace.toString(e) + "\n");
        }
      }
    }

    // try to write stats into the file
    if (fileOpen) {
      try {
        bw.close();
      }
      catch (IOException e) {
        System.err.print("Encountered an error while writing stats file: \n"
            + StackTrace.toString(e) + "\n");
      }
    }

    return null;
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
