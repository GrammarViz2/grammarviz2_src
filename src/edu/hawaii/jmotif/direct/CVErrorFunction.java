package edu.hawaii.jmotif.direct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import edu.hawaii.jmotif.sampler.Point;
import edu.hawaii.jmotif.sax.alphabet.Alphabet;
import edu.hawaii.jmotif.sax.alphabet.NormalAlphabet;
import edu.hawaii.jmotif.saxvsm.KNNOptimizedStackEntry;
import edu.hawaii.jmotif.text.SAXCollectionStrategy;
import edu.hawaii.jmotif.text.WordBag;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.util.StackTrace;

/**
 * This computes the classification error given the set of parameters.
 * 
 * @author psenin
 */
public class CVErrorFunction {

  /** The latin alphabet, lower case letters a-z. */
  private static final char[] ALPHABET = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
      'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

  private Alphabet a = new NormalAlphabet();

  private static final double NORMALIZATION_THRESHOLD = 0.05D;

  private SAXCollectionStrategy saxCollectionStrategy;

  private Map<String, List<double[]>> data;

  private int holdOutSampleSize;

  /**
   * Constructor.
   * 
   * @param data
   * @param holdOutSampleSize
   */
  public CVErrorFunction(Map<String, List<double[]>> data, int holdOutSampleSize,
      SAXCollectionStrategy strategy) {
    this.data = data;
    this.holdOutSampleSize = holdOutSampleSize;
    this.saxCollectionStrategy = strategy;
  }

