package net.seninp.jmotif.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import net.seninp.jmotif.text.CosineDistanceMatrix;
import net.seninp.jmotif.text.TextProcessor;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class TextKMeans {

  /*
   * Implements simple k-means clustering algorithm.
   */
  private static final double THRESHOLD = 0.000000001;

  // logger business
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.INFO;

  private static final TextProcessor tp = new TextProcessor();

  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(TextKMeans.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  /**
   * This clusters a map data structure of pairs bagName wordBag which is used as tfidf throughout
   * of JMotif.
   * 
   * @param tfidf the data to cluster.
   * @param clustersNum desired clusters number.
   * @param strategy The clustering strategy.
   * @return
   */
  public static HashMap<String, List<String>> cluster(
      HashMap<String, HashMap<String, Double>> tfidf, Integer clustersNum, StartStrategy strategy) {

    consoleLogger.info("starting KMeans with " + clustersNum + " centers.");
    // obtain centroids from the strategy
    //
    LinkedHashMap<String, HashMap<String, Double>> centroids = strategy.getCentroids(clustersNum,
        tfidf);

    // System.out.println(ClusterUtils.centroidsToString(centroids));

    // run the clustering iteration at least once
    //
    HashMap<String, List<String>> clusters = clusterize(centroids, tfidf);
    System.out.println(clusters.toString().replace("],", "],\n"));

    while (updateCentroids(centroids, clusters, tfidf)) {
      clusters = clusterize(centroids, tfidf);
      System.out.println(clusters.toString().replace("],", "],\n"));
    }

    System.out.println(centroidsToString(centroids));

    System.out.println(clusters.toString().replace("],", "],\n"));

    CosineDistanceMatrix m = new CosineDistanceMatrix(centroids);
    System.out.println(m.toString());

    double intraSS = computeIntraSS(tfidf, clusters);
    System.out.println("Objective function: " + intraSS + "\n\n");

    // System.out.println(centroidsToString(centroids));

    System.out.println(clusters.toString().replace("],", "],\n"));
    System.out.println("Objective function: " + intraSS + "\n\n");

    return clusters;
  }

  private static char[] centroidsToString(LinkedHashMap<String, HashMap<String, Double>> centroids) {
    // build all words
    return tp.tfidfToTable(centroids).toCharArray();
  }

  private static double computeIntraSS(HashMap<String, HashMap<String, Double>> tfidf,
      HashMap<String, List<String>> clusters) {
    double res = 0;
    for (Entry<String, List<String>> e : clusters.entrySet()) {
      for (int i = 0; i < e.getValue().size(); i++) {
        for (int j = i + 1; j < e.getValue().size(); j++) {
          String key1 = e.getValue().get(i);
          String key2 = e.getValue().get(j);

          res = res + tp.cosineDistance(tfidf.get(key1), tfidf.get(key2));
        }
      }
    }
    return res;
  }

  private static boolean updateCentroids(LinkedHashMap<String, HashMap<String, Double>> centroids,
      HashMap<String, List<String>> clusters, HashMap<String, HashMap<String, Double>> tfidf) {

    boolean res = false;

    // get a new centroid for the cluster
    //
    for (Entry<String, List<String>> cluster : clusters.entrySet()) {
      HashMap<String, Double> newCentroid = computeCentroid(tfidf, cluster.getValue());
      double dist = tp.cosineDistance(newCentroid, centroids.get(cluster.getKey()));
      if ((1.0d - dist) > THRESHOLD) {
        res = true;
        centroids.put(cluster.getKey(), newCentroid);
      }
    }

    return res;
  }

  /**
   * This computes centroid for a set of vectors.
   * 
   * @param tfidf
   * @param members
   * @return
   */
  public static HashMap<String, Double> computeCentroid(
      HashMap<String, HashMap<String, Double>> tfidf, List<String> members) {

    // extract the list of all words into a new bag
    //
    List<String> words = new ArrayList<String>();
    for (String word : tfidf.entrySet().iterator().next().getValue().keySet()) {
      words.add(word.substring(0));
    }

    // compute weights for these words one by one
    //
    HashMap<String, Double> res = new HashMap<String, Double>();
    for (String word : words) {
      double sum = 0D;
      for (String bagKey : members) {
        sum = sum + tfidf.get(bagKey).get(word);
      }
      res.put(word, sum / members.size());
    }

    // re-normalize to units and return
    //
    return tp.normalizeToUnitVector(res);
  }

  /**
   * Having centroids set, this assigns all other labels.
   * 
   * @param centroids
   * @param tfidf
   * @return
   */
  private static HashMap<String, List<String>> clusterize(
      LinkedHashMap<String, HashMap<String, Double>> centroids,
      HashMap<String, HashMap<String, Double>> tfidf) {

    // build centroids
    //
    HashMap<String, List<String>> res = new HashMap<String, List<String>>();
    for (String cName : centroids.keySet()) {
      res.put(cName.substring(0), new ArrayList<String>());
    }

    // take care of the rest of vectors
    //
    for (Entry<String, HashMap<String, Double>> bagEntry : tfidf.entrySet()) {

      String centroidKey = "";
      double minDist = -1.0D;
      for (Entry<String, HashMap<String, Double>> centroid : centroids.entrySet()) {
        double dist = tp.cosineDistance(centroid.getValue(), bagEntry.getValue());
        if (dist > minDist) {
          centroidKey = centroid.getKey();
          minDist = dist;
        }
      }
      res.get(centroidKey).add(bagEntry.getKey());

    }

    StringBuffer sb = new StringBuffer();
    for (Entry<String, List<String>> e : res.entrySet()) {
      sb.append(e.getKey()).append(": ").append(list2String(e.getValue())).append(", ");
    }
    consoleLogger.info("clusterizaton run finished, clusters: " + sb.toString());

    return res;
  }

  private static String list2String(List<String> value) {
    StringBuffer sb = new StringBuffer("[");
    for (String str : value) {
      sb.append(str).append(",");
    }
    if (sb.length() > 2) {
      sb.delete(sb.length() - 1, sb.length());
    }
    return sb.append("]").toString();
  }
}
