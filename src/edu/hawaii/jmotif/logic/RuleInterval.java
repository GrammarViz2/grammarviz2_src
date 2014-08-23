package edu.hawaii.jmotif.logic;

/**
 * 
 * Helper class implementing an interval used when plotting.
 * 
 * @author Manfred Lerner, seninp
 * 
 */
public class RuleInterval implements Comparable<RuleInterval> {
  public int startPos;
  public int endPos;
  public double coverage;
  public int id;

  public RuleInterval() {
    this.startPos = -1;
    this.endPos = -1;
  }

  public RuleInterval(int startPos, int endPos) {
    this.startPos = startPos;
    this.endPos = endPos;
  }

  public RuleInterval(int id, int startPos, int endPos, double coverage) {
    this.id = id;
    this.startPos = startPos;
    this.endPos = endPos;
    this.coverage = coverage;
  }

  /**
   * @param startPos starting position within the original time series
   */
  public void setStartPos(int startPos) {
    this.startPos = startPos;
  }

  /**
   * @return starting position within the original time series
   */
  public int getStartPos() {
    return startPos;
  }

  /**
   * @param endPos ending position within the original time series
   */
  public void setEndPos(int endPos) {
    this.endPos = endPos;
  }

  /**
   * @return ending position within the original time series
   */
  public int getEndPos() {
    return endPos;
  }

  /**
   * @return the coverage
   */
  public double getCoverage() {
    return this.coverage;
  }

  /**
   * @param coverage the coverage to set
   */
  public void setCoverage(double coverage) {
    this.coverage = coverage;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return "[" + startPos + " - " + endPos + "]";
  }

  public int getLength() {
    return this.endPos - this.startPos;
  }

  @Override
  public int compareTo(RuleInterval arg0) {
    return Integer.valueOf(this.getLength()).compareTo(Integer.valueOf(arg0.getLength()));
  }

  public void setId(int ruleIndex) {
    this.id = ruleIndex;
  }

  /**
   * @return the id
   */
  public int getId() {
    return id;
  }

}