  /**
   * Computes the value at point.
   * 
   * @param point
   * @return
   */
  public double valueAt(Point point) {

    double[] coords = point.toArray();

    int windowSize = Long.valueOf(Math.round(coords[0])).intValue();
    int paaSize = Long.valueOf(Math.round(coords[1])).intValue();
    int alphabetSize = Long.valueOf(Math.round(coords[2])).intValue();

    // if we stepped above window - return the max possible error value
    if (paaSize > windowSize) {
      return 1.0d;
    }

    try {
      // make a parameters vector
      int[][] params = new int[1][4];
      params[0][0] = windowSize;
      params[0][1] = paaSize;
      params[0][2] = alphabetSize;
      params[0][3] = this.saxCollectionStrategy.index();

      // push into stack all the samples we are going to validate for
      Stack<KNNOptimizedStackEntry> samples2go = new Stack<KNNOptimizedStackEntry>();
      for (Entry<String, List<double[]>> e : this.data.entrySet()) {
        String key = e.getKey();
        int index = 0;
        for (double[] sample : e.getValue()) {
          samples2go.push(new KNNOptimizedStackEntry(key, sample, index));
          index++;
        }
      }

      // total counter
      int totalSamples = samples2go.size();

      // missclassified counter
      int missclassifiedSamples = 0;

      // cache for word bags
      HashMap<String, WordBag> cache = new HashMap<String, WordBag>();

      // while something in stack
      while (!samples2go.isEmpty()) {

        // extracting validation samples batch
        //
        List<KNNOptimizedStackEntry> currentValidationSample = new ArrayList<KNNOptimizedStackEntry>();
        Set<Integer> currentValidationIndexes = new TreeSet<Integer>();

        for (int i = 0; i < this.holdOutSampleSize; i++) {
          // we can have less than a batch size for the last one, need to have this in place
          if (samples2go.isEmpty()) {
            break;
          }
          KNNOptimizedStackEntry sample = samples2go.pop();
          // questionable, but true. here we hold out only from a single class
          String cKey = sample.getKey();
          if (i > 0) {
            // if this one from the other class - push it back
            if (!(cKey.equalsIgnoreCase(currentValidationSample.get(i - 1).getKey()))) {
              samples2go.push(sample);
              break;
            }
          }
          currentValidationSample.add(sample);
          currentValidationIndexes.add(sample.getIndex());
        }

        // check if something in the validation sample
        //
        if (currentValidationSample.isEmpty()) {
          break;
        }

        // validation phase
        //
        String validationKey = currentValidationSample.get(0).getKey();

        // re-build bags if there is a need or pop them from the stack
        //
        for (Entry<String, List<double[]>> e : this.data.entrySet()) {

          // if there is a hit - need to rebuild that bag and replace it in the cache
          if (e.getKey().equalsIgnoreCase(validationKey)) {
            WordBag bag = new WordBag(validationKey);
            int index = -1;
            for (double[] series : e.getValue()) {
              index++;
              if (currentValidationIndexes.contains(index)) {
                // if (sampleContainsSeries(currentValidationSample, series)) {
                // System.out.println("bingo! ");
                // }
                // else {
                // System.out.println("Wrong! ");
                // System.exit(10);
                // }
                continue;
              }
              WordBag cb = seriesToWordBag("tmp", series, params, this.saxCollectionStrategy);
              bag.mergeWith(cb);
            }
            cache.put(validationKey, bag);
          }

          // else we just check if a bag is in place, if not - we put it in
          else {
            if (!cache.containsKey(e.getKey())) {
              WordBag bag = new WordBag(e.getKey());
              for (double[] series : e.getValue()) {
                WordBag cb = seriesToWordBag("tmp", series, params, this.saxCollectionStrategy);
                bag.mergeWith(cb);
              }
              cache.put(e.getKey(), bag);
            }
          }

        } // end of cache update loop

        // all stuff from the cache will build a classifier vectors
        //

        // compute TFIDF statistics for training set
        HashMap<String, HashMap<String, Double>> tfidf = computeTFIDF(cache.values());

        // normalize to unit vectors to avoid false discrimination by vector magnitude
        // tfidf = normalizeToUnitVectors(tfidf);

        // Classifying...
        //
        // is this sample correctly classified?
        for (KNNOptimizedStackEntry e : currentValidationSample) {
          int res = classify(e.getKey(), e.getValue(), tfidf, params, this.saxCollectionStrategy);
          if (0 == res) {
            missclassifiedSamples = missclassifiedSamples + 1;
          }
        }

      }

      double error = Integer.valueOf(missclassifiedSamples).doubleValue()
          / Integer.valueOf(totalSamples).doubleValue();

      return error;

    }
    catch (Exception e) {
      System.err.println("Exception caught: " + StackTrace.toString(e));
      return Double.MAX_VALUE;
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

          // double tfValue = Math.log(1.0D + Integer.valueOf(wordInBagFrequency).doubleValue());

          // double tfValue = 1.0D + Math.log(Integer.valueOf(wordInBagFrequency).doubleValue());
          // 0.5

          // double tfValue = normalizedTF(bag, word.getKey());
          // 0.46

          double tfValue = augmentedTF(bag, word.getKey());
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
  private double normalizedTF(WordBag bag, String term) {
    if (bag.contains(term)) {
      return Integer.valueOf(bag.getWordFrequency(term)).doubleValue()
          / Integer.valueOf(bag.getMaxFrequency()).doubleValue();
    }
    return 0;
  }

  private WordBag seriesToWordBag(String label, double[] series, int[][] params,
      SAXCollectionStrategy strategy) throws IndexOutOfBoundsException, TSException {

    WordBag resultBag = new WordBag(label);

    for (int[] p : params) {

      int windowSize = p[0];
      int paaSize = p[1];
      int alphabetSize = p[2];

      char[] oldStr = new char[0];
      for (int i = 0; i <= series.length - windowSize; i++) {

        double[] paa = paa(zNormalize(subseries(series, i, windowSize)), paaSize);

        char[] sax = ts2String(paa, a.getCuts(alphabetSize));

        if (SAXCollectionStrategy.CLASSIC.equals(strategy)) {
          if (oldStr.length > 0 && strSaxMinDistance(sax, oldStr) == 0) {
            continue;
          }
        }
        else if (SAXCollectionStrategy.EXACT.equals(strategy)) {
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
        // res = new double[len][paaSize];
        // for (int j = 0; j < len; j++) {
        // for (int i = 0; i < paaSize; i++) {
        // int idx = j * paaSize + i;
        // int row = idx % len;
        // int col = idx / len;
        // res[row][col] = ts[j];
        // }
        // }
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
  private double[][] reshape(double[][] a, int n, int m) {
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
      // res[j] = sum / ((Integer) a.length).doubleValue();
      res[j] = sum / ((double) a.length);
    }
    return res;
  }

  private int classify(String classKey, double[] series,
      HashMap<String, HashMap<String, Double>> tfidf, int[][] params, SAXCollectionStrategy strategy)
      throws IndexOutOfBoundsException, TSException {

    WordBag test = seriesToWordBag("test", series, params, strategy);

    double minDist = -1.0d;
    String className = "";
    double[] cosines = new double[tfidf.entrySet().size()];
    int index = 0;
    for (Entry<String, HashMap<String, Double>> e : tfidf.entrySet()) {
      double dist = cosineSimilarity(test, e.getValue());
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
