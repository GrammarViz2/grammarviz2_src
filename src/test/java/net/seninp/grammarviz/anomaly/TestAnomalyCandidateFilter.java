package net.seninp.grammarviz.anomaly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import net.seninp.gi.logic.RuleInterval;
import net.seninp.grammarviz.GrammarVizAnomaly;

public class TestAnomalyCandidateFilter {

  @Test
  public void testFilterDropsSinglePointBoundaryGaps() {
    int[] coverage = { 0, 1, 1, 0, 1, 1 };
    List<RuleInterval> zeros = GrammarVizAnomaly.getZeroIntervals(coverage);
    assertEquals(2, zeros.size());
    List<RuleInterval> filtered = GrammarVizAnomaly.filterZeroIntervalsForAnomalySearch(zeros);
    assertTrue(filtered.isEmpty());
  }

  @Test
  public void testFilterKeepsLongUncoveredRuns() {
    int[] coverage = { 1, 0, 0, 0, 1 };
    List<RuleInterval> zeros = GrammarVizAnomaly.getZeroIntervals(coverage);
    assertEquals(1, zeros.size());
    List<RuleInterval> filtered = GrammarVizAnomaly.filterZeroIntervalsForAnomalySearch(zeros);
    assertEquals(1, filtered.size());
    assertEquals(-1, filtered.get(0).getId());
    assertEquals(1, filtered.get(0).getStart());
    assertEquals(4, filtered.get(0).getEnd());
  }

  @Test
  public void testUncoveredRuleDisplayLabel() {
    assertEquals("uncovered gap #1", GrammarVizAnomaly.formatRuleIdForDisplay(-1));
    assertEquals("232", GrammarVizAnomaly.formatRuleIdForDisplay(232));
  }

  @Test
  public void testViableCandidateLength() {
    RuleInterval onePoint = new RuleInterval(-1, 0, 1, 0);
    RuleInterval twoPointGap = new RuleInterval(-2, 0, 2, 0);
    RuleInterval grammarRule = new RuleInterval(5, 0, 2, 1);
    assertFalse(GrammarVizAnomaly.isViableAnomalyCandidate(onePoint));
    assertTrue(GrammarVizAnomaly.isViableAnomalyCandidate(twoPointGap));
    assertTrue(GrammarVizAnomaly.isUncoveredInterval(onePoint));
    assertTrue(GrammarVizAnomaly.isUncoveredInterval(twoPointGap));
    assertFalse(GrammarVizAnomaly.isUncoveredInterval(grammarRule));
  }
}
