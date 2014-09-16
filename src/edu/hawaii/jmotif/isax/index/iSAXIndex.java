package edu.hawaii.jmotif.isax.index;

import edu.hawaii.jmotif.isax.ISAXUtils;
import edu.hawaii.jmotif.isax.Sequence;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.timeseries.Timeseries;

/**
 * Implements iSAX indexing
 * 
 * 
 * Search
 * 
 * The method of approximation is derived from the intuition that two similar time series are often
 * represented by the same iSAX word. Given this assumption, the approximate result is obtained by
 * attempting to find a terminal node in the index with the same iSAX representation as the query.
 * This is done by traversing the index in accordance with split policies and matching iSAX
 * representations at each internal node.
 * 
 * Because the index is hierarchical and without overlap, if such a terminal node exists, it is
 * promptly identified. Upon reaching this terminal node, the index file pointed to by the node is
 * fetched and returned. This file will contain at least 1 and at most th time series in it. A main
 * memory sequential scan over these time series gives the approximate search result.
 * 
 * In the (very) rare case that a matching terminal node does not exist, such a traversal will fail
 * at an internal node. We mitigate the effects of non-matches by proceeding down the tree,
 * selecting nodes whose last split dimension has a matching iSAX value with the query time series.
 * If no such node exists at a given junction, we simply select the first, and continue the descent.
 * 
 * Q. Why not hash directly to the iSAX pattern?
 * 
 * A. what if the bucket has exceeded its th threshold? at that point the iSAX space has been
 * promoted and split, we'll have to check the next level
 * 
 * 
 * Q. Why do we need internal nodes at all?
 * 
 * A. Otherwise we would not know to stop; these provide a type of "breadcrumbs" in search space to
 * let us know something is "down the rabbit hole".
 * 
 * 
 * Q. Why have such wide fan out from the root node?
 * 
 * A. to reduce to number of hops to the terminal nodes at the base cardinality level
 * 
 * 
 * Q. Are the { .., .., .. } nodes that are exist before the base-card-level actually terminal
 * nodes?
 * 
 * A. No. (the theory for now)
 * 
 * 
 * Q. Is there any need for intermediate nodes at the "pre-base-card-level" ?
 * 
 * A. No. (for now)
 * 
 * 
 * @author Josh Patterson
 * 
 */
public class iSAXIndex {

  private AbstractNode root_node;

  /**
   * Constructor
   * 
   * @param base_card
   * @param sax_word_len
   * @param orig_ts_len
   */
  public iSAXIndex(int base_card, int sax_word_len, int orig_ts_len) {

    IndexHashParams p = new IndexHashParams();
    p.base_card = base_card;
    p.d = 1;
    p.isax_word_length = sax_word_len;
    p.orig_ts_len = orig_ts_len;
    p.threshold = 100;

    Sequence s = new Sequence(orig_ts_len); // root node seqeunce, needs nothing more than a word
                                            // len

    this.root_node = new InternalNode(s, p, NodeType.ROOT);

  }

  /**
   * Takes a fragment of DNA characters and indexes them into the isax index based on a window size.
   * 
   * @param dna_sample
   * @param window_len
   * @param source_name
   */
  public void InsertDNASample(String dna_sample, int window_len, String source_name) {

    String dna_window = "";
    int curr_offset = 0;
    int beginIndex = 0;
    int endIndex = 0;

    while (curr_offset < dna_sample.length()) {

      beginIndex = curr_offset;
      if (beginIndex + window_len < dna_sample.length()) {

        endIndex = beginIndex + window_len;

      }
      else {

        endIndex = dna_sample.length();

      }

      dna_window = dna_sample.substring(beginIndex, endIndex);
      Timeseries ts_dna = null;

      try {
        ts_dna = ISAXUtils.CreateTimeseriesFromDNA(dna_window);
      }
      catch (TSException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      this.InsertSequence(ts_dna, source_name, beginIndex);

      curr_offset = endIndex;

    }

  }

  /**
   * Takes a Timeseries ts and inserts it into the isax index along with its source file and
   * location.
   * 
   * @param ts
   * @param filename
   * @param offset
   */
  public void InsertSequence(Timeseries ts, String filename, long offset) {

    TimeseriesInstance ts_inst = new TimeseriesInstance(ts);

    ts_inst.AddOccurence(filename, offset);
    try {
      this.InsertSequence(ts_inst);
    }
    catch (HashTreeException e) {

      // System.out.println("ts: " + ts.toString());
      // System.out.println("ts: " + ts_inst.getTS().toString());

    }
  }

  /**
   * Inserts a TimeseriesInstance into the isax index
   * 
   * @param ts_inst
   * @throws HashTreeException
   */
  public void InsertSequence(TimeseriesInstance ts_inst) throws HashTreeException {

    this.root_node.Insert(ts_inst);

  }

  /**
   * 
   * Approximate search for timeseries ts
   * 
   * Used to find all of the occurences of ts.
   * 
   * @param ts
   * @return a TimeseriesInstance object with filenames and offset positions.
   */
  public TimeseriesInstance ApproxSearch(Timeseries ts) {

    return this.root_node.ApproxSearch(ts);

  }

  /**
   * Search timeseries based on a SAX pattern which may include wildcard bits. Ideally used for a
   * more broad search when ApproxSearch fails. Not currently completely implemented.
   * 
   * @param seq
   * @return a TimeseriesInstance object with filenames and offset positions.
   */
  public TimeseriesInstance ApproxPatternSearch(Sequence seq) {

    // this.root_node.

    return null; // this.root_node.ApproxSearch(ts);

  }

  /**
   * we want to be able to take in an iSAX pattern with wildcards and find the subset that matches.
   * Works by taking the pattern and creating a Sequence with wildcard bits from that.
   * 
   * How?
   * 
   * - find all terminal nodes that match the wildcard pattern (no need for reverse tree)
   * 
   * - return this subset, or info about it
   * 
   * @return
   */
  public SearchResults WildcardSearch(String pattern, int limit) {

    return null;

  }

  /**
   * Find the kNN closest matches to the ts. (not complete)
   * 
   * - first find the terminal node that matches the pattern
   * 
   * - then we fill the results queue until we surpase "k"
   * 
   * - if we exhaust the local ts at the terminal node and we've yet to surpass "k"
   * 
   * -- then we back up to the previous internal node, selecting nodes whose last split dimension
   * has a matching iSAX value with the query time series.
   * 
   * If no such node exists at a given junction, we simply select the first, and continue the
   * descent.
   * 
   * @return
   */
  public kNNSearchResults kNNSearch(int k, Timeseries ts) {

    /*
     * In the (very) rare case that a matching terminal node does not exist, such a traversal will
     * fail at an internal node.
     * 
     * We mitigate the effects of non-matches by proceeding down the tree, selecting nodes whose
     * last split dimension has a matching iSAX value with the query time series.
     * 
     * If no such node exists at a given junction, we simply select the first, and continue the
     * descent.
     */

    kNNSearchResults results = new kNNSearchResults(k, ts);

    this.root_node.kNNSearch(results); // fix this

    return results;

  }

}
