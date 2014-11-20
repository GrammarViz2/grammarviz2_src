package edu.hawaii.jmotif.direct;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.hawaii.jmotif.text.SAXNumerosityReductionStrategy;
import edu.hawaii.jmotif.text.TextUtils;
import edu.hawaii.jmotif.text.WordBag;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.util.UCRUtils;

/**
 * This implements a classifier.
 * 
 * @author psenin
 * 
 */
public class SAXVSMClassifier {

  private static final DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
  private static DecimalFormat fmt = new DecimalFormat("0.00###", otherSymbols);

  private static final String COMMA = ", ";

  private static String TRAINING_DATA;
  private static String TEST_DATA;

  private static Integer WINDOW_SIZE;
  private static Integer PAA_SIZE;
  private static Integer ALPHABET_SIZE;
  private static Map<String, List<double[]>> trainData;
  private static Map<String, List<double[]>> testData;
  private static SAXNumerosityReductionStrategy STRATEGY;

  // static block - we instantiate the logger
  //
  private static final Logger consoleLogger;
  private static final Level LOGGING_LEVEL = Level.INFO;

  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(SAXVSMClassifier.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  public static void main(String[] args) throws IOException, IndexOutOfBoundsException, TSException {

    try {
      // args: <train dataset>, <test dataset>, Wsize , Psize, Asize, Startegy
      consoleLogger.info("processing paramleters: " + Arrays.toString(args));

      TRAINING_DATA = args[0];
      TEST_DATA = args[1];
      trainData = UCRUtils.readUCRData(TRAINING_DATA);
      consoleLogger.info("trainData classes: " + trainData.size() + ", series length: "
          + trainData.entrySet().iterator().next().getValue().get(0).length);
      for (Entry<String, List<double[]>> e : trainData.entrySet()) {
        consoleLogger.info(" training class: " + e.getKey() + " series: " + e.getValue().size());
      }

      testData = UCRUtils.readUCRData(TEST_DATA);
      consoleLogger.info("testData classes: " + testData.size() + ", series length: "
          + testData.entrySet().iterator().next().getValue().get(0).length);
      for (Entry<String, List<double[]>> e : testData.entrySet()) {
        consoleLogger.info(" test class: " + e.getKey() + " series: " + e.getValue().size());
      }

      WINDOW_SIZE = Integer.valueOf(args[2]);
      PAA_SIZE = Integer.valueOf(args[3]);
      ALPHABET_SIZE = Integer.valueOf(args[4]);

      STRATEGY = SAXNumerosityReductionStrategy.valueOf(args[5].toUpperCase());

    }
    catch (Exception e) {
      System.err.println("There was parameters error....");
      System.exit(-10);
    }
    int[] params = new int[] { WINDOW_SIZE, PAA_SIZE, ALPHABET_SIZE, STRATEGY.index() };
    classify(params);
  }

  private static void classify(int[] params) throws IndexOutOfBoundsException, TSException {
    // making training bags collection
    List<WordBag> bags = TextUtils.labeledSeries2WordBags(trainData, params);
    // getting TFIDF done
    HashMap<String, HashMap<String, Double>> tfidf = TextUtils.computeTFIDF(bags);
    // classifying
    int testSampleSize = 0;
    int positiveTestCounter = 0;
    for (String label : tfidf.keySet()) {
      List<double[]> testD = testData.get(label);
      for (double[] series : testD) {
        positiveTestCounter = positiveTestCounter
            + TextUtils.classify(label, series, tfidf, params);
        testSampleSize++;
      }
    }

    // accuracy and error
    double accuracy = (double) positiveTestCounter / (double) testSampleSize;
    double error = 1.0d - accuracy;

    // report results
    consoleLogger.info("classification results: " + toLogStr(params, accuracy, error));

  }

  protected static String toLogStr(int[] p, double accuracy, double error) {

    StringBuffer sb = new StringBuffer();
    if (SAXNumerosityReductionStrategy.CLASSIC.index() == p[3]) {
      sb.append("CLASSIC, ");
    }
    else if (SAXNumerosityReductionStrategy.EXACT.index() == p[3]) {
      sb.append("EXACT, ");
    }
    else if (SAXNumerosityReductionStrategy.NOREDUCTION.index() == p[3]) {
      sb.append("NOREDUCTION, ");
    }
    sb.append("window ").append(p[0]).append(COMMA);
    sb.append("PAA ").append(p[1]).append(COMMA);
    sb.append("alphabet ").append(p[2]).append(COMMA);
    sb.append(" accuracy ").append(fmt.format(accuracy)).append(COMMA);
    sb.append(" error ").append(fmt.format(error));

    return sb.toString();
  }

}
