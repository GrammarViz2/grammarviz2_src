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
import java.util.TreeMap;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.seninp.gi.GIAlgorithm;
import net.seninp.gi.logic.GrammarRuleRecord;
import net.seninp.gi.logic.GrammarRules;
import net.seninp.gi.logic.RuleInterval;
import net.seninp.gi.repair.RePairFactory;
import net.seninp.gi.repair.RePairGrammar;
import net.seninp.gi.sequitur.SAXRule;
import net.seninp.gi.sequitur.SequiturFactory;
import net.seninp.grammarviz.logic.GrammarVizChartData;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;
import net.seninp.jmotif.sax.datastructure.SAXRecord;
import net.seninp.jmotif.sax.parallel.ParallelSAXImplementation;
import net.seninp.util.StackTrace;

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
  private double[][] ts;

  /** Data structure that keeps the chart data. */
  private GrammarVizChartData chartData;

  // static block - we instantiate the logger
  //
  private static final Logger LOGGER = LoggerFactory.getLogger(GrammarVizModel.class);

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

    LOGGER.info("setting the file " + filename + " as current data source");

    // action
    this.dataFileName = filename;

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
    ArrayList<ArrayList<Double> > data = new ArrayList<ArrayList<Double> >();

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

        if(0 == lineCounter)
        {
          data = new ArrayList<ArrayList<Double> >();
          for (int i = 0; i < lineSplit.length; i++) {
            data.add(new ArrayList<Double>());
          }
        }

        if (lineSplit.length < data.size()) {
          this.log("line " + (lineCounter+1) + " of file " + this.dataFileName + " contains too few data points.");
        }

        // we read only first column
        for (int i = 0; i < lineSplit.length; i++) {
          double value = new BigDecimal(lineSplit[i]).doubleValue();
          data.get(i).add(value);
        }

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
      this.ts = new double[data.size()][data.get(0).size()];
      // this.ts[0] = new double[data.get(0).size()];

      for (int i = 0; i < data.size(); i++) {
        for (int j = 0; j < data.get(0).size(); j++) {
          this.ts[i][j] = data.get(i).get(j);
        }
      }
    }
    data = new ArrayList<ArrayList<Double> >();

    LOGGER.debug("loaded " + this.ts[0].length + " points....");

    // notify that the process finished
    this.log("loaded " + this.ts[0].length + " points from " + this.dataFileName);

    // and send the timeseries
    setChanged();
    notifyObservers(new GrammarVizMessage(GrammarVizMessage.TIME_SERIES_MESSAGE, this.ts));

  }

  /**
   * converts multiple SAXRecords in to a single one
   * 
   * @param the records to merge.
   */
  private SAXRecords mergeRecords(SAXRecords records[]) {

    ArrayList<Integer> mergedIndices = records[0].getAllIndices();
    ArrayList<char[]> mergedWords = new ArrayList<char[]>();
    for(Integer i : mergedIndices)
      mergedWords.add(records[0].getByIndex(i).getPayload());

    for(int i = 1; i < records.length; i++) {
      ArrayList<Integer> indices1 = mergedIndices;
      ArrayList<char[]> words1 = mergedWords;

      ArrayList<Integer> indices2 = records[i].getAllIndices();
      ArrayList<char[]> words2 = new ArrayList<char[]>();
      for(Integer j : indices2)
        words2.add(records[i].getByIndex(j).getPayload());


      mergedIndices = new ArrayList<Integer>();
      mergedWords = new ArrayList<char[]>();


      mergedIndices.add(0);
      
      // concatenate words
      char[] word1 = words1.get(0);
      char[] word2 = words2.get(0);
      char[] mergedWord = new char[word1.length + word2.length];
      System.arraycopy(word1, 0, mergedWord, 0, word1.length);
      System.arraycopy(word2, 0, mergedWord, word1.length, word2.length);
      mergedWords.add(mergedWord);

      int index1 = 0;
      int index2 = 0;
      while(index1+1<indices1.size() || index2+1<indices2.size()) {
        boolean inc1 = false, inc2 = false;
        if(index2+1>=indices2.size() || (index1+1<indices1.size() && index2+1<indices2.size() && indices1.get(index1+1) <= indices2.get(index2+1))) {
          inc1 = true;
        }
        if(index1+1>=indices1.size() || (index1+1<indices1.size() && index2+1<indices2.size() && indices2.get(index2+1) <= indices1.get(index1+1))) {
          inc2 = true;
        }
        if(inc1) {index1++;}
        if(inc2) {index2++;}

        mergedIndices.add(Math.max(indices1.get(index1),indices2.get(index2)));

        // concatenate words
        word1 = words1.get(index1);
        word2 = words2.get(index2);
        mergedWord = new char[word1.length + word2.length];
        System.arraycopy(word1, 0, mergedWord, 0, word1.length);
        System.arraycopy(word2, 0, mergedWord, word1.length, word2.length);
        mergedWords.add(mergedWord);
      }
    }

    SAXRecords mergedRecord = new SAXRecords();
    HashMap<Integer, char[]> hm = new HashMap<Integer,char[]>();
    for(int i = 0; i < mergedIndices.size(); i++) {
      hm.put(mergedIndices.get(i), mergedWords.get(i));
    }
    mergedRecord.addAll(hm);

    return mergedRecord;
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
  public synchronized void processData(GIAlgorithm algorithm, boolean useSlidingWindow,
      boolean useGlobalNormalization, NumerosityReductionStrategy numerosityReductionStrategy,
      int windowSize, int paaSize, int alphabetSize, double normalizationThreshold,
      String grammarOutputFileName)
          throws IOException {

    // check if the data is loaded
    //
    if (null == this.ts || null == this.ts[0] || this.ts[0].length == 0) {
      this.log("unable to \"Process data\" - no data were loaded ...");
    }
    else {

      // the logging block
      //
      StringBuffer sb = new StringBuffer("setting up GI with params: ");
      if (GIAlgorithm.SEQUITUR.equals(algorithm)) {
        sb.append("algorithm: Sequitur, ");
      }
      else {
        sb.append("algorithm: RePair, ");
      }
      sb.append("sliding window ").append(useSlidingWindow);
      sb.append(", global normalization ").append(useGlobalNormalization);
      sb.append(", numerosity reduction ").append(numerosityReductionStrategy.toString());
      sb.append(", SAX window ").append(windowSize);
      sb.append(", PAA ").append(paaSize);
      sb.append(", Alphabet ").append(alphabetSize);
      LOGGER.info(sb.toString());
      this.log(sb.toString());

      LOGGER.debug("creating ChartDataStructure");

      // only giving chart data the first time series
      // should probably give it all of the time series
      this.chartData = new GrammarVizChartData(this.dataFileName, this.ts[0], useSlidingWindow,
          numerosityReductionStrategy, windowSize, paaSize, alphabetSize);

      NormalAlphabet na = new NormalAlphabet();

      try {

        if (GIAlgorithm.SEQUITUR.equals(algorithm)) {

          SAXProcessor sp = new SAXProcessor();


          SAXRecords saxFrequencyDataArray[] = new SAXRecords[this.ts.length];
          for(int i = 0; i < this.ts.length; i++) {
            if (useSlidingWindow) {
              if(useGlobalNormalization) {
                saxFrequencyDataArray[i] = sp.ts2saxViaWindowGlobalNormalization(ts[i], windowSize, paaSize, na.getCuts(alphabetSize),
                    numerosityReductionStrategy, normalizationThreshold);
              } else {
                saxFrequencyDataArray[i] = sp.ts2saxViaWindow(ts[i], windowSize, paaSize, na.getCuts(alphabetSize),
                    numerosityReductionStrategy, normalizationThreshold);
              }
            } else {
              saxFrequencyDataArray[i] = sp.ts2saxByChunking(ts[i], paaSize, na.getCuts(alphabetSize),
                  normalizationThreshold);
            }
          }

          SAXRecords saxFrequencyData = mergeRecords(saxFrequencyDataArray);
          // saxFrequencyData = saxFrequencyDataArray[0];

          LOGGER.trace("String: " + saxFrequencyData.getSAXString(SPACE));

          LOGGER.debug("running sequitur ...");
          SAXRule sequiturGrammar = SequiturFactory
              .runSequitur(saxFrequencyData.getSAXString(SPACE));

          LOGGER.debug("collecting grammar rules data ...");
          GrammarRules rules = sequiturGrammar.toGrammarRulesData();

          LOGGER.debug("mapping rule intervals on timeseries ...");
          // only use first time series, updateRuleInterval only cares about length (why does it take an array?)
          SequiturFactory.updateRuleIntervals(rules, saxFrequencyData, useSlidingWindow, this.ts[0],
              windowSize, paaSize);

          LOGGER.debug("done ...");
          this.chartData.setGrammarRules(rules);
        }
        else {

          ParallelSAXImplementation ps = new ParallelSAXImplementation();
          SAXRecords parallelRes = ps.process(ts[0], 2, windowSize, paaSize, alphabetSize,
              numerosityReductionStrategy, normalizationThreshold);

          RePairGrammar rePairGrammar = RePairFactory.buildGrammar(parallelRes);

          rePairGrammar.expandRules();
          rePairGrammar.buildIntervals(parallelRes, ts[0], windowSize);

          GrammarRules rules = rePairGrammar.toGrammarRulesData();

          this.chartData.setGrammarRules(rules);

        }

      }
      catch (Exception e) {
        this.log("error while processing data " + StackTrace.toString(e));
        e.printStackTrace();
      }

      this.log("processed data, broadcasting charts");
      LOGGER.info("process finished");

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
  protected void saveGrammarStats(GrammarVizChartData data) {

    boolean fileOpen = false;

    BufferedWriter bw = null;
    try {
      String currentPath = new File(".").getCanonicalPath();
      bw = new BufferedWriter(new OutputStreamWriter(
          new FileOutputStream(currentPath + File.separator + "grammar_stats.txt"), "UTF-8"));
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
      System.err.print(
          "Encountered an error while writing stats file: \n" + StackTrace.toString(e) + "\n");
    }

    // ArrayList<int[]> ruleLengths = new ArrayList<int[]>();

    for (GrammarRuleRecord ruleRecord : data.getGrammarRules()) {

      StringBuffer sb = new StringBuffer();
      sb.append("/// ").append(ruleRecord.getRuleName()).append(CR);
      sb.append(ruleRecord.getRuleName()).append(" -> \'").append(ruleRecord.getRuleString().trim())
          .append("\', expanded rule string: \'").append(ruleRecord.getExpandedRuleString())
          .append("\'").append(CR);

      if (ruleRecord.getRuleIntervals().size() > 0) {

        int[] starts = new int[ruleRecord.getRuleIntervals().size()];
        int[] lengths = new int[ruleRecord.getRuleIntervals().size()];
        int i = 0;
        for (RuleInterval sp : ruleRecord.getRuleIntervals()) {
          starts[i] = sp.getStart();
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
          System.err.print(
              "Encountered an error while writing stats file: \n" + StackTrace.toString(e) + "\n");
        }
      }
    }

    // try to write stats into the file
    try {
      if (fileOpen) {
        bw.close();
      }
    }
    catch (IOException e) {
      System.err.print(
          "Encountered an error while writing stats file: \n" + StackTrace.toString(e) + "\n");
    }

  }

}
