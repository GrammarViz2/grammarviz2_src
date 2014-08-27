package edu.hawaii.jmotif.grammarviz.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Observable;
import java.util.Observer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import edu.hawaii.jmotif.gi.GrammarRuleRecord;
import edu.hawaii.jmotif.gi.GrammarRules;
import edu.hawaii.jmotif.gi.sequitur.SAXMotif;
import edu.hawaii.jmotif.grammarviz.model.SequiturMessage;
import edu.hawaii.jmotif.logic.RuleInterval;
import edu.hawaii.jmotif.sax.NumerosityReductionStrategy;
import edu.hawaii.jmotif.sax.alphabet.Alphabet;
import edu.hawaii.jmotif.sax.alphabet.NormalAlphabet;
import edu.hawaii.jmotif.sax.datastructures.DiscordRecords;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.util.SAXFileIOHelper;

/**
 * The main data structure used in SAXSequitur. It contains all the information needed for charting
 * and tables.
 * 
 * @author Manfred Lerner, seninp
 * 
 */
public class MotifChartData extends Observable implements Observer {

  /** The threshold to be used for the snippets window. */
  private static final double NORMALIZATION_THRESHOLD = 0.005;

  /** Alphabet instance we'll use. */
  private static final Alphabet normalA = new NormalAlphabet();

  private static final String CR = "\n";

  /** SAX conversion parameters. */
  protected final boolean slidingWindowOn;
  protected final NumerosityReductionStrategy numerosityReductionStrategy;
  protected final int saxWindowSize;
  protected final int saxAlphabetSize;
  protected final int saxPAASize;

  /** Original data file name. */
  private final String fname;

  /** Original data which will be used for the chart. */
  protected final double[] originalTimeSeries;

  /** The whole timeseries as a string */
  private String saxDisplayString = null;

  /** The grammar rules. */
  private GrammarRules grammarRules;

  /** The discords. */
  protected DiscordRecords discords;

  /** JMotif's data structure, product of series conversion into SAX words. */
  // protected SAXFrequencyData saxFrequencyData = new SAXFrequencyData();

  /** Pruning related vars. */
  private SAXPointsNumber[] pointsNumberRemoveStrategy;
  private ArrayList<SameLengthMotifs> allClassifiedMotifs;
  private ArrayList<PackedRuleRecord> arrPackedRuleRecords;

  /**
   * Constructor.
   * 
   * @param dataFileName the original filename.
   * @param ts the time series.
   * @param useSlidingWindow
   * @param numerosityReductionStrategy
   * @param windowSize SAX window size.
   * @param alphabetSize SAX alphabet size.
   * @param paaSize SAX PAA size.
   */
  public MotifChartData(String dataFileName, double[] ts, boolean useSlidingWindow,
      NumerosityReductionStrategy numerosityReductionStrategy, int windowSize, int paaSize,
      int alphabetSize) {

    this.fname = dataFileName.substring(0);

    this.slidingWindowOn = useSlidingWindow;
    this.numerosityReductionStrategy = numerosityReductionStrategy;

    this.originalTimeSeries = ts;

    this.saxWindowSize = windowSize;
    this.saxPAASize = paaSize;
    this.saxAlphabetSize = alphabetSize;
  }

  // public void setSAXFrequencyData(SAXFrequencyData saxFrequencyData) {
  // this.saxFrequencyData = saxFrequencyData;
  // }

  public void setGrammarRules(GrammarRules rules) {
    this.grammarRules = rules;
  }

  public GrammarRules getGrammarRules() {
    return this.grammarRules;
  }

  /**
   * Get the original, untransformed time series.
   * 
   * @return the original time series
   */
  public double[] getOriginalTimeseries() {
    return originalTimeSeries;
  }

  /**
   * @return SAX window size
   */
  public int getSAXWindowSize() {
    return saxWindowSize;
  }

  /**
   * @return SAX alphabet size
   */
  public int getSAXAlphabetSize() {
    return saxAlphabetSize;
  }

  /**
   * @return SAX PAA size
   */
  public int getSAXPaaSize() {
    return saxPAASize;
  }

  // /**
  // * @return SAX frequency data
  // */
  // public SAXFrequencyData getFreqData() {
  // return saxFrequencyData;
  // }

