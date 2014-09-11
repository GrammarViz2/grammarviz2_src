package edu.hawaii.jmotif.text.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import edu.hawaii.jmotif.text.CosineDistanceMatrix;
import edu.hawaii.jmotif.text.TextUtils;

public class TextKMeans {

  /*
   * Implements simple k-means clustering algorithm.
   */
  private static final double THRESHOLD = 0.000000001;

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

//    System.out.println(centroidsToString(centroids));
    
    System.out.println(clusters.toString().replace("],", "],\n"));
    System.out.println("Objective function: " + intraSS + "\n\n");

    return clusters;
  }

  private static char[] centroidsToString(LinkedHashMap<String, HashMap<String, Double>> centroids) {
    // build all words
    return TextUtils.tfidfToTable(centroids).toCharArray();
  }

  private static double computeIntraSS(HashMap<String, HashMap<String, Double>> tfidf,
      HashMap<String, List<String>> clusters) {
    double res = 0;
    for (Entry<String, List<String>> e : clusters.entrySet()) {
      for (int i = 0; i < e.getValue().size(); i++) {
        for (int j = i; j < e.getValue().size(); j++) {
          String key1 = e.getValue().get(i);
          String key2 = e.getValue().get(j);

          res = res + TextUtils.cosineDistance(tfidf.get(key1), tfidf.get(key2));
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
      double dist = TextUtils.cosineDistance(newCentroid, centroids.get(cluster.getKey()));
      if ((1.0d - dist) > THRESHOLD) {
        res = true;
        newCentroid = TextUtils.normalizeToUnitVector(newCentroid);
        centroids.put(cluster.getKey(), newCentroid);
      }
    }

    return res;
  }

  private static HashMap<String, Double> computeCentroid(
      HashMap<String, HashMap<String, Double>> tfidf, List<String> members) {

    List<String> words = new ArrayList<String>();
    for (String word : tfidf.entrySet().iterator().next().getValue().keySet()) {
      words.add(word.substring(0));
    }

    HashMap<String, Double> res = new HashMap<String, Double>();
    for (String word : words) {

      double sum = 0D;
      for (String bagKey : members) {
        HashMap<String, Double> bagValue = tfidf.get(bagKey);
        sum = sum + tfidf.get(bagKey).get(word);
      }

      res.put(word, sum);

    }

    return TextUtils.normalizeToUnitVector(res);
  }

  private static HashMap<String, List<String>> clusterize(
      LinkedHashMap<String, HashMap<String, Double>> centroids,
      HashMap<String, HashMap<String, Double>> tfidf) {

    HashMap<String, List<String>> res = new HashMap<String, List<String>>();
    for (String cName : centroids.keySet()) {
      res.put(cName.substring(0), new ArrayList<String>());
    }

    for (Entry<String, HashMap<String, Double>> bagEntry : tfidf.entrySet()) {

      String centroidKey = "";
      double minDist = -1.0D;
      for (Entry<String, HashMap<String, Double>> centroid : centroids.entrySet()) {
        double dist = TextUtils.cosineDistance(centroid.getValue(), bagEntry.getValue());
        // System.out.println("a="+centroid.getValue().values());
        // System.out.println("b="+bagEntry.getValue().values());
        // System.out.println("dist="+dist);
        if (dist > minDist) {
          centroidKey = centroid.getKey();
          minDist = dist;
        }
      }
      // System.out.println("dist= " + minDist + ", centroidKey: " + centroidKey);
      res.get(centroidKey).add(bagEntry.getKey());

    }
    return res;
  }

}
