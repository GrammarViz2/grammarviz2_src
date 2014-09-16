package edu.hawaii.jmotif.isax.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import edu.hawaii.jmotif.isax.ISAXUtils;
import edu.hawaii.jmotif.isax.Sequence;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.timeseries.Timeseries;

/**
 * A TerminalNode is the leaf node of an isax index. It contains TimeseriesInstance entries which
 * points to locations of Timeseries in the source data.
 * 
 * From iSAX paper:
 * 
 * A terminal node is a leaf node which contains a pointer to an index file on disk with raw time
 * series entries. All time series in the corresponding index file are characterized by the terminal
 * node�s representative iSAX word.
 * 
 * A terminal node represents the coarsest granularity necessary in SAX space to enclose the set of
 * contained time series entries.
 * 
 * In the event that an insertion causes the number of time series to exceed th, the SAX space (and
 * node) is split to provide additional differentiation.
 * 
 * @author Josh Patterson
 * 
 */
public class TerminalNode extends AbstractNode {

  public HashMap<String, TimeseriesInstance> arInstances = new HashMap<String, TimeseriesInstance>();

  /**
   * Constructor.
   */
  public TerminalNode(Sequence seq_key, IndexHashParams params) {
    super();
    this.setType(NodeType.TERMINAL);
    this.key = seq_key;
    this.params = params;
  }

  /**
   * Determines if a TerminalNode needs to split based on how many TimeseriesInstances it contains
   * and the threshold.
   * 
   */
  @Override
  public boolean IsOverThreshold() {

    if (this.params.threshold < 1) {
      // System.out.println("bad threshold!");
      return false;
    }

    if (this.arInstances.size() > this.params.threshold) {
      return true;
    }

    return false;

  }

  public Iterator<String> getNodeInstancesIterator() {
    return this.arInstances.keySet().iterator();
  }

  public void DebugInstances() {

    // System.out.println("TerminalNode > DebugInstances ----- ");

    Iterator<String> itr = this.arInstances.keySet().iterator();

    while (itr.hasNext()) {

      String strKey = itr.next().toString();

      // System.out.println("T-node-ts-key: " + strKey);

      if (null == this.arInstances.get(strKey)) {

        // System.out.println("TerminalNode > Debug > Null: " + strKey + ", count: "
        // + this.arInstances.size());

      }
      else {

      }
      // new_node.Insert( node.arInstances.get(strKey) );

    }

    // System.out.println("---------------------------------");

  }

  /**
   * The insert call used from a parent internal/root node.
   * 
   */
  @Override
  public void Insert(TimeseriesInstance ts_inst) {

    Sequence ts_isax = null;

    try {
      // lets get our SAX word based on the params of this node and its key
      ts_isax = ISAXUtils.CreateiSAXSequenceBasedOnCardinality(ts_inst.getTS(), this.key);
    }
    catch (TSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    String isax_hash = ts_isax.getIndexHash(); // this.params.createMaskedBitSequence(isax);

    // termination check, AKA "is this me?"

    if (this.key.getIndexHash().equals(isax_hash)) {

      if (this.arInstances.containsKey(ts_inst.getTS().toString())) {

        // merge

        TimeseriesInstance ts_int_existing = this.arInstances.get(ts_inst.getTS().toString());
        ts_int_existing.AddOccurences(ts_inst);

      }
      else {

        // add
        try {

          // // System.out.println( "add > key > " + ts_inst.getTS().toString() );

          this.arInstances.put(ts_inst.getTS().toString(), ts_inst.clone());
        }
        catch (CloneNotSupportedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

      }

      if (this.params.bDebug) {
        // System.out.println("|");
      }

      // this.DebugInstances();

      // // System.out.println( "TerminalNode > Inserted(instances:" + this.arInstances.size() +
      // ") > "
      // + isax_hash + " @ " + this.key.getStringRepresentation() + ", occurrences: " +
      // this.arInstances.get( ts_inst.getTS().toString() ).getOccurences().size() ) ;

    }
    else {

      // ok, how did we get here?

      // System.out.println("Should not have recv'd a ts at this TerminalNode!!!");

    }

  }

  /**
   * Approximate search function that is called as a search pass recurses down the hash-tree. Once
   * the search has hit a TerminalNode it typically terminates and returns TimeseriesInstances.
   * 
   */
  @Override
  public TimeseriesInstance ApproxSearch(Timeseries ts) {

    Sequence ts_isax = null;

    // ArrayList<Integer> arCards = IndexHashParams.generateChildCardinality( this.key );
    ArrayList<Integer> arCards = this.key.getCardinalities();

    try {
      ts_isax = ISAXUtils.CreateiSAXSequenceBasedOnCardinality(ts, arCards);
    }
    catch (TSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // System.out.println("Terminal Node Debug > Approx Search ");
    // System.out.println("Searching For > Seq: " + ts_isax.getBitStringRepresentation());
    // System.out.println("Searching For > ts: " + ts.toString());
    this.DebugInstances();

    String isax_hash = ts_isax.getIndexHash(); // this.params.createMaskedBitSequence(isax);

    // termination check, AKA "is this me?"

    // System.out.println("key( " + this.key.getIndexHash() + " ) == search( " + isax_hash +
    // " ) ?");

    if (this.key.getIndexHash().equals(isax_hash)) {

      // System.out.println("found > key > " + isax_hash + ", looking for exact match");

      if (this.arInstances.containsKey(ts.toString())) {

        // System.out.println("found match!");

        return this.arInstances.get(ts.toString());

      }

    } // if

    return null;

  }

  /**
   * kNNSearch at the terminal node level (Broken currently - fix me! - waiting on MINDIST_PAA_SAX
   * to work correctly!)
   * 
   * We have no descendants so we're only interested in looking at the instances in this node
   * 
   * while we have not hit ( count >= k )
   * 
   * - Do: add next closest instance to ts based on euclidean distance
   * 
   * 
   * Impl:
   * 
   * - we should add all ts to the results and then cull down the farthest at the end until we're
   * under k
   * 
   */
  @Override
  public void kNNSearch(kNNSearchResults results) {

    // System.out.println("Search > Terminal Node > key: " + this.key.getBitStringRepresentation()
    // + ", inst: " + this.arInstances.size());

    Iterator<String> itr = this.arInstances.keySet().iterator();

    while (itr.hasNext()) {

      String strKey = itr.next().toString();

      results.AddResult(this.arInstances.get(strKey));

    }

  }

  @Override
  public TimeseriesInstance getNodeInstanceByKey(String strKey) {
    return this.arInstances.get(strKey);
  }

  @Override
  public HashMap<String, TimeseriesInstance> getNodeInstances() {
    return this.arInstances;
  }

}
