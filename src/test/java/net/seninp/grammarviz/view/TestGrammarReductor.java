package net.seninp.grammarviz.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import org.junit.Test;
import net.seninp.gi.GIAlgorithm;
import net.seninp.gi.logic.GrammarRuleRecord;
import net.seninp.gi.logic.GrammarRules;
import net.seninp.gi.logic.RuleInterval;
import net.seninp.gi.rulepruner.RulePruner;
import net.seninp.gi.rulepruner.RulePrunerFactory;
import net.seninp.gi.rulepruner.SampledPoint;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.TSProcessor;

/**
 * Tests for the grammar reductor (jmotif-gi {@code net.seninp.gi.rulepruner}) as consumed by
 * GrammarViz's parameter selector. These are contract tests against the public jmotif-gi 2.0.0
 * API that GrammarViz depends on: the byte-cost model, the coverage primitives, the pruning
 * step, and the end-to-end {@link RulePruner#sample} pipeline.
 *
 * <p>The jmotif-gi repo itself ships a single {@code TestRulePruner} (size 24 -> 20 on the
 * paper's grammar); this file covers the behaviors GrammarViz relies on that were otherwise
 * untested.
 */
public class TestGrammarReductor {

  private static final double DELTA = 1e-9;
  private static final String TEST_DATA_FNAME = "src/resources/test-data/ecg0606_1.csv";

  // ---- A1: byte-cost model (computeGrammarSize / computeRuleSize via the public API) ----
  // Model (RulePrunerFactory.computeRuleSize): a non-terminal token "R*" costs 4 bytes;
  // a terminal token costs paaSize bytes.

  @Test
  public void grammarSizeCountsTerminalsByPaaSize() {
    GrammarRules rules = new GrammarRules();
    rules.addRule(ruleRecord(1, "abc", "abc")); // one terminal token
    assertEquals(Integer.valueOf(3), RulePrunerFactory.computeGrammarSize(rules, 3));
    assertEquals(Integer.valueOf(5), RulePrunerFactory.computeGrammarSize(rules, 5));
  }

  @Test
  public void grammarSizeCountsNonTerminalsAsFourBytes() {
    GrammarRules rules = new GrammarRules();
    // "R2 abc" -> 4 (R2) + 3 (abc @ paa=3) = 7
    rules.addRule(ruleRecord(1, "abc abc cba", "R2 abc"));
    assertEquals(Integer.valueOf(7), RulePrunerFactory.computeGrammarSize(rules, 3));

    GrammarRules rules2 = new GrammarRules();
    // "R1 xxx R1" -> 4 + 3 + 4 = 11
    rules2.addRule(ruleRecord(0, "abc abc cba xxx abc abc cba", "R1 xxx R1"));
    assertEquals(Integer.valueOf(11), RulePrunerFactory.computeGrammarSize(rules2, 3));
  }

  @Test
  public void grammarSizeScalesWithPaaSize() {
    GrammarRules rules = new GrammarRules();
    rules.addRule(ruleRecord(1, "abcd abcd", "abcd abcd")); // two terminal tokens
    // 2 terminals * paaSize
    assertEquals(Integer.valueOf(8), RulePrunerFactory.computeGrammarSize(rules, 4));
    assertEquals(Integer.valueOf(10), RulePrunerFactory.computeGrammarSize(rules, 5));
  }

  // ---- A2: coverage primitives (updateRanges / computeCover / isCovered / hasEmptyRanges) ----

  @Test
  public void fullCoverIsDetected() {
    boolean[] range = new boolean[10];
    ArrayList<RuleInterval> intervals = new ArrayList<RuleInterval>();
    intervals.add(new RuleInterval(0, 10)); // covers [0,10)
    range = RulePrunerFactory.updateRanges(range, intervals);
    assertEquals(1.0, RulePrunerFactory.computeCover(range), DELTA);
    assertTrue(RulePrunerFactory.isCovered(range));
    assertFalse(RulePrunerFactory.hasEmptyRanges(range));
  }