  // /**
  // * Builds (1) SAX frequency data structure respecting all the parameters, (2) runs Sequitur.
  // */
  // public void buildSAX() {
  //
  // this.saxFrequencyData = new SAXFrequencyData();
  //
  // try {
  //
  // String previousString = "";
  //
  // if (this.slidingWindowOn) {
  //
  // // scan across the time series extract sub sequences, and convert
  // // them to strings
  // for (int i = 0; i < this.originalTimeSeries.length - (this.saxWindowSize - 1); i++) {
  //
  // // fix the current subsection
  // double[] subSection = Arrays.copyOfRange(this.originalTimeSeries, i, i
  // + this.saxWindowSize);
  //
  // // Z normalize it
  // if (TSUtils.stDev(subSection) > NORMALIZATION_THRESHOLD) {
  // subSection = TSUtils.zNormalize(subSection);
  // }
  //
  // // perform PAA conversion if needed
  // double[] paa = TSUtils.optimizedPaa(subSection, this.saxPAASize);
  //
  // // Convert the PAA to a string.
  // char[] currentString = TSUtils.ts2String(paa, normalA.getCuts(this.saxAlphabetSize));
  //
  // // NumerosityReduction
  // if (!previousString.isEmpty()) {
  //
  // if ((NumerosityReductionStrategy.MINDIST == this.numerosityReductionStrategy)
  // && (0.0 == SAXFactory.saxMinDist(previousString.toCharArray(), currentString,
  // normalA.getDistanceMatrix(this.saxAlphabetSize)))) {
  // continue;
  // }
  // else if ((NumerosityReductionStrategy.EXACT == this.numerosityReductionStrategy)
  // && previousString.equalsIgnoreCase(new String(currentString))) {
  // continue;
  // }
  // }
  //
  // previousString = new String(currentString);
  // this.saxFrequencyData.put(currentString, i);
  // }
  // }
  // else {
  //
  // double[] normalizedSeries = TSUtils.zNormalize(this.originalTimeSeries);
  //
  // double[] paa = TSUtils.optimizedPaa(normalizedSeries, this.saxPAASize);
  //
  // char[] currentString = TSUtils.ts2String(paa, normalA.getCuts(this.saxAlphabetSize));
  //
  // for (int i = 0; i < currentString.length; i++) {
  // char[] cc = { currentString[i] };
  // // NumerosityReduction
  // if (!previousString.isEmpty()) {
  // if ((NumerosityReductionStrategy.MINDIST == this.numerosityReductionStrategy)
  // && (0.0 == SAXFactory.saxMinDist(previousString.toCharArray(), cc,
  // normalA.getDistanceMatrix(this.saxAlphabetSize)))) {
  // continue;
  // }
  // else if ((NumerosityReductionStrategy.EXACT == this.numerosityReductionStrategy)
  // && previousString.equalsIgnoreCase(new String(cc))) {
  // continue;
  // }
  // }
  // previousString = new String(cc);
  // this.saxFrequencyData.put(cc, i);
  //
  // }
  // }
  //
  // // get a whole series representation as SAX words
  // //
  // this.saxDisplayString = this.saxFrequencyData.getSAXString(" ");
  // String currentPath = new File(".").getCanonicalPath();
  // BufferedWriter bw = new BufferedWriter(new FileWriter(new File(currentPath + File.separator
  // + "sequitur_str.txt")));
  // StringTokenizer st = new StringTokenizer(this.saxDisplayString, " ");
  // int currentNum = 1;
  // HashMap<String, Integer> dictionary = new HashMap<String, Integer>();
  // while (st.hasMoreTokens()) {
  // String token = st.nextToken();
  // Integer key = dictionary.get(token);
  // if (null == key) {
  // key = currentNum;
  // dictionary.put(token, currentNum);
  // currentNum = currentNum + 1;
  // }
  // bw.write(key + " ");
  // }
  // bw.close();
  //
  // bw = new BufferedWriter(new FileWriter(new File(currentPath + File.separator
  // + "sequitur_dictionary.txt")));
  // for (Entry<String, Integer> e : dictionary.entrySet()) {
  // bw.write(e.getKey() + " " + e.getValue() + "\n");
  // }
  // bw.close();
  //
  // // writing the frequency data
  // //
  // bw = new BufferedWriter(new FileWriter(new File(currentPath + File.separator
  // + "frequencyData.ser")));
  // this.saxFrequencyData.save(bw);
  // bw.close();
  //
  // // System.out.println("SAX Display String: " + saxDisplayString);
  //
  // // weird conversion to numbers begins
  // //
  // // _NumericSAX = saxDisplayString;
  // // boolean bPavel = true;
  // // if (bPavel) {
  // // for (int i = 0; i < saxAlphabetSize; i++) {
  // // char c1 = (char) ('a' + i);
  // // char c2 = (char) ('1' + i);
  // // _NumericSAX = _NumericSAX.replace(c1, c2);
  // // }
  // // }
  // // else {
  // // for (int i = 0; i < saxAlphabetSize; i++) {
  // // char c1 = (char) ('a' + saxAlphabetSize - i - 1);
  // // char c2 = (char) ('1' + i);
  // // _NumericSAX = _NumericSAX.replace(c1, c2);
  // //
  // // }
  // // }
  // // weird conversion to numbers ends
  // //
  //
  // // call to Sequitur
  // runSequitur(saxDisplayString);
  //
  // // //System.out.println("SAX Numeric String: " + _NumericSAX);
  // }
  // catch (Exception ex) {
  // ex.printStackTrace();
  // }
  //
  // // this.grammar.getRules();
  // }

