package edu.hawaii.jmotif.sax.alphabet;

import edu.hawaii.jmotif.timeseries.TSException;

/**
 * Implements alphabet component for SAX.
 *
 * @author Pavel Senin
 *
 */
public final class BuildTelemetryAlphabet extends Alphabet {

  /** Maximal possible alphabet size. */
  public static final int MAX_SIZE = 20;
  private static final double[] case2 = { -0.31629 };
  private static final double[] case3 = { -0.424264, -0.027207 };
  private static final double[] case4 = { -0.504457, -0.316293, 0.11136 };
  private static final double[] case5 = { -0.576046, -0.397728, -0.18622, 0.3947960000000000 };
  private static final double[] case6 = { -0.646652, -0.424264, -0.316293, -0.0272073, 0.578424 };
  private static final double[] case7 = { -0.682417, -0.48938550000000003, -0.3782755, -0.189843,
      0.0532246, 0.67971 };
  private static final double[] case8 = { -0.690725, -0.504457, -0.406979, -0.316293, -0.18622,
      0.111364, 0.832802 };
  private static final double[] case9 = { -0.700299, -0.549895, -0.424264, -0.373166, -0.251759,
      -0.0272073, 0.232181, 1.0159 };
  private static final double[] case10 = { -0.7113645, -0.576046, -0.479022, -0.397728, -0.316293,
      -0.18622, 0.01621855, 0.373536, 1.1532 };
  private static final double[] case11 = { -0.73341, -0.619341, -0.495173, -0.418996, -0.361787,
      -0.267323, -0.1856175, 0.07382125, 0.518278, 1.3277 };
  private static final double[] case12 = { -0.767127, -0.646652, -0.5045645000000001, -0.424264,
      -0.386869, -0.316293, -0.18622, -0.02726315, 0.111364, 0.571378, 1.4118 };
  private static final double[] case13 = { -0.803006, -0.66765, -0.53968, -0.469042, -0.406979,
      -0.360898, -0.281504, -0.18622, 0.00691037, 0.163763, 0.620456, 1.58328 };
  private static final double[] case14 = { -0.831888, -0.682417, -0.55613, -0.48938550000000003,
      -0.418996, -0.3782755, -0.316293, -0.189843, -0.13982050000000001, 0.0532246, 0.268463,
      0.679712, 1.66716 };
  private static final double[] case15 = { -0.834708, -0.682417, -0.576046, -0.501368, -0.424264,
      -0.397728, -0.360898, -0.291386, -0.18622, -0.0386586, 0.0887098, 0.364898,
      0.760286, 1.67477 };
  private static final double[] case16 = { -0.836314, -0.690725, -0.610888, -0.504457, -0.469042,
      -0.406979, -0.373166, -0.316293, -0.21748, -0.18622, -0.00251944, 0.111364, 0.456325,
      0.8328025, 1.76271 };
  private static final double[] case17 = { -0.836975, -0.700299, -0.6260625, -0.53968, -0.479022,
      -0.420619, -0.386869, -0.360898, -0.291386, -0.18622, -0.130304, 0.0511682, 0.137553,
      0.525289, 0.978182, 1.8249 };
  private static final double[] case18 = { -0.8486015, -0.700299, -0.646652, -0.549895, -0.495173,
      -0.424264, -0.399186, -0.373166, -0.316293, -0.251759, -0.18622, -0.0272073, 0.0545247,
      0.232181, 0.5784245, 1.01598, 1.8389 };
  private static final double[] case19 = { -0.878591, -0.703445, -0.664535, -0.567434,
      -0.5038549999999999, -0.464827, -0.41659, -0.386869, -0.360718, -0.291386, -0.18622,
      -0.18622, -0.00293918, 0.0972272, 0.302399, 0.615038, 1.10698, 1.8389 };
  private static final double[] case20 = { -0.878591, -0.714952, -0.679315, -0.576046, -0.506336,
      -0.479022, -0.420619, -0.397728, -0.373166, -0.316293, -0.261354, -0.18622, -0.130304,
      0.00691037, 0.111364, 0.3484165, 0.619224, 1.11661, 1.8389 };

  /**
   * Constructor.
   */
  public BuildTelemetryAlphabet() {
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
    // TODO Auto-generated method stub
    double[][] res = new double[0][0];
    return res;
  }

}
