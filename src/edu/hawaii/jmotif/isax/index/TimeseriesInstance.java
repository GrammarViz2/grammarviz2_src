package edu.hawaii.jmotif.isax.index;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import edu.hawaii.jmotif.distance.EuclideanDistance;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.timeseries.Timeseries;

/**
 * 
 * Used to hold multiple occurrences of the same Timeseries instance in a Node so as to prevent the
 * edge case of inserting the same ts over and over above the threshold
 * 
 * we also map the source of the ts and the offset
 * 
 * @author jpatterson
 * 
 */
public class TimeseriesInstance implements Cloneable, Comparable<TimeseriesInstance>, Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 1L;
  private Timeseries ts; // representative of this instance
  private Timeseries ts_compareTo_BaseRef;

  private HashMap<String, Long> hmOccurences = new HashMap<String, Long>();

  /**
   * Ctor
   * 
   * @param ts the Timeseries object that represents this class
   */
  public TimeseriesInstance(Timeseries ts) {

    this.ts = ts;
    this.ts_compareTo_BaseRef = null;

  }

  /**
   * Get the occurences reference.
   * 
   * @return
   */
  public HashMap<String, Long> getOccurences() {

    return this.hmOccurences;

  }

  public void Debug() {

    Iterator<String> itr = this.getOccurences().keySet().iterator();

    System.out.println("TimeseriesInstace > Debug >");

    while (itr.hasNext()) {

      String strKey = itr.next().toString();

      System.out.println("\t\t ---" + strKey + " => " + this.hmOccurences.get(strKey).longValue());

    }

  }

  public TimeseriesInstance clone() throws CloneNotSupportedException {

    // return new Symbol( this.SAXCharacter, this.Cardinality, this.wildcardbits );
    TimeseriesInstance tsi = new TimeseriesInstance(this.ts.clone());

    HashMap<String, Long> hm = this.getOccurences();
    Iterator<String> itr = this.getOccurences().keySet().iterator();

    while (itr.hasNext()) {

      String strKey = itr.next().toString();

      tsi.AddOccurenceByKey(strKey, hm.get(strKey).longValue());

    }

    return tsi;

  }

  /**
   * Get a reference to the Timeseries object that represents this group of instances.
   * 
   * @return
   */
  public Timeseries getTS() {
    return this.ts;
  }

  /**
   * Add an occurence to this group.
   * 
   * @param instances
   */
  public void AddOccurences(TimeseriesInstance instances) {

    HashMap<String, Long> hm = instances.getOccurences();
    Iterator<String> itr = instances.getOccurences().keySet().iterator();

    // iterate through HashMap values iterator
    while (itr.hasNext()) {
      // System.out.println(itr.next());

      String strKey = itr.next().toString();

      this.AddOccurenceByKey(strKey, hm.get(strKey).longValue());

    }

  }

  /**
   * Add an occurence to a specific key.
   * 
   * @param strKey
   * @param offset
   */
  public void AddOccurenceByKey(String strKey, long offset) {

    if (this.hmOccurences.containsKey(strKey)) {

      // we've somehow already indexed this spot
      System.out.println("> " + strKey + " - has already been indexed");

    }
    else {

      this.hmOccurences.put(strKey, offset);

    }

  }

  public void AddOccurence(String strFilename, long offset) {

    String strKey = strFilename + "+" + offset;

    this.AddOccurenceByKey(strKey, offset);

  }

  @Override
  public boolean equals(Object o) {

    if (o instanceof TimeseriesInstance) {
      TimeseriesInstance other = (TimeseriesInstance) o;
      /*
       * if ( other.getStringRepresentation().equals( this.getStringRepresentation() ) ) { return
       * true; }
       */

      return this.ts.equals(other.ts);

    }

    return false;
  }

  /**
   * This is the reference timeseries that the compareTo() method uses to calculate which has a
   * larger distance from a base point for kNN calculations
   * 
   * - this is only used for compareTo(), which is used by the heap to sort
   * 
   */
  public void setComparableReferencePoint(Timeseries ts_base) {

    this.ts_compareTo_BaseRef = ts_base;

  }

  @Override
  public int compareTo(TimeseriesInstance other) {

    // if we dont have a reference point, there's no way to compare

    if (null == this.ts_compareTo_BaseRef) {
      return 0;
    }

    double my_dist = 0;
    double other_dist = 0;

    try {
      my_dist = EuclideanDistance.seriesDistance(this.ts.values(),
          this.ts_compareTo_BaseRef.values());
      other_dist = EuclideanDistance.seriesDistance(other.ts.values(),
          this.ts_compareTo_BaseRef.values());
    }
    catch (TSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    if (my_dist == other_dist) {
      return 0;
    }
    else if (my_dist > other_dist) {
      return 1;
    }
    else if (my_dist < other_dist) {
      return -1;
    }

    return 0;
  }

  public void writeObject(ObjectOutputStream out) throws IOException {
    out.defaultWriteObject();
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();

  }

}