  // /**
  // * Runs the sequitur algorithm for SAX
  // *
  // * @param strSAX string of SAX subsequences
  // * @return the sequitur rules as string
  // * @throws TSException
  // */
  // private void runSequitur(String strSAX) throws TSException {
  //
  // SAXRule.numRules = new AtomicInteger(0);
  // SAXSymbol.theDigrams.clear();
  // SAXSymbol.theSubstituteTable.clear();
  //
  // grammar = new SAXRule();
  // // SAXRule.arrayRuleStrings = new ArrayList<String>();
  // SAXRule.arrRuleRecords = new ArrayList<SAXRuleRecord>();
  //
  // StringTokenizer st = new StringTokenizer(strSAX, " ");
  // int currentPosition = 0;
  // while (st.hasMoreTokens()) {
  //
  // String token = st.nextToken();
  //
  // // boolean merged = false;
  // // for (String str : SAXSymbol.theSubstituteTable.keySet()) {
  // // double dist = SAXFactory.saxMinDist(str.toCharArray(), token.toCharArray(),
  // // normalA.getDistanceMatrix(this.saxAlphabetSize));
  // // if (dist < 2) {
  // // merged = true;
  // // SAXSymbol.theSubstituteTable.get(str).put(token.substring(0), currentPosition);
  // // token = str;
  // // }
  // // }
  // // if (!(merged)) {
  // // SAXSymbol.theSubstituteTable.put(token, new Hashtable<String, Integer>());
  // // }
  //
  // grammar.last().insertAfter(new SAXTerminal(token, currentPosition));
  // grammar.last().p.check();
  // currentPosition++;
  // }
  //
  // // System.out.println("\nSequitur finished...\n " + grammar.getRules() +
  // // "\nExpanding rules...\n");
  //
  // // *** IMPORTANT: this fills up structure for all rules
  // // grammar.getRules();
  // grammar.getSAXRules();
  // // *** IMPORTANT: this collects stats
  // collectMotifStats();
  //
  // // grammar.getSAXRules();
  // }

  public ArrayList<PackedRuleRecord> getArrPackedRuleRecords() {
    return arrPackedRuleRecords;
  }

  public void setArrPackedRuleRecords(ArrayList<PackedRuleRecord> arrPackedRuleRecords) {
    this.arrPackedRuleRecords = arrPackedRuleRecords;
  }

  /**
   * converts rules from a foreign alphabet to the internal original SAX alphabet
   * 
   * @param rule the SAX rule in foreign SAX alphabet
   * @return the SAX string in original alphabet, e.g. aabbdd
   */
  public String convert2OriginalSAXAlphabet(char firstForeignAlphabetChar, String rule) {
    String textRule = rule;
    for (int i = 0; i < getSAXAlphabetSize(); i++) {
      char c1 = (char) (firstForeignAlphabetChar + i);
      char c2 = (char) ('a' + i);
      textRule = textRule.replace(c1, c2);
    }
    return textRule;
  }

  /**
   * @return SAX display formatted string
   */
  public String getSAXDisplay() {
    return saxDisplayString;
  }

  /**
   * @param SAXDisplay SAX display formatted string
   */
  public void setSAXDisplay(String SAXDisplay) {
    saxDisplayString = SAXDisplay;
  }

  // /**
  // * @return SAX string in numerical format
  // */
  // public String getNumericSAX() {
  // return _NumericSAX;
  // }
  //
  // /**
  // * @param numericSAX SAX string in numerical format
  // */
  // public void setNumericSAX(String numericSAX) {
  // _NumericSAX = numericSAX;
  // }

  // /**
  // * Get the grammar.
  // *
  // * @return the built by Sequitur grammar.
  // */
  // public SAXRule getGrammar() {
  // return grammar;
  // }

  /**
   * Recovers start and stop coordinates ofRule's subsequences.
   * 
   * @param ruleIdx The rule index.
   * @return The array of all intervals corresponding to this rule.
   */
  public ArrayList<RuleInterval> getRulePositionsByRuleNum(Integer ruleIdx) {
    GrammarRuleRecord ruleRec = this.grammarRules.getRuleRecord(ruleIdx);
    return ruleRec.getRuleIntervals();
  }

