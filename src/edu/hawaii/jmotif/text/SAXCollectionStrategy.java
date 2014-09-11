package edu.hawaii.jmotif.text;

/**
 * The SAX Collection srategy.
 * 
 * @author Pavel Senin
 * 
 */
public enum SAXCollectionStrategy {
  /** Classic - the Lin's and Keogh's MINDIST based strategy. */
  CLASSIC(0),

  /**
   * Exact - the strategy based on the exact string match - only strings that exactly match previous
   * string are excluded.
   */
  EXACT(1),

  /** Noreduction - all the words going make it into collection. */
  NOREDUCTION(2);

  private final int index;

  SAXCollectionStrategy(int index) {
    this.index = index;
  }

  /**
   * Gets the integer index of the instance.
   * 
   * @return integer key of the instance.
   */
  public int index() {
    return index;
  }

  /**
   * Makes a strategy out of integer. 0 stands for Classic, 1 for Exact, and 3 for Noreduction.
   * 
   * @param value the key value.
   * @return the new Strategy instance.
   */
  public static SAXCollectionStrategy fromValue(int value) {
    switch (value) {
    case 0:
      return SAXCollectionStrategy.CLASSIC;
    case 1:
      return SAXCollectionStrategy.EXACT;
    case 2:
      return SAXCollectionStrategy.NOREDUCTION;
    default:
      throw new RuntimeException("Unknown index:" + value);
    }
  }
}
