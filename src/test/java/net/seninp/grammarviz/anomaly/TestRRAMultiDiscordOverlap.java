package net.seninp.grammarviz.anomaly;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

import net.seninp.gi.GIAlgorithm;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.discord.DiscordRecord;
import net.seninp.jmotif.sax.discord.DiscordRecords;

public class TestRRAMultiDiscordOverlap {

  @Test
  public void testReportedDiscordsDoNotOverlap() throws Exception {
    double[] series = RRATestSupport.loadSeries("src/resources/test-data/ecg0606_1.csv", 1600);
    DiscordRecords discords = RRATestSupport.runRRA(series,
        RRATestSupport.inferGrammar(GIAlgorithm.REPAIR, series, 120, 5, 4,
            NumerosityReductionStrategy.NONE, 0.01),
        5, 5, 0.01);

    RRATestSupport.assertDiscordsAreValid("multi-discord overlap", series, discords);
    for (int i = 0; i < discords.getSize(); i++) {
      DiscordRecord a = discords.get(i);
      int aStart = a.getPosition();
      int aEnd = a.getPosition() + a.getLength();
      for (int j = i + 1; j < discords.getSize(); j++) {
        DiscordRecord b = discords.get(j);
        int bStart = b.getPosition();
        int bEnd = b.getPosition() + b.getLength();
        boolean overlaps = aStart < bEnd && bStart < aEnd;
        assertFalse("discords must not overlap: " + a + " vs " + b, overlaps);
      }
    }
  }

  @Test
  public void testRejectsNonFiniteDistance() {
    DiscordRecord discord = new DiscordRecord(10, Double.NaN);
    discord.setLength(20);
    discord.setRuleId(3);
    assertFalse(RRAValidation.isValidDiscord(discord));
  }
}
