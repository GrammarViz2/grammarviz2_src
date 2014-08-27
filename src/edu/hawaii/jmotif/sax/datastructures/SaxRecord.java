package edu.hawaii.jmotif.sax.datastructures;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Implement a data container for the parallel SAX.
 * 
 * @author psenin
 * 
 */
public class SaxRecord implements Comparable<SaxRecord> {

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
    this.saxString = str.clone();
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

  /**
   * This comparator compares entries by the length of the entries array - i.e. by the total
   * frequency of entry occurrence.
   * 
   * @param o an entry to compare with.
   */
  @Override
  public int compareTo(SaxRecord o) {
    int a = this.occurrences.size();
    int b = o.getIndexes().size();
    if (a == b) {
      return 0;
    }
    else if (a > b) {
      return 1;
    }
    return -1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object o) {
    if (o instanceof SaxRecord) {
      SaxRecord other = (SaxRecord) o;
      if (Arrays.equals(other.getPayload(), this.saxString)
          && (other.getIndexes().size() == this.occurrences.size())) {
        for (Integer e : this.occurrences) {
          if (!other.getIndexes().contains(e)) {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    int hash = 7;
    int num0 = 0;
    if (null == this.saxString || 0 == this.saxString.length) {
      num0 = 32;
    }
    else {
      for (int i = 0; i < this.saxString.length; i++) {
        num0 = num0 + Character.getNumericValue(this.saxString[i]);
      }
    }

    int num1 = 0;
    if (this.occurrences.isEmpty()) {
      num1 = 17;
    }
    else {
      for (Integer i : this.occurrences) {
        num1 = num1 + i;
      }
    }

    hash = num0 + hash * num1;
    return hash;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(this.saxString).append(" -> ");
    sb.append(occurrences.toString());
    return sb.toString();
  }

}
