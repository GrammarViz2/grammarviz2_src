package net.seninp.jmotif.cluster;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeSet;
import net.seninp.jmotif.text.CosineDistanceMatrix;

public class FurthestFirstStrategy implements StartStrategy {

  @Override
  public LinkedHashMap<String, HashMap<String, Double>> getCentroids(Integer num,
      HashMap<String, HashMap<String, Double>> data) {

    Random random = new Random();

    // need to make distance matrix for all the words in here
    //
    CosineDistanceMatrix matrix = new CosineDistanceMatrix(data);

    TreeSet<String> keys = new TreeSet<String>();
    TreeSet<String> resultKeys = new TreeSet<String>();
    for (String k : data.keySet()) {
      keys.add(k.substring(0));
    }

    int rand = random.nextInt(keys.size());
    String center1 = matrix.getRows()[rand];
    resultKeys.add(center1);

    while (resultKeys.size() < num) {

      // now need to find the next furthest of other elements
      //
      double maxDist = 0D;
      String furthestElement = null;

      for (String k : keys) {

        if (resultKeys.contains(k)) {
          continue;
        }

        if (null == furthestElement) {
          furthestElement = k;
          maxDist = minCosineDistance(furthestElement, resultKeys, matrix);
        }
        else {
          if (maxDist > minCosineDistance(k, resultKeys, matrix)) {
            furthestElement = k;
            maxDist = minCosineDistance(furthestElement, resultKeys, matrix);
          }
        }

      }

      resultKeys.add(furthestElement);

    }

    // compose the result map
    LinkedHashMap<String, HashMap<String, Double>> res = new LinkedHashMap<String, HashMap<String, Double>>();
    int counter = 0;
    for (String key : resultKeys) {
      HashMap<String, Double> value = new HashMap<String, Double>();
      for (Entry<String, Double> e : data.get(key).entrySet()) {
        value.put(e.getKey().substring(0), new Double(e.getValue()));
      }
      res.put(String.valueOf(counter), value);
      counter++;
    }

    return res;
  }

  /**
   * Finds a minimal distance (largest cosine value) value between vector of interest and all other
   * vectors.
   * 
   * @param furthestElement
   * @param resultKeys
   * @param matrix
   * @return
   */
  private double minCosineDistance(String furthestElement, TreeSet<String> resultKeys,
      CosineDistanceMatrix matrix) {
    double minDist = 0.0D;
    for (String currKey : resultKeys) {
      if (matrix.distanceBetween(currKey, furthestElement) > minDist) {
        minDist = matrix.distanceBetween(currKey, furthestElement);
      }
    }
    return minDist;
  }

}