  public ArrayList<RuleInterval> getSubsequencesPositionsByClassNum(Integer clsIdx) {

    // this will be the result
    ArrayList<RuleInterval> positions = new ArrayList<RuleInterval>();

    // the sub-sequences class container
    SameLengthMotifs thisClass = allClassifiedMotifs.get(clsIdx);

    // Use minimal length to name the file.
    String fileName = thisClass.getMinMotifLen() + ".txt";
    // The position of those sub-sequences in the original time series.
    String positionFileName = thisClass.getMinMotifLen() + "Position" + ".txt";

    String path = "Result" + System.getProperties().getProperty("file.separator") + "data"
        + System.getProperties().getProperty("file.separator");

    double[] values = this.getOriginalTimeseries();

    XYSeriesCollection data = new XYSeriesCollection();

    for (SAXMotif subSequence : thisClass.getSameLenMotifs()) {
      positions.add(new RuleInterval(subSequence.getPos().startPos, subSequence.getPos().endPos));
    }

    for (RuleInterval pos : positions) {
      XYSeries dataset = new XYSeries("Daten");
      int count = 0;

      int start = pos.getStartPos();
      int end = pos.getEndPos();

      for (int i = start; (i <= end) && (i < values.length); i++) {
        dataset.add(count++, values[i]);
      }
      data.addSeries(dataset);
    }
    SAXFileIOHelper.writeFileXYSeries(path, fileName, positionFileName, data, positions);

    return positions;
  }

  // private String collectMotifStats() {
  //
  // // start collecting stats
  // //
  // // System.out.println("Collecting stats:");
  //
  // // basic stats
  // //
  // // System.out.println("Series length: " + this.originalTimeSeries.length);
  // // System.out.println("SAX params: w: " + this.saxWindowSize + ", paa: " + this.saxPAASize
  // // + ", a: " + this.saxAlphabetSize);
  // // System.out.println("SAX params: use sliding window: " + slidingWindowOn
  // // + ", numerosity reduction: " + this.numerosityReductionStrategy);
  //
  // // SAX transform statistics
  // //
  // // System.out.println("Total SAX words seen: " + this.saxDisplayString.split(" ").length);
  // // System.out.println("Unique SAX words seen: " + this.saxFrequencyData.size());
  // // for (SAXFrequencyEntry sfe : this.saxFrequencyData.getSortedFrequencies()) {
  // // System.out.println("  " + sfe.getSubstring() + ", seen " + sfe.getEntries().size()
  // // + " times.");
  // // }
  //
  // // Sequitur rules statistics
  // //
  // // System.out.println("Sequitur rules built: " +
  // // this.getGrammar().getSAXContainerList().size());
  //
  // boolean fileOpen = false;
  // BufferedWriter bw = null;
  // try {
  // String currentPath = new File(".").getCanonicalPath();
  // bw = new BufferedWriter(new FileWriter(new File(currentPath + File.separator
  // + "grammar_stats.txt")));
  // StringBuffer sb = new StringBuffer();
  // sb.append("# filename: ").append(this.fname).append(CR);
  // sb.append("# sliding window: ").append(this.saxWindowSize).append(CR);
  // if (this.slidingWindowOn) {
  // sb.append("# window size: ").append(this.slidingWindowOn).append(CR);
  // }
  // sb.append("# paa size: ").append(this.saxPAASize).append(CR);
  // sb.append("# alphabet size: ").append(this.saxAlphabetSize).append(CR);
  // bw.write(sb.toString());
  // fileOpen = true;
  // }
  // catch (IOException e) {
  // System.err.print("Encountered an error while writing stats file: \n" + StackTrace.toString(e)
  // + "\n");
  // }
  //
  // ArrayList<int[]> ruleLengths = new ArrayList<int[]>();
  //
  // for (GrammarRuleRecord ruleRecord : this.grammarRules) {
  //
  // StringBuffer sb = new StringBuffer();
  // sb.append("/// ").append(ruleRecord.getRuleName()).append(CR);
  // sb.append(ruleRecord.getRuleName()).append(" -> \'")
  // .append(ruleRecord.getRuleString().trim()).append("\', expanded rule string: \'")
  // .append(ruleRecord.getExpandedRuleString()).append("\'").append(CR);
  //
  // // the original indexes of all SAX words
  // ArrayList<Integer> indices = saxFrequencyData.getAllIndices();
  //
  // // expand all rules which built this particular rule
  // // expandedSAXwordsRule = rule.expandRules();
  // // System.out.println(container.getRuleName() + " -> " + container.getRuleString() + ", "
  // // + container.getExpandedRuleString());
  //
  // ArrayList<RuleInterval> positions = new ArrayList<RuleInterval>();
  // String[] split = ruleRecord.getExpandedRuleString().split(" ");
  // // rule.expandRules();
  //
  // // TODO: some sort of dirty hack here
  // if (this.slidingWindowOn) {
  // for (Integer s : ruleRecord.getOccurrences()) {
  // if ((s + split.length) >= indices.size()) {
  // positions.add(new RuleInterval(indices.get(s), this.originalTimeSeries.length - 1));
  // }
  // else {
  // positions.add(new RuleInterval(indices.get(s), indices.get(s + split.length) - 1
  // + saxWindowSize));
  // }
  // }
  // }
  // else {
  // double step = (double) this.originalTimeSeries.length / (double) this.saxPAASize;
  // for (Integer s : ruleRecord.getOccurrences()) {
  // Long start = Math.round(indices.get(s) * step);
  // Long end = 0L;
  // if ((s + split.length) >= indices.size()) {
  // end = (long) this.originalTimeSeries.length;
  // }
  // else {
  // end = Math.round(indices.get(s + split.length) * step);
  // }
  // positions.add(new RuleInterval(start.intValue(), end.intValue()));
  //
  // }
  // }
  //
  // if (positions.size() > 0) {
  // int[] starts = new int[positions.size()];
  // int[] lengths = new int[positions.size()];
  // int i = 0;
  // for (RuleInterval sp : positions) {
  // starts[i] = sp.getStartPos();
  // lengths[i] = (sp.endPos - sp.startPos);
  // i++;
  // }
  // sb.append("subsequences starts: ").append(Arrays.toString(starts)).append(CR)
  // .append("subsequences lengths: ").append(Arrays.toString(lengths)).append(CR);
  // ruleLengths.add(lengths);
  // ruleRecord.setMeanLength(getMeanLength(lengths));
  // ruleRecord.setMinMaxLength(lengths);
  // // here kicks in the periodicity stat
  // //
  // double meanPeriod = getMeanPeriod(starts);
  // ruleRecord.setPeriod(meanPeriod);
  // ruleRecord.setPeriodError(getPeriodError(starts, meanPeriod));
  // }
  //
  // sb.append("rule occurrence frequency ").append(positions.size()).append(CR);
  // sb.append("rule use frequency ").append(ruleRecord.getRuleUseFrequency()).append(CR);
  // sb.append("min length ").append(ruleRecord.minMaxLengthAsString().split(" - ")[0]).append(CR);
  // sb.append("max length ").append(ruleRecord.minMaxLengthAsString().split(" - ")[1]).append(CR);
  // sb.append("mean length ").append(ruleRecord.getMeanLength()).append(CR);
  // // sb.append("period ").append(container.getPeriod()).append(CR);
  // // sb.append("period error ").append(container.getPeriodError()).append(CR);
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
  // // for (int i = 0; i < ruleLengths.size(); i++) {
  // // System.out.println("R" + (i + 1) + "," + getFrequency(ruleLengths.get(i)) + ","
  // // + getMax(ruleLengths.get(i)) + "," + getMaxVariation(ruleLengths.get(i)) + ","
  // // + Arrays.toString(ruleLengths.get(i)).replace("[", "").replace("]", ""));
  // // }
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

