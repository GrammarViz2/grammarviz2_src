package net.seninp.jmotif.cluster;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;
import net.seninp.jmotif.text.CosineDistanceMatrix;

/**
 * Implements a cluster node for SAX terms clustering.
 * 
 * @author psenin
 * 
 */
public class Cluster {

  /** The left sub-cluster. */
  public Cluster left = null;

  /** The right sub-cluster. */
  public Cluster right = null;

  /** The level from the root. */
  public int level;

  /** The distance between left and right sub-clusters. */
  public double distanceBetween;

  /** The keys. Words which are within this cluster. */
  private TreeSet<String> keys;

  /**
   * Constructor.
   */
  public Cluster() {
    super();
  }

  /**
   * Constructor.
   * 
   * @param key The single name of the cluster.
   */
  public Cluster(String key) {
    super();
    this.keys = new TreeSet<String>();
    keys.add(key);
  }

  /**
   * Merging together clusters.
   * 
   * @param left The left cluster.
   * @param right The right cluster.
   */
  public void merge(Cluster left, Cluster right, Double distance) {
    this.left = left;
    this.right = right;
    this.keys = new TreeSet<String>();
    this.keys.addAll(left.keys);
    this.keys.addAll(right.keys);
    this.distanceBetween = distance;
  }

  /**
   * Compute the distance between words clusters.
   * 
   * @param otherCluster The other cluster.
   * @param data
   * @param distanceMatrix The pre-computed distance matrix.
   * @param criterion The linkage criterion.
   * @return The distance between clusters based on the distances and the linkage.
   */
  public Double distanceTo(Cluster otherCluster, HashMap<String, HashMap<String, Double>> data,
      CosineDistanceMatrix distanceMatrix, LinkageCriterion criterion) {
    if (otherCluster.keys.size() == 1 && this.keys.size() == 1) {
      return distanceMatrix.distanceBetween(otherCluster.keys.first(), this.keys.first());
    }
    if (criterion.equals(LinkageCriterion.SINGLE)) {
      double minDist = Double.MAX_VALUE;
      for (String keyA : this.keys) {
        for (String keyB : otherCluster.keys) {
          Double dist = distanceMatrix.distanceBetween(keyA, keyB);
          if (dist < minDist) {
            minDist = dist;
          }
        }
      }
      return minDist;
    }
    else if (criterion.equals(LinkageCriterion.COMPLETE)) {
      double maxDist = Double.MIN_VALUE;
      for (String keyA : this.keys) {
        for (String keyB : otherCluster.keys) {
          Double dist = distanceMatrix.distanceBetween(keyA, keyB);
          if (dist > maxDist) {
            maxDist = dist;
          }
        }
      }
      return maxDist;
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((keys == null) ? 0 : keys.hashCode());
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Cluster other = (Cluster) obj;
    if (keys == null) {
      if (other.keys != null) {
        return false;
      }
    }
    else if (!keys.equals(other.keys)) {
      return false;
    }
    return true;
  }

  /**
   * {@inheritDoc}
   */
  public String toString() {
    return Arrays.toString(this.keys.toArray());
  }

  /**
   * Returns the NEWICK format representation of this node cluster. You still need to take it in
   * parenthesis if this is your ROOT.
   * 
   * @return The newick string representation of the cluster.
   */
  public String toNewick() {
    StringBuilder sb = new StringBuilder();
    if ((null != this.left) && (null != this.right)) {

      double height = 0D;

      if (left.isTerminal() && !(right.isTerminal())) {
        height = Math.abs(this.distanceBetween - right.distanceBetween);
      }
      else if (!(left.isTerminal()) && right.isTerminal()) {
        height = Math.abs(this.distanceBetween - left.distanceBetween);
      }
      else {
        height = this.distanceBetween;
      }

      height = height / 2D;

      if (left.isTerminal()) {
        sb.append(left.toNewick()).append(":").append(String.valueOf(height));
      }
      else {
        sb.append("(").append(left.toNewick()).append(")").append(":")
            .append(String.valueOf(height));
      }
      sb.append(",");
      if (right.isTerminal()) {
        sb.append(right.toNewick()).append(":").append(formatNumber(height));
      }
      else {
        sb.append("(").append(right.toNewick()).append(")").append(":")
            .append(formatNumber(height));
      }

      return sb.toString();
    }
    else {
      return this.keys.first();
    }
  }

  /**
   * Formats the double flow-point number.
   * 
   * @param number The number to format.
   * @return formatted flow-point.
   */
  private String formatNumber(double number) {
    DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
    symbols.setDecimalSeparator('.');
    DecimalFormat format = new DecimalFormat("#.##########", symbols);
    return format.format(number);
  }

  /**
   * Returns true if left and right sub-nodes of this node are NULLs.
   * 
   * @return true if the node is terminal.
   */
  public boolean isTerminal() {
    if ((null == this.left) && (null == this.right)) {
      return true;
    }
    return false;
  }

}