  @Test
  public void partialCoverIsDetected() {
    boolean[] range = new boolean[10];
    ArrayList<RuleInterval> intervals = new ArrayList<RuleInterval>();
    intervals.add(new RuleInterval(0, 4)); // covers [0,4) -> 4/10
    range = RulePrunerFactory.updateRanges(range, intervals);
    assertEquals(0.4, RulePrunerFactory.computeCover(range), DELTA);
    assertFalse(RulePrunerFactory.isCovered(range));
    assertTrue(RulePrunerFactory.hasEmptyRanges(range));
  }

  @Test
  public void overlappingIntervalsAreUnionedNotDoubleCounted() {
    boolean[] range = new boolean[10];
    ArrayList<RuleInterval> intervals = new ArrayList<RuleInterval>();
    intervals.add(new RuleInterval(0, 6));
    intervals.add(new RuleInterval(4, 8)); // overlaps [4,6); union is [0,8) -> 8/10
    range = RulePrunerFactory.updateRanges(range, intervals);
    assertEquals(0.8, RulePrunerFactory.computeCover(range), DELTA);
  }

  /**
   * Locks the invariant behind the parameter-selector's coverage filter: {@code isCovered()}
   * is true iff coverage is exactly 1.0. (Filtering on {@code isCovered()} would therefore
   * ignore any user threshold below 1.0 -- the selector must filter on {@code getCoverage() >=
   * threshold} instead. This test documents and guards that.)
   */
  @Test
  public void isCoveredImpliesCoverageIsOne() {
    boolean[] full = new boolean[5];
    for (int i = 0; i < full.length; i++) {
      full[i] = true;
    }
    assertTrue(RulePrunerFactory.isCovered(full));
    assertEquals(1.0, RulePrunerFactory.computeCover(full), DELTA);

    boolean[] almost = new boolean[5];
    for (int i = 0; i < 4; i++) {
      almost[i] = true; // 0.8 cover -> NOT isCovered
    }
    assertFalse(RulePrunerFactory.isCovered(almost));
    assertEquals(0.8, RulePrunerFactory.computeCover(almost), DELTA);
  }

  // ---- A3: pruning behavior on the paper's grammar (size + idempotence) ----

  @Test
  public void pruningShrinksTheGrammarAndIsIdempotent() {
    GrammarRules grammar = paperGrammar();
    assertEquals(Integer.valueOf(24), RulePrunerFactory.computeGrammarSize(grammar, 3));

    GrammarRules pruned = RulePrunerFactory.performPruning(new double[7], grammar);
    int prunedSize = RulePrunerFactory.computeGrammarSize(pruned, 3);
    assertEquals(20, prunedSize);
    assertTrue("pruning must not grow the grammar", prunedSize <= 24);

    // idempotence: pruning an already-pruned grammar changes nothing
    GrammarRules prunedTwice = RulePrunerFactory.performPruning(new double[7], pruned);
    assertEquals(Integer.valueOf(prunedSize),
        RulePrunerFactory.computeGrammarSize(prunedTwice, 3));
  }

  // ---- A4: end-to-end RulePruner.sample() golden/characterization test on real data ----

