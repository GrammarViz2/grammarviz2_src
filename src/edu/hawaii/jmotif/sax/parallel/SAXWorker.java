package edu.hawaii.jmotif.sax.parallel;

import java.util.Arrays;
import java.util.concurrent.Callable;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.hawaii.jmotif.algorithm.MatrixFactory;
import edu.hawaii.jmotif.sax.alphabet.Alphabet;
import edu.hawaii.jmotif.sax.alphabet.NormalAlphabet;
import edu.hawaii.jmotif.sax.NumerosityReductionStrategy;
import edu.hawaii.jmotif.timeseries.TSException;

public class SAXWorker implements Callable<SAXRecords> {

  /** The latin alphabet, lower case letters a-z. */
  private static final char[] ALPHABET = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
      'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
  private static final Alphabet normalA = new NormalAlphabet();

  private long id;
  private double[] data;
  private int intervalStart;
  private int intervalEnd;
  private int saxWindowSize;
  private int saxPAASize;
  private int saxAlphabetSize;
  private NumerosityReductionStrategy numerosityReductionStrategy;
  private double normalizationThreshold;

  // logging stuff
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.DEBUG;

  // static block - we instantiate the logger
  //
  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(SAXWorker.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  /**
   * Performs SAX discretization.
   * 
   * @param id the job id.
   * @param data the data array.
   * @param intervalStart from which coordinate to start the conversion.
   * @param intervalEnd where to end conversion (non-ionclusive).
   * @param offset the offset for final values of SAX word positions.
   * @param windowSize SAX window size.
   * @param paaSize SAX paa size.
   * @param alphabetSize SAX alphabet size.
   * @param nrs The numerosity reduction strategy.
   * @param normalizationThreshold The normalization strategy.
   */
  public SAXWorker(long id, double[] data, int intervalStart, int intervalEnd, int windowSize,
      int paaSize, int alphabetSize, NumerosityReductionStrategy nrs, double normalizationThreshold) {
    super();
    this.id = id;
    this.data = data;
    this.intervalStart = intervalStart;
    this.intervalEnd = intervalEnd;
    this.saxWindowSize = windowSize;
    this.saxPAASize = paaSize;
    this.saxAlphabetSize = alphabetSize;
    this.numerosityReductionStrategy = nrs;
    this.normalizationThreshold = normalizationThreshold;
    consoleLogger.debug("sax worker instance id " + this.id + ", data " + this.data.length
        + ", window " + this.saxWindowSize + ", paa " + this.saxPAASize + ", alphabet "
        + this.saxAlphabetSize + ", nr " + this.numerosityReductionStrategy.toString()
        + ", threshold: " + normalizationThreshold + ", start: " + this.intervalStart + ", end: "
        + this.intervalEnd);
  }

  @Override
  public SAXRecords call() throws Exception {

    SAXRecords res = new SAXRecords(this.id);

    if (this.data.length < this.saxWindowSize) {
      return res;
    }

    // scan across the time series extract sub sequences, and convert
    // them to strings
    char[] previousString = null;
    for (int i = this.intervalStart; i < this.intervalEnd - (this.saxWindowSize - 1); i++) {

      // fix the current subsection
      double[] subSection = Arrays.copyOfRange(this.data, i, i + this.saxWindowSize);

      // Z normalize it
      subSection = zNormalize(subSection);

      // perform PAA conversion if needed
      double[] paa = paa(subSection, this.saxPAASize);

      // Convert the PAA to a string.
      char[] currentString = ts2String(paa, normalA.getCuts(this.saxAlphabetSize));

      if (NumerosityReductionStrategy.EXACT.equals(this.numerosityReductionStrategy)
          && Arrays.equals(previousString, currentString)) {
        continue;
      }
      else if ((null != previousString)
          && NumerosityReductionStrategy.MINDIST.equals(this.numerosityReductionStrategy)) {
        double dist = saxMinDist(previousString, currentString,
            normalA.getDistanceMatrix(this.saxAlphabetSize));
        if (0.0D == dist) {
          continue;
        }
      }

      previousString = currentString;
      // res.add(currentString, i + offset);
      res.add(currentString, i);
      // System.out.println(this.id + ", " + String.valueOf(currentString) + ", " + i);

    }

    return res;

  }

  /**
   * Computes the standard deviation of timeseries.
   * 
   * @param series The timeseries.
   * @return the standard deviation.
   */
  private double stDev(double[] series) {
    double num0 = 0D;
    double sum = 0D;
    int count = 0;
    for (double tp : series) {
      num0 = num0 + tp * tp;
      sum = sum + tp;
      count += 1;
    }
    double len = ((Integer) count).doubleValue();
    return Math.sqrt((len * num0 - sum * sum) / (len * (len - 1)));
  }

  /**
   * Computes the mean value of timeseries.
   * 
   * @param series The timeseries.
   * @return The mean value.
   */
  private double mean(double[] series) {
    double res = 0D;
    int count = 0;
    for (double tp : series) {
      res += tp;
      count += 1;
    }
    return res / ((Integer) count).doubleValue();
  }

  /**
   * Z-Normalize timeseries to the mean zero and standard deviation of one.
   * 
   * @param series The timeseries.
   * @return Z-normalized time-series.
   * @throws TSException if error occurs.
   */
  private double[] zNormalize(double[] series) throws TSException {
    double[] res = new double[series.length];
    double mean = mean(series);
    double sd = stDev(series);
    if (sd <= this.normalizationThreshold) {
      return series.clone();
    }
    for (int i = 0; i < res.length; i++) {
      res[i] = (series[i] - mean) / sd;
    }
    return res;
  }

  /**
   * Approximate the timeseries using PAA. If the timeseries has some NaN's they are handled as
   * follows: 1) if all values of the piece are NaNs - the piece is approximated as NaN, 2) if there
   * are some (more or equal one) values happened to be in the piece - algorithm will handle it as
   * usual - getting the mean.
   * 
   * @param ts The timeseries to approximate.
   * @param paaSize The desired length of approximated timeseries.
   * @return PAA-approximated timeseries.
   * @throws TSException if error occurs.
   */
  private double[] paa(double[] ts, int paaSize) throws TSException {
    int len = ts.length;
    if (len == paaSize) {
      return Arrays.copyOf(ts, ts.length);
    }
    else {
      if (len % paaSize == 0) {
        return MatrixFactory.colMeans(MatrixFactory.reshape(asMatrix(ts), len / paaSize, paaSize));
      }
      else {
        double[] paa = new double[paaSize];
        for (int i = 0; i < len * paaSize; i++) {
          int idx = i / len; // the spot
          int pos = i / paaSize; // the col spot
          paa[idx] = paa[idx] + ts[pos];
        }
        for (int i = 0; i < paaSize; i++) {
          paa[i] = paa[i] / (double) len;
        }
        return paa;
      }
    }
  }

  /**
   * Converts the vector into one-row matrix.
   * 
   * @param vector The vector.
   * @return The matrix.
   */
  private double[][] asMatrix(double[] vector) {
    double[][] res = new double[1][vector.length];
    for (int i = 0; i < vector.length; i++) {
      res[0][i] = vector[i];
    }
    return res;
  }

  /**
   * Converts the timeseries into string using given cuts intervals. Useful for not-normal
   * distribution cuts.
   * 
   * @param vals The timeseries.
   * @param cuts The cut intervals.
   * @return The timeseries SAX representation.
   */
  private char[] ts2String(double[] vals, double[] cuts) {
    char[] res = new char[vals.length];
    for (int i = 0; i < vals.length; i++) {
      res[i] = num2char(vals[i], cuts);
    }
    return res;
  }

  private char num2char(double value, double[] cuts) {
    int count = 0;
    while ((count < cuts.length) && (cuts[count] <= value)) {
      count++;
    }
    return ALPHABET[count];
  }

  /**
   * This function implements SAX MINDIST function which uses alphabet based distance matrix.
   * 
   * @param a The SAX string.
   * @param b The SAX string.
   * @param distanceMatrix The distance matrix to use.
   * @return distance between strings.
   * @throws TSException If error occurs.
   */
  private double saxMinDist(char[] a, char[] b, double[][] distanceMatrix) throws TSException {
    double dist = 0.0D;
    for (int i = 0; i < a.length; i++) {
      double localDist = distanceMatrix[Character.getNumericValue(a[i]) - 10][Character
          .getNumericValue(b[i]) - 10];
      dist += localDist;
    }
    return dist;
  }
}
