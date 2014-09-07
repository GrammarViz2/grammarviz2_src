package edu.hawaii.jmotif.isax.index;

import java.util.HashMap;
import java.util.Iterator;
import edu.hawaii.jmotif.isax.Sequence;
import edu.hawaii.jmotif.timeseries.Timeseries;

/**
 * Base class for iSAX hash tree
 * 
 * 
 * @author Josh Patterson
 * 
 */
public abstract class AbstractNode {

  private NodeType nt = NodeType.TERMINAL;

  public Sequence key; // = new Sequence(4); // this is the fundamental iSAX word that representats
                       // this split in sax space

  public IndexHashParams params;

  /**
   * Get the node type (leaf or internal node).
   * 
   * @return the Node type.
   */
  public NodeType getType() {
    return this.nt;
  }

  /**
   * Constructor.
   */
  public AbstractNode() {
    super();
    // this.label = "";
    // this.arSequences = new ArrayList<Timeseries>();
  }

  public HashMap<String, TimeseriesInstance> getNodeInstances() {
    return null;
  }

  public TimeseriesInstance getNodeInstanceByKey(String strKey) {
    return null;
  }

  public Iterator<String> getChildNodeIterator() {
    return null;
  }

  public Iterator<String> getNodeInstancesIterator() {
    return null;
  }

  /**
   * 
   * not sure what to do wiht this one
   * 
   * @throws HashTreeException
   * 
   */
  public void Insert(TimeseriesInstance ts_inst) throws HashTreeException {

  }

  /*
   * public boolean SequenceFallsInRange( Sequence isax ) {
   * 
   * return this.nk.ContainsSequence(isax);
   * 
   * }
   */
  public boolean IsOverThreshold() {

    return false;
  }

  public void setType(NodeType t) {
    this.nt = t;
  }

  /**
   * Performs a recursive lookup to find an approximate match, if it exists.
   * 
   * @param ts
   * @return
   */
  public TimeseriesInstance ApproxSearch(Timeseries ts) {

    return null;

  }

  /**
   * Performs kNN search for a sequence across the buckets. Currently broken.
   * 
   * @param results
   */
  public void kNNSearch(kNNSearchResults results) {

    // return null;
  }

  /*
   * public static boolean ContainsSequence( Sequence ts, IndexHashParams params, String
   * node_symbol_rep ) {
   * 
   * 
   * String ts_symbol_rep = params.createMaskedBitSequence(ts);
   * //ts.symbols.get(x).getiSAXBitRepresentation( params.arWildBits.get(x) );
   * 
   * if ( false == node_symbol_rep.equals(ts_symbol_rep) ) { return false; }
   * 
   * return true; }
   */

}
