package edu.hawaii.jmotif.sax.datastructures;

/**
 * The SAX result entry used for SAX chopped TS representation.
 * 
 * @author pavel Senin.
 * 
 */
public class SAXDataEntry {
  /** The substring. */
  private final String Str;
  /** The index pointer. */
  private final Integer Idx;

  /**
   * Constructor.
   * 
   * @param s The substring value.
   * @param i The index pointer.
   */
  public SAXDataEntry(String s, Integer i) {
    this.Str = s;
    this.Idx = i;
  }

  /**
   * Get the substring value.
   * 
   * @return The substring value.
   */
  public String getStr() {
    return Str;
  }

  /**
   * Get the index.
   * 
   * @return The index value.
   */
  public Integer getIdx() {
    return Idx;
  }

}
