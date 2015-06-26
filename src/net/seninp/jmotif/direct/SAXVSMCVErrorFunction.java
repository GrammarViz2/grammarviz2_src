package net.seninp.jmotif.direct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.alphabet.Alphabet;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.text.WordBag;
import net.seninp.util.StackTrace;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * This computes the cross-validation classification error given the set of parameters.
 * 
 * @author psenin
 */
public class SAXVSMCVErrorFunction implements AbstractErrorFunction {

  public static final Character DELIMITER = '~';

  /** The latin alphabet, lower case letters a-z. */
  private static final char[] ALPHABET = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
      'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
  private Alphabet a = new NormalAlphabet();

  // the default normalization threshold
  private static final double NORMALIZATION_THRESHOLD = 0.05D;

  // the default numerosity strategy
  private NumerosityReductionStrategy numerosityReductionStrategy;

  // the data
  private Map<String, double[]> tsData;

  // the hold out sample size
  private int holdOutSampleSize;

  // static block - we instantiate the logger
  //
  private static final Logger consoleLogger;
  private static final Level LOGGING_LEVEL = Level.INFO;
  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(SAXVSMCVErrorFunction.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  /**
   * Constructor.
   * 
   * @param data
   * @param holdOutSampleSize
   */
  public SAXVSMCVErrorFunction(Map<String, List<double[]>> data, int holdOutSampleSize,
      NumerosityReductionStrategy strategy) {

    this.tsData = new HashMap<String, double[]>();

    for (Entry<String, List<double[]>> e : data.entrySet()) {
      String classLabel = e.getKey();
      int classCounter = 0;
      for (double[] series : e.getValue()) {
        this.tsData.put(classLabel + DELIMITER + classCounter, series);
        classCounter++;
      }
    }

    this.holdOutSampleSize = holdOutSampleSize;
    this.numerosityReductionStrategy = strategy;
  }

  /**
   * Computes the value at point.
   * 
   * @param point
   * @return
   */
  public double valueAt(Point point) {

    // point is in fact a aset of parameters - window, paa, and the alphabet
    //
    double[] coords = point.toArray();
    int windowSize = Long.valueOf(Math.round(coords[0])).intValue();
    int paaSize = Long.valueOf(Math.round(coords[1])).intValue();
    int alphabetSize = Long.valueOf(Math.round(coords[2])).intValue();

    // if we stepped above window length with PAA size - for some reason - return the max possible
    // error value
    if (paaSize > windowSize) {
      return 1.0d;
    }

    // the whole thing begins here
    //
    try {

      // make a parameters vector
      int[][] params = new int[1][4];
      params[0][0] = windowSize;
      params[0][1] = paaSize;
      params[0][2] = alphabetSize;
      params[0][3] = this.numerosityReductionStrategy.index();
      consoleLogger.debug("parameters: " + windowSize + ", " + paaSize + ", " + alphabetSize + ", "
          + this.numerosityReductionStrategy.toString());

      // cache for word bags
      HashMap<String, WordBag> seriesBags = new HashMap<String, WordBag>();

      // the class series bags
      HashMap<String, WordBag> bags = new HashMap<String, WordBag>();

      // push into stack all the samples we are going to validate for
      Stack<String> samples2go = new Stack<String>();
      for (Entry<String, double[]> e : this.tsData.entrySet()) {

        String seriesKey = e.getKey();
        String classLabel = seriesKey.substring(0, seriesKey.indexOf(DELIMITER));
        double[] series = e.getValue();

        WordBag seriesBag = seriesToWordBag(seriesKey, series, params,
            this.numerosityReductionStrategy);

        samples2go.push(seriesKey);

        seriesBags.put(seriesKey, seriesBag);

        WordBag classBag = bags.get(classLabel);
        if (null == classBag) {
          classBag = new WordBag(classLabel);
          bags.put(classLabel, classBag);
        }
        classBag.mergeWith(seriesBag);

      }
      Collections.shuffle(samples2go);
      consoleLogger.debug("series: " + seriesBags.keySet().toString());
      consoleLogger.debug("samples2go: " + samples2go.toString());

      // total counter
      int totalSamples = samples2go.size();

      // missclassified counter
      int missclassifiedSamples = 0;

      // while something is in the stack
      while (!samples2go.isEmpty()) {

        consoleLogger
            .debug("cross valiadtion iteration, in stack " + samples2go.size() + " series");

        // extracting validation samples batch and building to remove collection
        //
        HashMap<String, WordBag> wordsToRemove = new HashMap<String, WordBag>();
        List<String> currentValidationSample = new ArrayList<String>();
        for (int i = 0; i < this.holdOutSampleSize && !samples2go.isEmpty(); i++) {

          String seriesKey = samples2go.pop();
          String classLabel = seriesKey.substring(0, seriesKey.indexOf(DELIMITER));
          currentValidationSample.add(seriesKey);

          WordBag classBag = wordsToRemove.get(classLabel);
          if (null == classBag) {
            classBag = new WordBag(classLabel);
            wordsToRemove.put(classLabel, classBag);
          }
          classBag.mergeWith(seriesBags.get(seriesKey));

        }

        consoleLogger.debug("cross valiadtion sample: " + currentValidationSample.toString());

        // adjust word bags
        //
        HashMap<String, WordBag> basisBags = adjustWordBags(bags, wordsToRemove);

        // validation phase
        //
        // all stuff from the cache will build a classifier vectors
        //
        // compute TFIDF statistics for training set
        HashMap<String, HashMap<String, Double>> tfidf = computeTFIDF(basisBags.values());

        // Classifying...
        // is this sample correctly classified?
        for (String e : currentValidationSample) {
          String trueClassLabel = e.substring(0, e.indexOf(DELIMITER));
          int res = classify(trueClassLabel, seriesBags.get(e), tfidf,
              this.numerosityReductionStrategy);
          if (0 == res) {
            missclassifiedSamples = missclassifiedSamples + 1;
          }
        }

      }

      double error = Integer.valueOf(missclassifiedSamples).doubleValue()
          / Integer.valueOf(totalSamples).doubleValue();

      consoleLogger.debug("## " + Arrays.toString(params[0]) + ", " + error);
      return error;

    }
    catch (Exception e) {
      System.err.println("Exception caught: " + StackTrace.toString(e));
      return Double.MAX_VALUE;
    }

  }

  private HashMap<String, WordBag> adjustWordBags(HashMap<String, WordBag> bags,
      HashMap<String, WordBag> wordsToRemove) {

    HashMap<String, WordBag> res = new HashMap<String, WordBag>();
    for (Entry<String, WordBag> e : bags.entrySet()) {
      res.put(e.getKey(), e.getValue().clone());
    }

    for (Entry<String, WordBag> e : wordsToRemove.entrySet()) {
      String classKey = e.getKey();
      for (Entry<String, AtomicInteger> eBag : e.getValue().getInternalWords().entrySet()) {
        res.get(classKey).addWord(eBag.getKey(), -eBag.getValue().intValue());
      }
    }

    return res;
  }

  private int classify(String trueClassKey, WordBag testBag,
      HashMap<String, HashMap<String, Double>> tfidf, SAXNumerosityReductionStrategy strategy) {

    double minDist = -1.0d;
    String className = "";
    double[] cosines = new double[tfidf.entrySet().size()];
    int index = 0;
    for (Entry<String, HashMap<String, Double>> e : tfidf.entrySet()) {
      double dist = cosineSimilarity(testBag, e.getValue());
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

    if (!(allEqual) && className.equalsIgnoreCase(trueClassKey)) {
      return 1;
    }
    return 0;
  }

  /**
   * Classifies the timeseries.
   * 
   * @param trueClassKey
   * @param series
   * @param tfidf
   * @param params
   * @param strategy
   * @return
   * @throws IndexOutOfBoundsException
   * @throws TSException
   */
  @SuppressWarnings("unused")
  private int classify(String trueClassKey, double[] series,
      HashMap<String, HashMap<String, Double>> tfidf, int[][] params,
      SAXNumerosityReductionStrategy strategy) throws IndexOutOfBoundsException, TSException {
    WordBag testBag = seriesToWordBag("test", series, params, strategy);
    return classify(trueClassKey, testBag, tfidf, strategy);
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
    HashMap<String, AtomicInteger> allWords = new HashMap<String, AtomicInteger>();
    for (WordBag bag : texts) {

      // here populate result map with empty entries
      res.put(bag.getLabel(), new HashMap<String, Double>());

      // and get those words
      for (Entry<String, AtomicInteger> e : bag.getInternalWords().entrySet()) {

        if (allWords.containsKey(e.getKey())) {
          allWords.get(e.getKey()).incrementAndGet();
        }
        else {
          allWords.put(e.getKey(), new AtomicInteger(1));
        }
      }

    }

    // outer loop - iterating over documents
    for (WordBag bag : texts) {

      // fix the doc name
      String bagName = bag.getLabel();
      HashMap<String, AtomicInteger> bagWords = bag.getInternalWords(); // these are words of
                                                                        // documents

      // what we want to do for TF*IDF is to compute it for all WORDS ever seen in set
      //
      for (Entry<String, AtomicInteger> word : allWords.entrySet()) {

        // by default it is zero
        //
        double tfidf = 0;

        // if this document contains the word - here we go
        if (bagWords.containsKey(word.getKey()) & (totalDocs != word.getValue().intValue())) {

          int wordInBagFrequency = bagWords.get(word.getKey()).intValue();

          // compute TF: we take a log and correct for 0 by adding 1

          double tfValue = Math.log(1.0D + Integer.valueOf(wordInBagFrequency).doubleValue());

          // double tfValue = 1.0D + Math.log(Integer.valueOf(wordInBagFrequency).doubleValue());
          // 0.5

          // double tfValue = normalizedTF(bag, word.getKey());
          // 0.46

          // double tfValue = augmentedTF(bag, word.getKey());
          // 0.5
          // double tfValue = logAveTF(bag, word.getKey());
          // 0.5
          // compute the IDF
          //
          double idfLOGValue = Math.log10(Integer.valueOf(totalDocs).doubleValue()
              / word.getValue().doubleValue());

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
   * Compute TF (term frequency) metrics. This is normalized TF without bias towards longer
   * documents.
   * 
   * @param bag The words bag.
   * @param term The term.
   * @return The term frequency value.
   */
  private double augmentedTF(WordBag bag, String term) {
    if (bag.contains(term)) {
      return 0.5D + (Integer.valueOf(bag.getWordFrequency(term)).doubleValue())
          / (2.0D * Integer.valueOf(bag.getMaxFrequency()).doubleValue());
    }
    return 0;
  }

  /**
   * Compute TF (term frequency) metrics. This is logarithmically scaled TF.
   * 
   * @param bag The words bag.
   * @param term The term.
   * @return The term frequency value.
   */
  @SuppressWarnings("unused")
  private double logTF(WordBag bag, String term) {
    if (bag.contains(term)) {
      return 1.0d + Math.log(bag.getWordFrequency(term).doubleValue());
    }
    return 0d;
  }

  /**
   * Compute TF (term frequency) metrics. This is normalized TF without bias towards longer
   * documents.
   * 
   * @param bag The words bag.
   * @param term The term.
   * @return The term frequency value.
   */
  @SuppressWarnings("unused")
  private double logAveTF(WordBag bag, String term) {
    if (bag.contains(term)) {
      return (1D + Math.log(Integer.valueOf(bag.getWordFrequency(term)).doubleValue()))
          / (1D + Math.log(bag.getAverageFrequency()));
    }
    return 0;
  }

  /**
   * Compute TF (term frequency) metrics. This is normalized TF without bias towards longer
   * documents.
   * 
   * @param bag The words bag.
   * @param term The term.
   * @return The term frequency value.
   */
  @SuppressWarnings("unused")
  private double normalizedTF(WordBag bag, String term) {
    if (bag.contains(term)) {
      return Integer.valueOf(bag.getWordFrequency(term)).doubleValue()
          / Integer.valueOf(bag.getMaxFrequency()).doubleValue();
    }
    return 0;
  }

  private WordBag seriesToWordBag(String label, double[] series, int[][] params,
      SAXNumerosityReductionStrategy strategy) throws IndexOutOfBoundsException, TSException {

    WordBag resultBag = new WordBag(label);

    for (int[] p : params) {

      int windowSize = p[0];
      int paaSize = p[1];
      int alphabetSize = p[2];

      char[] oldStr = new char[0];
      for (int i = 0; i <= series.length - windowSize; i++) {

        double[] paa = paa(zNormalize(subseries(series, i, windowSize)), paaSize);

        char[] sax = ts2String(paa, a.getCuts(alphabetSize));

        if (SAXNumerosityReductionStrategy.CLASSIC.equals(strategy)) {
          if (oldStr.length > 0 && strSaxMinDistance(sax, oldStr) == 0) {
            continue;
          }
        }
        else if (SAXNumerosityReductionStrategy.EXACT.equals(strategy)) {
          if (Arrays.equals(oldStr, sax)) {
            continue;
          }
        }

        oldStr = sax;

        resultBag.addWord(String.valueOf(sax));
      }
    }

    return resultBag;
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

  /**
   * Computes the vector's magnitude.
   * 
   * @param values
   * @return
   */
  private double magnitude(Collection<Double> values) {
    double res = 0.0D;
    for (Double v : values) {
      res = res + (double) v * (double) v;
    }
    return Math.sqrt(res);
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
      if (len % paaSize == 0) {
        return colMeans(reshape(asMatrix(ts), len / paaSize, paaSize));
      }
      else {
        double[] paa = new double[paaSize];
        for (int i = 0; i < len * paaSize; i++) {
          int idx = i / len; // the spot
          int pos = i / paaSize; // the col spot
          paa[idx] = paa[idx] + ts[pos];
        }
        for (int i = 0; i < paaSize; i++) {
          paa[i] = paa[i] / (double) len;
        }
        return paa;
      }
    }
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
  private static double[][] reshape(double[][] a, int n, int m) {
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
  private double[] colMeans(double[][] a) {
    double[] res = new double[a[0].length];
    for (int j = 0; j < a[0].length; j++) {
      double sum = 0;
      for (int i = 0; i < a.length; i++) {
        sum += a[i][j];
      }
      res[j] = sum / ((double) a.length);
    }
    return res;
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
    double[] res = new double[series.length];
    double mean = mean(series);
    double sd = stDev(series);
    if (sd < NORMALIZATION_THRESHOLD) {
      return series.clone();
    }
    for (int i = 0; i < res.length; i++) {
      res[i] = (series[i] - mean) / sd;
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
      res += tp;
      count += 1;
    }
    return res / ((Integer) count).doubleValue();
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
      num0 = num0 + tp * tp;
      sum = sum + tp;
      count += 1;
    }
    double len = ((Integer) count).doubleValue();
    return Math.sqrt((len * num0 - sum * sum) / (len * (len - 1)));
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
    return Arrays.copyOfRange(series, start, start + length);
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

  /**
   * Compute the distance between the two strings, this function use the numbers associated with
   * ASCII codes, i.e. distance between a and b would be 1.
   * 
   * @param a The first string.
   * @param b The second string.
   * @return The pairwise distance.
   * @throws TSException if length are differ.
   */
  private int strSaxMinDistance(char[] a, char[] b) throws TSException {
    if (a.length == b.length) {
      int distance = 0;
      for (int i = 0; i < a.length; i++) {
        int tDist = Math.abs(Character.getNumericValue(a[i]) - Character.getNumericValue(b[i]));
        if (tDist > 1) {
          distance += tDist;
        }
      }
      return distance;
    }
    else {
      throw new TSException("Unable to compute SAX distance, string lengths are not equal");
    }
  }

}
