package net.seninp.jmotif.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import net.seninp.jmotif.text.CosineDistanceMatrix;

/**
 * Hierarchical clustering factory.
 * 
 * @author psenin
 * 
 */
public class HC {

  /**
   * Implements hierarchical clustering for word bags.
   * 
   * @param tfidfData The data to cluster.
   * @param criterion The linkage criterion.
   * @return The resulting cluster structure.
   */
  public static Cluster Hc(HashMap<String, HashMap<String, Double>> tfidfData, LinkageCriterion criterion) {

    // pre-compute distances matrix
    //
    CosineDistanceMatrix distanceMatrix = new CosineDistanceMatrix(tfidfData);

    // Note, however, that Cosine distance is INVERSE - i.e. lower value means GREATER angle
    // so we need to substract it from 1
    distanceMatrix.transformForHC();

    // first put everything into own clusters
    //
    List<Cluster> activeClusters = new ArrayList<Cluster>();
    for (String key : tfidfData.keySet()) {
      Cluster c = new Cluster(key);
      activeClusters.add(c);
    }

    // make a stack structure
    //
    Stack<Cluster> stack = new Stack<Cluster>();

    // main loop goes on while there is more then one element in the active set
    while (activeClusters.size() > 1) {

      // if the stack is empty - push something into
      //
      if (stack.isEmpty()) {
        Cluster cc = activeClusters.get(0);
        stack.push(cc);
      }

      // find the cluster which is nearest to the one in stack head
      //
      Cluster top = stack.peek();
      Cluster nearest = getNearest(top, activeClusters, tfidfData, distanceMatrix, criterion);

      // if the nearest is in the stack already - it must be the very next to the head
      // pop both and merge together
      // remove merged clusters from active set and add a newly merged one
      if (stack.contains(nearest)) {
        Cluster a = stack.pop();
        Cluster b = stack.pop();
        activeClusters.remove(a);
        activeClusters.remove(b);
        Cluster merged = new Cluster();
        merged.merge(a, b, a.distanceTo(b, tfidfData, distanceMatrix, criterion));
        activeClusters.add(merged);
      }
      // if nearest not in the stack - push it into
      else {
        stack.push(nearest);
      }
    }

    // recompute heights of joints

    // return resulting single cluster
    Cluster res = activeClusters.get(0);
    return res;
  }

  private static Cluster getNearest(Cluster stackTop, List<Cluster> activeClusters,
      HashMap<String, HashMap<String, Double>> data, CosineDistanceMatrix distanceMatrix,
      LinkageCriterion criterion) {
    Cluster res = null;
    double minDistance = Double.MAX_VALUE;
    for (Cluster cc : activeClusters) {
      if (stackTop.equals(cc)) {
        continue;
      }
      Double distance = stackTop.distanceTo(cc, data, distanceMatrix, criterion);
      if (distance < minDistance) {
        res = cc;
        minDistance = distance;
      }
    }
    return res;
  }

}
