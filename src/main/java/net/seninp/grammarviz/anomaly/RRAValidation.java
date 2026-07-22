package net.seninp.grammarviz.anomaly;

import net.seninp.grammarviz.GrammarVizAnomaly;
import net.seninp.jmotif.sax.discord.DiscordRecord;

/**
 * Shared validation helpers for RRA discord results.
 */
public final class RRAValidation {

  private RRAValidation() {
    // utility
  }

  /**
   * Returns {@code true} when the record represents a usable discord candidate.
   *
   * @param discord the discord record.
   * @return whether the discord has a finite positive NN distance and valid position.
   */
  public static boolean isValidDiscord(DiscordRecord discord) {
    if (discord == null) {
      return false;
    }
    if (discord.getPosition() == Integer.MIN_VALUE || discord.getPosition() < 0) {
      return false;
    }
    double distance = discord.getNNDistance();
    if (!Double.isFinite(distance) || distance <= 0.0D) {
      return false;
    }
    if (discord.getLength() < GrammarVizAnomaly.MIN_ANOMALY_CANDIDATE_LENGTH) {
      return false;
    }
    return true;
  }
}
