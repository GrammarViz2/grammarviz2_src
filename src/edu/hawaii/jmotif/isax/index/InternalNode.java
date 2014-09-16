package edu.hawaii.jmotif.isax.index;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import edu.hawaii.jmotif.isax.ISAXUtils;
import edu.hawaii.jmotif.isax.Sequence;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.timeseries.Timeseries;

/**
 * 
 * An internal node designates a split in SAX space and is created when the number of time series
 * contained by a terminal node exceeds th. The internal node splits the SAX space by promotion of
 * cardinal values along one or more dimensions as per the iterative doubling policy.
 * 
 * A hash from iSAX words (representing subdivisions of the SAX space) to nodes is maintained to
 * distinguish differentiation between entries.
 * 
 * Time series from the terminal node which triggered the split are inserted into the newly created
 * internal node and hashed to their respective locations.
 * 
 * If the hash does not contain a matching iSAX entry, a new terminal node is created prior to
 * insertion, and the hash is updated accordingly. For simplicity, we employ binary splits along a
 * single dimension, using round robin to determine the split dimension.
 * 
 * 
 * 
 * Root Node
 * 
 * The root node is representative of the complete SAX space and is similar in functionality to an
 * internal node. The root node evaluates time series at base cardinality, that is, the granularity
 * of each dimension in the reduced representation is b. Encountered iSAX words correspond to some
 * terminal or internal node and are used to direct index functions accordingly.
 * 
 * @author jpatterson
 * 
 */
public class InternalNode extends AbstractNode {

  private TreeMap<String, AbstractNode> descendants = new TreeMap<String, AbstractNode>();

  /**
   * Constructor.
   */
  public InternalNode() {
    super();

  }

  /**
   * Constructor
   * 
   * @param isax_base_rep
   * @param params
   * @param nt
   */
  public InternalNode(Sequence isax_base_rep, IndexHashParams params, NodeType nt) {

    super();

    this.params = params;
    this.setType(nt);

    this.key = isax_base_rep;

  }

  public String getMaskedRepresentation() {

    return this.params.createMaskedBitSequence(this.key);

  }

  /**
   * This impl should not return anything since the internal nodes never actual hold instances
   */
  public Iterator<String> getChildNodeIterator() {

    return null; // this.arInstances.keySet().iterator();

  }

