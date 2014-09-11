package edu.hawaii.jmotif.saxvsm.cbf;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import edu.hawaii.jmotif.sax.alphabet.Alphabet;
import edu.hawaii.jmotif.sax.alphabet.NormalAlphabet;
import edu.hawaii.jmotif.saxvsm.TfIdfEntryComparator;
import edu.hawaii.jmotif.saxvsm.UCRGenericClassifier;
import edu.hawaii.jmotif.saxvsm.UCRUtils;
import edu.hawaii.jmotif.text.SAXCollectionStrategy;
import edu.hawaii.jmotif.text.TextUtils;
import edu.hawaii.jmotif.text.WordBag;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.timeseries.TSUtils;

/**
 * Hunts for best scoring patterns for the class and prints them out.
 * 
 * @author psenin
 * 
 */
public class UCRcbfPatternsHunter extends UCRGenericClassifier {

  // prefix for all of the output
  private static final String TRAINING_DATA = "data/CBF/CBF_TRAIN";
  private static final String TEST_DATA = "data/CBF/CBF_TEST";

  // SAX parameters to use
  //
  private static final int[][] params = { { 55, 4, 5, NOREDUCTION } };

  // defines the amount of words from each class's vector to print
  //
  private static final int MAX_WORDS_2_PRINT = 0;

  // how many patterns and how many series to output
  //
  private static final int MAX_SERIES_2PRINT = 20;
  private static final int MAX_PATTERNS_2PRINT = 5;

  private static Logger consoleLogger;

  private static String LOGGING_LEVEL = "FINE";

  private static final DecimalFormat df = new DecimalFormat("0.00##");

  /**
   * @param args
   * @throws TSException
   * @throws IndexOutOfBoundsException
   * @throws IOException
   */
  public static void main(String[] args) throws IndexOutOfBoundsException, TSException, IOException {

    // making training and test collections
    Map<String, List<double[]>> trainData = UCRUtils.readUCRData(TRAINING_DATA);
    Map<String, List<double[]>> testData = UCRUtils.readUCRData(TEST_DATA);

    // loop over parameters
    for (int[] p : params) {

      // extract parameters
      int WINDOW_SIZE = p[0];
      int PAA_SIZE = p[1];
      int ALPHABET_SIZE = p[2];
      SAXCollectionStrategy strategy = SAXCollectionStrategy.CLASSIC;
      if (EXACT == p[3]) {
        strategy = SAXCollectionStrategy.EXACT;
      }
      else if (NOREDUCTION == p[3]) {
        strategy = SAXCollectionStrategy.NOREDUCTION;
      }

      // making training bags collection
      List<WordBag> bags = TextUtils.labeledSeries2WordBags(trainData, PAA_SIZE, ALPHABET_SIZE,
          WINDOW_SIZE, strategy);

      // get tfidf statistics
      HashMap<String, HashMap<String, Double>> tfidf = TextUtils.computeTFIDF(bags);

      // normalize all vectors to a unit - so it will be fair comparison
      tfidf = TextUtils.normalizeToUnitVectors(tfidf);

      // sort words by their weight and print top 10 of these for each class
      for (Entry<String, HashMap<String, Double>> e : tfidf.entrySet()) {
        String className = e.getKey();
        ArrayList<Entry<String, Double>> values = new ArrayList<Entry<String, Double>>();
        values.addAll(e.getValue().entrySet());

        Collections.sort(values, new TfIdfEntryComparator());

        System.out.print("Class key: " + className + CR);
        for (int i = 0; i < MAX_WORDS_2_PRINT; i++) {
          String pattern = values.get(i).getKey();
          Double weight = values.get(i).getValue();
          System.out.println("\"" + pattern + "\", " + weight);
        }

      }

      // get best patterns for each class
      //
      // iterating over each of classes
      //
      for (Entry<String, HashMap<String, Double>> e : tfidf.entrySet()) {

        // class name and weights vector
        String className = e.getKey();
        ArrayList<Entry<String, Double>> values = new ArrayList<Entry<String, Double>>();
        values.addAll(e.getValue().entrySet());

        // form the output
        Collections.sort(values, new TfIdfEntryComparator());
        System.out.print("Class key: " + className + CR);

        for (int i = 0; i < MAX_PATTERNS_2PRINT; i++) {

          String pattern = values.get(i).getKey();
          Double weight = values.get(i).getValue();
          System.out.println("pattern=\"" + pattern + "\"; weight=" + df.format(weight));

          StringBuffer seriesBuff = new StringBuffer("series = c(");
          StringBuffer offsetBuff = new StringBuffer("offsets = c(");
          Map<Integer, Integer[]> hits = getPatternLocationsForTheClass(className, trainData,
              pattern, WINDOW_SIZE, PAA_SIZE, ALPHABET_SIZE);

          int k = 0;
          int printedK = 0;
          do {
            if (hits.get(k).length > 0) {
              System.out.print(k + ": " + Arrays.toString(hits.get(k)) + ", ");
              System.out.println(Arrays.toString(trainData.get(className).get(k)));
              System.out.println(Arrays.toString(seriesValuesAsHeat(
                  trainData.get(className).get(k), className, tfidf, WINDOW_SIZE, PAA_SIZE,
                  ALPHABET_SIZE)));
              for (int offset : hits.get(k)) {
                seriesBuff.append(String.valueOf(k + 1) + ",");
                offsetBuff.append(String.valueOf(offset + 1) + ",");
              }
              printedK++;
            }
            k++;
          }
          while (k < hits.size() && printedK < MAX_SERIES_2PRINT);

          System.out.print(seriesBuff.delete(seriesBuff.length() - 1, seriesBuff.length())
              .toString() + ")" + CR);
          System.out.print(offsetBuff.delete(offsetBuff.length() - 1, offsetBuff.length())
              .toString() + ")" + CR + "#" + CR);

        }

        System.out.print("Missclassified for Class key: " + className + CR);
        List<double[]> testD = testData.get(className);
        int seriesIdx = 0;
        for (double[] series : testD) {
          int classificationResult = TextUtils.classify(className, series, tfidf, PAA_SIZE,
              ALPHABET_SIZE, WINDOW_SIZE, strategy);
          if (0 == classificationResult) {
            System.out.println(seriesIdx + 1);
          }
          seriesIdx++;
        }
        System.out.println(" ============== ");
      }

    }
  }

