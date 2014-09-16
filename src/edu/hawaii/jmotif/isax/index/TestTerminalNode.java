package edu.hawaii.jmotif.isax.index;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import edu.hawaii.jmotif.isax.ISAXUtils;
import edu.hawaii.jmotif.isax.Sequence;
import edu.hawaii.jmotif.timeseries.TPoint;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.timeseries.Timeseries;

public class TestTerminalNode {

  @Test
  public void testSplitThreshold() {

    Timeseries ts_2 = new Timeseries();

    ts_2.add(new TPoint(-1.0, 0));
    ts_2.add(new TPoint(-0.5, 1));
    ts_2.add(new TPoint(-0.25, 2));
    ts_2.add(new TPoint(0.0, 3));

    ts_2.add(new TPoint(0.25, 4));
    ts_2.add(new TPoint(0.50, 5));
    ts_2.add(new TPoint(0.75, 6));
    ts_2.add(new TPoint(2.0, 7));

    Sequence seq = null;
    try {
      seq = ISAXUtils.CreateiSAXSequence(ts_2, 4, 4);
    }
    catch (TSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // IndexHashParams params = new IndexHashParams();
    IndexHashParams p = new IndexHashParams();
    p.base_card = 4;
    p.d = 1;
    p.isax_word_length = 4;
    p.orig_ts_len = 8;

    TerminalNode node = new TerminalNode(seq, p);

    TimeseriesInstance tsi_A = new TimeseriesInstance(ts_2);
    tsi_A.AddOccurence("foo.txt", 10);

    TimeseriesInstance tsi_B = new TimeseriesInstance(ts_2);
    tsi_B.AddOccurence("foo.txt", 1);

    node.Insert(tsi_B);

    // System.out.println(" size: " + node.arInstances.size());

    assertEquals("base size test", 1, node.arInstances.size());

    node.Insert(tsi_A);

    // System.out.println(" size: " + node.arInstances.size());

    assertEquals("same instance key insert test", 1, node.arInstances.size());

    node.DebugInstances();

  }

  @Test
  public void testSplitThreshold_2() {

    Timeseries ts_1 = new Timeseries();

    ts_1.add(new TPoint(1.0, 0));
    ts_1.add(new TPoint(-0.5, 1));
    ts_1.add(new TPoint(-0.25, 2));
    ts_1.add(new TPoint(0.0, 3));

    ts_1.add(new TPoint(0.25, 4));
    ts_1.add(new TPoint(0.50, 5));
    ts_1.add(new TPoint(0.75, 6));
    ts_1.add(new TPoint(-2.0, 7));

    Timeseries ts_2 = new Timeseries();

    ts_2.add(new TPoint(-1.0, 0));
    ts_2.add(new TPoint(-0.5, 1));
    ts_2.add(new TPoint(-0.25, 2));
    ts_2.add(new TPoint(0.0, 3));

    ts_2.add(new TPoint(0.25, 4));
    ts_2.add(new TPoint(0.50, 5));
    ts_2.add(new TPoint(0.75, 6));
    ts_2.add(new TPoint(2.0, 7));

    Sequence seq = null;
    try {
      seq = ISAXUtils.CreateiSAXSequence(ts_2, 4, 4);
    }
    catch (TSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // IndexHashParams params = new IndexHashParams();
    IndexHashParams p = new IndexHashParams();
    p.base_card = 4;
    p.d = 1;
    p.isax_word_length = 4;
    p.orig_ts_len = 8;
    p.threshold = 2;

    TerminalNode node = new TerminalNode(seq, p);

    TimeseriesInstance tsi_A = new TimeseriesInstance(ts_1);
    tsi_A.AddOccurence("foo.txt", 10);

    TimeseriesInstance tsi_B = new TimeseriesInstance(ts_2);
    tsi_B.AddOccurence("foo.txt", 1);

    node.Insert(tsi_B);

    // System.out.println(" size: " + node.arInstances.size());

    assertEquals("base size test", 1, node.arInstances.size());

    node.Insert(tsi_A);

    // System.out.println(" size: " + node.arInstances.size());

    assertEquals("should not get this ts", 1, node.arInstances.size());

    node.DebugInstances();

  }

  @Test
  public void testSplitThreshold_3() {

    // node = new TerminalNode( ts_isax, this.params );

    Timeseries ts_1 = new Timeseries();

    ts_1.add(new TPoint(1.0, 0));
    ts_1.add(new TPoint(-0.5, 1));
    ts_1.add(new TPoint(-0.25, 2));
    ts_1.add(new TPoint(0.0, 3));

    ts_1.add(new TPoint(0.25, 4));
    ts_1.add(new TPoint(0.50, 5));
    ts_1.add(new TPoint(0.75, 6));
    ts_1.add(new TPoint(-2.0, 7));

    Timeseries ts_2 = new Timeseries();

    ts_2.add(new TPoint(1.0, 0));
    ts_2.add(new TPoint(-0.5, 1));
    ts_2.add(new TPoint(-0.25, 2));
    ts_2.add(new TPoint(0.0, 3));

    ts_2.add(new TPoint(0.25, 4));
    ts_2.add(new TPoint(0.50, 5));
    ts_2.add(new TPoint(0.75, 6));
    ts_2.add(new TPoint(-2.1, 7));

    Timeseries ts_3 = new Timeseries();

    ts_3.add(new TPoint(1.0, 0));
    ts_3.add(new TPoint(-0.5, 1));
    ts_3.add(new TPoint(-0.25, 2));
    ts_3.add(new TPoint(0.0, 3));

    ts_3.add(new TPoint(0.25, 4));
    ts_3.add(new TPoint(0.50, 5));
    ts_3.add(new TPoint(0.75, 6));
    ts_3.add(new TPoint(-1.9, 7));

    Timeseries ts_4 = new Timeseries();

    ts_4.add(new TPoint(1.0, 0));
    ts_4.add(new TPoint(-0.5, 1));
    ts_4.add(new TPoint(-0.25, 2));
    ts_4.add(new TPoint(0.0, 3));

    ts_4.add(new TPoint(0.25, 4));
    ts_4.add(new TPoint(0.50, 5));
    ts_4.add(new TPoint(0.75, 6));
    ts_4.add(new TPoint(-1.92, 7));

    Timeseries ts_5 = new Timeseries();

    ts_5.add(new TPoint(1.0, 0));
    ts_5.add(new TPoint(-0.5, 1));
    ts_5.add(new TPoint(-0.25, 2));
    ts_5.add(new TPoint(0.0, 3));

    ts_5.add(new TPoint(0.25, 4));
    ts_5.add(new TPoint(0.50, 5));
    ts_5.add(new TPoint(0.75, 6));
    ts_5.add(new TPoint(-1.92, 7));

    Sequence seq = null;
    try {
      seq = ISAXUtils.CreateiSAXSequence(ts_2, 4, 4);
    }
    catch (TSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // IndexHashParams params = new IndexHashParams();
    IndexHashParams p = new IndexHashParams();
    p.base_card = 4;
    p.d = 1;
    p.isax_word_length = 4;
    p.orig_ts_len = 8;
    p.threshold = 2;

    TerminalNode node = new TerminalNode(seq, p);

    TimeseriesInstance tsi_A = new TimeseriesInstance(ts_1);
    tsi_A.AddOccurence("foo.txt", 10);

    TimeseriesInstance tsi_B = new TimeseriesInstance(ts_2);
    tsi_B.AddOccurence("foo.txt", 1);

    TimeseriesInstance tsi_C = new TimeseriesInstance(ts_3);
    tsi_B.AddOccurence("foo.txt", 12);

    node.Insert(tsi_B);

    // System.out.println(" size: " + node.arInstances.size());

    assertEquals("base size test", 1, node.arInstances.size());

    // System.out.println("split? " + node.IsOverThreshold());

    node.Insert(tsi_A);

    // System.out.println(" size: " + node.arInstances.size());

    assertEquals("both should be here", 2, node.arInstances.size());

    // System.out.println("split? " + node.IsOverThreshold());

    node.Insert(tsi_C);

    // System.out.println(" size: " + node.arInstances.size());

    assertEquals("3 instances", 3, node.arInstances.size());

    // System.out.println("split? " + node.IsOverThreshold());

    node.DebugInstances();

    assertEquals("Should Split", true, node.IsOverThreshold());

  }

  @Test
  public void testGen() {

    // System.out.println("testGen ---------------------");

    // System.out.println("genTS: " + ISAXUtils.generateRandomTS(8));
    // System.out.println("genTS: " + ISAXUtils.generateRandomTS(8));
    // System.out.println("genTS: " + ISAXUtils.generateRandomTS(8));
    // System.out.println("genTS: " + ISAXUtils.generateRandomTS(8));
    // System.out.println("genTS: " + ISAXUtils.generateRandomTS(8));

  }

}
