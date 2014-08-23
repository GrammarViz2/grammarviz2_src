package edu.hawaii.jmotif.sax.datastructures;

/**
 * The discord data record.
 * 
 * @author Pavel Senin
 * 
 */
public class DiscordRecord implements Comparable<DiscordRecord> {

  /** The discord position. */
  private int position;

  /** The discord length */
  private int length;

  /** The NN distance. */
  private double nnDistance;

  /** The Rule - useful for SAXSequitur. */
  private int rule;

  /** The payload - auxiliary variable. */
  private String payload;

  /** The info string - auxiliary variable. */
  private String info;

  /**
   * Constructor.
   */
  public DiscordRecord() {
    this.position = -1;
    this.length = -1;
    this.nnDistance = -1.0D;
  }

  /**
   * Constructor.
   * 
   * @param index The index discord found at.
   * @param dist The distance from other sequences.
   */
  public DiscordRecord(int index, double dist) {
    this.position = index;
    this.nnDistance = dist;
    this.payload = "";
  }

  /**
   * Constructor.
   * 
   * @param index The index discord found at.
   * @param dist The distance from other sequences.
   * @param payload The payload.
   */
  public DiscordRecord(int index, double dist, String payload) {
    this.position = index;
    this.nnDistance = dist;
    this.payload = payload;
  }

  /**
   * Set the payload value.
   * 
   * @param payload The payload.
   */
  public void setPayload(String payload) {
    this.payload = payload;
  }

  /**
   * Get the payload.
   * 
   * @return The payload.
   */
  public String getPayload() {
    return payload;
  }

  /**
   * Set the position at the time series list.
   * 
   * @param position the position to set.
   */
  public void setPosition(int position) {
    this.position = position;
  }

  /**
   * Get the position.
   * 
   * @return the position at the time series list.
   */
  public int getPosition() {
    return this.position;
  }

  /**
   * Set the distance to the closest neighbor.
   * 
   * @param distance the distance to set.
   */
  public void setNNDistance(double distance) {
    this.nnDistance = distance;
  }

  /**
   * Get the distance to the closest neighbor.
   * 
   * @return the distance to the closest neighbor.
   */
  public double getNNDistance() {
    return this.nnDistance;
  }

  public void setInfo(String info) {
    this.info = info;
  }

  public String getInfo() {
    return info;
  }

  public void setLength(int length) {
    this.length = length;
  }

  public Integer getLength() {
    return this.length;
  }

  public void setRule(int rule) {
    this.rule = rule;
  }

  public int getRule() {
    return rule;
  }

  /**
   * The simple comparator based on the distance. Note that discord is better if the distance is
   * greater.
   * 
   * @param other The discord record this one is compared to.
   * @return True if equals.
   */
  @Override
  public int compareTo(DiscordRecord other) {
    if (null == other) {
      throw new NullPointerException("Unable compare to null!");
    }
    if (this.nnDistance < other.getNNDistance()) {
      return 1;
    }
    else if (this.nnDistance > other.getNNDistance()) {
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
    temp = Double.doubleToLongBits(nnDistance);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + position;
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
    DiscordRecord other = (DiscordRecord) obj;
    if (Double.doubleToLongBits(nnDistance) != Double.doubleToLongBits(other.nnDistance)) {
      return false;
    }
    if (position != other.position) {
      return false;
    }
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "'" + this.payload + "', distance " + this.getNNDistance() + " position: "
        + this.getPosition();
  }

}
