package edu.hawaii.jmotif.saxvsm;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import edu.hawaii.jmotif.sax.SAXFactory;
import edu.hawaii.jmotif.sax.alphabet.Alphabet;
import edu.hawaii.jmotif.sax.alphabet.NormalAlphabet;
import edu.hawaii.jmotif.text.SAXNumerosityReductionStrategy;
import edu.hawaii.jmotif.text.TextUtils;
import edu.hawaii.jmotif.text.WordBag;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.util.StackTrace;

public class UCRClassificationSeriesJob implements Callable<String> {

  /** The latin alphabet, lower case letters a-z. */
  private static final char[] ALPHABET = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
      'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

  private static final String COMMA = ",";

  private SAXNumerosityReductionStrategy strategy;
  private int windowSize;
  private int paaSize;
  private int alphabetSize;
  private Map<String, List<double[]>> trainData;
  private Map<String, List<double[]>> testData;

  public UCRClassificationSeriesJob(SAXNumerosityReductionStrategy strategy, int windowSize, int paaSize,
      int alphabetSize, Map<String, List<double[]>> testData, Map<String, List<double[]>> trainData) {
    this.strategy = strategy;
    this.windowSize = windowSize;
    this.paaSize = paaSize;
    this.alphabetSize = alphabetSize;
    this.trainData = trainData;
    this.testData = testData;
  }

  @Override
  public String call() throws Exception {

    try {
      // params
      int[] params = new int[4];
      params[0] = windowSize;
      params[1] = paaSize;
      params[2] = alphabetSize;
      params[3] = strategy.index();
      // making training bags collection
      List<WordBag> bags = TextUtils.labeledSeries2WordBags(trainData, params);

      // HashMap<String, HashMap<String, Double>> tfidf = TextUtils.computeTFIDF(bags);
      HashMap<String, HashMap<String, Double>> tfidf = computeTFIDF(bags);

      // tfidf = TextUtils.normalizeToUnitVectors(tfidf);
      tfidf = normalizeToUnitVectors(tfidf);

      int totalTestSample = 0;
      int totalPositiveTests = 0;

      for (String currenClassUnderTest : testData.keySet()) {
        List<double[]> testD = testData.get(currenClassUnderTest);

        int positives = 0;
        for (double[] series : testD) {
          // positives = positives
          // + TextUtils.classify(currenClassUnderTest, series, tfidf, params, strategy);
          positives = positives + classify(currenClassUnderTest, series, tfidf, params, strategy);

          totalTestSample++;
        }
        totalPositiveTests = totalPositiveTests + positives;

      }

      double accuracy = (double) totalPositiveTests / (double) totalTestSample;
      double error = 1.0d - accuracy;

      String res = "ok_" + toLogStr(params, strategy, 1.0D - error, error);

      return res;

    }
    catch (Exception e) {
      return StackTrace.toString(e);
    }

  }

  /**
   * Computes TF*IDF values.
   * 
   * @param texts The collection of text documents for which the statistics need to be computed.
   * @return The map of source documents names to the word - tf*idf weight collections.
   */
  private HashMap<String, HashMap<String, Double>> computeTFIDF(Collection<WordBag> texts) {

    // the number of docs
    int totalDocs = texts.size();

    // the result. map of document names to the pairs word - tfidf weight
    HashMap<String, HashMap<String, Double>> res = new HashMap<String, HashMap<String, Double>>();

    // build a collection of all observed words and their frequency in corpus
    TreeMap<String, Integer> allWords = new TreeMap<String, Integer>();
    for (WordBag bag : texts) {

      // here populate result map with empty entries
      res.put(bag.getLabel(), new HashMap<String, Double>());

      // and get those words
      for (Entry<String, Integer> e : bag.getWords().entrySet()) {
        Integer oldFrequency = allWords.get(e.getKey());
        if (null == oldFrequency) {
          allWords.put(e.getKey(), 1);
        }
        else {
          allWords.put(e.getKey(), oldFrequency + 1);
        }
      }

    }

    // outer loop - iterating over documents
    for (WordBag bag : texts) {

      // fix the doc name
      String bagName = bag.getLabel();
      HashMap<String, Integer> bagWords = bag.getWords(); // these are words of documents

      // what we want to do for TF*IDF is to compute it for all WORDS ever seen in set
      //
      for (Entry<String, Integer> word : allWords.entrySet()) {

        // by default it is zero
        //
        double tfidf = 0;

        // if this document contains the word - here we go
        if (bagWords.containsKey(word.getKey())) {

          int wordFrequency = bagWords.get(word.getKey());

          // compute TF: we take a log and correct for 0 by adding 1
          double tfValue = 1.0D + Math.log(Integer.valueOf(wordFrequency).doubleValue());

          // compute the IDF
          //
          double idfLOGValue = Math.log10(Integer.valueOf(totalDocs).doubleValue()
              / Integer.valueOf(word.getValue()).doubleValue());

          // and the TF-IDF
          //
          tfidf = tfValue * idfLOGValue;

        }

        res.get(bagName).put(word.getKey(), tfidf);

      }
    }
    return res;
  }

