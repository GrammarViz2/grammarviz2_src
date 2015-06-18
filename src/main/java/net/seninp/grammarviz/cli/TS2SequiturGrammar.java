package net.seninp.grammarviz.cli;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import net.seninp.gi.GrammarRuleRecord;
import net.seninp.gi.GrammarRules;
import net.seninp.gi.RuleInterval;
import net.seninp.gi.sequitur.SAXRule;
import net.seninp.gi.sequitur.SequiturFactory;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.algorithm.BitmapParameters;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructures.SAXRecords;
import net.seninp.util.StackTrace;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.beust.jcommander.JCommander;

public class TS2SequiturGrammar {

  private static final String CR = "\n";

  private static TSProcessor tp = new TSProcessor();
  private static NormalAlphabet na = new NormalAlphabet();
  private static SAXProcessor sp = new SAXProcessor();


  // the logger business
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.DEBUG;
  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(TS2SequiturGrammar.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  public static void main(String[] args) throws Exception {

    TS2GrammarParameters params = new TS2GrammarParameters();
    JCommander jct = new JCommander(params, args);

    if (0 == args.length) {
      jct.usage();
    }
    else {
      // get params printed
      //
      StringBuffer sb = new StringBuffer(1024);
      sb.append("GrammarViz2 CLI converter v.1").append(CR);
      sb.append("parameters:").append(CR);

      sb.append("  input file:                  ").append(TS2GrammarParameters.IN_FILE).append(CR);
      sb.append("  output file:                 ").append(TS2GrammarParameters.OUT_FILE).append(CR);
      
      sb.append("  SAX sliding window size:     ").append(TS2GrammarParameters.SAX_WINDOW_SIZE).append(CR);
      sb.append("  SAX PAA size:                ").append(TS2GrammarParameters.SAX_PAA_SIZE).append(CR);
      sb.append("  SAX alphabet size:           ").append(TS2GrammarParameters.SAX_ALPHABET_SIZE).append(CR);
      sb.append("  SAX numerosity reduction:    ").append(TS2GrammarParameters.SAX_NR_STRATEGY).append(CR);
      sb.append("  SAX normalization threshold: ").append(TS2GrammarParameters.SAX_NORM_THRESHOLD).append(CR);

      sb.append(CR);
      System.out.println(sb.toString());
    }
    
    // read the file
    //
    consoleLogger.info("Reading data ...");
    double[] series = tp.readTS(TS2GrammarParameters.IN_FILE, 0);
    consoleLogger.info("read " + series.length + " points from " + BitmapParameters.IN_FILE);

    // discretize
    //
    consoleLogger.info("Performing SAX conversion ...");
    SAXRecords saxData = sp.ts2saxViaWindow(series, TS2GrammarParameters.SAX_WINDOW_SIZE,
        TS2GrammarParameters.SAX_PAA_SIZE, na.getCuts(TS2GrammarParameters.SAX_ALPHABET_SIZE),
        TS2GrammarParameters.SAX_NR_STRATEGY, TS2GrammarParameters.SAX_NORM_THRESHOLD);
    String str = saxData.getSAXString(" ");

    // infer the grammar
    //
    consoleLogger.info("Inferring Sequitur grammar ...");
    SAXRule grammar = SequiturFactory.runSequitur(str);

    // collect stats
    //
    consoleLogger.info("Collecting stats ...");
    GrammarRules rules = grammar.toGrammarRulesData();
    SequiturFactory.updateRuleIntervals(rules, saxData, true, series,
        TS2GrammarParameters.SAX_ALPHABET_SIZE, TS2GrammarParameters.SAX_PAA_SIZE);
    
    // collect stats
    //
    consoleLogger.info("Producing the output ...");
     String currentPath = new File(".").getCanonicalPath();
     String fname = TS2GrammarParameters.OUT_FILE;
    
    boolean fileOpen = false;
    BufferedWriter bw = null;
    try {
      bw = new BufferedWriter(new FileWriter(new File(fname)));
      StringBuffer sb = new StringBuffer();
      sb.append("# filename: ").append(fname).append(CR);
      sb.append("# sliding window: ").append(TS2GrammarParameters.SAX_WINDOW_SIZE).append(CR);
      sb.append("# paa size: ").append(TS2GrammarParameters.SAX_PAA_SIZE).append(CR);
      sb.append("# alphabet size: ").append(TS2GrammarParameters.SAX_ALPHABET_SIZE).append(CR);
      bw.write(sb.toString());
      fileOpen = true;
    }
    catch (IOException e) {
      System.err.print("Encountered an error while writing stats file: \n" + StackTrace.toString(e)
          + "\n");
    }
    
    
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
    
    
    // SAXRecords saxFrequencyData = new SAXRecords();
    //
    // SAXRule.numRules = new AtomicInteger(0);
    // SAXSymbol.theDigrams.clear();
    // SAXSymbol.theSubstituteTable.clear();
    // SAXRule.arrRuleRecords = new ArrayList<GrammarRuleRecord>();
    //
    // SAXRule grammar = new SAXRule();
    //   // String currentPath = new File(".").getCanonicalPath();
     // String fname = currentPath + File.separator + outputPrefix + "_sequitur_grammar_stat.txt";
     //
     // boolean fileOpen = false;
     // BufferedWriter bw = null;
     // try {
     // bw = new BufferedWriter(new FileWriter(new File(fname)));
     // StringBuffer sb = new StringBuffer();
     // sb.append("# filename: ").append(fname).append(CR);
     // sb.append("# sliding window: ").append(saxWindowSize).append(CR);
     // sb.append("# paa size: ").append(saxPaaSize).append(CR);
     // sb.append("# alphabet size: ").append(saxAlphabetSize).append(CR);
     // bw.write(sb.toString());
     // fileOpen = true;
     // }
     // catch (IOException e) {
     // System.err.print("Encountered an error while writing stats file: \n" + StackTrace.toString(e)
     // + "\n");
     // }
    // Date fullStart = new Date();
    //
    // try {
    //
    // String previousString = "";
    //
    // // scan across the time series extract sub sequences, and convert
    // // them to strings
    // int stringPosCounter = 0;
    // for (int i = 0; i < originalTimeSeries.length - (saxWindowSize - 1); i++) {
    //
    // // fix the current subsection
    // double[] subSection = Arrays.copyOfRange(originalTimeSeries, i, i + saxWindowSize);
    //
    // // Z normalize it
    // if (tp.stDev(subSection) > NORMALIZATION_THRESHOLD) {
    // subSection = tp.znorm(subSection, NORMALIZATION_THRESHOLD);
    // }
    //
    // // perform PAA conversion if needed
    // double[] paa = tp.paa(subSection, saxPaaSize);
    //
    // // Convert the PAA to a string.
    // char[] currentString = tp.ts2String(paa, normalA.getCuts(saxAlphabetSize));
    //
    // // NumerosityReduction
    // if (!previousString.isEmpty()) {
    //
    // if ((NumerosityReductionStrategy.MINDIST == numerosityReductionStrategy)
    // && (0.0 == sp.saxMinDist(previousString.toCharArray(), currentString,
    // normalA.getDistanceMatrix(saxAlphabetSize)))) {
    // continue;
    // }
    // else if ((NumerosityReductionStrategy.EXACT == numerosityReductionStrategy)
    // && previousString.equalsIgnoreCase(new String(currentString))) {
    // continue;
    // }
    // }
    //
    // previousString = new String(currentString);
    //
    // grammar.last().insertAfter(new SAXTerminal(new String(currentString), stringPosCounter));
    // grammar.last().p.check();
    //
    // saxFrequencyData.add(currentString, i);
    //
    // stringPosCounter++;
    //
    // }
    //
    // // String res = saxFrequencyData.getSAXString(" ");
    // // res = null;
    //
    // Date end = new Date();
    //
    // System.out.println("Discretized and inferred grammar with Sequitur in  "
    // + SAXProcessor.timeToString(fullStart.getTime(), end.getTime()));
    //
    // Date start = new Date();
    // GrammarRules rules = grammar.toGrammarRulesData();
    //
    // SequiturFactory.updateRuleIntervals(rules, saxFrequencyData, true, originalTimeSeries,
    // saxWindowSize, saxPaaSize);
    // end = new Date();
    //
    // System.out.println("Expanded rules and computed intervals  in  "
    // + String.valueOf(end.getTime() - start.getTime()) + " ms, "
    // + SAXProcessor.timeToString(start.getTime(), end.getTime()));
    //
    // start = new Date();
    //
    // int[] coverageArray = new int[originalTimeSeries.length];
    //
    // for (GrammarRuleRecord r : rules) {
    // if (0 == r.ruleNumber()) {
    // continue;
    // }
    // ArrayList<RuleInterval> arrPos = r.getRuleIntervals();
    // for (RuleInterval saxPos : arrPos) {
    // int startPos = saxPos.getStartPos();
    // int endPos = saxPos.getEndPos();
    // for (int j = startPos; j < endPos; j++) {
    // coverageArray[j] = coverageArray[j] + 1;
    // }
    // }
    // }
    // end = new Date();
    // System.out.println("Computed rule coverage in  "
    // + String.valueOf(end.getTime() - start.getTime()) + " ms, "
    // + SAXProcessor.timeToString(start.getTime(), end.getTime()));
    //
    // System.out.println("Total runtime  " + String.valueOf(end.getTime() - fullStart.getTime())
    // + " ms, " + SAXProcessor.timeToString(fullStart.getTime(), end.getTime()));
    //
    // // write down the coverage array
    // //
    // String currentPath = new File(".").getCanonicalPath();
    // BufferedWriter bw = new BufferedWriter(new FileWriter(new File(currentPath + File.separator
    // + outputPrefix + "_SEQUITUR_density_curve.txt")));
    // for (int c : coverageArray) {
    // bw.write(String.valueOf(c) + "\n");
    // }
    // bw.close();
    //
    // }
    // catch (Exception ex) {
    // ex.printStackTrace();
    // }
    //
    // }
    //
    // @SuppressWarnings("unused")
    // private static String collectMotifStats(SAXRule grammar) throws IOException {
    //
    // // start collecting stats
    // //
    // consoleLogger.info("Collecting stats:");
    // String currentPath = new File(".").getCanonicalPath();
    // String fname = currentPath + File.separator + outputPrefix + "_sequitur_grammar_stat.txt";
    //
    // boolean fileOpen = false;
    // BufferedWriter bw = null;
    // try {
    // bw = new BufferedWriter(new FileWriter(new File(fname)));
    // StringBuffer sb = new StringBuffer();
    // sb.append("# filename: ").append(fname).append(CR);
    // sb.append("# sliding window: ").append(saxWindowSize).append(CR);
    // sb.append("# paa size: ").append(saxPaaSize).append(CR);
    // sb.append("# alphabet size: ").append(saxAlphabetSize).append(CR);
    // bw.write(sb.toString());
    // fileOpen = true;
    // }
    // catch (IOException e) {
    // System.err.print("Encountered an error while writing stats file: \n" + StackTrace.toString(e)
    // + "\n");
    // }
    //
    // GrammarRules rules = grammar.toGrammarRulesData();
    //
    // // SequiturFactory.updateRuleIntervals(rules, saxFrequencyData, true, originalTimeSeries,
    // // saxWindowSize, saxPaaSize);
    //
    // for (GrammarRuleRecord ruleRecord : rules) {
    //
    // StringBuffer sb = new StringBuffer();
    // sb.append("/// ").append(ruleRecord.getRuleName()).append(CR);
    // sb.append(ruleRecord.getRuleName()).append(" -> \'")
    // .append(ruleRecord.getRuleString().trim()).append("\', expanded rule string: \'")
    // .append(ruleRecord.getExpandedRuleString()).append("\'").append(CR);
    //
    // if (!ruleRecord.getOccurrences().isEmpty()) {
    // sb.append("subsequences starts: ")
    // .append(
    // Arrays.toString(ruleRecord.getOccurrences().toArray(
    // new Integer[ruleRecord.getOccurrences().size()]))).append(CR);
    // int[] lengths = new int[ruleRecord.getRuleIntervals().size()];
    // int i = 0;
    // for (RuleInterval r : ruleRecord.getRuleIntervals()) {
    // lengths[i] = r.getEndPos() - r.getStartPos();
    // i++;
    // }
    // sb.append("subsequences lengths: ").append(Arrays.toString(lengths)).append(CR);
    // }
    //
    // sb.append("rule occurrence frequency ").append(ruleRecord.getOccurrences().size()).append(CR);
    // sb.append("rule use frequency ").append(ruleRecord.getRuleUseFrequency()).append(CR);
    // sb.append("min length ").append(ruleRecord.minMaxLengthAsString().split(" - ")[0]).append(CR);
    // sb.append("max length ").append(ruleRecord.minMaxLengthAsString().split(" - ")[1]).append(CR);
    // sb.append("mean length ").append(ruleRecord.getMeanLength()).append(CR);
    //
    // if (fileOpen) {
    // try {
    // bw.write(sb.toString());
    // }
    // catch (IOException e) {
    // System.err.print("Encountered an error while writing stats file: \n"
    // + StackTrace.toString(e) + "\n");
    // }
    // }
    // }
    //
    // // try to write stats into the file
    // if (fileOpen) {
    // try {
    // bw.close();
    // }
    // catch (IOException e) {
    // System.err.print("Encountered an error while writing stats file: \n"
    // + StackTrace.toString(e) + "\n");
    // }
    // }
    //
    // return null;
    // }
    //
 
  }
}
