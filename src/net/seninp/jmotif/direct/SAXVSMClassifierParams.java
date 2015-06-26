package net.seninp.jmotif.direct;

import java.util.ArrayList;
import java.util.List;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import com.beust.jcommander.Parameter;

/**
 * This implements a classifier.
 * 
 * @author psenin
 * 
 */
public class SAXVSMClassifierParams {

  // general setup
  //
  @Parameter
  public List<String> parameters = new ArrayList<String>();

  // datasets
  //
  @Parameter(names = { "--train_data", "-train" }, description = "The input file name")
  public static String TRAIN_FILE;

  @Parameter(names = { "--test_data", "-test" }, description = "The input file name")
  public static String TEST_FILE;

  // discretization parameters
  //
  @Parameter(names = { "--window_size", "-w" }, description = "SAX sliding window size")
  public static int SAX_WINDOW_SIZE = 30;

  @Parameter(names = { "--word_size", "-p" }, description = "SAX PAA word size")
  public static int SAX_PAA_SIZE = 4;

  @Parameter(names = { "--alphabet_size", "-a" }, description = "SAX alphabet size")
  public static int SAX_ALPHABET_SIZE = 3;

  @Parameter(names = "--strategy", description = "SAX numerosity reduction strategy")
  public static NumerosityReductionStrategy SAX_NR_STRATEGY = NumerosityReductionStrategy.EXACT;

  @Parameter(names = "--threshold", description = "SAX normalization threshold")
  public static double SAX_NORM_THRESHOLD = 0.01;

}