  /**
   * 
   * When a split occurs in SAX space, we turn a terminal node into an internal node, which then
   * changes how it handles inserts: example: internal node A now points to B and C
   * 
   * node A's SAX key stays the same, yet B and C have a split in SAX space based on increasing the
   * dim on the lowest cardinality in the array of symbols: 0^2 becomes 00^4 and 01^4, where 1^2
   * becomes 10^4 and 11^4
   * 
   * when we insert a new ts in this internal node, we hash at the card of its key (params)
   * 
   * if this new hash rep of the ts is in the hash-table, we pass it on to that node for insertion
   * 
   * 
   * @throws HashTreeException
   */
  @Override
  public void Insert(TimeseriesInstance ts_inst) throws HashTreeException {

    Sequence ts_isax = null;

    if (null == ts_inst) {

      // System.out.println("ts_inst came in null!!");

      throw new HashTreeException("null ts!");
    }

    // we know this is not the final resting place for this ts since this is an InternalNode

    AbstractNode node = null;

    if (this.getType() == NodeType.ROOT) {

      try {
        // lets get our SAX word based on the params of this node and its key
        // ts_isax = ISAXUtils.CreateiSAXSequenceBasedOnCardinality( ts, this.key );
        ts_isax = ISAXUtils.CreateiSAXSequence(ts_inst.getTS(), this.params.base_card,
            this.params.isax_word_length);
      }
      catch (TSException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    }
    else {

      ArrayList<Integer> arCards = IndexHashParams.generateChildCardinality(this.key);

      try {

        if (null == ts_inst.getTS()) {
          // System.out.println("getTS() null");
        }

        if (null == arCards) {
          // System.out.println("arCards null");
        }

        ts_isax = ISAXUtils.CreateiSAXSequenceBasedOnCardinality(ts_inst.getTS(), arCards);

      }
      catch (TSException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    }

    if (null == ts_isax) {
      // failed to insert
      // System.out.println(" InternalNode > Insert > Fail > " + ts_inst.getTS());
      return;
    }

    String isax_hash = ts_isax.getIndexHash(); // this.params.createMaskedBitSequence(isax);

    // we want to fan out at a rate of 2^d

    if (this.descendants.containsKey(isax_hash)) {

      //

      node = this.descendants.get(isax_hash);

      if (node.getType() == NodeType.TERMINAL) {

        // does it need to split?

        if (node.IsOverThreshold() == false) {

          // System.out.println(ts_inst + " -> " + isax_hash);

          node.Insert(ts_inst); // should be terminal node

        }
        else {

          // need to change this to "InternalNode.cloneAs( NodeType )"
          InternalNode new_node = new InternalNode(node.key, node.params, NodeType.INTERNAL);

          new_node.Insert(ts_inst);

          Iterator<String> itr = node.getNodeInstancesIterator();

          if (itr == null) {
            // System.out.println("ITR > no? type: " + node.getType());
          }

          while (itr.hasNext()) {

            String strKey = itr.next().toString();
            new_node.Insert(node.getNodeInstanceByKey(strKey));

          }

          this.descendants.remove(isax_hash);
          this.descendants.put(isax_hash, new_node);

        }

      }
      else if (node.getType() == NodeType.INTERNAL) {

        node.Insert(ts_inst);

      }

    }
    else {

      // if it does not contain this node, create a new one

      // create a key seqeunce based on the base cardinality
      node = new TerminalNode(ts_isax, this.params);

      // System.out.println("inserting new terminal node: " + isax_hash);

      node.Insert(ts_inst);
      this.descendants.put(isax_hash, node);

    }

  }

  public void DebugKeys() {

    Iterator<String> i = this.descendants.keySet().iterator();

    if (this.getType() == NodeType.ROOT) {
      // System.out.println("Debug > Root Node");
    }

    // System.out.println("DebugKeys > " + this.key.getBitStringRepresentation());
    while (i.hasNext()) {

      String key = i.next();

      // System.out.println("Node Key > Debug > " + key);

    }
  }

  public void DebugChildNodes() {

    Iterator<String> i = this.descendants.keySet().iterator();

    if (this.getType() == NodeType.ROOT) {
      // System.out.println("Debug > Root Node");
    }

    // System.out.println("Debug > This Node's Key > " + this.key.getBitStringRepresentation());
    while (i.hasNext()) {

      String key = i.next();

      // System.out.println("Decendant Node Key > Debug > " + key + ", instances: "
      // + this.descendants.get(key).getNodeInstances().size());

    }
  }

  /**
   * Approximate search finds a bucket, if it exists, that most closely matches the search key.
   * 
   */
  @Override
  public TimeseriesInstance ApproxSearch(Timeseries ts) {

    Sequence ts_isax = null;

    AbstractNode node = null;

    if (this.getType() == NodeType.ROOT) {

      try {
        // lets get our SAX word based on the params of this node and its key
        // ts_isax = ISAXUtils.CreateiSAXSequenceBasedOnCardinality( ts, this.key );
        ts_isax = ISAXUtils.CreateiSAXSequence(ts, this.params.base_card,
            this.params.isax_word_length);
      }
      catch (TSException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    }
    else {

      ArrayList<Integer> arCards = IndexHashParams.generateChildCardinality(this.key);

      try {
        ts_isax = ISAXUtils.CreateiSAXSequenceBasedOnCardinality(ts, arCards);
      }
      catch (TSException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    }

    String isax_hash = ts_isax.getIndexHash(); // this.params.createMaskedBitSequence(isax);

    // System.out.println("\nSearching For key: " + isax_hash + " from sequence: " + ts + "\n");
    this.DebugKeys();

    // we want to fan out at a rate of 2^d

    if (this.descendants.containsKey(isax_hash)) {

      node = this.descendants.get(isax_hash);

      return node.ApproxSearch(ts);

    }
    else {

      // if it does not contain this node

      // System.out.println("Debug > no descendant contained a key for this level!");
      return null;

    }
  }

  /**
   * kNN search functionality is currently broken due to invalid MINDIST_PAA_SAX function.
   * 
   */
  @Override
  public void kNNSearch(kNNSearchResults results) {

    // return null;
  }

}