  private static double[] seriesValuesAsHeat(double[] series, String className,
      HashMap<String, HashMap<String, Double>> tfidf, int window_size, int paa_size,
      int alphabet_size) throws TSException {

    Alphabet a = new NormalAlphabet();

    double[] weights = new double[series.length];
    HashMap<String, Integer> words = new HashMap<String, Integer>();

    for (int i = 0; i <= series.length - window_size; i++) {
      double[] subseries = TSUtils.subseries(series, i, window_size);
      double[] paa = TSUtils.paa(TSUtils.zNormalize(subseries), paa_size);
      char[] sax = TSUtils.ts2String(paa, a.getCuts(alphabet_size));
      words.put(String.valueOf(sax), i);
    }

    for (Entry<String, HashMap<String, Double>> e : tfidf.entrySet()) {
      for (Entry<String, Double> e1 : e.getValue().entrySet()) {
        if (words.containsKey(e1.getKey())) {
          double increment = 0.0;
          if (className.equalsIgnoreCase(e.getKey())) {
            increment = e1.getValue();
          }
          else {
             increment = -e1.getValue();
          }
          for (int i = 0; i < window_size; i++) {
            weights[i] = weights[i] + increment;
          }
        }
      }
    }

    return weights;

  }

  private static Map<Integer, Integer[]> getPatternLocationsForTheClass(String className,
      Map<String, List<double[]>> trainData, String pattern, int windowSize, int paaSize,
      int alphabetSize) throws IndexOutOfBoundsException, TSException {

    Alphabet a = new NormalAlphabet();

    Map<Integer, Integer[]> res = new HashMap<Integer, Integer[]>();

    int seriesCounter = 0;
    for (double[] series : trainData.get(className)) {

      List<Integer> arr = new ArrayList<Integer>();

      for (int i = 0; i <= series.length - windowSize; i++) {
        double[] paa = TSUtils.paa(TSUtils.zNormalize(TSUtils.subseries(series, i, windowSize)),
            paaSize);
        char[] sax = TSUtils.ts2String(paa, a.getCuts(alphabetSize));
        if (pattern.equalsIgnoreCase(String.valueOf(sax))) {
          arr.add(i);
        }
      }

      res.put(seriesCounter, arr.toArray(new Integer[0]));
      seriesCounter++;
    }

    return res;
  }
}
