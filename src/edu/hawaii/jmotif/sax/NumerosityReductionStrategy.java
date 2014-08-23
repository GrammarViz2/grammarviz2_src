package edu.hawaii.jmotif.sax;

public enum NumerosityReductionStrategy {
  NONE(0), EXACT(1), MINDIST(2);

  private final int index;

  private static final String KEY_NONE = "NONE";
  private static final String KEY_EXACT = "EXACT";
  private static final String KEY_MINDIST = "MINDIST";

  NumerosityReductionStrategy(int index) {
    this.index = index;
  }

  public int index() {
    return index;
  }

  public static NumerosityReductionStrategy fromValue(int value) {
    switch (value) {
    case 0:
      return NumerosityReductionStrategy.NONE;
    case 1:
      return NumerosityReductionStrategy.EXACT;
    case 2:
      return NumerosityReductionStrategy.MINDIST;
    default:
      throw new RuntimeException("Unknown index:" + value);
    }
  }

  public static NumerosityReductionStrategy fromString(String command) {
    if (KEY_NONE.equalsIgnoreCase(command)) {
      return NONE;
    }
    else if (KEY_EXACT.equalsIgnoreCase(command)) {
      return EXACT;
    }
    else if (KEY_MINDIST.equalsIgnoreCase(command)) {
      return MINDIST;
    }
    throw new RuntimeException("Unknown index");
  }

  @Override
  public String toString() {
    switch (this.index) {
    case 0:
      return KEY_NONE;
    case 1:
      return KEY_EXACT;
    case 2:
      return KEY_MINDIST;
    default:
      throw new RuntimeException("Unknown index");
    }
  }
}
