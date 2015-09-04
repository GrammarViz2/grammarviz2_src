package net.seninp.grammarviz.anomaly;

public enum AnomalyAlgorithm {
  BRUTEFORCE(0), HOTSAX(1), HOTSAXTRIE(2), RRA(3), RRAPRUNED(4), RRASAMPLED(5);

  private final int index;

  AnomalyAlgorithm(int index) {
    this.index = index;
  }

  public int index() {
    return index;
  }

  public static AnomalyAlgorithm fromValue(int value) {
    switch (value) {
    case 0:
      return AnomalyAlgorithm.BRUTEFORCE;
    case 1:
      return AnomalyAlgorithm.HOTSAX;
    case 2:
      return AnomalyAlgorithm.HOTSAXTRIE;
    case 3:
      return AnomalyAlgorithm.RRA;
    case 4:
      return AnomalyAlgorithm.RRAPRUNED;
    case 5:
      return AnomalyAlgorithm.RRASAMPLED;
    default:
      throw new RuntimeException("Unknown index:" + value);
    }
  }

  public static AnomalyAlgorithm fromValue(String value) {
    if (value.equalsIgnoreCase("bruteforce")) {
      return AnomalyAlgorithm.BRUTEFORCE;
    }
    else if (value.equalsIgnoreCase("hotsax")) {
      return AnomalyAlgorithm.HOTSAX;
    }
    else if (value.equalsIgnoreCase("hotsaxtrie")) {
      return AnomalyAlgorithm.HOTSAXTRIE;
    }
    else if (value.equalsIgnoreCase("rra")) {
      return AnomalyAlgorithm.RRA;
    }
    else if (value.equalsIgnoreCase("rrapruned")) {
      return AnomalyAlgorithm.RRAPRUNED;
    }
    else if (value.equalsIgnoreCase("rrasampled")) {
      return AnomalyAlgorithm.RRASAMPLED;
    }
    else {
      throw new RuntimeException("Unknown index:" + value);
    }
  }

  @Override
  public String toString() {
    switch (this.index) {
    case 0:
      return "BRUTEFORCE";
    case 1:
      return "HOTSAX";
    case 2:
      return "HOTSAXTRIE";
    case 3:
      return "RRA";
    case 4:
      return "RRAPRUNED";
    case 5:
      return "RRASAMPLED";
    default:
      throw new RuntimeException("Unknown index");
    }
  }
}
