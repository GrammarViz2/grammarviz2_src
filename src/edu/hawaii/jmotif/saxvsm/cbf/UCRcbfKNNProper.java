package edu.hawaii.jmotif.saxvsm.cbf;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import edu.hawaii.jmotif.saxvsm.UCRGenericClassifier;
import edu.hawaii.jmotif.saxvsm.UCRUtils;
import edu.hawaii.jmotif.text.SAXNumerosityReductionStrategy;

/**
 * Helper-runner for CBF test.
 * 
 * @author psenin
 * 
 */
public class UCRcbfKNNProper extends UCRGenericClassifier {

  // data
  //
  private static final String TRAINING_DATA = "data/CBF/CBF_TRAIN";
  private static final String TEST_DATA = "data/CBF/CBF_TEST";

  // output prefix
  //
  private static final String outputPrefix = "cbf_nfold";

  // SAX parameters to use
  //
  private static final int WINDOW_MIN = 30;
  private static final int WINDOW_MAX = 30;
  private static final int WINDOW_INCREMENT = 5;

  private static final int PAA_MIN = 6;
  private static final int PAA_MAX = 6;
  private static final int PAA_INCREMENT = 2;

  private static final int ALPHABET_MIN = 4;
  private static final int ALPHABET_MAX = 4;
  private static final int ALPHABET_INCREMENT = 2;

  // leave out parameters
  //
  private static final int LEAVE_OUT_NUM = 1;

  private UCRcbfKNNProper() {
    super();
  }

  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {

    // configuring strategy
    //
    SAXNumerosityReductionStrategy strategy = SAXNumerosityReductionStrategy.EXACT;
    String strategyPrefix = "noreduction";
    if (args.length > 0) {
      String strategyP = args[0];
      if ("EXACT".equalsIgnoreCase(strategyP)) {
        strategy = SAXNumerosityReductionStrategy.EXACT;
        strategyPrefix = "exact";
      }
      if ("CLASSIC".equalsIgnoreCase(strategyP)) {
        strategy = SAXNumerosityReductionStrategy.CLASSIC;
        strategyPrefix = "classic";
      }
    }
    consoleLogger.debug("strategy: " + strategyPrefix + ", leaving out: " + LEAVE_OUT_NUM);

    // make up window sizes
    int[] window_sizes = makeArray(WINDOW_MIN, WINDOW_MAX, WINDOW_INCREMENT);

    // make up paa sizes
    int[] paa_sizes = makeArray(PAA_MIN, PAA_MAX, PAA_INCREMENT);

    // make up alphabet sizes
    int[] alphabet_sizes = makeArray(ALPHABET_MIN, ALPHABET_MAX, ALPHABET_INCREMENT);

    // reading training and test collections
    //
    Map<String, List<double[]>> trainData = UCRUtils.readUCRData(TRAINING_DATA);
    consoleLogger.debug("trainData classes: " + trainData.size() + ", series length: "
        + trainData.entrySet().iterator().next().getValue().get(0).length);
    for (Entry<String, List<double[]>> e : trainData.entrySet()) {
      consoleLogger.debug(" training class: " + e.getKey() + " series: " + e.getValue().size());
    }

    int totalTestSample = 0;
    Map<String, List<double[]>> testData = UCRUtils.readUCRData(TEST_DATA);
    consoleLogger.debug("testData classes: " + testData.size());
    for (Entry<String, List<double[]>> e : testData.entrySet()) {
      consoleLogger.debug(" test class: " + e.getKey() + " series: " + e.getValue().size());
      totalTestSample = totalTestSample + e.getValue().size();
    }

    List<String> results = trainKNNFoldJMotif(window_sizes, paa_sizes, alphabet_sizes, strategy,
        trainData, LEAVE_OUT_NUM);

    BufferedWriter bw = new BufferedWriter(new FileWriter(outputPrefix + "_" + strategyPrefix + "_"
        + LEAVE_OUT_NUM + ".csv"));
    
    for (String res : results) {
      bw.write(res + CR);
    }
    bw.close();

  }

}
