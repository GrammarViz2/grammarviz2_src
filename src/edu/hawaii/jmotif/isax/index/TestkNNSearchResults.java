package edu.hawaii.jmotif.isax.index;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import edu.hawaii.jmotif.timeseries.TPoint;
import edu.hawaii.jmotif.timeseries.Timeseries;

public class TestkNNSearchResults {

  @Test
  public void testBasicQueueFunctionality() {

    Timeseries ts_search = new Timeseries();
    ts_search.add(new TPoint(1.0, 0));
    ts_search.add(new TPoint(1.0, 1));

    kNNSearchResults results = new kNNSearchResults(3, ts_search);

    Timeseries ts1 = new Timeseries();
    ts1.add(new TPoint(1.0, 0));
    ts1.add(new TPoint(1.0, 1));

    TimeseriesInstance A = new TimeseriesInstance(ts1);

    results.AddResult(A);

    assertEquals("test Count", 1, results.count());

    assertEquals("test has next", true, results.hasNext());

    assertEquals("test next", true, results.next() instanceof TimeseriesInstance);

  }

  @Test
  public void testBasicQueueSorting() {

    Timeseries ts_search = new Timeseries();
    ts_search.add(new TPoint(1.0, 0));
    ts_search.add(new TPoint(1.0, 1));

    kNNSearchResults results = new kNNSearchResults(3, ts_search);

    Timeseries ts1 = new Timeseries(); // 1
    ts1.add(new TPoint(1.0, 0));
    ts1.add(new TPoint(1.0, 1));

    TimeseriesInstance A = new TimeseriesInstance(ts1);

    Timeseries ts2 = new Timeseries(); // 3
    ts2.add(new TPoint(0.0, 0));
    ts2.add(new TPoint(0.0, 1));

    TimeseriesInstance B = new TimeseriesInstance(ts2);

    Timeseries ts3 = new Timeseries(); // 2
    ts3.add(new TPoint(0.0, 0));
    ts3.add(new TPoint(1.0, 1));

    TimeseriesInstance C = new TimeseriesInstance(ts3);

    results.AddResult(A);
    results.AddResult(B);
    results.AddResult(C);

    assertEquals("test Count", 3, results.count());

    assertEquals("test has next", true, results.hasNext());

    assertEquals("test least distance", ts1.equals(ts_search), true);

    assertEquals("test knn nearest found", true, results.peekNext().getTS().equals(ts1));

    while (results.hasNext()) {

      TimeseriesInstance out = results.next();

      System.out.println(" count: " + results.count() + ", ts: " + out.getTS());

    }

  }

  @Test
  public void testBasicQueueSorting_closest_neighbor() {

    System.out.println("\ntestBasicQueueSorting_closest_neighbor");

    Timeseries ts_search = new Timeseries();
    ts_search.add(new TPoint(1.05, 0));
    ts_search.add(new TPoint(0.99, 1));

    kNNSearchResults results = new kNNSearchResults(3, ts_search);

    Timeseries ts1 = new Timeseries(); // 1
    ts1.add(new TPoint(1.0, 0));
    ts1.add(new TPoint(1.0, 1));

    TimeseriesInstance A = new TimeseriesInstance(ts1);

    Timeseries ts2 = new Timeseries(); // 3
    ts2.add(new TPoint(0.0, 0));
    ts2.add(new TPoint(0.0, 1));

    TimeseriesInstance B = new TimeseriesInstance(ts2);

    Timeseries ts3 = new Timeseries(); // 2
    ts3.add(new TPoint(0.0, 0));
    ts3.add(new TPoint(1.0, 1));

    TimeseriesInstance C = new TimeseriesInstance(ts3);

    Timeseries ts4 = new Timeseries(); // 2
    ts4.add(new TPoint(0.01, 0));
    ts4.add(new TPoint(1.01, 1));

    TimeseriesInstance D = new TimeseriesInstance(ts4);

    results.AddResult(A);
    results.AddResult(B);
    results.AddResult(C);
    results.AddResult(D);

    assertEquals("test Count", 4, results.count());

    assertEquals("test has next", true, results.hasNext());

    // assertEquals( "test next", true, results.next() instanceof TimeseriesInstance );

    // assertEquals( "test least distance", ts1.equals(ts_search), true );

    assertEquals("test knn nearest found", true, results.peekNext().getTS().equals(ts1));

    while (results.hasNext()) {

      TimeseriesInstance out = results.next();

      System.out.println(" count: " + results.count() + ", ts: " + out.getTS());

    }

  }

