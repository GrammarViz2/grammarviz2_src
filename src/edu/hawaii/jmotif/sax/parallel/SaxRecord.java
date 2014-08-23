package edu.hawaii.jmotif.sax.parallel;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Implement a data container for the parallel SAX.
 * 
 * @author psenin
 * 
 */
public class SaxRecord {

  /** The payload */
  private char[] saxString;

  /** The index of occurrences in the raw sequence. */
  private ArrayList<Integer> occurrences;

  /** Disable the constructor. */
  @SuppressWarnings("unused")
  private SaxRecord() {
    super();
  }

  /** The allowed constructor. */
  public SaxRecord(char[] str, int idx) {
    super();
    this.saxString = Arrays.copyOf(str, str.length);
    this.occurrences = new ArrayList<Integer>();
    this.addIndex(idx);
  }

  /**
   * Adds an index.
   * 
   * @param idx The index to add.
   */
  public void addIndex(int idx) {
    if (!(this.occurrences.contains(idx))) {
      this.occurrences.add(idx);
    }
  }

  /**
   * Shift occurrences of each entry by the offset.
   * 
   * @param offset The offset to shift on.
   */
  public void shiftIndexes(int offset) {
    for (int i = 0; i < this.occurrences.size(); i++) {
      this.occurrences.set(i, this.occurrences.get(i) + offset);
    }
  }

  /**
   * Removes a single index.
   * 
   * @param idx The index to remove.
   */
  public void removeIndex(Integer idx) {
    this.occurrences.remove(idx);
  }

  /**
   * Gets the payload of the structure.
   * 
   * @return The string.
   */
  public char[] getPayload() {
    return this.saxString;
  }

  /**
   * Get all indexes.
   * 
   * @return all indexes.
   */
  public ArrayList<Integer> getIndexes() {
    return this.occurrences;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(this.saxString).append(" ");
    sb.append(occurrences.toString());
    return sb.toString();
  }

}
