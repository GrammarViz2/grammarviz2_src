package edu.hawaii.jmotif.sax.datastructures;

import java.util.Iterator;
import java.util.List;
import edu.hawaii.jmotif.logic.SortedArrayList;

/**
 * The discord records collection.
 * 
 * @author Pavel Senin
 * 
 */
public class DiscordRecords implements Iterable<DiscordRecord> {

  /** Storage container. */
  private final SortedArrayList<DiscordRecord> discords;

  /**
   * Constructor.
   */
  public DiscordRecords() {
    this.discords = new SortedArrayList<DiscordRecord>();
  }

  /**
   * Add a new discord to the list.
   * 
   * @param discord The discord instance to add.
   * @return if the discord got added.
   */
  public void add(DiscordRecord discord) {
    this.discords.insertSorted(discord);
  }

  /**
   * Returns the number of the top hits.
   * 
   * @param num The number of instances to return. If the number larger than the storage size -
   * returns the storage as is.
   * @return the top discord hits.
   */
  public List<DiscordRecord> getTopHits(Integer num) {
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
        sb.append("discord #" + i + " \"\", at " + record.getPosition()
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

  /**
   * Get the collection size.
   * 
   * @return the discords collection size.
   */
  public int getSize() {
    return this.discords.size();
  }

  /**
   * Get a discord record by its index.
   * 
   * @param i the index.
   * @return the discord record.
   */
  public DiscordRecord get(int i) {
    return this.discords.get(i);
  }
}
