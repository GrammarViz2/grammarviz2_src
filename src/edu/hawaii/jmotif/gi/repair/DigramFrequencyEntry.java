package edu.hawaii.jmotif.gi.repair;

/**
 * Implements a digram frequency entry.
 * 
 * @author psenin
 * 
 */
public class DigramFrequencyEntry {

  /** The payload - the digram string itself. */
  private String digram;

  /** The observed frequency. */
  private int frequency;

  /** The very first occurrence. */
  private int firstOccurrence;

  /**
   * Constructor.
   * 
   * @param digram the digram string.
   * @param frequency the digram frequency.
   * @param firstOccurrence the digram first occurrence.
   */
  public DigramFrequencyEntry(String digram, int frequency, int firstOccurrence) {
    super();
    this.digram = digram;
    this.frequency = frequency;
    this.firstOccurrence = firstOccurrence;
  }

  /**
   * Get the payload.
   * 
   * @return the digram string.
   */
  public String getDigram() {
    return digram;
  }

  /**
   * Set the digram string.
   * 
   * @param digram the string.
   */
  public void setDigram(String digram) {
    this.digram = digram;
  }

  /**
   * Frequency getter.
   * 
   * @return the frequency value.
   */
  public int getFrequency() {
    return frequency;
  }

  /**
   * Frequency setter.
   * 
   * @param frequency the new frequency value.
   */
  public void setFrequency(int frequency) {
    this.frequency = frequency;
  }

  /**
   * Get the first occurrence.
   * 
   * @return the first occurrence.
   */
  public int getFirstOccurrence() {
    return firstOccurrence;
  }

  /**
   * Set the first occurrence.
   * 
   * @param firstOccurrence the new value.
   */
  public void setFirstOccurrence(int firstOccurrence) {
    this.firstOccurrence = firstOccurrence;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((digram == null) ? 0 : digram.hashCode());
    result = prime * result + firstOccurrence;
    result = prime * result + frequency;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    DigramFrequencyEntry other = (DigramFrequencyEntry) obj;
    if (digram == null) {
      if (other.digram != null)
        return false;
    }
    else if (!digram.equals(other.digram))
      return false;
    if (firstOccurrence != other.firstOccurrence)
      return false;
    if (frequency != other.frequency)
      return false;
    return true;
  }

  public String toString() {
    return this.digram + " " + this.frequency;
  }
}