  private double getPeriodError(int[] starts, double meanPeriod) {
    double sqd = 0.0;
    for (int i = 1; i < starts.length; i++) {
      double periodDiff = ((double) starts[i] - starts[i - 1]) - meanPeriod;
      sqd = sqd + periodDiff * periodDiff;
    }
    return Math.sqrt(sqd / (starts.length - 1));
  }

  private double getMeanPeriod(int[] starts) {
    int sum = 0;
    for (int i = 1; i < starts.length; i++) {
      sum = sum + starts[i] - starts[i - 1];
    }
    return ((double) sum) / (double) (starts.length - 1);
  }

  private Integer getMeanLength(int[] lengths) {
    int sum = 0;
    for (int l : lengths) {
      sum = sum + l;
    }
    return sum / lengths.length;
  }

  public int getRulesNumber() {
    return grammarRules.size();
  }

  // ********************************
  // Refactoring in Xing's code below
  // ********************************

  /**
   * This method counts how many times each data point is used in ANY sequitur rule (i.e. data point
   * 1 appears only in R1 and R2, the number for data point 1 is two). The function will get the
   * occurrence time for all points, and write the result into a text file named as
   * "PointsNumber.txt".
   */
  protected void countPointNumber() {

    // init the data structure and copy the original values
    SAXPointsNumber pointsNumber[] = new SAXPointsNumber[this.originalTimeSeries.length];
    for (int i = 0; i < this.originalTimeSeries.length; i++) {
      pointsNumber[i] = new SAXPointsNumber();
      pointsNumber[i].setPointIndex(i);
      pointsNumber[i].setPointValue(this.originalTimeSeries[i]);
    }

    // get all the rules and populate the occurrence density
    int rulesNum = this.getRulesNumber();
    for (int i = 0; i < rulesNum; i++) {
      ArrayList<RuleInterval> arrPos = this.getRulePositionsByRuleNum(i);
      for (RuleInterval saxPos : arrPos) {
        int start = saxPos.getStartPos();
        int end = saxPos.getEndPos();
        for (int position = start; position <= end; position++) {
          pointsNumber[position].setPointOccurenceNumber(pointsNumber[position]
              .getPointOccurenceNumber() + 1);
        }
      }
    }

    // make an output
    String path = "Result" + System.getProperties().getProperty("file.separator");
    String fileName = "PointsNumber.txt";
    SAXFileIOHelper.deleteFile(path, fileName);
    SAXFileIOHelper.writeFile(path, fileName, Arrays.toString(pointsNumber));

    this.pointsNumberRemoveStrategy = pointsNumber;
  }