  /**
   * Computes a cosine normalization of TFIDF statistics.
   * 
   * @param data The data.
   * @return The normalized tfidf statistics.
   */
  private HashMap<String, HashMap<String, Double>> normalizeToUnitVectors(
      HashMap<String, HashMap<String, Double>> data) {
    // result
    HashMap<String, HashMap<String, Double>> res = new HashMap<String, HashMap<String, Double>>();
    // cosine normalize these rows corresponding to docs TFIDF
    //
    for (Entry<String, HashMap<String, Double>> e : data.entrySet()) {
      double sum = 0D;
      for (double el : e.getValue().values()) {
        if (!(0. == el)) {
          sum = sum + el * el;
        }
      }
      double sqRoot = Math.sqrt(sum);
      //
      // here is normalization coefficient is calculated - all the elements must be divided by its
      // value
      HashMap<String, Double> newEntry = new HashMap<String, Double>(e.getValue().size());
      for (Entry<String, Double> val : e.getValue().entrySet()) {
        if (val.getValue().equals(0D)) {
          newEntry.put(val.getKey(), 0D);
        }
        else {
          newEntry.put(val.getKey(), val.getValue() / sqRoot);
        }
      }
      res.put(e.getKey(), newEntry);
    }
    return res;
  }

  private String getStrategy(SAXNumerosityReductionStrategy strategy) {
    String strategyP = "noreduction";
    if (SAXNumerosityReductionStrategy.EXACT.equals(strategy)) {
      strategyP = "exact";
    }
    if (SAXNumerosityReductionStrategy.CLASSIC.equals(strategy)) {
      strategy = SAXNumerosityReductionStrategy.CLASSIC;
      strategyP = "classic";
    }
    return strategyP;
  }

  private int classify(String classKey, double[] series,
      HashMap<String, HashMap<String, Double>> tfidf, int[] params, SAXNumerosityReductionStrategy strategy)
      throws IndexOutOfBoundsException, TSException {

    WordBag test = seriesToWordBag("test", series, params, strategy);

    double minDist = -1.0d;
    String className = "";
    double[] cosines = new double[tfidf.entrySet().size()];
    int index = 0;
    for (Entry<String, HashMap<String, Double>> e : tfidf.entrySet()) {
      double dist = TextUtils.cosineSimilarity(test, e.getValue());
      cosines[index] = dist;
      index++;
      if (dist > minDist) {
        className = e.getKey();
        minDist = dist;
      }
    }

    boolean allEqual = true;
    double cosine = cosines[0];
    for (int i = 1; i < cosines.length; i++) {
      if (!(cosines[i] == cosine)) {
        allEqual = false;
      }
    }

    if (!(allEqual) && className.equalsIgnoreCase(classKey)) {
      return 1;
    }
    return 0;
  }

  private double cosineSimilarity(WordBag testSample, HashMap<String, Double> weightVector) {
    double res = 0;
    for (Entry<String, Integer> entry : testSample.getWords().entrySet()) {
      if (weightVector.containsKey(entry.getKey())) {
        res = res + entry.getValue().doubleValue() * weightVector.get(entry.getKey()).doubleValue();
      }
    }
    double m1 = magnitude(testSample.getWordsAsDoubles().values());
    double m2 = magnitude(weightVector.values());
    return res / (m1 * m2);
  }

  private double magnitude(Collection<Double> values) {
    Double res = 0.0D;
    for (Double v : values) {
      res = res + v * v;
    }
    return Math.sqrt(res.doubleValue());
  }

