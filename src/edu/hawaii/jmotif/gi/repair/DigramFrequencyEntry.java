package edu.hawaii.jmotif.gi.repair;

public class DigramFrequencyEntry {

  private String digram;
  private int frequency;
  private int firstOccurrence;

  public DigramFrequencyEntry(String digram, int frequency, int firstOccurrence) {
    super();
    this.digram = digram;
    this.frequency = frequency;
    this.firstOccurrence = firstOccurrence;
  }

  public String getDigram() {
    return digram;
  }

  public void setDigram(String digram) {
    this.digram = digram;
  }

  public int getFrequency() {
    return frequency;
  }

  public void setFrequency(int frequency) {
    this.frequency = frequency;
  }

  public int getFirstOccurrence() {
    return firstOccurrence;
  }

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
