package net.seninp.grammarviz.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
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
import java.util.Observable;
import net.seninp.gi.GrammarRuleRecord;
import net.seninp.gi.GrammarRules;
import net.seninp.gi.RuleInterval;
import net.seninp.gi.repair.RePairFactory;
import net.seninp.gi.repair.RePairGrammar;
import net.seninp.gi.sequitur.SAXRule;
import net.seninp.gi.sequitur.SequiturFactory;
import net.seninp.grammarviz.logic.MotifChartData;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.datastructures.SAXRecords;
import net.seninp.jmotif.sax.parallel.ParallelSAXImplementation;
import net.seninp.util.StackTrace;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Implements the Sequitur Model component of MVC GUI pattern.
 * 
 * @author psenin
 * 
 */
public class GrammarVizModel extends Observable {

  final static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  private static final String SPACE = " ";
  private static final String CR = "\n";

  /** The data filename. */
  private String dataFileName;

  /** If that data was read - it is stored here. */
  private double[] ts;

  /** Data structure to keep chart data. */
  private MotifChartData chartData;

  // the logger business
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.DEBUG;
  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(GrammarVizModel.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  /**
   * The file name setter.
   * 
   * @param filename The file name to set.
   */
  private synchronized void setDataFileName(String filename) {
    this.dataFileName = filename;
  }

  /**
   * The file name getter.
   * 
   * @return current filename.
   */
  public synchronized String getDataFileName() {
    return this.dataFileName;
  }

  /**
   * Set data source filename.
   * 
   * @param filename the filename.
   */
  public synchronized void setDataSource(String filename) {

    consoleLogger.info("setting the file " + filename + " as current data source");

    // action
    this.setDataFileName(filename);

    // notify the View
    this.setChanged();
    notifyObservers(new GrammarVizMessage(GrammarVizMessage.DATA_FNAME, this.getDataFileName()));

    // this notification tells GUI which file was selected as the data source
    this.log("set file " + filename + " as current data source");

  }

  /**
   * Load the data which is supposedly in the file which is selected as the data source.
   * 
   * @param limitStr the limit of lines to read.
   */
  public synchronized void loadData(String limitStr) {

    // check if everything is ready
    if ((null == this.dataFileName) || this.dataFileName.isEmpty()) {
      this.log("unable to load data - no data source selected yet");
      return;
    }

    // make sure the path exists
    Path path = Paths.get(this.dataFileName);
    if (!(Files.exists(path))) {
      this.log("file " + this.dataFileName + " doesn't exist.");
      return;
    }

    // read the input
    //
    // init the data araay
    ArrayList<Double> data = new ArrayList<Double>();

    // lets go
    try {

      // set the lines limit
      long loadLimit = 0l;
      if (!(null == limitStr) && !(limitStr.isEmpty())) {
        loadLimit = Long.parseLong(limitStr);
      }

      // open the reader
      BufferedReader reader = Files.newBufferedReader(path, DEFAULT_CHARSET);

      // read by the line in the loop from reader
      String line = null;
      long lineCounter = 0;
      while ((line = reader.readLine()) != null) {
        String[] lineSplit = line.trim().split("\\s+");
        // we read only first column
        // for (int i = 0; i < lineSplit.length; i++) {
        double value = new BigDecimal(lineSplit[0]).doubleValue();
        data.add(value);
        // }
        lineCounter++;
        // break the load if needed
        if ((loadLimit > 0) && (lineCounter > loadLimit)) {
          break;
        }
      }
      reader.close();
    }
    catch (Exception e) {
      String stackTrace = StackTrace.toString(e);
      System.err.println(StackTrace.toString(e));
      this.log("error while trying to read data from " + this.dataFileName + ":\n" + stackTrace);
    }
    finally {
      assert true;
    }

    // convert to simple doubles array and clean the variable
    if (!(data.isEmpty())) {
      this.ts = new double[data.size()];
      for (int i = 0; i < data.size(); i++) {
        this.ts[i] = data.get(i);
      }
    }
    data = new ArrayList<Double>();

    consoleLogger.debug("loaded " + this.ts.length + " points....");

    // notify that the process finished
    this.log("loaded " + this.ts.length + " points from " + this.dataFileName);

    // and send the timeseries
    setChanged();
    notifyObservers(new GrammarVizMessage(GrammarVizMessage.TIME_SERIES_MESSAGE, this.ts));

  }

  /**
   * Process data with Sequitur. Populate and broadcast ChartData object.
   * 
   * @param algorithm the algorithm, 0 Sequitur, 1 RE-PAIR.
   * @param useSlidingWindow The use sliding window parameter.
   * @param numerosityReductionStrategy The numerosity reduction strategy.
   * @param windowSize The SAX sliding window size.
   * @param paaSize The SAX PAA size.
   * @param alphabetSize The SAX alphabet size.
   * @param normalizationThreshold The normalization threshold.
   * @param grammarOutputFileName The file name to where save the grammar.
   * @throws IOException
   */
  public synchronized void processData(int algorithm, boolean useSlidingWindow,
      NumerosityReductionStrategy numerosityReductionStrategy, int windowSize, int paaSize,
      int alphabetSize, double normalizationThreshold, String grammarOutputFileName)
      throws IOException {

    // check if the data is loaded
    //
    if (null == this.ts || this.ts.length == 0) {
      this.log("unable to \"Process data\" - no data were loaded ...");
    }
    else {

      // the logging block
      //
      StringBuffer sb = new StringBuffer("setting up GI with params: ");
      if (0 == algorithm) {
        sb.append("algorithm: Sequitur, ");
      }
      else {
        sb.append("algorithm: RePair, ");
      }
      sb.append("sliding window ").append(useSlidingWindow);
      sb.append(", numerosity reduction ").append(numerosityReductionStrategy.toString());
      sb.append(", SAX window ").append(windowSize);
      sb.append(", PAA ").append(paaSize);
      sb.append(", Alphabet ").append(alphabetSize);
      consoleLogger.info(sb.toString());
      this.log(sb.toString());

      consoleLogger.debug("creating ChartDataStructure");
      this.chartData = new MotifChartData(this.dataFileName, this.ts, useSlidingWindow,
          numerosityReductionStrategy, windowSize, paaSize, alphabetSize);

      try {

        if (0 == algorithm) {

          SAXRecords saxFrequencyData = null;
          if (useSlidingWindow) {
            consoleLogger.debug("discretizing string ...");
            saxFrequencyData = SequiturFactory.discretize(this.ts, numerosityReductionStrategy,
                windowSize, paaSize, alphabetSize, normalizationThreshold);
          }
          else {
            saxFrequencyData = SequiturFactory.discretizeNoSlidingWindow(this.ts, paaSize,
                alphabetSize, normalizationThreshold);
          }

          consoleLogger.trace("String: " + saxFrequencyData.getSAXString(SPACE));

          consoleLogger.debug("running sequitur ...");
          SAXRule sequiturGrammar = SequiturFactory.runSequitur(saxFrequencyData
              .getSAXString(SPACE));

          consoleLogger.debug("collecting grammar rules data ...");
          GrammarRules rules = sequiturGrammar.toGrammarRulesData();

          consoleLogger.debug("mapping rule intervals on timeseries ...");
          SequiturFactory.updateRuleIntervals(rules, saxFrequencyData, useSlidingWindow, this.ts,
              windowSize, paaSize);

          consoleLogger.debug("done ...");
          this.chartData.setGrammarRules(rules);

        }
        else {

          ParallelSAXImplementation ps = new ParallelSAXImplementation();
          SAXRecords parallelRes = ps.process(ts, 2, windowSize, paaSize, alphabetSize,
              NumerosityReductionStrategy.EXACT, normalizationThreshold);

          RePairGrammar rePairGrammar = RePairFactory.buildGrammar(parallelRes);

          rePairGrammar.expandRules();
          rePairGrammar.buildIntervals(parallelRes, ts, windowSize);

          GrammarRules rules = rePairGrammar.toGrammarRulesData();

          this.chartData.setGrammarRules(rules);

        }

      }
      catch (Exception e) {
        this.log("error while processing data " + StackTrace.toString(e));
        e.printStackTrace();
      }

      this.log("processed data, broadcasting charts");
      consoleLogger.info("process finished");

      setChanged();
      notifyObservers(new GrammarVizMessage(GrammarVizMessage.CHART_MESSAGE, this.chartData));
    }
  }

  /**
   * Performs logging messages distribution.
   * 
   * @param message the message to log.
   */
  private void log(String message) {
    this.setChanged();
    notifyObservers(new GrammarVizMessage(GrammarVizMessage.STATUS_MESSAGE, "model: " + message));
  }

  /**
   * Saves the grammar stats.
   * 
   * @param data the data for collecting stats.
   */
  protected void saveGrammarStats(MotifChartData data) {

    boolean fileOpen = false;

    BufferedWriter bw = null;
    try {
      String currentPath = new File(".").getCanonicalPath();
      bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(currentPath
          + File.separator + "grammar_stats.txt"), "UTF-8"));
      StringBuffer sb = new StringBuffer();
      sb.append("# filename: ").append(this.dataFileName).append(CR);
      sb.append("# sliding window: ").append(data.getSAXWindowSize()).append(CR);
      if (data.isSlidingWindowOn()) {
        sb.append("# window size: ").append(data.getSAXWindowSize()).append(CR);
      }
      sb.append("# paa size: ").append(data.getSAXPaaSize()).append(CR);
      sb.append("# alphabet size: ").append(data.getSAXAlphabetSize()).append(CR);
      bw.write(sb.toString());
      fileOpen = true;
    }
    catch (IOException e) {
      System.err.print("Encountered an error while writing stats file: \n" + StackTrace.toString(e)
          + "\n");
    }

