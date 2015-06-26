package net.seninp.jmotif.direct;

public class LOOCVStackEntry {

  private String key;
  private double[] value;
  private int index;

  public LOOCVStackEntry(String key, double[] value, int index) {
    this.key = key;
    this.value = value;
    this.index = index;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public double[] getValue() {
    return value;
  }

  public void setValue(double[] value) {
    this.value = value;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

}
