package edu.hawaii.jmotif.sax.alphabet;

import edu.hawaii.jmotif.timeseries.TSException;

/**
 * Implements alphabet component for SAX.
 *
 * @author Pavel Senin
 *
 */
public final class UniversalTelemetryAlphabet extends Alphabet {

  /** Maximal possible alphabet size. */
  public static final int MAX_SIZE = 20;
  private static final double[] case2 = { -0.098181 };
  private static final double[] case3 = { -0.359373, 0.23535 };
  private static final double[] case4 = { -0.504457, -0.0981819, 0.5882 };
  private static final double[] case5 = { -0.6280585000000001, -0.204556, 0.0386529, 0.67383 };
  private static final double[] case6 = { -0.7234505, -0.35969300000000004, -0.09837019999999999,
      0.235351, 0.73786 };
  private static final double[] case7 = { -0.795985, -0.443235, -0.165274, 0.0, 0.399261, 0.77940 };
  private static final double[] case8 = { -0.871629, -0.504457, -0.240192, -0.0985585, 0.11146,
      0.58829, 0.82722 };
  private static final double[] case9 = { -0.908181, -0.574222, -0.359373, -0.127147, 0.0,
      0.235351, 0.6496175, 0.86243 };
  private static final double[] case10 = { -0.961024, -0.6280585000000001, -0.41915, -0.204556,
      -0.0981819, 0.0386529, 0.352826, 0.673831, 0.90595 };
  private static final double[] case11 = { -1.01398, -0.6878945, -0.471544, -0.279776, -0.127147,
      0.0, 0.127883, 0.44545, 0.689865, 0.96710 };
  private static final double[] case12 = { -1.07482, -0.723502, -0.504457, -0.35975, -0.18622,
      -0.0985585, 0.0, 0.235351, 0.58829, 0.737868, 0.96711 };
  private static final double[] case13 = { -1.14459, -0.770156, -0.552345, -0.404853, -0.217426,
      -0.126917, -0.00482288, 0.0887098, 0.283123, 0.617833, 0.748159, 0.99905 };
  private static final double[] case14 = { -1.2366350000000002, -0.7961965, -0.600472, -0.443235,
      -0.291386, -0.167444, -0.0985585, 0.0, 0.141421, 0.399261, 0.663464, 0.777597, 1.0347 };
  private static final double[] case15 = { -1.24835, -0.836314, -0.6284395, -0.485515, -0.35975,
      -0.204556, -0.126917, -0.00482288, 0.0386529, 0.235351, 0.4914515, 0.673831, 0.786437,1.0402};
  private static final double[] case16 = { -1.26418, -0.871712, -0.677687, -0.504457, -0.399186,
      -0.24193399999999998, -0.154091, -0.09862715, 0.0, 0.11146, 0.27735, 0.58829, 0.675703,
      0.827228, 1.0780 };
  private static final double[] case17 = { -1.29105, -0.893322, -0.703445, -0.539947, -0.420619,
      -0.301511, -0.186965, -0.114332, -0.00719573, 0.0, 0.169067, 0.360858, 0.617153, 0.722289,
      0.846299, 1.1122 };
  private static final double[] case18 = { -1.35105, -0.908181, -0.723502, -0.574222, -0.4604155,
      -0.35975, -0.211997, -0.127147, -0.0985585, 0.0, 0.086341, 0.235351, 0.433026, 0.6469935,
      0.737868, 0.862435, 1.14557 };
  private static final double[] case19 = { -1.37419, -0.938227, -0.752944, -0.605508, -0.490632,
      -0.393162, -0.261693, -0.181716, -0.114332, -0.00976562, 0.0, 0.125995, 0.27735, 0.509486,
      0.6714765, 0.748159, 0.874168, 1.2066 };
  private static final double[] case20 = { -1.39625, -0.961024, -0.785905, -0.6280585000000001,
      -0.504457, -0.41915, -0.308907, -0.204556, -0.127147, -0.0981819, 0.0, 0.0386529, 0.186409,
      0.352826, 0.58829, 0.673831, 0.771844, 0.905955, 1.26996 };

  /**
   * Constructor.
   */
  public UniversalTelemetryAlphabet() {
    super();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double[] getCuts(Integer size) throws TSException {
    switch (size) {
    case 2:
      return case2.clone();
    case 3:
      return case3.clone();
    case 4:
      return case4.clone();
    case 5:
      return case5.clone();
    case 6:
      return case6.clone();
    case 7:
      return case7.clone();
    case 8:
      return case8.clone();
    case 9:
      return case9.clone();
    case 10:
      return case10.clone();
    case 11:
      return case11.clone();
    case 12:
      return case12.clone();
    case 13:
      return case13.clone();
    case 14:
      return case14.clone();
    case 15:
      return case15.clone();
    case 16:
      return case16.clone();
    case 17:
      return case17.clone();
    case 18:
      return case18.clone();
    case 19:
      return case19.clone();
    case 20:
      return case20.clone();
    default:
      throw new TSException("Invalid alphabet size.");
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Integer getMaxSize() {
    return MAX_SIZE;
  }

  @Override
  public double[][] getDistanceMatrix(Integer size) throws TSException {
    double[][] res = new double[0][0];
    return res;
  }

}
