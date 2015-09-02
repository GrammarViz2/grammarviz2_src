package net.seninp.grammarviz.logic;

/**
 * The GI algorithm selection.
 * 
 * @author Pavel Senin
 * 
 */
public enum GIAlgorithm {

  SEQUITUR(0),

  REPAIR(1);

  private final int index;

  /**
   * Constructor.
   * 
   * @param index The algorithm's index.
   */
  GIAlgorithm(int index) {
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
   * Makes an algorithm selection out of an integer.
   * 
   * @param value the key value.
   * @return the new Strategy instance.
   */
  public static GIAlgorithm fromValue(int value) {
    switch (value) {
    case 0:
      return GIAlgorithm.SEQUITUR;
    case 1:
      return GIAlgorithm.REPAIR;
    default:
      throw new RuntimeException("Unknown index:" + value);
    }
  }

  /**
   * Parse the string value into an instance.
   * 
   * @param value the string value.
   * @return new instance.
   */
  public static GIAlgorithm fromValue(String value) {
    if ("sequitur".equalsIgnoreCase(value)) {
      return GIAlgorithm.SEQUITUR;
    }
    else if ("repair".equalsIgnoreCase(value) || "re-pair".equalsIgnoreCase(value)) {
      return GIAlgorithm.REPAIR;
    }
    else {
      throw new RuntimeException("Unknown index:" + value);
    }
  }

  /**
   * {@inheritDoc}
   */
  public String toString() {
    switch (this.index) {
    case 0:
      return "SEQUITUR";
    case 1:
      return "REPAIR";
    default:
      throw new RuntimeException("Unknown index:" + this.index);
    }
  }
}