  private WordBag seriesToWordBag(String label, double[] series, int[] params,
      SAXNumerosityReductionStrategy strategy) throws IndexOutOfBoundsException, TSException {

    Alphabet a = new NormalAlphabet();

    WordBag resultBag = new WordBag(label);

    int windowSize = params[0];
    int paaSize = params[1];
    int alphabetSize = params[2];

    String oldStr = "";
    for (int i = 0; i <= series.length - windowSize; i++) {

      double[] paa = paa(zNormalize(subseries(series, i, windowSize)), paaSize);

      char[] sax = ts2String(paa, a.getCuts(alphabetSize));

      if (SAXNumerosityReductionStrategy.CLASSIC.equals(strategy)) {
        if (oldStr.length() > 0 && SAXFactory.strDistance(sax, oldStr.toCharArray()) == 0) {
          continue;
        }
      }
      else if (SAXNumerosityReductionStrategy.EXACT.equals(strategy)) {
        if (oldStr.equalsIgnoreCase(String.valueOf(sax))) {
          continue;
        }
      }

      oldStr = String.valueOf(sax);

      resultBag.addWord(String.valueOf(sax));
    }

    return resultBag;
  }

  /**
   * Approximate the timeseries using PAA. If the timeseries has some NaN's they are handled as
   * follows: 1) if all values of the piece are NaNs - the piece is approximated as NaN, 2) if there
   * are some (more or equal one) values happened to be in the piece - algorithm will handle it as
   * usual - getting the mean.
   * 
   * @param ts The timeseries to approximate.
   * @param paaSize The desired length of approximated timeseries.
   * @return PAA-approximated timeseries.
   * @throws TSException if error occurs.
   */
  private double[] paa(double[] ts, int paaSize) throws TSException {
    // fix the length
    int len = ts.length;
    // check for the trivial case
    if (len == paaSize) {
      return Arrays.copyOf(ts, ts.length);
    }
    else {
      // get values and timestamps
      double[][] vals = asMatrix(ts);
      // work out PAA by reshaping arrays
      double[][] res;
      if (len % paaSize == 0) {
        res = reshape(vals, len / paaSize, paaSize);
      }
      else {
        double[][] tmp = new double[paaSize][len];
        for (int i = 0; i < paaSize; i++) {
          for (int j = 0; j < len; j++) {
            tmp[i][j] = vals[0][j];
          }
        }
        double[][] expandedSS = reshape(tmp, 1, len * paaSize);
        res = reshape(expandedSS, len, paaSize);
      }
      double[] newVals = colMeans(res);

      return newVals;
    }

  }

  /**
   * Converts the vector into one-row matrix.
   * 
   * @param vector The vector.
   * @return The matrix.
   */
  private double[][] asMatrix(double[] vector) {
    double[][] res = new double[1][vector.length];
    for (int i = 0; i < vector.length; i++) {
      res[0][i] = vector[i];
    }
    return res;
  }

  /**
   * Z-Normalize timeseries to the mean zero and standard deviation of one.
   * 
   * @param series The timeseries.
   * @return Z-normalized time-series.
   * @throws TSException if error occurs.
   */
  private double[] zNormalize(double[] series) throws TSException {

    // this is the resulting normalization
    //
    double[] res = new double[series.length];

    // get mean and sdev, NaN's will be handled
    //
    double mean = mean(series);
    double sd = stDev(series);

    // another special case, where SD happens to be close to a zero, i.e. they all are the same for
    // example
    //
    if (sd <= 0.001D) {

      // here I assign another magic value - 0.001D which makes to middle band of the normal
      // Alphabet
      //
      for (int i = 0; i < res.length; i++) {
        if (Double.isInfinite(series[i]) || Double.isNaN(series[i])) {
          res[i] = series[i];
        }
        else {
          res[i] = 0.1D;
        }
      }
    }

    // normal case, everything seems to be fine
    //
    else {
      // sd and mean here, - go-go-go
      for (int i = 0; i < res.length; i++) {
        res[i] = (series[i] - mean) / sd;
      }
    }
    return res;

  }

  /**
   * Computes the mean value of timeseries.
   * 
   * @param series The timeseries.
   * @return The mean value.
   */
  private double mean(double[] series) {
    double res = 0D;
    int count = 0;
    for (double tp : series) {
      if (Double.isNaN(tp) || Double.isInfinite(tp)) {
        continue;
      }
      else {
        res += tp;
        count += 1;
      }
    }
    if (count > 0) {
      return res / ((Integer) count).doubleValue();
    }
    return Double.NaN;
  }