  /**
   * This method counts how many times each data point is used in REDUCED sequitur rule (i.e. data
   * point 1 appears only in R1 and R2, the number for data point 1 is two). The function will get
   * the occurrence time for all points, and write the result into a text file named as
   * "PointsNumberAfterRemoving.txt".
   */
  protected void countPointNumberAfterRemoving() {

    // init the data structure and copy the original values
    SAXPointsNumber pointsNumber[] = new SAXPointsNumber[this.originalTimeSeries.length];
    for (int i = 0; i < this.originalTimeSeries.length; i++) {
      pointsNumber[i] = new SAXPointsNumber();
      pointsNumber[i].setPointIndex(i);
      pointsNumber[i].setPointValue(this.originalTimeSeries[i]);
    }

    for (SameLengthMotifs sameLenMotifs : this.getReducedMotifs()) {
      for (SAXMotif motif : sameLenMotifs.getSameLenMotifs()) {
        RuleInterval pos = motif.getPos();
        for (int i = pos.getStartPos(); i <= pos.getEndPos(); i++) {
          pointsNumber[i].setPointOccurenceNumber(pointsNumber[i].getPointOccurenceNumber() + 1);
          // pointsNumber[i].setRule(textRule);
        }
      }
    }

    // make an output
    String path = "Result" + System.getProperties().getProperty("file.separator");
    String fileName = "PointsNumberAfterRemoving.txt";
    SAXFileIOHelper.deleteFile(path, fileName);
    SAXFileIOHelper.writeFile(path, fileName, Arrays.toString(pointsNumber));

  }

  /**
   * Cleans-up the rules set by classifying the sub-sequences by length and removing the overlapping
   * in the same length range.
   * 
   * Sub-sequences with the length difference within threshold: "thresouldLength" will be classified
   * as a class with the function "classifyMotifs(double)", i.e. 1-100 and 101-205 will be
   * classified as a class when the threshold is 0.1, because the length difference is 5, which is
   * less than the threshold (0.1 * 100 = 10). If two sub-sequences within one class share a common
   * part which is more than the threshold: "thresouldCom", one of them will be removed by the
   * function "removeOverlappingInSimiliar(double)". i.e. 1-100 and 21-120.
   * 
   * @param intraThreshold, the threshold between the same motifs.
   * @param interThreshould, the threshold between the different motifs.
   */
  protected void removeOverlapping(double intraThreshold, double interThreshould) {

    classifyMotifs(intraThreshold);
    ArrayList<SAXMotif> motifsBeDeleted = removeOverlappingInSimiliar(interThreshould);

    String path = "Result" + System.getProperties().getProperty("file.separator");
    String fileName = "Deleted Motifs.txt";
    SAXFileIOHelper.deleteFile(path, fileName);
    SAXFileIOHelper.writeFile(path, fileName, motifsBeDeleted.toString());

  }

