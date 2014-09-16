package edu.hawaii.jmotif.isax.index;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import edu.hawaii.jmotif.timeseries.TPoint;
import edu.hawaii.jmotif.timeseries.Timeseries;

public class TestTimeseriesInstance {

  @Test
  public void testAddOccurences() {

    Timeseries ts = new Timeseries();
    ts.add(new TPoint(1.0, 0));
    ts.add(new TPoint(-0.5, 1));
    ts.add(new TPoint(0.25, 2));
    ts.add(new TPoint(0.0, 3));
    ts.add(new TPoint(0.25, 4));
    ts.add(new TPoint(0.50, 5));
    ts.add(new TPoint(0.75, 6));
    ts.add(new TPoint(0.0, 7));

    TimeseriesInstance tsi = new TimeseriesInstance(ts);

    tsi.AddOccurence("foo.txt", 20);
    tsi.AddOccurence("foo.txt", 30);

    assertEquals("occurences test", tsi.getOccurences().size(), 2);

    Timeseries ts2 = new Timeseries();
    ts2.add(new TPoint(1.0, 0));
    ts2.add(new TPoint(-0.5, 1));
    ts2.add(new TPoint(0.25, 2));
    ts2.add(new TPoint(0.0, 3));
    ts2.add(new TPoint(0.25, 4));
    ts2.add(new TPoint(0.50, 5));
    ts2.add(new TPoint(0.75, 6));
    ts2.add(new TPoint(0.0, 7));

    TimeseriesInstance tsi2 = new TimeseriesInstance(ts2);

    tsi.AddOccurence("foo2.txt", 4420);
    tsi.AddOccurence("foo2.txt", 44312330);

    tsi.AddOccurences(tsi2);

    assertEquals("occrences test 2", tsi.getOccurences().size(), 4);

  }

  @Test
  public void testEquals() {

    Timeseries ts1 = new Timeseries();
    ts1.add(new TPoint(1.0, 0));
    ts1.add(new TPoint(0.0, 1));

    TimeseriesInstance A = new TimeseriesInstance(ts1);

    Timeseries ts2 = new Timeseries();
    ts2.add(new TPoint(1.0, 0));
    ts2.add(new TPoint(0.0, 1));

    TimeseriesInstance B = new TimeseriesInstance(ts2);

    Timeseries ts3 = new Timeseries();
    ts3.add(new TPoint(1.0, 0));
    ts3.add(new TPoint(1.0, 1));

    TimeseriesInstance C = new TimeseriesInstance(ts3);

    assertEquals("tsi equals base test", true, A.equals(B));

    assertEquals("tsi not-equals base test", false, A.equals(C));

  }

  @Test
  public void testCompareTo_0() {

    // System.out.println("\n\n------ testCompareTo() --------");

    Timeseries ts1 = new Timeseries();
    ts1.add(new TPoint(1.0, 0));
    ts1.add(new TPoint(0.0, 1));

    TimeseriesInstance A = new TimeseriesInstance(ts1);

    Timeseries ts2 = new Timeseries();
    ts2.add(new TPoint(1.0, 0));
    ts2.add(new TPoint(0.0, 1));

    TimeseriesInstance B = new TimeseriesInstance(ts2);

    Timeseries ts3 = new Timeseries();
    ts3.add(new TPoint(0.0, 0));
    ts3.add(new TPoint(0.0, 1));

    A.setComparableReferencePoint(ts3);
    B.setComparableReferencePoint(ts3);

    int comp = A.compareTo(B);

    // System.out.println("compareTo: " + comp);

    assertEquals("testCompareTo", 0, comp);
  }

  @Test
  public void testCompareTo_1() {

    // System.out.println("\n\n------ testCompareTo() 1 --------");

    Timeseries ts1 = new Timeseries();
    ts1.add(new TPoint(1.0, 0));
    ts1.add(new TPoint(1.0, 1));

    TimeseriesInstance A = new TimeseriesInstance(ts1);

    Timeseries ts2 = new Timeseries();
    ts2.add(new TPoint(0.0, 0));
    ts2.add(new TPoint(0.0, 1));

    TimeseriesInstance B = new TimeseriesInstance(ts2);

    Timeseries ts3 = new Timeseries();
    ts3.add(new TPoint(0.0, 0));
    ts3.add(new TPoint(0.0, 1));

    A.setComparableReferencePoint(ts3);
    B.setComparableReferencePoint(ts3);

    int comp = A.compareTo(B);

    // System.out.println("compareTo: " + comp);

    assertEquals("testCompareTo 1", 1, comp);

    int comp2 = B.compareTo(A);

    assertEquals("testCompareTo -1", -1, comp2);

  }

}
