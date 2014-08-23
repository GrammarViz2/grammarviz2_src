package edu.hawaii.jmotif.sax.datastructures;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Data structure to keep the SAX frequency information for the single TS.
 * 
 * @author Pavel Senin.
 * 
 */
public class SAXFrequencyEntry implements Comparable<SAXFrequencyEntry> {

  private final char[] substring;
  private final ArrayList<Integer> entries;

  /**
   * Constructor.
   * 
   * @param substring The string frequencies collected for.
   * @param idx The first index.
   */
  public SAXFrequencyEntry(char[] substring, int idx) {
    this.substring = substring;
    this.entries = new ArrayList<Integer>();
    this.entries.add(idx);
  }

  /**
   * Get the list of all entries.
   * 
   * @return the list of entries.
   */
  public ArrayList<Integer> getEntries() {
    @SuppressWarnings("unchecked")
    ArrayList<Integer> res = (ArrayList<Integer>) this.entries.clone();
    for (int i = 0; i < this.entries.size(); i++) {
      res.set(i, this.entries.get(i));
    }
    return res;
  }

  /**
   * Add the entry index into the entries. If entry index exists in the array addition ignored.
   * 
   * @param idx The index to add.
   */
  public void put(int idx) {
    if (!this.entries.contains(idx)) {
      this.entries.add(idx);
    }
  }

  /**
   * Get the substring value.
   * 
   * @return The substring value.
   */
  public char[] getSubstring() {
    return this.substring;
  }

  /**
   * This comparator compares entries by the length of the entries array - i.e. by frequency of
   * entry occurrence.
   * 
   * @param o an entry to compare with.
   */
  @Override
  public int compareTo(SAXFrequencyEntry o) {
    int a = this.entries.size();
    int b = o.getEntries().size();
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
    if (o instanceof SAXFrequencyEntry) {
      SAXFrequencyEntry other = (SAXFrequencyEntry) o;
      if (Arrays.equals(other.getSubstring(), this.substring)
          && (other.getEntries().size() == this.entries.size())) {
        for (Integer e : this.entries) {
          if (!other.getEntries().contains(e)) {
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
    if (null == this.substring || 0 == this.substring.length) {
      num0 = 32;
    }
    else {
      for (int i = 0; i < this.substring.length; i++) {
        num0 = num0 + Character.getNumericValue(this.substring[i]);
      }
    }

    int num1 = 0;
    if (this.entries.isEmpty()) {
      num1 = 17;
    }
    else {
      for (Integer i : this.entries) {
        num1 = num1 + i;
      }
    }

    hash = num0 + hash * num1;
    return hash;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(this.substring).append(" -> ");
    for (Integer i : this.entries) {
      sb.append(i.toString()).append(" ");
    }
    return sb.toString();
  }
}
