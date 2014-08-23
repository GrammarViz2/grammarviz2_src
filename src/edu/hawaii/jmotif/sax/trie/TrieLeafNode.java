package edu.hawaii.jmotif.sax.trie;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements a leaf node of the trie structure.
 *
 * @author Pavel Senin.
 *
 */
public class TrieLeafNode extends TrieAbstractNode {

  /** The occurrence locations. */
  private ArrayList<Integer> occurrences;

  /**
   * Constructor.
   */
  public TrieLeafNode() {
    super();
    initStorage();
  }

  /**
   * Constructor.
   *
   * @param name The node label to set.
   */
  public TrieLeafNode(String name) {
    super(name);
    initStorage();
  }

  /**
   * Internal data structures init.
   */
  private void initStorage() {
    this.occurrences = new ArrayList<Integer>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  TrieNodeType getType() {
    return TrieNodeType.LEAF;
  }

  /**
   * Get all occurrences associated with this leaf.
   *
   * @return Array of all occurrences associated with this leaf.
   */
  public List<Integer> getOccurences() {
    return this.occurrences;
  }

  /**
   * Add new occurrence.
   *
   * @param pos Occurrence position.
   */
  public void addOccurrence(int pos) {
    if (!this.occurrences.contains(Integer.valueOf(pos))) {
      this.occurrences.add(pos);
    }
  }

  /**
   * Add multiple new occurrences.
   *
   * @param occurrences List of occurrences to add.
   */
  public void addOccurrences(List<Integer> occurrences) {
    this.occurrences.addAll(occurrences);
  }

}
