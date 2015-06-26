package net.seninp.jmotif.cluster;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class ClusterUtils {

  private static final String CR = "\n";
  private static final DecimalFormat df = new DecimalFormat("#0.00000");

  public static String centroidsToString(LinkedHashMap<String, HashMap<String, Double>> centroids) {

    StringBuffer res = new StringBuffer();

    // header
    //
    for (Entry<String, HashMap<String, Double>> e : centroids.entrySet()) {
      res.append("\"").append(e.getKey()).append("\",");
    }
    res.delete(res.length() - 1, res.length()).append(CR);

    // iterate over words retrieving centroid values
    //
    for (String s : centroids.entrySet().iterator().next().getValue().keySet()) {
      res.append("\"").append(s).append("\",");
      for (Entry<String, HashMap<String, Double>> e : centroids.entrySet()) {
        res.append(df.format(e.getValue().get(s))).append(",");
      }
      res.delete(res.length() - 1, res.length()).append(CR);
    }

    return res.toString();

  }
}
