package edu.hawaii.jmotif.sax.datastructures;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * The discord records collection.
 * 
 * @author Pavel Senin
 * 
 */
public class DiscordRecords implements Iterable<DiscordRecord> {

  /** Storage container. */
  private final ArrayList<DiscordRecord> discords;

  /**
   * Constructor.
   */
  public DiscordRecords() {
    this.discords = new ArrayList<DiscordRecord>();
  }

  /**
   * Add a new discord to the list.
   * 
   * @param discord The discord instance to add.
   * @return if the discord got added.
   */
  public void add(DiscordRecord discord) {
    this.discords.add(discord);
    Collections.sort(discords);
  }

  /**
   * Returns the number of the top hits.
   * 
   * @param num The number of instances to return. If the number larger than the storage size -
   * returns the storage as is.
   * @return the top discord hits.
   */
  public List<DiscordRecord> getTopHits(Integer num) {
    Collections.sort(discords);
    if (num >= this.discords.size()) {
      return this.discords;
    }
    List<DiscordRecord> res = this.discords.subList(this.discords.size() - num,
        this.discords.size());
    return res;
  }

  /**
   * Get the minimal distance found among all instances in the collection.
   * 
   * @return The minimal distance found among all instances in the collection.
   */
  public double getMinDistance() {
    if (this.discords.size() > 0) {
      return discords.get(0).getNNDistance();
    }
    return -1;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < discords.size(); i++) {
      DiscordRecord record = discords.get(i);
      if (record.getPayload().isEmpty()) {
        // brute force discord info
        //
        sb.append("discord #" + i + " \"" + record.getPayload() + "\", at " + record.getPosition()
            + " distance to closest neighbor: " + record.getNNDistance() + "\"\n");
      }
      else {
        sb.append("discord #" + i + " \"" + record.getPayload() + "\", at " + record.getPosition()
            + " distance to closest neighbor: " + record.getNNDistance() + "\"\n");
      }
    }
    return sb.toString();
  }

  @Override
  public Iterator<DiscordRecord> iterator() {
    return this.discords.iterator();
  }

  public int getSize() {
    return this.discords.size();
  }

  public double getWorstDistance() {
    if (this.discords.isEmpty()) {
      return 0D;
    }
    double res = Double.MAX_VALUE;
    for (DiscordRecord r : discords) {
      if (r.getNNDistance() < res) {
        res = r.getNNDistance();
      }
    }
    return res;
  }

  public String toCoordinates() {
    StringBuffer sb = new StringBuffer();
    for (DiscordRecord r : discords) {
      sb.append(r.getPosition() + ",");
    }
    return sb.delete(sb.length() - 1, sb.length()).toString();
  }

  public String toPayloads() {
    StringBuffer sb = new StringBuffer();
    for (DiscordRecord r : discords) {
      sb.append("\"" + r.getPayload() + "\",");
    }
    return sb.delete(sb.length() - 1, sb.length()).toString();
  }

  public String toDistances() {
    NumberFormat nf = new DecimalFormat("##0.####");
    StringBuffer sb = new StringBuffer();
    for (DiscordRecord r : discords) {
      sb.append(nf.format(r.getNNDistance()) + ",");
    }
    return sb.delete(sb.length() - 1, sb.length()).toString();
  }

  public DiscordRecord get(int i) {
    return this.discords.get(i);
  }
}
