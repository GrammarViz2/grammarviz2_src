package net.seninp.jmotif.text;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

/**
 * Implements cosine distance matrix.
 * 
 * @author psenin
 * 
 */
public class CosineDistanceMatrix {

  /** Distance matrix. */
  private double[][] distances;

  /** Row names. */
  private String[] rows;

  private HashMap<String, Integer> keysToIndex = new HashMap<String, Integer>();

  private static final String COMMA = ",";
  private static final String CR = "\n";
  private static final DecimalFormat df = new DecimalFormat("#0.00000");

  private static final TextProcessor tp = new TextProcessor();

  /**
   * Builds a distance matrix.
   * 
   * @param tfidf The data to use.
   */
  public CosineDistanceMatrix(HashMap<String, HashMap<String, Double>> tfidf) {

    Locale.setDefault(Locale.US);

    rows = tfidf.keySet().toArray(new String[0]);

    Arrays.sort(rows);

    distances = new double[rows.length][rows.length];

    for (int i = 0; i < rows.length; i++) {
      keysToIndex.put(rows[i], i);
      for (int j = 0; j < i; j++) {
        HashMap<String, Double> vectorA = tfidf.get(rows[i]);
        HashMap<String, Double> vectorB = tfidf.get(rows[j]);
        double distance = tp.cosineDistance(vectorA, vectorB);
        distances[i][j] = distance;
      }
    }
  }

  /**
   * Get all the row names - i.e. keys.
   * 
   * @return
   */
  public String[] getRows() {
    return this.rows;
  }

  /**
   * Get the distances as matrix.
   * 
   * @return
   */
  public double[][] getDistances() {
    return this.distances;
  }

  /**
   * Prints matrix.
   */
  @Override
  public String toString() {

    StringBuffer sb = new StringBuffer();

    sb.append("\"\",");
    for (String s : rows) {
      sb.append("\"").append(s).append("\"").append(COMMA);
    }
    sb.delete(sb.length() - 1, sb.length()).append(CR);

    for (int i = 0; i < rows.length; i++) {
      sb.append("\"").append(rows[i]).append("\",");
      for (int j = 0; j < rows.length; j++) {
        sb.append(df.format(distances[i][j])).append(COMMA);
      }
      sb.delete(sb.length() - 1, sb.length()).append(CR);
    }

    return sb.toString();
  }

  /**
   * get the distance value between two keys.
   * 
   * @param keyA first key.
   * @param keyB second key.
   * @return the distance between vectors.
   */
  public double distanceBetween(String keyA, String keyB) {
    if (keysToIndex.get(keyA) >= keysToIndex.get(keyB)) {
      return distances[keysToIndex.get(keyA)][keysToIndex.get(keyB)];
    }
    return distances[keysToIndex.get(keyB)][keysToIndex.get(keyA)];
  }

  /**
   * This will subtract all distance values from 1 - so distance becomes inversed - good for
   * clustering.
   */
  public void transformForHC() {
    for (int i = 0; i < distances.length; i++) {
      for (int j = 0; j < distances[0].length; j++) {
        distances[i][j] = 1.0D - distances[i][j];
      }
    }
  }

}