    // ArrayList<int[]> ruleLengths = new ArrayList<int[]>();

    for (GrammarRuleRecord ruleRecord : data.getGrammarRules()) {

      StringBuffer sb = new StringBuffer();
      sb.append("/// ").append(ruleRecord.getRuleName()).append(CR);
      sb.append(ruleRecord.getRuleName()).append(" -> \'")
          .append(ruleRecord.getRuleString().trim()).append("\', expanded rule string: \'")
          .append(ruleRecord.getExpandedRuleString()).append("\'").append(CR);

      if (ruleRecord.getRuleIntervals().size() > 0) {

        int[] starts = new int[ruleRecord.getRuleIntervals().size()];
        int[] lengths = new int[ruleRecord.getRuleIntervals().size()];
        int i = 0;
        for (RuleInterval sp : ruleRecord.getRuleIntervals()) {
          starts[i] = sp.getStartPos();
          lengths[i] = (sp.endPos - sp.startPos);
          i++;
        }
        sb.append("subsequences starts: ").append(Arrays.toString(starts)).append(CR)
            .append("subsequences lengths: ").append(Arrays.toString(lengths)).append(CR);
      }

      sb.append("rule occurrence frequency ").append(ruleRecord.getRuleIntervals().size())
          .append(CR);
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

  }

}