  @Test
  public void testTailofResults() {

    System.out.println("\ntestTailofResults");

    Timeseries ts_search = new Timeseries();
    ts_search.add(new TPoint(1.05, 0));
    ts_search.add(new TPoint(0.99, 1));

    kNNSearchResults results = new kNNSearchResults(3, ts_search);

    Timeseries ts1 = new Timeseries(); // 1
    ts1.add(new TPoint(1.0, 0));
    ts1.add(new TPoint(1.0, 1));

    TimeseriesInstance A = new TimeseriesInstance(ts1);

    Timeseries ts2 = new Timeseries(); // 3
    ts2.add(new TPoint(0.0, 0));
    ts2.add(new TPoint(0.0, 1));

    TimeseriesInstance B = new TimeseriesInstance(ts2);

    Timeseries ts3 = new Timeseries(); // 2
    ts3.add(new TPoint(0.0, 0));
    ts3.add(new TPoint(1.0, 1));

    TimeseriesInstance C = new TimeseriesInstance(ts3);

    Timeseries ts4 = new Timeseries(); // 2
    ts4.add(new TPoint(0.01, 0));
    ts4.add(new TPoint(1.01, 1));

    TimeseriesInstance D = new TimeseriesInstance(ts4);

    results.AddResult(A);
    results.AddResult(B);
    results.AddResult(C);
    results.AddResult(D);

    assertEquals("test Count", 4, results.count());

    assertEquals("test has next", true, results.hasNext());

    // assertEquals( "test next", true, results.next() instanceof TimeseriesInstance );

    // assertEquals( "test least distance", ts1.equals(ts_search), true );

    assertEquals("test last in queue", true, results.peekLast().getTS().equals(ts2));

    while (results.hasNext()) {

      TimeseriesInstance out = results.last();

      System.out.println(" count: " + results.count() + ", ts: " + out.getTS());

    }

  }

  @Test
  public void testResultsComplete() {

    System.out.println("\ntestResultsComplete");

    Timeseries ts_search = new Timeseries();
    ts_search.add(new TPoint(1.05, 0));
    ts_search.add(new TPoint(0.99, 1));

    kNNSearchResults results = new kNNSearchResults(2, ts_search);

    Timeseries ts1 = new Timeseries(); // 1
    ts1.add(new TPoint(1.0, 0));
    ts1.add(new TPoint(1.0, 1));

    TimeseriesInstance A = new TimeseriesInstance(ts1);

    Timeseries ts2 = new Timeseries(); // 3
    ts2.add(new TPoint(0.0, 0));
    ts2.add(new TPoint(0.0, 1));

    TimeseriesInstance B = new TimeseriesInstance(ts2);

    Timeseries ts3 = new Timeseries(); // 2
    ts3.add(new TPoint(0.0, 0));
    ts3.add(new TPoint(1.0, 1));

    TimeseriesInstance C = new TimeseriesInstance(ts3);

    Timeseries ts4 = new Timeseries(); // 2
    ts4.add(new TPoint(0.01, 0));
    ts4.add(new TPoint(1.01, 1));

    TimeseriesInstance D = new TimeseriesInstance(ts4);

    results.AddResult(D);
    results.AddResult(C);
    results.AddResult(B);
    results.AddResult(A);

    assertEquals("test Count", 4, results.count());

    assertEquals("test complete", true, results.complete());

    assertEquals("test has next", true, results.hasNext());

    assertEquals("remove extra results", true, results.removeExtraResults());

    assertEquals("test peek last in queue", true, results.peekLast().getTS().equals(ts4));

    assertEquals("test next 1 in queue", true, results.next().getTS().equals(ts1));

    assertEquals("test next 2 in queue", true, results.next().getTS().equals(ts4));

  }

}
