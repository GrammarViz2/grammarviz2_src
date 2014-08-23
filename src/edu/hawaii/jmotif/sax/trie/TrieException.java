package edu.hawaii.jmotif.sax.trie;

/**
 * The timeseries custom exception.
 *
 * @author Pavel Senin
 *
 */
public class TrieException extends Exception {

  /** The default serial version UID. */
  private static final long serialVersionUID = 1L;

  /**
   * Thrown when some problem occurs.
   *
   * @param description The problem description.
   * @param error The previous error.
   */
  public TrieException(String description, Throwable error) {
    super(description, error);
  }

  /**
   * Thrown when some problem occurs.
   *
   * @param description The problem description.
   */
  public TrieException(String description) {
    super(description);
  }

}
