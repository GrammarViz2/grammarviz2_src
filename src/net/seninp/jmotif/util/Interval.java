package net.seninp.jmotif.util;

public class Interval {

  private int start;
  private int end;
  private double coverage;

  public Interval(Integer start, Integer end, Double coverage) {
    this.start = start.intValue();
    this.end = end.intValue();
    this.coverage = coverage.doubleValue();
  }

  public Interval(int start, int end, double coverage) {
    this.start = start;
    this.end = end;
    this.coverage = coverage;
  }

  public double getCoverage() {
    return coverage;
  }

  public void setCoverage(double coverage) {
    this.coverage = coverage;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public void setEnd(int end) {
    this.end = end;
  }

  public boolean overlaps(Interval intervalB) {
    if ((this.start <= intervalB.getEnd()) && (this.end >= intervalB.getStart())) {
      return true;
    }
    return false;
  }

  public int getStart() {
    return this.start;
  }

  public int getEnd() {
    return this.end;
  }

  public int getLength() {
    return Math.abs(this.end - this.start);
  }

  public Double overlapInPercent(Interval otherInterval) {
    if (this.overlaps(otherInterval)) {
      int overlapStart = Math.max(this.start, otherInterval.start);
      int overlapEnd = Math.min(this.end, otherInterval.end);
      return Double.valueOf((Integer.valueOf(overlapEnd).doubleValue() - Integer.valueOf(
          overlapStart).doubleValue())
          / Integer.valueOf(Math.abs(this.end - this.start)).doubleValue());
    }
    return 0D;
  }

  public int basesInsideOverlap(Interval otherInterval) {
    int res = 0;
    if (this.overlaps(otherInterval)) {
      int overlapStart = Math.max(this.start, otherInterval.start);
      int overlapEnd = Math.min(this.end, otherInterval.end);
      res = Math.abs(overlapEnd - overlapStart);
    }
    return res;
  }

  public int basesOutsideOverlap(Interval otherInterval) {
    int res = 0;
    if (this.overlaps(otherInterval)) {
      int overlapStart = Math.max(this.start, otherInterval.start);
      int overlapEnd = Math.min(this.end, otherInterval.end);
      res = res + Math.abs(overlapStart - this.start)
          + Math.abs(overlapStart - otherInterval.start);

      res = res + Math.abs(overlapEnd - this.end) + Math.abs(overlapEnd - otherInterval.end);
    }
    return res;
  }

  public int extendsLeft(Interval other) {
    return other.start - this.start;
  }

  public int extendsRight(Interval other) {
    return this.end - other.end;
  }
}
