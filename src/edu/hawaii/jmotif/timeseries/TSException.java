package edu.hawaii.jmotif.timeseries;

/**
 * The timeseries custom exception.
 * 
 * @author Pavel Senin
 * 
 */
public class TSException extends Exception {

  /** The default serial version UID. */
  private static final long serialVersionUID = 1L;

  /**
   * Thrown when some problem occurs.
   * 
   * @param description The problem description.
   * @param error The previous error.
   */
  public TSException(String description, Throwable error) {
    super(description, error);
  }

  /**
   * Thrown when some problem occurs.
   * 
   * @param description The problem description.
   */
  public TSException(String description) {
    super(description);
  }

}
