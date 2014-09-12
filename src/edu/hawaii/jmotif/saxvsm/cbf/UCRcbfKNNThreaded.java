package edu.hawaii.jmotif.saxvsm.cbf;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import edu.hawaii.jmotif.saxvsm.UCRGenericClassifier;
import edu.hawaii.jmotif.saxvsm.UCRUtils;
import edu.hawaii.jmotif.text.SAXCollectionStrategy;

/**
 * Helper-runner for CBF test.
 * 
 * @author psenin
 * 
 */
public class UCRcbfKNNThreaded extends UCRGenericClassifier {

  // num of threads to use
  //
  private static final int THREADS_NUM = 16;

  // data
  //
  private static final String TRAINING_DATA = "data/CBF/CBF_TRAIN";
  private static final String TEST_DATA = "data/CBF/CBF_TEST";

  // output prefix
  //
  private static final String outputPrefix = "cbf_loocv_generated_5";

  // SAX parameters to use
  //
  private static final int WINDOW_MIN = 20;
  private static final int WINDOW_MAX = 30;
  private static final int WINDOW_INCREMENT = 1;

  private static final int PAA_MIN = 6;
  private static final int PAA_MAX = 6;
  private static final int PAA_INCREMENT = 2;

  private static final int ALPHABET_MIN = 4;
  private static final int ALPHABET_MAX = 4;
  private static final int ALPHABET_INCREMENT = 2;

  // leave out parameters
  //
  private static final int LEAVE_OUT_NUM = 1;

  private static final int SERIES_LENGTH = 128;

  private UCRcbfKNNThreaded() {
    super();
  }

  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {

    // configuring strategy
    //
    SAXCollectionStrategy strategy = SAXCollectionStrategy.NOREDUCTION;
    String strategyPrefix = "noreduction";
    if (args.length > 0) {
      String strategyP = args[0];
      if ("EXACT".equalsIgnoreCase(strategyP)) {
        strategy = SAXCollectionStrategy.EXACT;
        strategyPrefix = "exact";
      }
      if ("CLASSIC".equalsIgnoreCase(strategyP)) {
        strategy = SAXCollectionStrategy.CLASSIC;
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

//    Map<String, List<double[]>> trainData = generateSample(5);

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

    List<String> result = trainKNNFoldJMotifThreaded(THREADS_NUM, window_sizes, paa_sizes,
        alphabet_sizes, strategy, trainData, LEAVE_OUT_NUM);

    BufferedWriter bw = new BufferedWriter(new FileWriter(outputPrefix + "_" + strategyPrefix + "_"
        + LEAVE_OUT_NUM + ".csv"));

    for (String line : result) {
      bw.write(line + CR);
    }
    bw.close();

  }

  private static Map<String, List<double[]>> generateSample(int sampleSize) {

    Map<String, List<double[]>> res = new HashMap<String, List<double[]>>();

    // ticks
    int[] t = new int[SERIES_LENGTH];
    for (int i = 0; i < SERIES_LENGTH; i++) {
      t[i] = i;
    }

    // cylinder sample
    List<double[]> cylinders = new ArrayList<double[]>();
    for (int i = 0; i < sampleSize; i++) {
      cylinders.add(CBFGenerator.cylinder(t));
    }
    res.put("1", cylinders);

    // bell sample
    List<double[]> bells = new ArrayList<double[]>();
    for (int i = 0; i < sampleSize; i++) {
      bells.add(CBFGenerator.bell(t));
    }
    res.put("2", bells);

    // funnel sample
    List<double[]> funnels = new ArrayList<double[]>();
    for (int i = 0; i < sampleSize; i++) {
      funnels.add(CBFGenerator.funnel(t));
    }
    res.put("3", funnels);

    return res;
  }

}
