package net.seninp.grammarviz.anomaly;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Random;

import org.junit.Test;

import net.seninp.gi.logic.RuleInterval;
import net.seninp.gi.repair.RePairFactory;
import net.seninp.gi.repair.RePairGrammar;
import net.seninp.gi.repair.RePairRule;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.alphabet.NormalAlphabet;
import net.seninp.jmotif.sax.datastructure.SAXRecords;
import net.seninp.jmotif.sax.discord.DiscordRecord;
import net.seninp.jmotif.sax.discord.DiscordRecords;

/**
 * Tier-B region checks aligned with jmotif-conformance {@code rra_discord} cases.
 */
public class TestRRATierBConformance {

  private static final double Z = 0.01;
  private static final int HOTSAX_TOP = 430;
  private static final int GT_START = 400;
  private static final int GT_END = 560;
  private static final double MIN_FRAC = 0.5;

  @Test
  public void testEcgRePairW100MatchesTierBRegion() throws Exception {
    assertTierBTop(100, 4, 4, 430, 531);
  }

  @Test
  public void testEcgRePairW120MatchesTierBRegion() throws Exception {
    assertTierBTop(120, 4, 4, 430, 551);
  }

  private static void assertTierBTop(int window, int paa, int alphabet, int expectStart,
      int expectEnd) throws Exception {
    double[] series = TSProcessor.readFileColumn("src/resources/test-data/ecg0606_1.csv", 0, 0);
    DiscordRecord top = runConformanceStyleRRA(series, window, paa, alphabet, 0);
    assertTrue("expected start " + expectStart + " got " + top.getPosition(),
        Math.abs(top.getPosition() - expectStart) <= 1);
    assertTrue("expected end " + expectEnd + " got " + (top.getPosition() + top.getLength()),
        Math.abs(top.getPosition() + top.getLength() - expectEnd) <= 1);
    assertTrue("HOT-SAX overlap", overlapFrac(top.getPosition(), top.getPosition() + top.getLength(),
        HOTSAX_TOP, HOTSAX_TOP + window, window) >= MIN_FRAC);
    assertTrue("ground-truth overlap", overlapFrac(top.getPosition(),
        top.getPosition() + top.getLength(), GT_START, GT_END, window) >= MIN_FRAC);
  }

  private static DiscordRecord runConformanceStyleRRA(double[] series, int window, int paa,
      int alphabet, int seed) throws Exception {
    SAXProcessor sp = new SAXProcessor();
    SAXRecords saxRecords = sp.ts2saxViaWindow(series, window, paa,
        new NormalAlphabet().getCuts(alphabet), NumerosityReductionStrategy.NONE, Z);
    saxRecords.buildIndex();

    RePairGrammar grammar = RePairFactory.buildGrammar(saxRecords.getSAXString(" "));
    grammar.expandRules();

    ArrayList<RuleInterval> intervals = new ArrayList<>();
    int[] coverageArray = new int[series.length];
    for (RePairRule rule : grammar.getRules().values()) {
      if (rule.getId() == 0) {
        continue;
      }
      int freq = rule.getOccurrences().length;
      String[] tokens = rule.toExpandedRuleString().trim().split("\\s+");
      int tokenSpan = tokens.length;
      for (int strPos : rule.getOccurrences()) {
        Integer tsStart = saxRecords.mapStringIndexToTSPosition(strPos);
        Integer tsEndToken = saxRecords.mapStringIndexToTSPosition(strPos + tokenSpan - 1);
        if (tsStart == null || tsEndToken == null) {
          continue;
        }
        int start = tsStart;
        int end = tsEndToken + window;
        intervals.add(new RuleInterval(rule.getId(), start, end, freq));
        for (int j = start; j < end; j++) {
          coverageArray[j]++;
        }
      }
    }
    addZeroIntervals(intervals, coverageArray, paa);

    DiscordRecords discords = RRAImplementation.series2RRAAnomalies(series, 1, intervals, Z,
        new Random(seed));
    RRATestSupport.assertDiscordsAreValid("tier-B w" + window, series, discords);
    return discords.get(0);
  }

  private static void addZeroIntervals(ArrayList<RuleInterval> intervals, int[] coverageArray,
      int paaSize) {
    int minUncovered = Math.max(2, paaSize);
    int start = -1;
    boolean inInterval = false;
    int zeroId = -1;
    for (int i = 0; i < coverageArray.length; i++) {
      if (coverageArray[i] == 0 && !inInterval) {
        start = i;
        inInterval = true;
      }
      if (coverageArray[i] > 0 && inInterval) {
        if (i - start >= minUncovered) {
          intervals.add(new RuleInterval(zeroId, start, i, 0));
          zeroId--;
        }
        inInterval = false;
      }
    }
    if (inInterval) {
      int runLen = coverageArray.length - start;
      if (runLen >= minUncovered) {
        intervals.add(new RuleInterval(zeroId, start, coverageArray.length, 0));
      }
    }
  }

  private static double overlapFrac(int aStart, int aEnd, int bStart, int bEnd, int window) {
    int ov = Math.max(0, Math.min(aEnd, bEnd) - Math.max(aStart, bStart));
    return ov / (double) window;
  }
}
