package edu.hawaii.jmotif.isax.index;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import org.junit.Test;
import edu.hawaii.jmotif.isax.ISAXUtils;
import edu.hawaii.jmotif.isax.Sequence;
import edu.hawaii.jmotif.timeseries.TPoint;
import edu.hawaii.jmotif.timeseries.Timeseries;

public class TestInternalNode {

  @Test
  public void testkNNSearchForNode() {

    IndexHashParams p = new IndexHashParams();
    p.base_card = 4;
    p.d = 1;
    p.isax_word_length = 4;
    p.orig_ts_len = 8;
    p.threshold = 100;

    Sequence s = new Sequence(8); // root node seqeunce, needs nothing more than a word len

    InternalNode node = new InternalNode(s, p, NodeType.ROOT);

    Timeseries ts = new Timeseries();

    ts.add(new TPoint(-1.0, 0));
    ts.add(new TPoint(-0.5, 1));
    ts.add(new TPoint(-0.25, 2));
    ts.add(new TPoint(0.0, 3));

    ts.add(new TPoint(0.25, 4));
    ts.add(new TPoint(0.50, 5));
    ts.add(new TPoint(0.75, 6));

    // the one we change
    ts.add(new TPoint(1.0, 7));

    System.out.println("------- Inserting -----------");
    for (int x = 0; x < 5; x++) {

      ts.elementAt(7).setValue(x);

      System.out.println("ts: " + ts);

      TimeseriesInstance ts_inst = new TimeseriesInstance(ts);

      ts_inst.AddOccurence("foo.txt", x);
      try {
        // node.InsertSequence( ts_inst );
        node.Insert(ts_inst);
      }
      catch (HashTreeException e) {

        System.out.println("ts: " + ts.toString());
        System.out.println("ts: " + ts_inst.getTS().toString());

      }

    }

    node.DebugChildNodes();

    kNNSearchResults r = new kNNSearchResults(10, ts);

    System.out.println("Search ---------------------------");

    System.out.println("search pattern > " + ts);

    node.kNNSearch(r);

    System.out.println("results: " + r.count());

    while (r.hasNext()) {

      System.out.println("> " + r.next().getTS().toString());

    }

  }

  @Test
  public void testkNNSearchForNode_2() {

    System.out.println("\n\n\ntestkNNSearchForNode_2 ");

    IndexHashParams p = new IndexHashParams();
    p.base_card = 4;
    p.d = 1;
    p.isax_word_length = 4;
    p.orig_ts_len = 8;
    p.threshold = 100;

    Sequence s = new Sequence(8); // root node seqeunce, needs nothing more than a word len

    InternalNode node = new InternalNode(s, p, NodeType.ROOT);

    Timeseries ts = new Timeseries();

    ts.add(new TPoint(-1.0, 0));
    ts.add(new TPoint(-0.5, 1));
    ts.add(new TPoint(-0.25, 2));
    ts.add(new TPoint(0.0, 3));

    ts.add(new TPoint(0.25, 4));
    ts.add(new TPoint(0.50, 5));
    ts.add(new TPoint(0.75, 6));

    // the one we change
    ts.add(new TPoint(1.0, 7));

    System.out.println("------- Inserting -----------");
    for (int x = 0; x < 5; x++) {

      // ts.elementAt(3).setValue(10 - x);
      ts.elementAt(7).setValue(x);

      System.out.println("ts: " + ts);

      // TimeseriesInstance tsi_A = new TimeseriesInstance( ts );

      // index.InsertSequence(ts, "genome.txt", 104526 + (x * 10) );

      TimeseriesInstance ts_inst = new TimeseriesInstance(ts);

      ts_inst.AddOccurence("foo.txt", x);
      try {
        // node.InsertSequence( ts_inst );
        node.Insert(ts_inst);
      }
      catch (HashTreeException e) {

        System.out.println("ts: " + ts.toString());
        System.out.println("ts: " + ts_inst.getTS().toString());

      }

    }

    for (int x = 0; x < 100; x++) {
      Timeseries ts_insert = ISAXUtils.generateRandomTS(8);
      // node.InsertSequence( ts_insert, "ts.txt", 1000 + x * 20);

      TimeseriesInstance ts_inst = new TimeseriesInstance(ts_insert);

      ts_inst.AddOccurence("foo2.txt", x);

      try {
        node.Insert(ts_inst);
      }
      catch (HashTreeException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    }

    node.DebugChildNodes();

    kNNSearchResults r = new kNNSearchResults(5, ts);

    System.out.println("Search ---------------------------");

    System.out.println("search pattern > " + ts);

    node.kNNSearch(r);

    System.out.println("results: " + r.count());
    /*
     * while ( r.hasNext() ) {
     * 
     * System.out.println( "> " + r.next().getTS().toString() );
     * 
     * }
     */
    r.removeExtraResults();
    r.Debug();

  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSerDe() {

    ArrayList<String> arList = new ArrayList<String>();
    arList.add("alpha");
    arList.add("beta");
    arList.add("gamma");
    /*
     * String[] arStr = new String[3]; arStr[0] = "a"; arStr[1] = "b"; arStr[2] = "c";
     */
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream out = null;
    try {
      out = new ObjectOutputStream(baos);
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.println("bytes: " + baos.size());

    try {
      out.writeObject(arList);
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.println("bytes: " + baos.size());

    ObjectInputStream in = null;
    try {
      in = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    try {
      ArrayList<String> o = (ArrayList<String>) in.readObject();

      in.close();

      for (int x = 0; x < o.size(); x++) {

        System.out.println(x + " > " + o.get(x));

      }

    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
