package net.seninp.grammarviz.anomaly;

import java.util.Arrays;
import java.util.Random;

import net.seninp.gi.GIAlgorithm;
import net.seninp.gi.logic.GrammarRules;
import net.seninp.gi.repair.RePairFactory;
import net.seninp.gi.repair.RePairGrammar;
import net.seninp.gi.rulepruner.RulePrunerFactory;
import net.seninp.gi.sequitur.SequiturFactory;
import net.seninp.grammarviz.GrammarVizAnomaly;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.datastructure.SAXRecords;
import net.seninp.jmotif.sax.discord.DiscordRecord;
import net.seninp.jmotif.sax.discord.DiscordRecords;
import net.seninp.jmotif.sax.parallel.ParallelSAXImplementation;

/**
 * Shared helpers for RRA regression tests.
 */
final class RRATestSupport {

  private static final long SEED = 42L;

  private RRATestSupport() {
    // utility
  }

  static double[] loadSeries(String path, int maxLength) throws Exception {
    double[] series = TSProcessor.readFileColumn(path, 0, 0);
    if (maxLength > 0 && series.length > maxLength) {
      return Arrays.copyOf(series, maxLength);
    }
    return series;
  }

  static GrammarRules inferGrammar(GIAlgorithm algorithm, double[] series, int window, int paa,
      int alphabet, NumerosityReductionStrategy nr, double zNormThreshold) throws Exception {
    if (GIAlgorithm.SEQUITUR.equals(algorithm)) {
      return SequiturFactory.series2SequiturRules(series, window, paa, alphabet, nr,
          zNormThreshold);
    }
    ParallelSAXImplementation ps = new ParallelSAXImplementation();
    SAXRecords sax = ps.process(series, 2, window, paa, alphabet, nr, zNormThreshold);
    RePairGrammar grammar = RePairFactory.buildGrammar(sax);
    grammar.expandRules();
    grammar.buildIntervals(sax, series, window);
    return grammar.toGrammarRulesData();
  }

  static DiscordRecords runRRA(double[] series, GrammarRules rules, int paa, int discordCount,
      double zNormThreshold) throws Exception {
    return RRAImplementation.series2RRAAnomalies(series, discordCount,
        RRAIntervalBuilder.fromGrammarRules(rules, series.length, paa), zNormThreshold,
        new Random(SEED));
  }

  static DiscordRecords runRRAWithPruning(double[] series, GrammarRules rules, int paa,
      int discordCount, double zNormThreshold) throws Exception {
    GrammarRules pruned = RulePrunerFactory.performPruning(series, rules);
    return runRRA(series, pruned, paa, discordCount, zNormThreshold);
  }

  static void assertDiscordsAreValid(String scenario, double[] series, DiscordRecords discords) {
    if (discords.getSize() == 0) {
      throw new AssertionError(scenario + ": expected at least one discord");
    }
    for (int i = 0; i < discords.getSize(); i++) {
      DiscordRecord discord = discords.get(i);
      if (!RRAValidation.isValidDiscord(discord)) {
        throw new AssertionError(scenario + ": invalid discord at rank " + i + ": " + discord);
      }
      if (discord.getRuleId() < 0
          && discord.getLength() < GrammarVizAnomaly.MIN_ANOMALY_CANDIDATE_LENGTH) {
        throw new AssertionError(
            scenario + ": degenerate uncovered-gap discord: " + discord);
      }
      if (discord.getPosition() + discord.getLength() > series.length) {
        throw new AssertionError(scenario + ": discord out of bounds: " + discord);
      }
    }
  }
}
