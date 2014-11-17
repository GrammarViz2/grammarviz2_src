package edu.hawaii.jmotif.direct;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.hawaii.jmotif.saxvsm.UCRGenericClassifier;
import edu.hawaii.jmotif.saxvsm.UCRUtils;
import edu.hawaii.jmotif.text.SAXNumerosityReductionStrategy;
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
  // private static final String TRAINING_DATA = "data/cbf/CBF_TRAIN";
  // private static final String TEST_DATA = "data/cbf/CBF_TEST";
  private static final String TRAINING_DATA = "data/Beef/Beef_TRAIN";
  private static final String TEST_DATA = "data/Beef/Beef_TEST";
  // private static final String TRAINING_DATA = "data/synthetic_control/synthetic_control_TRAIN";
  // private static final String TEST_DATA = "data/synthetic_control/synthetic_control_TEST";
  // SAX parameters to iterate over
  //
  private static final int[][] params = { { 393, 35, 9, NOREDUCTION }, { 393, 35, 9, EXACT },
      { 393, 35, 9, CLASSIC }, { 60, 11, 6, NOREDUCTION }, { 60, 11, 6, EXACT },
      { 60, 11, 6, CLASSIC }, };

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
      SAXNumerosityReductionStrategy strategy = SAXNumerosityReductionStrategy.CLASSIC;
      if (EXACT == p[3]) {
        strategy = SAXNumerosityReductionStrategy.EXACT;
      }
      else if (NOREDUCTION == p[3]) {
        strategy = SAXNumerosityReductionStrategy.NOREDUCTION;
      }

      // making training bags collection
      List<WordBag> bags = TextUtils.labeledSeries2WordBags(trainData, PAA_SIZE, ALPHABET_SIZE,
          WINDOW_SIZE, strategy);

      // getting TFIDF done
      HashMap<String, HashMap<String, Double>> tfidf = TextUtils.computeTFIDF(bags);

      // System.out.println(TextUtils.bagsToTable(bags));

      // normalize vectors
      // tfidf = TextUtils.normalizeToUnitVectors(tfidf);

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
