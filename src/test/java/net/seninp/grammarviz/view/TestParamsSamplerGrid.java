package net.seninp.grammarviz.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import org.junit.BeforeClass;
import org.junit.Test;
import net.seninp.gi.GIAlgorithm;
import net.seninp.gi.rulepruner.RulePruner;
import net.seninp.gi.rulepruner.SampledPoint;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.TSProcessor;

/**
 * Tests for the pure grid-scan core of the parameter selector
 * ({@link GrammarvizParamsSampler#sampleGrid}). This is the loop that was previously buried in
 * the Swing-coupled {@code call()}; extracting it lets us regression-lock the inclusive-bounds,
 * PAA&gt;window skip, empty-grid, and bad-point-skip behavior introduced in 3.0.0.
 */
public class TestParamsSamplerGrid {

  private static final String TEST_DATA_FNAME = "src/resources/test-data/ecg0606_1.csv";
  private static double[] ts;

  @BeforeClass
  public static void load() throws Exception {
    ts = TSProcessor.readFileColumn(TEST_DATA_FNAME, 0, 0);
  }

  private static ArrayList<SampledPoint> run(int[] boundaries, int winLimit) throws Exception {
    ArrayList<SampledPoint> res = new ArrayList<SampledPoint>();
    GrammarvizParamsSampler.sampleGrid(new RulePruner(ts), boundaries, winLimit,
        GIAlgorithm.SEQUITUR, NumerosityReductionStrategy.EXACT, 0.01, res);
    return res;
  }

  @Test
  public void inclusiveMaxBoundIsSampled() throws Exception {
    // a single-point grid: window=100..100, paa=5..5, alphabet=4..4 -- must produce exactly
    // one point (the typed MAX is evaluated thanks to the <= bounds fix)
    int[] boundaries = new int[] { 100, 100, 10, 5, 5, 1, 4, 4, 1 };
    ArrayList<SampledPoint> res = run(boundaries, 100);
    assertEquals(1, res.size());
    assertEquals(100, res.get(0).getWindow());
    assertEquals(5, res.get(0).getPAA());
    assertEquals(4, res.get(0).getAlphabet());
  }

  @Test
  public void multiStepGridVisitsEveryInclusiveCombination() throws Exception {
    // window {100,120 step 20} = {100,120} (2), paa {4,5 step 1} = {4,5} (2),
    // alphabet {3,4 step 1} = {3,4} (2) -> 2*2*2 = 8 points (all paa <= window)
    int[] boundaries = new int[] { 100, 120, 20, 4, 5, 1, 3, 4, 1 };
    ArrayList<SampledPoint> res = run(boundaries, 120);
    assertEquals(8, res.size());
  }

  @Test
  public void paaGreaterThanWindowIsSkipped() throws Exception {
    // window fixed at 6, paa ranges 4..8: paa in {7,8} exceed the window and must be skipped,
    // leaving paa in {4,5,6} -> 3 points
    int[] boundaries = new int[] { 6, 6, 1, 4, 8, 1, 3, 3, 1 };
    ArrayList<SampledPoint> res = run(boundaries, 6);
    assertEquals(3, res.size());
    for (SampledPoint p : res) {
      assertTrue("paa must not exceed window", p.getPAA() <= p.getWindow());
    }
  }

  @Test
  public void degenerateRangeYieldsEmptyList() throws Exception {
    // winLimit below the window min -> outer loop never runs -> empty (feeds the SAMPLING_FAILED guard)
    int[] boundaries = new int[] { 50, 200, 10, 4, 5, 1, 3, 4, 1 };
    ArrayList<SampledPoint> res = run(boundaries, 10);
    assertTrue("degenerate range must produce no points", res.isEmpty());
  }

  @Test
  public void badAlphabetPointIsSkippedNotFatal() throws Exception {
    // alphabet 4..25: NormalAlphabet supports only [2,20], so 21..25 throw inside sample();
    // they must be skipped (logged) rather than aborting the scan -- valid points still land
    int[] boundaries = new int[] { 100, 100, 10, 5, 5, 1, 4, 25, 1 };
    ArrayList<SampledPoint> res = run(boundaries, 100);
    assertFalse("valid alphabet points should still be sampled", res.isEmpty());
    for (SampledPoint p : res) {
      assertTrue("only in-range alphabets survive", p.getAlphabet() >= 2 && p.getAlphabet() <= 20);
    }
  }
}
