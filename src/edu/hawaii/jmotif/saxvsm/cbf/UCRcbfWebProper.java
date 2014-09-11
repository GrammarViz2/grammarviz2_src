package edu.hawaii.jmotif.saxvsm.cbf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.hawaii.jmotif.saxvsm.UCRGenericClassifier;
import edu.hawaii.jmotif.saxvsm.UCRUtils;
import edu.hawaii.jmotif.text.SAXCollectionStrategy;
import edu.hawaii.jmotif.text.TextUtils;
import edu.hawaii.jmotif.text.WordBag;

/**
 * Helper-runner for CBF test.
 * 
 * @author psenin
 * 
 */
public class UCRcbfWebProper extends UCRGenericClassifier {

  // data locations
  //
  private static final String TRAINING_DATA = "data/CBF/CBF_TRAIN";
  private static final String TEST_DATA = "data/CBF/CBF_TEST";

  // SAX parameters to iterate over
  //
  private static final int[][] params = {
    { 42, 5, 5, NOREDUCTION }, { 42, 5, 5, EXACT }, { 42, 5, 5, CLASSIC },
    { 55, 4, 12, NOREDUCTION }, { 55, 4, 12, EXACT }, { 55, 4, 12, CLASSIC } };

  /**
   * Runnable.
   * 
   * @throws Exception if error occurs.
   */
  public static void main(String[] args) throws Exception {

    // making training and test collections
    //
    Map<String, List<double[]>> trainData = UCRUtils.readUCRData(TRAINING_DATA);
    Map<String, List<double[]>> testData = UCRUtils.readUCRData(TEST_DATA);

    // iterate over parameters
    //
    for (int[] p : params) {

      // converting back from easy encoding
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

      // getting TFIDF done
      HashMap<String, HashMap<String, Double>> tfidf = TextUtils.computeTFIDF(bags);

      // System.out.println(TextUtils.bagsToTable(bags));

      // normalize vectors
      tfidf = TextUtils.normalizeToUnitVectors(tfidf);

      // pairwise words
      int areaBell = 0;
      int areaFunnel = 0;
      int areaCylinder = 0;
      int nCB = 0;
      int nCF = 0;
      int nBF = 0;
      int nCBF = 0;

      for (String w : tfidf.get("1").keySet()) {
        // cylinder
        if (tfidf.get("1").get(w) > 0) {
          areaCylinder++;
        }
        // bell
        if (tfidf.get("2").get(w) > 0) {
          areaBell++;
        }
        // funnel
        if (tfidf.get("3").get(w) > 0) {
          areaFunnel++;
        }
        // pairs
        if (tfidf.get("1").get(w) > 0 && tfidf.get("2").get(w) > 0) {
          nCB++;
        }
        if (tfidf.get("2").get(w) > 0 && tfidf.get("3").get(w) > 0) {
          nBF++;
        }
        if (tfidf.get("1").get(w) > 0 && tfidf.get("3").get(w) > 0) {
          nCF++;
        }
        // all together
        if (tfidf.get("1").get(w) > 0 && tfidf.get("2").get(w) > 0 && tfidf.get("3").get(w) > 0) {
          nCBF++;
        }
      }

//      System.out.println("areaBell=" + areaBell);
//      System.out.println("areaFunnel=" + areaFunnel);
//      System.out.println("areaCylinder=" + areaCylinder);
//      System.out.println("nCB=" + nCB);
//      System.out.println("nCF=" + nCF);
//      System.out.println("nBF=" + nBF);
//      System.out.println("nCBF=" + nCBF);

      // System.out.println(TextUtils.tfidfToTable(tfidf));

      // classifying
      int testSampleSize = 0;
      int positiveTestCounter = 0;

      for (String label : tfidf.keySet()) {
        List<double[]> testD = testData.get(label);
        for (double[] series : testD) {
          positiveTestCounter = positiveTestCounter
              + TextUtils.classify(label, series, tfidf, PAA_SIZE, ALPHABET_SIZE, WINDOW_SIZE,
                  strategy);
          testSampleSize++;
        }
      }

      // accuracy and error
      double accuracy = (double) positiveTestCounter / (double) testSampleSize;
      double error = 1.0d - accuracy;

      // report results
      System.out.println(toLogStr(p, accuracy, error));
    }

  }

}
