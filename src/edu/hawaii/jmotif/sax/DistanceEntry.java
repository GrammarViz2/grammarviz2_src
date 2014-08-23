package edu.hawaii.jmotif.sax;

public class DistanceEntry {

  private double distance;
  private boolean abandoned;

  public DistanceEntry(double nearestNeighborDist, boolean breakLoop) {
    this.distance = nearestNeighborDist;
    this.abandoned = breakLoop;
  }

  public double getDistance() {
    return distance;
  }

  public void setDistance(double distance) {
    this.distance = distance;
  }

  public boolean isAbandoned() {
    return abandoned;
  }

  public void setAbandoned(boolean abandoned) {
    this.abandoned = abandoned;
  }

  public String toString() {
    return this.distance + " abandoned: " + this.abandoned;
  }

}
