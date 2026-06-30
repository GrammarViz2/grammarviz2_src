package net.seninp.grammarviz.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.util.ArrayList;
import org.junit.Test;
import net.seninp.gi.rulepruner.SampledPoint;

/**
 * Unit tests for the pure selection core of {@link GrammarvizParamsSampler#selectBest}. These
 * cover the behaviors that the param-guesser fix introduced: empty input, coverage-threshold
 * filtering, fallback when no point reaches the threshold, and the deterministic grammar-size
 * tie-break among equal-reduction points.
 */
public class TestParamsSamplerSelection {

  private static SampledPoint point(int window, int paa, int alphabet, double coverage,
      double reduction, int grammarSize) {
    SampledPoint p = new SampledPoint();
    p.setWindow(window);
    p.setPAA(paa);
    p.setAlphabet(alphabet);
    p.setCoverage(coverage);
    p.setReduction(reduction);
    p.setGrammarSize(grammarSize);
    return p;
  }

  @Test
  public void emptyInputReturnsNull() {
    assertNull(GrammarvizParamsSampler.selectBest(new ArrayList<SampledPoint>(), 0.9));
  }

  @Test
  public void picksLowestReductionAmongCoveredPoints() {
    ArrayList<SampledPoint> res = new ArrayList<SampledPoint>();
    res.add(point(30, 4, 3, 0.99, 0.50, 100)); // covered, higher reduction
    res.add(point(40, 5, 4, 0.98, 0.20, 120)); // covered, LOWEST reduction -> winner
    res.add(point(50, 6, 5, 0.50, 0.05, 80)); // best reduction but BELOW threshold -> excluded
    SampledPoint best = GrammarvizParamsSampler.selectBest(res, 0.95);
    assertEquals(40, best.getWindow());
    assertEquals(5, best.getPAA());
    assertEquals(4, best.getAlphabet());
  }

  @Test
  public void fallsBackToAllPointsWhenNoneMeetThreshold() {
    ArrayList<SampledPoint> res = new ArrayList<SampledPoint>();
    res.add(point(30, 4, 3, 0.60, 0.40, 100));
    res.add(point(40, 5, 4, 0.55, 0.10, 120)); // lowest reduction overall -> winner via fallback
    SampledPoint best = GrammarvizParamsSampler.selectBest(res, 0.95);
    assertEquals(40, best.getWindow());
  }

  @Test
  public void breaksReductionTiesByGrammarSize() {
    ArrayList<SampledPoint> res = new ArrayList<SampledPoint>();
    res.add(point(30, 4, 3, 0.99, 0.20, 150)); // same reduction, larger grammar
    res.add(point(40, 5, 4, 0.99, 0.20, 90)); // same reduction, SMALLER grammar -> winner
    SampledPoint best = GrammarvizParamsSampler.selectBest(res, 0.95);
    assertEquals(40, best.getWindow());
    assertEquals(90, best.getGrammarSize());
  }

  /**
   * The exact regression the coverage fix addresses: a covered point with a WORSE reduction
   * must beat an uncovered point with a BETTER reduction. (Pre-fix, selection ranked by
   * reduction alone and would have picked the uncovered point.)
   */
  @Test
  public void coveredWorseReductionBeatsUncoveredBetterReduction() {
    ArrayList<SampledPoint> res = new ArrayList<SampledPoint>();
    res.add(point(30, 4, 3, 0.50, 0.05, 80)); // best reduction but uncovered -> must NOT win
    res.add(point(40, 5, 4, 0.99, 0.45, 200)); // covered, worse reduction -> winner
    SampledPoint best = GrammarvizParamsSampler.selectBest(res, 0.95);
    assertEquals(40, best.getWindow());
  }

  @Test
  public void fullTieIsDeterministic() {
    ArrayList<SampledPoint> res = new ArrayList<SampledPoint>();
    res.add(point(30, 4, 3, 0.99, 0.20, 100)); // identical reduction AND grammar size
    res.add(point(40, 5, 4, 0.99, 0.20, 100));
    // stable sort keeps the first-seen on a full tie -> repeatable result
    SampledPoint a = GrammarvizParamsSampler.selectBest(new ArrayList<SampledPoint>(res), 0.95);
    SampledPoint b = GrammarvizParamsSampler.selectBest(new ArrayList<SampledPoint>(res), 0.95);
    assertEquals(a.getWindow(), b.getWindow());
    assertEquals(30, a.getWindow());
  }
}
