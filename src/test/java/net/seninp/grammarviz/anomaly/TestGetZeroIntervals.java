package net.seninp.grammarviz.anomaly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import net.seninp.gi.logic.RuleInterval;
import net.seninp.grammarviz.GrammarVizAnomaly;

public class TestGetZeroIntervals {

  @Test
  public void testInteriorZeroRun() {
    int[] coverage = { 1, 1, 0, 0, 1, 1 };
    List<RuleInterval> zeros = GrammarVizAnomaly.getZeroIntervals(coverage);
    assertEquals(1, zeros.size());
    assertEquals(2, zeros.get(0).getStart());
    assertEquals(4, zeros.get(0).getEnd());
  }

  @Test
  public void testTrailingZeroRun() {
    int[] coverage = { 1, 0, 0, 0 };
    List<RuleInterval> zeros = GrammarVizAnomaly.getZeroIntervals(coverage);
    assertEquals(1, zeros.size());
    assertEquals(1, zeros.get(0).getStart());
    assertEquals(coverage.length, zeros.get(0).getEnd());
  }

  @Test
  public void testAllZeroCoverage() {
    int[] coverage = { 0, 0, 0, 0, 0 };
    List<RuleInterval> zeros = GrammarVizAnomaly.getZeroIntervals(coverage);
    assertEquals(1, zeros.size());
    assertEquals(0, zeros.get(0).getStart());
    assertEquals(coverage.length, zeros.get(0).getEnd());
    assertTrue(zeros.get(0).getCoverage() == 0);
  }
}
