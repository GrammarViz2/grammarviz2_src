package net.seninp.grammarviz.cli;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import net.seninp.gi.sequitur.SAXRule;
import net.seninp.gi.sequitur.SequiturFactory;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;
import net.seninp.util.StackTrace;

public class TS2Grammar {

  private static final String CR = "\n";

  private static TSProcessor tp = new TSProcessor();
  private static NormalAlphabet na = new NormalAlphabet();
  private static SAXProcessor sp = new SAXProcessor();

  // the logger business
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.DEBUG;

  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(TS2Grammar.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  public static void main(String[] args) throws Exception {

    TS2GrammarParameters params = new TS2GrammarParameters();
    JCommander jct = new JCommander(params, args);

    if (0 == args.length) {
      jct.usage();
      System.exit(10);
    }
    
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

    sb.append("  GI implementation:           ")
        .append(TS2GrammarParameters.GI_ALGORITHM_IMPLEMENTATION).append(CR);

    sb.append(CR);
    System.out.println(sb.toString());

    // read the file
    //
    consoleLogger.info("Reading data ...");
    double[] series = tp.readTS(TS2GrammarParameters.IN_FILE, 0);
    consoleLogger.info("read " + series.length + " points from " + TS2GrammarParameters.IN_FILE);

    // discretize
    //
    consoleLogger.info("Performing SAX conversion ...");
    SAXRecords saxData = sp.ts2saxViaWindow(series, TS2GrammarParameters.SAX_WINDOW_SIZE,
        TS2GrammarParameters.SAX_PAA_SIZE, na.getCuts(TS2GrammarParameters.SAX_ALPHABET_SIZE),
        TS2GrammarParameters.SAX_NR_STRATEGY, TS2GrammarParameters.SAX_NORM_THRESHOLD);

    String discretizedTS = saxData.getSAXString(" ");

    // infer the grammar
    //
    GrammarRules rules = null;
    if (GIAlgorithm.SEQUITUR == TS2GrammarParameters.GI_ALGORITHM_IMPLEMENTATION) {
      consoleLogger.info("Inferring Sequitur grammar ...");
      SAXRule grammar = SequiturFactory.runSequitur(discretizedTS);
      rules = grammar.toGrammarRulesData();
      SequiturFactory.updateRuleIntervals(rules, saxData, true, series,
          TS2GrammarParameters.SAX_WINDOW_SIZE, TS2GrammarParameters.SAX_PAA_SIZE);
    }
    else if (GIAlgorithm.REPAIR == TS2GrammarParameters.GI_ALGORITHM_IMPLEMENTATION) {
      consoleLogger.info("Inferring RePair grammar ...");
      RePairGrammar grammar = RePairFactory.buildGrammar(discretizedTS);
      grammar.expandRules();
      grammar.buildIntervals(saxData, series, TS2GrammarParameters.SAX_WINDOW_SIZE);
      rules = grammar.toGrammarRulesData();
    }

    // collect stats
    //
    consoleLogger.info("Collecting stats ...");

    // produce the output
    //
    consoleLogger.info("Producing the output ...");
    String fname = TS2GrammarParameters.OUT_FILE;

    boolean fileOpen = false;
    BufferedWriter bw = null;
    try {
      bw = new BufferedWriter(new FileWriter(new File(fname)));
      fileOpen = true;
      bw.write("#" + sb.toString().replaceAll("\n", "\n#"));
    }
    catch (IOException e) {
      System.err.print(
          "Encountered an error while writing stats file: \n" + StackTrace.toString(e) + "\n");
    }

    // collect stats object
    //
    GrammarStats grammarStats = new GrammarStats();

    // each rule stats
    for (GrammarRuleRecord ruleRecord : rules) {

      // make sure this processed by the stats object
      grammarStats.process(ruleRecord);

      sb = new StringBuffer();
      sb.append("/// ").append(ruleRecord.getRuleName()).append(CR);
      sb.append(ruleRecord.getRuleName()).append(" -> \'").append(ruleRecord.getRuleString().trim())
          .append("\', expanded rule string: \'").append(ruleRecord.getExpandedRuleString())
          .append("\'").append(CR);

      if (!ruleRecord.getOccurrences().isEmpty()) {

        ArrayList<RuleInterval> intervals = ruleRecord.getRuleIntervals();
        int[] starts = new int[intervals.size()];
        int[] lengths = new int[intervals.size()];
        for (int i = 0; i < intervals.size(); i++) {
          starts[i] = intervals.get(i).getStart();
          lengths[i] = intervals.get(i).getEnd() - intervals.get(i).getStart();
        }

        sb.append("subsequence starts: ").append(Arrays.toString(starts)).append(CR);
        sb.append("subsequence lengths: ").append(Arrays.toString(lengths)).append(CR);
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
          System.err.print(
              "Encountered an error while writing stats file: \n" + StackTrace.toString(e) + "\n");
        }
      }
    }

    // close the file
    //
    if (fileOpen) {
      try {
        bw.write(grammarStats.toString());
        bw.close();
      }
      catch (IOException e) {
        System.err.print(
            "Encountered an error while writing stats file: \n" + StackTrace.toString(e) + "\n");
      }
    }

  }
}
