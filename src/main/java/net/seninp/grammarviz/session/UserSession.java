package net.seninp.grammarviz.session;

import java.io.File;
import java.io.IOException;
import net.seninp.gi.GIAlgorithm;
import net.seninp.grammarviz.logic.CoverageCountStrategy;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.util.StackTrace;

/**
 * Keeps user parameters organized.
 * 
 * @author psenin
 * 
 */
public class UserSession {

  private static final int DEFAULT_SAX_WINDOW = 170;
  private static final int DEFAULT_SAX_PAA = 4;
  private static final int DEFAULT_SAX_ALPHABET = 4;

  private static final boolean USE_SLIDING_WINDOW = true;

  private static final Double DEFAULT_NORMALIZATION_THRESHOLD_VALUE = 0.05;

  private static final NumerosityReductionStrategy DEFAULT_NUMEROSITY_REDUCTION_STRATEGY = NumerosityReductionStrategy.EXACT;

  private static final GIAlgorithm DEFAULT_GI_ALGORITHM = GIAlgorithm.SEQUITUR;
  private static final CoverageCountStrategy DEFAULT_COUNT_STRATEGY = CoverageCountStrategy.COUNT;

  // discretization variables
  //
  public volatile int saxWindow;
  public volatile int saxPAA;
  public volatile int saxAlphabet;

  public volatile boolean useSlidingWindow;

  public volatile NumerosityReductionStrategy numerosityReductionStrategy;

  public volatile Double normalizationThreshold;

  // core algorithms variables
  //
  public volatile CoverageCountStrategy countStrategy;
  public volatile GIAlgorithm giAlgorithm;

  // auxiliary variables
  //
  public volatile String ruleDensityOutputFileName;
  public volatile String grammarOutputFileName;
  public volatile String anomaliesOutputFileName;
  public volatile String chartsSaveFolder;

  public UserSession() {

    super();

    this.saxWindow = DEFAULT_SAX_WINDOW;
    this.saxPAA = DEFAULT_SAX_PAA;
    this.saxAlphabet = DEFAULT_SAX_ALPHABET;

    this.useSlidingWindow = USE_SLIDING_WINDOW;

    this.numerosityReductionStrategy = DEFAULT_NUMEROSITY_REDUCTION_STRATEGY;

    this.normalizationThreshold = DEFAULT_NORMALIZATION_THRESHOLD_VALUE;

    this.giAlgorithm = DEFAULT_GI_ALGORITHM;

    this.countStrategy = DEFAULT_COUNT_STRATEGY;

    // attempt to fill the rule coverage name automatically
    //
    String filename = "";
    try {
      String currentPath = new File(".").getCanonicalPath();
      filename = currentPath + File.separator + "density_curve.txt";
    }
    catch (IOException e) {
      System.err.println(
          "Error has been thrown, unable to findout the current path: " + StackTrace.toString(e));
    }
    this.ruleDensityOutputFileName = filename;

  }

}
