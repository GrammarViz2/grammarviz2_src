package net.seninp.jmotif.cluster;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Template for initial k-means centroids strategy.
 * 
 * @author psenin
 * 
 */
public interface StartStrategy {

  /**
   * Samples out of data desired number of entries to use as initial cluster centers.
   * 
   * @param num The desired number of centers.
   * @param data The data. JMotif's tfidf structure used.
   * @return Sampled according to the strategy data.
   */
  public LinkedHashMap<String, HashMap<String, Double>> getCentroids(Integer num,
      HashMap<String, HashMap<String, Double>> data);

}
