package net.seninp.grammarviz.anomaly;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.seninp.gi.GIAlgorithm;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.discord.DiscordRecords;

public class TestRRARejectZeroDistance {

  @Test
  public void testRePairEcgGuessParams_noAllZeroDiscords() throws Exception {
    double[] series = RRATestSupport.loadSeries("src/resources/test-data/ecg0606_1.csv", 0);
    DiscordRecords discords = RRATestSupport.runRRA(series,
        RRATestSupport.inferGrammar(GIAlgorithm.REPAIR, series, 150, 7, 4,
            NumerosityReductionStrategy.NONE, 0.01),
        7, 5, 0.01);
    RRATestSupport.assertDiscordsAreValid("ecg RePair regression", series, discords);
    for (int i = 0; i < discords.getSize(); i++) {
      if (discords.get(i).getRuleId() < 0) {
        throw new AssertionError("single-point boundary gaps must not rank as discords: "
            + discords.get(i));
      }
    }
  }
}
