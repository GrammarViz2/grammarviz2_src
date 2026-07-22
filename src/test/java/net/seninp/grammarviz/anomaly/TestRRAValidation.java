package net.seninp.grammarviz.anomaly;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.seninp.jmotif.sax.discord.DiscordRecord;

public class TestRRAValidation {

  @Test
  public void testRejectsZeroDistance() {
    DiscordRecord discord = new DiscordRecord(100, 0.0D);
    discord.setLength(50);
    discord.setRuleId(12);
    assertFalse(RRAValidation.isValidDiscord(discord));
  }

  @Test
  public void testAllowsLongUncoveredGapDiscord() {
    DiscordRecord discord = new DiscordRecord(100, 0.05D);
    discord.setLength(50);
    discord.setRuleId(-1);
    assertTrue(RRAValidation.isValidDiscord(discord));
  }

  @Test
  public void testRejectsInfiniteDistance() {
    DiscordRecord discord = new DiscordRecord(100, Double.POSITIVE_INFINITY);
    discord.setLength(50);
    discord.setRuleId(12);
    assertFalse(RRAValidation.isValidDiscord(discord));
  }

  @Test
  public void testAcceptsTypicalDiscord() {
    DiscordRecord discord = new DiscordRecord(433, 0.045D);
    discord.setLength(157);
    discord.setRuleId(243);
    assertTrue(RRAValidation.isValidDiscord(discord));
  }

  @Test
  public void testRejectsSinglePointLength() {
    DiscordRecord discord = new DiscordRecord(0, 0.01D);
    discord.setLength(1);
    discord.setRuleId(-1);
    assertFalse(RRAValidation.isValidDiscord(discord));
  }
}