  @Test
  public void sampleProducesConsistentPointForBothAlgorithms() throws Exception {
    double[] ts = TSProcessor.readFileColumn(TEST_DATA_FNAME, 0, 0);
    assertTrue("test series should be non-trivial", ts.length > 200);

    for (GIAlgorithm gi : new GIAlgorithm[] { GIAlgorithm.SEQUITUR, GIAlgorithm.REPAIR }) {
      RulePruner rp = new RulePruner(ts);
      SampledPoint p = rp.sample(100, 5, 4, gi, NumerosityReductionStrategy.EXACT, 0.01);

      assertNotNull("sample() must return a point for " + gi, p);
      assertEquals(100, p.getWindow());
      assertEquals(5, p.getPAA());
      assertEquals(4, p.getAlphabet());
      // invariants the selector relies on. NOTE: compressedGrammarSize is NOT bounded by
      // grammarSize -- pruning a rule inlines its references as terminals (paaSize bytes each)
      // instead of 4-byte pointers, so the "compressed" byte size can GROW. The active
      // selection metric is reduction = prunedRules/grammarRules (a rule-count ratio), which is
      // what we assert here; the byte-ratio variant is commented out in RulePruner.
      assertTrue("pruning must not increase the rule count",
          p.getPrunedRules() <= p.getGrammarRules());
      assertTrue("grammar sizes are positive", p.getGrammarSize() > 0
          && p.getCompressedGrammarSize() > 0);
      assertTrue("coverage in [0,1]", p.getCoverage() >= 0.0 && p.getCoverage() <= 1.0);
      assertTrue("reduction in (0,1]", p.getReduction() > 0.0 && p.getReduction() <= 1.0);

      // determinism: identical inputs -> identical metrics
      SampledPoint q = new RulePruner(ts).sample(100, 5, 4, gi,
          NumerosityReductionStrategy.EXACT, 0.01);
      assertEquals(p.getGrammarSize(), q.getGrammarSize());
      assertEquals(p.getCompressedGrammarSize(), q.getCompressedGrammarSize());
      assertEquals(p.getReduction(), q.getReduction(), DELTA);
      assertEquals(p.getCoverage(), q.getCoverage(), DELTA);
    }

    // golden/characterization values (observed under jmotif-gi 2.0.0; window 100, paa 5,
    // alphabet 4, EXACT, norm 0.01 on ecg0606_1.csv). A change here flags an algorithm shift.
    SampledPoint seq = new RulePruner(ts).sample(100, 5, 4, GIAlgorithm.SEQUITUR,
        NumerosityReductionStrategy.EXACT, 0.01);
    assertEquals(70, seq.getGrammarRules());
    assertEquals(8, seq.getPrunedRules());
    assertEquals(1.0, seq.getCoverage(), DELTA);
    assertEquals(8.0 / 70.0, seq.getReduction(), DELTA);
  }

  // ---- fixtures ----

  private static GrammarRuleRecord ruleRecord(int number, String expanded, String ruleString) {
    GrammarRuleRecord r = new GrammarRuleRecord();
    r.setRuleNumber(number);
    r.setExpandedRuleString(expanded);
    r.setRuleString(ruleString);
    r.setOccurrences(new int[] { 0 });
    return r;
  }

  /** The grammar from jmotif-gi's TestRulePruner (paper example): R0 -> R1 xxx R1, R1 -> R2 abc, R2 -> abc abc. */
  private static GrammarRules paperGrammar() {
    GrammarRules grammar = new GrammarRules();

    GrammarRuleRecord r2 = new GrammarRuleRecord();
    r2.setRuleNumber(2);
    r2.setExpandedRuleString("abc abc");
    r2.setRuleString("abc abc");
    ArrayList<RuleInterval> i2 = new ArrayList<RuleInterval>();
    i2.add(new RuleInterval(0, 1));
    i2.add(new RuleInterval(4, 5));
    r2.setRuleIntervals(i2);
    r2.setOccurrences(new int[] { 0, 4 });
    grammar.addRule(r2);

    GrammarRuleRecord r1 = new GrammarRuleRecord();
    r1.setRuleNumber(1);
    r1.setExpandedRuleString("abc abc cba");
    r1.setRuleString("R2 abc");
    ArrayList<RuleInterval> i1 = new ArrayList<RuleInterval>();
    i1.add(new RuleInterval(0, 2));
    i1.add(new RuleInterval(4, 6));
    r1.setRuleIntervals(i1);
    r1.setOccurrences(new int[] { 0, 4 });
    grammar.addRule(r1);

    GrammarRuleRecord r0 = new GrammarRuleRecord();
    r0.setRuleNumber(0);
    r0.setExpandedRuleString("abc abc cba xxx abc abc cba");
    r0.setRuleString("R1 xxx R1");
    ArrayList<RuleInterval> i0 = new ArrayList<RuleInterval>();
    i0.add(new RuleInterval(0, 7));
    r0.setRuleIntervals(i0);
    r0.setOccurrences(new int[] { 0 });
    grammar.addRule(r0);

    return grammar;
  }
}
