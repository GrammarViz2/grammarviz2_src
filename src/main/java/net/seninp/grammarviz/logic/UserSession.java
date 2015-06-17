package edu.hawaii.jmotif.grammarviz.logic;

import java.io.File;
import java.io.IOException;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.util.StackTrace;

/**
 * Keeps user parameters organized.
 * 
 * @author psenin
 * 
 */
public class UserSession {

  /** Default normalization threshold. */
  public static final Double DEFAULT_NORMALIZATION_THRESHOLD_VALUE = 0.05;

  /** Default GI algorithm implementation. */
  public static final int DEFAULT_GI_ALGORITHM = 0;
  public static final CoverageCountStrategy DEFAULT_COUNT_STRATEGY = CoverageCountStrategy.COUNT;

  public static boolean DEFAULT_SLIDING_WINDOW = true;

  public static final int DEFAULT_SAX_WINDOW = 170;
  public static final int DEFAULT_SAX_PAA = 4;
  public static final int DEFAULT_SAX_ALPHABET = 4;

  private boolean useSlidingWindow;
  private NumerosityReductionStrategy numerosityReductionStrategy;
  private int saxWindow;
  private int saxPAA;
  private int saxAlphabet;

  private String ruleDensityOutputFileName;
  private String grammarOutputFileName;
  private String anomaliesOutputFileName;
  private String chartsSaveFolder;
  private CoverageCountStrategy countStrategy;
  private int giAlgorithm;
  private Double normalizationThreshold;

  public UserSession() {
    super();
    this.normalizationThreshold = DEFAULT_NORMALIZATION_THRESHOLD_VALUE;
    this.giAlgorithm = DEFAULT_GI_ALGORITHM;
    this.countStrategy = DEFAULT_COUNT_STRATEGY;
    this.useSlidingWindow = DEFAULT_SLIDING_WINDOW;
    this.saxWindow = DEFAULT_SAX_WINDOW;
    this.saxPAA = DEFAULT_SAX_PAA;
    this.saxAlphabet = DEFAULT_SAX_ALPHABET;
    //
    // attempt to fill the rule coverage name automatically
    String filename = "";
    try {
      String currentPath = new File(".").getCanonicalPath();
      filename = currentPath + File.separator + "density_curve.txt";
    }
    catch (IOException e) {
      System.err.println("Error has been thrown, unable to findout the current path: "
          + StackTrace.toString(e));
    }
    this.ruleDensityOutputFileName = filename;
  }

  public String getRuleDensityOutputFileName() {
    return ruleDensityOutputFileName;
  }

  public void setRuleDensityOutputFileName(String ruleDensityOutputFileName) {
    this.ruleDensityOutputFileName = ruleDensityOutputFileName;
  }

  public String getGrammarOutputFileName() {
    return grammarOutputFileName;
  }

  public void setGrammarOutputFileName(String grammarOutputFileName) {
    this.grammarOutputFileName = grammarOutputFileName;
  }

  public String getAnomaliesOutputFileName() {
    return anomaliesOutputFileName;
  }

  public void setAnomaliesOutputFileName(String anomaliesOutputFileName) {
    this.anomaliesOutputFileName = anomaliesOutputFileName;
  }

  public String getChartsSaveFolder() {
    return chartsSaveFolder;
  }

  public void setChartsSaveFolder(String chartsSaveFolder) {
    this.chartsSaveFolder = chartsSaveFolder;
  }

  public void setCountStrategy(int countStrategy) {
    this.countStrategy = CoverageCountStrategy.fromValue(countStrategy);
  }

  public void setGIAlgorithm(int giAlgorithm) {
    this.giAlgorithm = giAlgorithm;
  }

  public void setNormalizationThreshold(Double normalizationThreshold) {
    this.normalizationThreshold = normalizationThreshold;
  }

  public CoverageCountStrategy getCountStrategy() {
    return countStrategy;
  }

  public void setCountStrategy(CoverageCountStrategy countStrategy) {
    this.countStrategy = countStrategy;
  }

  public int getGiAlgorithm() {
    return giAlgorithm;
  }

  public void setGiAlgorithm(int giAlgorithm) {
    this.giAlgorithm = giAlgorithm;
  }

  public Double getNormalizationThreshold() {
    return normalizationThreshold;
  }

  public boolean isUseSlidingWindow() {
    return useSlidingWindow;
  }

  public void setUseSlidingWindow(boolean useSlidingWindow) {
    this.useSlidingWindow = useSlidingWindow;
  }

  public NumerosityReductionStrategy getNumerosityReductionStrategy() {
    return numerosityReductionStrategy;
  }

  public void setNumerosityReductionStrategy(NumerosityReductionStrategy numerosityReductionStrategy) {
    this.numerosityReductionStrategy = numerosityReductionStrategy;
  }

  public int getSaxWindow() {
    return saxWindow;
  }

  public void setSaxWindow(int saxWindow) {
    this.saxWindow = saxWindow;
  }

  public int getSaxPAA() {
    return saxPAA;
  }

  public void setSaxPAA(int saxPAA) {
    this.saxPAA = saxPAA;
  }

  public int getSaxAlphabet() {
    return saxAlphabet;
  }

  public void setSaxAlphabet(int saxAlphabet) {
    this.saxAlphabet = saxAlphabet;
  }

}
