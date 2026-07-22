package net.seninp.grammarviz.anomaly;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import net.seninp.gi.GIAlgorithm;
import net.seninp.gi.logic.GrammarRules;
import net.seninp.gi.logic.RuleInterval;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.discord.DiscordRecords;
import org.junit.Test;

/**
 * Smoke tests for RRA on longer series and single-pass interval/coverage construction.
 */
public class TestRRAMemoryLean {

  @Test
  public void rraCompletesOnMediumSeries() throws Exception {
    double[] series = RRATestSupport.loadSeries("src/resources/test-data/ecg0606_1.csv", 0);
    GrammarRules rules = RRATestSupport.inferGrammar(GIAlgorithm.SEQUITUR, series, 100, 5, 5,
        NumerosityReductionStrategy.EXACT, 0.01);
    RRAIntervalBuilder.BuildResult built = RRAIntervalBuilder.buildFromGrammarRules(rules,
        series.length, 5);

    DiscordRecords discords = RRAImplementation.series2RRAAnomalies(series, 5,
        built.getIntervals(), 0.01);

    assertTrue(discords.getSize() > 0);
    RRATestSupport.assertDiscordsAreValid("memory lean smoke", series, discords);
  }

  @Test
  public void buildResultAvoidsSecondCoveragePass() throws Exception {
    double[] series = RRATestSupport.loadSeries("data/dutch_power_demand.txt", 8000);
    GrammarRules rules = RRATestSupport.inferGrammar(GIAlgorithm.SEQUITUR, series, 750, 6, 3,
        NumerosityReductionStrategy.EXACT, 0.01);
    RRAIntervalBuilder.BuildResult built = RRAIntervalBuilder.buildFromGrammarRules(rules,
        series.length, 6);

    ArrayList<RuleInterval> intervals = built.getIntervals();
    int[] coverage = built.getCoverageArray();
    assertFalse(intervals.isEmpty());
    assertTrue(coverage.length == series.length);
  }
}
