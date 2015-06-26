package net.seninp.jmotif.cluster;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import net.seninp.jmotif.text.CosineDistanceMatrix;
import net.seninp.jmotif.text.TextProcessor;

public class TestTextKMeans {

  private static double[][] data = { { 4.06387505698408, 3.09890160240014, 4.24608942626356 },
      { 3.37771588307569, 2.00342142630004, 2.81755429626629 },
      { 1.61617453373732, 3.8985047305272, 4.12777436769707 },
      { 4.49139545065148, 2.95576503671283, 4.98495407716619 },
      { 10.4156650941901, 8.05221905735779, 8.65323808973196 },
      { 8.0134266096798, 8.33908098895459, 8.16441680324641 },
      { 7.71152040817188, 8.40938960038683, 8.17098422814849 },
      { 7.57680445460878, 7.90829242972174, 10.402903595769 },
      { 4.23379927177077, 5.33935210959201, 7.71852055859631 },
      { 6.66827245243122, 5.30284709836843, 6.64062709791412 },
      { 6.17700092897287, 6.92640462552435, 6.96067286425928 },
      { 4.89846939451114, 6.20772361067014, 4.08115382280967 }, };

  private static final TextProcessor tp = new TextProcessor();

  public static void main(String[] args) throws Exception {

    HashMap<String, HashMap<String, Double>> tfidf = new HashMap<String, HashMap<String, Double>>();

    int counter = 0;
    for (double[] p : data) {
      HashMap<String, Double> cl = new HashMap<String, Double>();
      cl.put("x", p[0]);
      cl.put("y", p[1]);
      cl.put("z", p[2]);
      tfidf.put(String.valueOf(counter), cl);
      counter++;
    }

    for (Entry<String, HashMap<String, Double>> e : tfidf.entrySet()) {
      for (Entry<String, HashMap<String, Double>> e1 : tfidf.entrySet()) {
        if (!e.getKey().equalsIgnoreCase(e1.getKey())) {
          System.out.println(e.getKey() + ", " + e1.getKey() + ", "
              + tp.cosineDistance(e.getValue(), e1.getValue()) + ", " + e.getValue() + ", "
              + e1.getValue());
        }
      }
    }

    @SuppressWarnings("unused")
    HashMap<String, List<String>> clustersK = TextKMeans.cluster(tfidf, 3,
        new RandomStartStrategy());

    Cluster clusters = HC.Hc(tfidf, LinkageCriterion.COMPLETE);
    System.out.println((new CosineDistanceMatrix(tfidf)).toString());
    BufferedWriter bw = new BufferedWriter(new FileWriter("/home/psenin/dendroscope/test2.newick"));
    bw.write("(" + clusters.toNewick() + ")");
    bw.close();

  }
}
