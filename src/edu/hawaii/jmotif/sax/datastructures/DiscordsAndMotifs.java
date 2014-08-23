package edu.hawaii.jmotif.sax.datastructures;

import java.util.List;

/**
 * Just a container for discords/motifs.
 * 
 * @author psenin
 * 
 */
public class DiscordsAndMotifs {

  private final DiscordRecords discords;

  private final MotifRecords motifs;

  /**
   * Constructor.
   * 
   * @param discordCollectionSize maxSize of discords collection.
   * @param motifsCollectionSize maxSize of motifs collection.
   */
  public DiscordsAndMotifs(int discordCollectionSize, int motifsCollectionSize) {
    this.discords = new DiscordRecords();
    this.motifs = new MotifRecords();
  }

  /**
   * Add the motif into the storage.
   * 
   * @param motifRecord The motif to add.
   */
  public void addMotif(MotifRecord motifRecord) {
    this.motifs.add(motifRecord);
  }

  public void addMotifs(MotifRecords motifsCollection) {
    for (MotifRecord m : motifsCollection) {
      this.motifs.add(m);
    }
  }

  /**
   * Add the discord into the storage.
   * 
   * @param discordRecord The discord record.
   */
  public void addDiscord(DiscordRecord discordRecord) {
    this.discords.add(discordRecord);
  }

  public void addDiscords(DiscordRecords discordsCollection) {
    for (DiscordRecord d : discordsCollection) {
      this.discords.add(d);
    }
  }

  /**
   * Returns the current min distance in discords - so alleviates the searching troubles.
   * 
   * @return The min distance.
   */
  public double getMinDistance() {
    return this.discords.getMinDistance();
  }

  /**
   * Get the hit motifs.
   * 
   * @param num The number of instances asked.
   * @return The sorted by decreasing frequency list of motifs.
   */
  public List<MotifRecord> getTopMotifs(int num) {
    return this.motifs.getTopHits(num);
  }

  /**
   * Get the list of top (most distant) discords.
   * 
   * @param num The number of instances asked.
   * @return The sorted by decreasing distance list of discords.
   */
  public List<DiscordRecord> getTopDiscords(int num) {
    return this.discords.getTopHits(num);
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer(1024);

    sb.append("Motifs, as a list <frequency> [<offset1>,...,<offsetN>], from last to first:\n");
    sb.append(motifs.toString());

    sb.append("\nDiscords, as a list <distance> <offset>, from last to first:\n");
    sb.append(discords.toString());

    return sb.toString();
  }

}