  /**
   * Computes the standard deviation of timeseries.
   * 
   * @param series The timeseries.
   * @return the standard deviation.
   */
  private double stDev(double[] series) {
    double num0 = 0D;
    double sum = 0D;
    int count = 0;
    for (double tp : series) {
      if (Double.isNaN(tp) || Double.isInfinite(tp)) {
        continue;
      }
      else {
        num0 = num0 + tp * tp;
        sum = sum + tp;
        count += 1;
      }
    }
    if (count > 0) {
      double len = ((Integer) count).doubleValue();
      return Math.sqrt((len * num0 - sum * sum) / (len * (len - 1)));
    }
    return Double.NaN;
  }

  /**
   * Extract subseries out of series.
   * 
   * @param series The series array.
   * @param start Start position
   * @param length Length of subseries to extract.
   * @return The subseries.
   * @throws IndexOutOfBoundsException If error occurs.
   */
  private double[] subseries(double[] series, int start, int length)
      throws IndexOutOfBoundsException {
    if (start + length > series.length) {
      throw new IndexOutOfBoundsException("Unable to extract subseries, series length: "
          + series.length + ", start: " + start + ", subseries length: " + length);
    }
    double[] res = new double[length];
    for (int i = 0; i < length; i++) {
      res[i] = series[start + i];
    }
    return res;
  }

  /**
   * Converts the timeseries into string using given cuts intervals. Useful for not-normal
   * distribution cuts.
   * 
   * @param vals The timeseries.
   * @param cuts The cut intervals.
   * @return The timeseries SAX representation.
   */
  private char[] ts2String(double[] vals, double[] cuts) {
    char[] res = new char[vals.length];
    for (int i = 0; i < vals.length; i++) {
      res[i] = num2char(vals[i], cuts);
    }
    return res;
  }

  /**
   * Get mapping of a number to char.
   * 
   * @param value the value to map.
   * @param cuts the array of intervals.
   * @return character corresponding to numeric value.
   */
  private char num2char(double value, double[] cuts) {
    int count = 0;
    while ((count < cuts.length) && (cuts[count] <= value)) {
      count++;
    }
    return ALPHABET[count];
  }

  protected static String toLogStr(int[] params, SAXNumerosityReductionStrategy strategy, double accuracy,
      double error) {
    StringBuffer sb = new StringBuffer();
    if (strategy.equals(SAXNumerosityReductionStrategy.CLASSIC)) {
      sb.append("CLASSIC,");
    }
    else if (strategy.equals(SAXNumerosityReductionStrategy.EXACT)) {
      sb.append("EXACT,");
    }
    else if (strategy.equals(SAXNumerosityReductionStrategy.NOREDUCTION)) {
      sb.append("NOREDUCTION,");
    }
    sb.append(params[0]).append(COMMA);
    sb.append(params[1]).append(COMMA);
    sb.append(params[2]).append(COMMA);
    sb.append(accuracy).append(COMMA);
    sb.append(error);

    return sb.toString();
  }

  /**
   * Mimics Matlab function for reshape: returns the m-by-n matrix B whose elements are taken
   * column-wise from A. An error results if A does not have m*n elements.
   * 
   * @param a the source matrix.
   * @param n number of rows in the new matrix.
   * @param m number of columns in the new matrix.
   * 
   * @return reshaped matrix.
   */
  public static double[][] reshape(double[][] a, int n, int m) {
    int cEl = 0;
    int aRows = a.length;

    double[][] res = new double[n][m];

    for (int j = 0; j < m; j++) {
      for (int i = 0; i < n; i++) {
        res[i][j] = a[cEl % aRows][cEl / aRows];
        cEl++;
      }
    }

    return res;
  }

  /**
   * Computes column means for the matrix.
   * 
   * @param a the input matrix.
   * @return result.
   */
  public static double[] colMeans(double[][] a) {
    double[] res = new double[a[0].length];
    for (int j = 0; j < a[0].length; j++) {
      double sum = 0;
      int counter = 0;
      for (int i = 0; i < a.length; i++) {
        if (Double.isNaN(a[i][j]) || Double.isInfinite(a[i][j])) {
          continue;
        }
        sum += a[i][j];
        counter += 1;
      }
      if (counter == 0) {
        res[j] = Double.NaN;
      }
      else {
        res[j] = sum / ((Integer) counter).doubleValue();
      }
    }
    return res;
  }
}
