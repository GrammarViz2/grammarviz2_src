package edu.hawaii.jmotif.sax.trie;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Inner node implementation.
 *
 * @author Pavel Senin
 *
 */
public class TrieInnerNode extends TrieAbstractNode {

  private HashMap<String, TrieAbstractNode> descendats;

  /** The occurrence locations. */
  private ArrayList<Integer> occurrences;

  /**
   * Constructor.
   */
  public TrieInnerNode() {
    super();
    initStorage();
  }

  /**
   * Constructor.
   *
   * @param nodeLabel The node label to set.
   */
  public TrieInnerNode(String nodeLabel) {
    super(nodeLabel);
    initStorage();
  }

  /**
   * Internal data structures init.
   */
  private void initStorage() {
    this.descendats = new HashMap<String, TrieAbstractNode>();
    this.occurrences = new ArrayList<Integer>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  TrieNodeType getType() {
    return TrieNodeType.INNER;
  }

  /**
   * Add node to the descendants list.
   *
   * @param node The node to add.
   */
  public void addNext(TrieAbstractNode node) {
    if (null != node) {
      this.descendats.put(node.getLabel(), node);
    }
  }

  /**
   * Deletes the node from the list of descendants.
   *
   * @param name Name of the node to delete.
   */
  public void delete(String name) {
    this.descendats.remove(name);
  }

  /**
   * Get all descendants as a list.
   *
   * @return all descendants as a list.
   */
  public Collection<TrieAbstractNode> getDescendants() {
    return this.descendats.values();
  }

  /**
   * Get the descendant by name.
   *
   * @param name The name parameter.
   * @return The found node or null.
   */
  public TrieAbstractNode getDescendant(String name) {
    return this.descendats.get(name);
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
