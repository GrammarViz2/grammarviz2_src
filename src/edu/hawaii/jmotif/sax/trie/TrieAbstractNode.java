package edu.hawaii.jmotif.sax.trie;

/**
 * Abstract node class.
 *
 * @author Pavel Senin
 *
 */
public abstract class TrieAbstractNode {

  /** The node label. */
  private String label;

  /**
   * Get the node type (leaf or internal node).
   *
   * @return the Node type.
   */
  abstract TrieNodeType getType();

  /**
   * Constructor.
   */
  public TrieAbstractNode() {
    super();
    this.label = "";
  }

  /**
   * Constructor.
   *
   * @param label The node label.
   */
  public TrieAbstractNode(String label) {
    super();
    if (null == label) {
      this.label = "";
    }
    else {
      this.label = String.copyValueOf(label.toCharArray());
    }
  }

  /**
   * Set the node label.
   *
   * @param label The label to set.
   */
  public void setLabel(String label) {
    this.label = String.copyValueOf(label.toCharArray());
  }

  /**
   * Get the label.
   *
   * @return The node label.
   */
  public String getLabel() {
    return this.label;
  }

}
