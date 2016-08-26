package net.seninp.grammarviz.anomaly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import net.seninp.gi.logic.GrammarRuleRecord;
import net.seninp.gi.logic.GrammarRules;
import net.seninp.gi.logic.RuleInterval;
import net.seninp.gi.sequitur.SequiturFactory;
import net.seninp.grammarviz.GrammarVizAnomaly;
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
      ArrayList<RuleInterval> intervals = new ArrayList<RuleInterval>();

      // populate all intervals with their frequency
      for (GrammarRuleRecord rule : rules) {
        if (0 != rule.ruleNumber()) {
          for (RuleInterval ri : rule.getRuleIntervals()) {
            RuleInterval i = (RuleInterval) ri.clone();
            i.setCoverage(rule.getRuleIntervals().size());
            i.setId(rule.ruleNumber());
            intervals.add(i);
          }
        }
      }
      // get the coverage array
      //
      int[] coverageArray = new int[series.length];
      for (GrammarRuleRecord rule : rules) {
        if (0 != rule.ruleNumber()) {
          ArrayList<RuleInterval> arrPos = rule.getRuleIntervals();
          for (RuleInterval saxPos : arrPos) {
            int startPos = saxPos.getStart();
            int endPos = saxPos.getEnd();
            for (int j = startPos; j < endPos; j++) {
              coverageArray[j] = coverageArray[j] + 1;
            }
          }
        }
      }

      // look for zero-covered intervals and add those to the list
      //
      List<RuleInterval> zeros = GrammarVizAnomaly.getZeroIntervals(coverageArray);
      if (zeros.size() > 0) {
        intervals.addAll(zeros);
      }

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

      Integer p1 = discordsBruteForce.get(i).getPosition();
      Integer p2 = discordsHash.get(i).getPosition();
      Integer p3 = discordsRRA.get(i).getPosition();

      assertEquals(p1, p2);
      assertTrue(Math.abs(p1 - p3) < 50);

    }

  }
}
