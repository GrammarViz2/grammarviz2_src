package edu.hawaii.jmotif.isax.index;

import java.util.Iterator;
import java.util.TreeSet;
import edu.hawaii.jmotif.distance.EuclideanDistance;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.timeseries.Timeseries;

/**
 * Ideally we want these sorted by distance to a reference point
 * 
 * -- here the reference point is the search sequence -- so to sort these elements, we'd want to
 * compare each to the search sequence --
 */
public class kNNSearchResults {

  int kNN = 0;
  TreeSet<TimeseriesInstance> results = new TreeSet<TimeseriesInstance>();
  Timeseries search_ts = null;

  public kNNSearchResults(int k, Timeseries ts_search) {
    this.kNN = k;
    this.search_ts = ts_search;

  }

  public void AddResult(TimeseriesInstance tsi) {

    TimeseriesInstance insert = null;

    try {
      insert = tsi.clone();
    }
    catch (CloneNotSupportedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    insert.setComparableReferencePoint(search_ts);
    this.results.add(insert);

  }

  public int count() {
    return this.results.size();
  }

  public boolean complete() {
    if (this.kNN <= this.results.size()) {
      return true;
    }
    return false;
  }

  public void Debug() {

    Iterator<TimeseriesInstance> i = this.results.iterator();

    while (i.hasNext()) {

      double my_dist = 0;
      TimeseriesInstance tsi = i.next();
      try {
        my_dist = EuclideanDistance.seriesDistance(tsi.getTS().values(), this.search_ts.values());
      }
      catch (TSException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      System.out.println("Debug > Result > " + tsi.getTS() + ", dist: " + my_dist);

    }

  }

  public boolean removeExtraResults() {

    if (this.complete()) {

      while (this.results.size() > this.kNN) {

        System.out.println("Debug > removeExtraResults!");
        this.last();

      }

      return true;
    }

    return false;

  }

  public boolean hasNext() {

    return !this.results.isEmpty();

  }

  public TimeseriesInstance next() {

    return this.results.pollFirst();

  }

  public TimeseriesInstance peekNext() {

    return this.results.first();

  }

  public TimeseriesInstance peekLast() {

    return this.results.last();

  }

  public TimeseriesInstance last() {

    return this.results.pollLast();

  }

}