  /**
   * Classify the motifs based on their length.
   * 
   * It calls "getAllMotifs()" to get all the sub-sequences that were generated by Sequitur rules in
   * ascending order. Then bins all the sub-sequences by length based on the length of the first
   * sub-sequence in each class, that is, the shortest sub-sequence in each class.
   * 
   * @param lengthThreshold the motif length threshold.
   */
  protected void classifyMotifs(double lengthThreshold) {

    // reset vars
    allClassifiedMotifs = new ArrayList<SameLengthMotifs>();

    // down to business
    ArrayList<SAXMotif> allMotifs = getAllMotifs();

    // is this one better?
    int currentIndex = 0;
    for (SAXMotif tmpMotif : allMotifs) {

      currentIndex++;

      if (tmpMotif.isClassified()) {
        // this breaks the loop flow, so it goes to //for (SAXMotif tempMotif : allMotifs) {
        continue;
      }

      SameLengthMotifs tmpSameLengthMotifs = new SameLengthMotifs();
      int tmpMotifLen = tmpMotif.getPos().getEndPos() - tmpMotif.getPos().getStartPos() + 1;
      int minLen = tmpMotifLen;
      int maxLen = tmpMotifLen;

      // TODO: assuming that this motif has not been processed, right?
      ArrayList<SAXMotif> newMotifClass = new ArrayList<SAXMotif>();
      newMotifClass.add(tmpMotif);
      tmpMotif.setClassified(true);

      // TODO: this motif assumed to be the first one of it's class, traverse the rest down
      for (int i = currentIndex; i < allMotifs.size(); i++) {

        SAXMotif anotherMotif = allMotifs.get(i);

        // if the two motifs are similar or not.
        int anotherMotifLen = anotherMotif.getPos().getEndPos()
            - anotherMotif.getPos().getStartPos() + 1;

        // if they have the similar length.
        if (Math.abs(anotherMotifLen - tmpMotifLen) < (tmpMotifLen * lengthThreshold)) {
          newMotifClass.add(anotherMotif);
          anotherMotif.setClassified(true);
          if (anotherMotifLen > maxLen) {
            maxLen = anotherMotifLen;
          }
          else if (anotherMotifLen < minLen) {
            minLen = anotherMotifLen;
          }
        }
      }

      tmpSameLengthMotifs.setSameLenMotifs(newMotifClass);
      tmpSameLengthMotifs.setMinMotifLen(minLen);
      tmpSameLengthMotifs.setMaxMotifLen(maxLen);
      allClassifiedMotifs.add(tmpSameLengthMotifs);
    }
    // System.out.println();
  }

  protected ArrayList<SAXMotif> removeOverlappingInSimiliar(double thresouldCom) {

    ArrayList<SAXMotif> motifsBeDeleted = new ArrayList<SAXMotif>();

    for (SameLengthMotifs sameLenMotifs : allClassifiedMotifs) {
      outer: for (int j = 0; j < sameLenMotifs.getSameLenMotifs().size(); j++) {
        SAXMotif tempMotif = sameLenMotifs.getSameLenMotifs().get(j);
        int tempMotifLen = tempMotif.getPos().getEndPos() - tempMotif.getPos().getStartPos() + 1;

        for (int i = j + 1; i < sameLenMotifs.getSameLenMotifs().size(); i++) {
          SAXMotif anotherMotif = sameLenMotifs.getSameLenMotifs().get(i);
          int anotherMotifLen = anotherMotif.getPos().getEndPos()
              - anotherMotif.getPos().getStartPos() + 1;

          double minEndPos = Math.min(tempMotif.getPos().getEndPos(), anotherMotif.getPos()
              .getEndPos());
          double maxStartPos = Math.max(tempMotif.getPos().getStartPos(), anotherMotif.getPos()
              .getStartPos());
          // the length in common.
          double commonLen = minEndPos - maxStartPos + 1;

          // if they are overlapped motif, remove the shorter one
          if (commonLen > (tempMotifLen * thresouldCom)) {
            SAXMotif deletedMotif = new SAXMotif();
            SAXMotif similarWith = new SAXMotif();

            boolean isAnotherBetter;

            if (pointsNumberRemoveStrategy != null) {
              isAnotherBetter = decideRemove(anotherMotif, tempMotif);
            }
            else {
              isAnotherBetter = anotherMotifLen > tempMotifLen;

            }
            if (isAnotherBetter) {
              deletedMotif = tempMotif;
              similarWith = anotherMotif;
              sameLenMotifs.getSameLenMotifs().remove(j);
              deletedMotif.setSimilarWith(similarWith);
              motifsBeDeleted.add(deletedMotif);
              j--;
              continue outer;
            }
            else {
              deletedMotif = anotherMotif;
              similarWith = tempMotif;
              sameLenMotifs.getSameLenMotifs().remove(i);
              deletedMotif.setSimilarWith(similarWith);
              motifsBeDeleted.add(deletedMotif);
              i--;
            }
          }
        }
      }

      int minLength = sameLenMotifs.getSameLenMotifs().get(0).getPos().endPos
          - sameLenMotifs.getSameLenMotifs().get(0).getPos().startPos + 1;
      int sameLenMotifsSize = sameLenMotifs.getSameLenMotifs().size();
      int maxLength = sameLenMotifs.getSameLenMotifs().get(sameLenMotifsSize - 1).getPos().endPos
          - sameLenMotifs.getSameLenMotifs().get(sameLenMotifsSize - 1).getPos().startPos + 1;
      sameLenMotifs.setMinMotifLen(minLength);
      sameLenMotifs.setMaxMotifLen(maxLength);
    }
    return motifsBeDeleted;
  }

