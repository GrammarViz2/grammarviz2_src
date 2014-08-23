package edu.hawaii.jmotif.sax.datastructures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The motif data record. Kind of useful of keeping information concerning a motif occurrence etc...
 * 
 * @author Pavel Senin
 * 
 */
public class MotifRecord implements Comparable<MotifRecord> {

  /** Indicates the position of this motif on the timeline. */
  private final ArrayList<Integer> occurrences;

  /** The payload. */
  private char[] payload;

  /**
   * Constructor.
   */
  public MotifRecord() {
    this.payload = null;
    this.occurrences = new ArrayList<Integer>();
  }

  /**
   * Constructor.
   * 
   * @param str The motif string
   * @param occurences The list of occurrences.
   */
  public MotifRecord(char[] str, List<Integer> occurences) {
    StringBuilder sb = new StringBuilder();
    for (char c : str) {
      sb.append(c);
    }
    this.payload = Arrays.copyOf(str, str.length);
    this.occurrences = new ArrayList<Integer>();
    this.occurrences.addAll(occurences);
  }

  /**
   * Set the payload value.
   * 
   * @param payload The payload.
   */
  public void setPayload(char[] payload) {
    this.payload = Arrays.copyOf(payload, payload.length);
  }

  /**
   * Get the payload.
   * 
   * @return The payload.
   */
  public char[] getPayload() {
    return payload;
  }

  /**
   * Add the position at the time series list.
   * 
   * @param position the position to set.
   */
  public void addIndex(int position) {
    if (!(this.occurrences.contains(position))) {
      this.occurrences.add(position);
    }
  }

  /**
   * Get the positions.
   * 
   * @return the positions at the time series list.
   */
  public ArrayList<Integer> getPositions() {
    return this.occurrences;
  }

  /**
   * Get the frequency.
   * 
   * @return Frequency of observance.
   */
  public int getFrequency() {
    return this.occurrences.size();
  }

  /**
   * The simple comparator based on the occurrence frequency.
   * 
   * @param other The motif record this is compared to.
   * @return True if equals.
   */
  @Override
  public int compareTo(MotifRecord other) {
    if (null == other) {
      throw new NullPointerException("Unable compare to null!");
    }
    if (this.occurrences.size() > other.getFrequency()) {
      return 1;
    }
    else if (this.occurrences.size() < other.getFrequency()) {
      return -1;
    }
    return 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = this.occurrences.size();
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + occurrences.hashCode();
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    MotifRecord other = (MotifRecord) obj;
    if (this.occurrences.size() != other.getFrequency()) {
      return false;
    }
    if (!Arrays.equals(this.payload, other.getPayload())) {
      return false;
    }
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "'" + String.valueOf(this.payload) + "', frequency: " + this.getFrequency()
        + " positions: " + occurrences.toString();
  }

}
