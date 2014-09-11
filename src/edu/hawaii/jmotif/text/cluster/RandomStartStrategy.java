package edu.hawaii.jmotif.text.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Random;

/**
 * Implements a random strategy
 * 
 * @author psenin
 * 
 */
public class RandomStartStrategy implements StartStrategy {

  /**
   * {@inheritDoc}
   */
  @Override
  public LinkedHashMap<String, HashMap<String, Double>> getCentroids(Integer num,
      HashMap<String, HashMap<String, Double>> data) {

    Random random = new Random();

    LinkedHashMap<String, HashMap<String, Double>> res = new LinkedHashMap<String, HashMap<String, Double>>();

    ArrayList<String> keys = new ArrayList<String>();
    for (String k : data.keySet()) {
      keys.add(k.substring(0));
    }

    for (int i = 0; i < num; i++) {

      int rand = random.nextInt(keys.size());
      String key = keys.get(rand).substring(0);
      keys.remove(rand);
      System.out.println("cluster " + i + ", " + key);

      HashMap<String, Double> value = new HashMap<String, Double>();
      for (Entry<String, Double> e : data.get(key).entrySet()) {
        value.put(e.getKey().substring(0), new Double(e.getValue()));
      }

      res.put(String.valueOf(i), value);

    }
    return res;
  }

}
