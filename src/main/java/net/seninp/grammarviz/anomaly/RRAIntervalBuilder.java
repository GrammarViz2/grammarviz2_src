package net.seninp.grammarviz.anomaly;

import java.util.ArrayList;
import java.util.List;
import net.seninp.gi.logic.GrammarRuleRecord;
import net.seninp.gi.logic.GrammarRules;
import net.seninp.gi.logic.RuleInterval;
import net.seninp.grammarviz.GrammarVizAnomaly;

/**
 * Builds the candidate interval list used by RRA from an inferred grammar.
 */
public final class RRAIntervalBuilder {

  private RRAIntervalBuilder() {
    // utility
  }

  /**
   * Intervals plus coverage metadata from a single grammar walk (avoids recomputing coverage).
   */
  public static final class BuildResult {
    private final ArrayList<RuleInterval> intervals;
    private final int[] coverageArray;
    private final List<RuleInterval> zeroIntervals;

    BuildResult(ArrayList<RuleInterval> intervals, int[] coverageArray,
        List<RuleInterval> zeroIntervals) {
      this.intervals = intervals;
      this.coverageArray = coverageArray;
      this.zeroIntervals = zeroIntervals;
    }

    public ArrayList<RuleInterval> getIntervals() {
      return intervals;
    }

    public int[] getCoverageArray() {
      return coverageArray;
    }

    public List<RuleInterval> getZeroIntervals() {
      return zeroIntervals;
    }
  }

  /**
   * Counts how many grammar rule intervals cover each time-series index.
   *
   * @param rules the grammar rules.
   * @param seriesLength length of the original time series.
   * @return per-point coverage counts (used for zero-gap detection and CLI export).
   */
  public static int[] computePointCoverage(GrammarRules rules, int seriesLength) {
    int[] coverageArray = new int[seriesLength];
    for (GrammarRuleRecord rule : rules) {
      if (0 == rule.ruleNumber()) {
        continue;
      }
      for (RuleInterval ri : rule.getRuleIntervals()) {
        for (int j = ri.getStart(); j < ri.getEnd(); j++) {
          coverageArray[j] = coverageArray[j] + 1;
        }
      }
    }
    return coverageArray;
  }

  /**
   * Collects rule intervals (cloned so grammar state is not mutated), computes coverage, and
   * appends filtered uncovered-gap candidates.
   *
   * @param rules the grammar rules.
   * @param seriesLength length of the original time series.
   * @return intervals ready for {@link RRAImplementation#series2RRAAnomalies}.
   * @throws CloneNotSupportedException if a rule interval cannot be cloned.
   */
  public static ArrayList<RuleInterval> fromGrammarRules(GrammarRules rules, int seriesLength)
      throws CloneNotSupportedException {
    return fromGrammarRules(rules, seriesLength, GrammarVizAnomaly.MIN_ANOMALY_CANDIDATE_LENGTH);
  }

  /**
   * Collects rule intervals with PAA-aware zero-gap filtering.
   *
   * @param rules the grammar rules.
   * @param seriesLength length of the original time series.
   * @param paaSize PAA size (zero gaps shorter than {@code max(2, paaSize)} are dropped).
   * @return intervals ready for {@link RRAImplementation#series2RRAAnomalies}.
   * @throws CloneNotSupportedException if a rule interval cannot be cloned.
   */
  public static ArrayList<RuleInterval> fromGrammarRules(GrammarRules rules, int seriesLength,
      int paaSize) throws CloneNotSupportedException {
    return buildFromGrammarRules(rules, seriesLength, paaSize).getIntervals();
  }

  /**
   * Builds intervals and coverage in one pass.
   */
  public static BuildResult buildFromGrammarRules(GrammarRules rules, int seriesLength, int paaSize)
      throws CloneNotSupportedException {
    ArrayList<RuleInterval> intervals = new ArrayList<RuleInterval>(rules.size() * 6);
    int[] coverageArray = computePointCoverage(rules, seriesLength);

    for (GrammarRuleRecord rule : rules) {
      if (0 == rule.ruleNumber()) {
        continue;
      }
      for (RuleInterval ri : rule.getRuleIntervals()) {
        RuleInterval clone = (RuleInterval) ri.clone();
        clone.setCoverage(rule.getRuleIntervals().size());
        clone.setId(rule.ruleNumber());
        intervals.add(clone);
      }
    }

    List<RuleInterval> zeros = GrammarVizAnomaly.filterZeroIntervalsForAnomalySearch(
        GrammarVizAnomaly.getZeroIntervals(coverageArray), paaSize);
    intervals.addAll(zeros);
    return new BuildResult(intervals, coverageArray, zeros);
  }
}