  /**
   * Stores all the sub-sequences that generated by Sequitur rules into an array list sorted by
   * sub-sequence length in ascending order.
   * 
   * @return the list of all sub-sequences sorted by length in ascending order.
   */
  protected ArrayList<SAXMotif> getAllMotifs() {

    // result
    ArrayList<SAXMotif> allMotifs = new ArrayList<SAXMotif>();

    // iterate over all rules
    for (int i = 0; i < this.getRulesNumber(); i++) {

      // iterate over all segments/motifs/sub-sequences which correspond to the rule
      ArrayList<RuleInterval> arrPos = this.getRulePositionsByRuleNum(i);
      for (RuleInterval saxPos : arrPos) {
        SAXMotif motif = new SAXMotif();
        motif.setPos(saxPos);
        motif.setRuleIndex(i);
        motif.setClassified(false);
        allMotifs.add(motif);
      }

    }

    // ascending order
    Collections.sort(allMotifs);
    return allMotifs;
  }

  /**
   * Decide which one from overlapping subsequences should be removed. The decision rule is that
   * each sub-sequence has a weight, the one with the smaller weight should be removed.
   * 
   * The weight is S/(A * L). S is the sum of occurrence time of all data points in that
   * sub-sequence, A is the average weight of the whole time series, and L is the length of that
   * sub-sequence.
   * 
   * @param motif1
   * @param motif2
   * 
   * @return
   */
  protected boolean decideRemove(SAXMotif motif1, SAXMotif motif2) {

    // motif1 details
    int motif1Start = motif1.getPos().getStartPos();
    int motif1End = motif1.getPos().getEndPos();
    int length1 = motif1End - motif1Start;

    // motif2 details
    int motif2Start = motif2.getPos().getStartPos();
    int motif2End = motif1.getPos().getEndPos();
    int length2 = motif2End - motif2Start;

    int countsMotif1 = 0;
    int countsMotif2 = 0;

    // compute the averageWeight
    double averageWeight = 1;
    int count = 0;
    for (int i = 0; i < pointsNumberRemoveStrategy.length; i++) {
      count += pointsNumberRemoveStrategy[i].getPointOccurenceNumber();
    }
    averageWeight = count / pointsNumberRemoveStrategy.length;

    // compute counts for motif 1
    for (int i = motif1Start; i <= motif1End; i++) {
      countsMotif1 += pointsNumberRemoveStrategy[i].getPointOccurenceNumber();
    }

    // compute counts for motif 2
    for (int i = motif2Start; i <= motif2End; i++) {
      countsMotif2 += pointsNumberRemoveStrategy[i].getPointOccurenceNumber();
    }

    // get weights
    double weight1 = countsMotif1 / (averageWeight * length1);
    double weight2 = countsMotif2 / (averageWeight * length2);

    if (weight1 > weight2) {
      return true;
    }

    return false;
  }

  /**
   * Performs rules pruning based on their overlap.
   * 
   * @param thresholdLength
   * @param thresholdCom
   */
  public void performRemoveOverlapping(double thresholdLength, double thresholdCom) {

    removeOverlapping(thresholdLength, thresholdCom);

    arrPackedRuleRecords = new ArrayList<PackedRuleRecord>();

    int i = 0;
    for (SameLengthMotifs subsequencesInClass : allClassifiedMotifs) {
      int classIndex = i;
      int subsequencesNumber = subsequencesInClass.getSameLenMotifs().size();
      int minLength = subsequencesInClass.getMinMotifLen();
      int maxLength = subsequencesInClass.getMaxMotifLen();

      PackedRuleRecord packedRuleRecord = new PackedRuleRecord();
      packedRuleRecord.setClassIndex(classIndex);
      packedRuleRecord.setSubsequenceNumber(subsequencesNumber);
      packedRuleRecord.setMinLength(minLength);
      packedRuleRecord.setMaxLength(maxLength);

      arrPackedRuleRecords.add(packedRuleRecord);
      i++;
    }

  }

  public ArrayList<SameLengthMotifs> getReducedMotifs() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * This computes anomalies.
   * 
   * @throws TSException
   */
  public void findAnomalies() throws TSException {
    SAXSequiturAnomalyFinder finder = new SAXSequiturAnomalyFinder(this);
    finder.addObserver(this);
    finder.run();
  }

  public DiscordRecords getAnomalies() {
    return this.discords;
  }

  @Override
  public void update(Observable o, Object arg) {
    if (arg instanceof SequiturMessage) {
      this.setChanged();
      notifyObservers(arg);
    }
  }

  public GrammarRuleRecord getRule(Integer ruleIndex) {
    return this.grammarRules.get(ruleIndex);
  }

}
