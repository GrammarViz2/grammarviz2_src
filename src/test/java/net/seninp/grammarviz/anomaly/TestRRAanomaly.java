package net.seninp.grammarviz.anomaly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import net.seninp.gi.logic.GrammarRules;
import net.seninp.gi.logic.RuleInterval;
import net.seninp.gi.sequitur.SequiturFactory;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.discord.BruteForceDiscordImplementation;
import net.seninp.jmotif.sax.discord.DiscordRecord;
import net.seninp.jmotif.sax.discord.DiscordRecords;
import net.seninp.jmotif.sax.discord.HOTSAXImplementation;
import net.seninp.jmotif.sax.registry.LargeWindowAlgorithm;
import net.seninp.util.StackTrace;

public class TestRRAanomaly {

  private static final String TEST_DATA_FNAME = "src/resources/test-data/ecg0606_1.csv";

  private static final int WIN_SIZE = 100;
  private static final int PAA_SIZE = 3;
  private static final int ALPHABET_SIZE = 3;

  private static final double NORM_THRESHOLD = 0.5;

  private static final int DISCORDS_TO_TEST = 1;

  private static final NumerosityReductionStrategy STRATEGY = NumerosityReductionStrategy.NONE;

  private double[] series;

  @Before
  public void setUp() throws Exception {
    series = TSProcessor.readFileColumn(TEST_DATA_FNAME, 0, 0);
    series = Arrays.copyOf(series, 1600);
  }

  @Test
  public void test() {

    DiscordRecords discordsBruteForce = null;
    DiscordRecords discordsHash = null;
    DiscordRecords discordsRRA = null;

    try {

      discordsBruteForce = BruteForceDiscordImplementation.series2BruteForceDiscords(series,
          WIN_SIZE, DISCORDS_TO_TEST, new LargeWindowAlgorithm(), NORM_THRESHOLD);
      for (DiscordRecord d : discordsBruteForce) {
        System.out.println("brute force discord " + d.toString());
      }

      discordsHash = HOTSAXImplementation.series2Discords(series, DISCORDS_TO_TEST, WIN_SIZE,
          PAA_SIZE, ALPHABET_SIZE, STRATEGY, NORM_THRESHOLD);
      for (DiscordRecord d : discordsHash) {
        System.out.println("hotsax hash discord " + d.toString());
      }

      // RRA
      //
      //

      GrammarRules rules = SequiturFactory.series2SequiturRules(series, WIN_SIZE, PAA_SIZE,
          ALPHABET_SIZE, STRATEGY, NORM_THRESHOLD);
      ArrayList<RuleInterval> intervals = RRAIntervalBuilder.fromGrammarRules(rules, series.length, 3);

      discordsRRA = RRAImplementation.series2RRAAnomalies(series, DISCORDS_TO_TEST, intervals,
          NORM_THRESHOLD);

      for (DiscordRecord d : discordsRRA) {
        System.out.println("RRA discords " + d.toString());
      }

    }
    catch (Exception e) {
      fail("shouldn't throw an exception, exception thrown: \n" + StackTrace.toString(e));
      e.printStackTrace();
    }

    for (int i = 0; i < DISCORDS_TO_TEST; i++) {

      Double d1 = discordsBruteForce.get(i).getNNDistance();
      Double d2 = discordsHash.get(i).getNNDistance();
      assertEquals(d1, d2);

      assertTrue("RRA discord must have positive NN distance",
          RRAValidation.isValidDiscord(discordsRRA.get(i)));

    }

  }
}
