package net.seninp.grammarviz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import net.seninp.gi.rulepruner.SampledPoint;

public class TestSampledPointPool {

  private static SampledPoint point(double coverage) {
    SampledPoint p = new SampledPoint();
    p.setCoverage(coverage);
    p.setWindow(100);
    p.setPAA(5);
    p.setAlphabet(4);
    return p;
  }

  @Test
  public void poolAtCoverageThresholdKeepsQualifyingPointsOnly() {
    ArrayList<SampledPoint> res = new ArrayList<SampledPoint>();
    res.add(point(0.50));
    res.add(point(0.995));
    ArrayList<SampledPoint> pool = GrammarVizAnomaly.poolAtCoverageThreshold(res, 0.99);
    assertEquals(1, pool.size());
    assertEquals(0.995, pool.get(0).getCoverage(), 1e-9);
  }

  @Test
  public void poolAtCoverageThresholdFallsBackWhenNoneMeetThreshold() {
    ArrayList<SampledPoint> res = new ArrayList<SampledPoint>();
    res.add(point(0.50));
    res.add(point(0.80));
    ArrayList<SampledPoint> pool = GrammarVizAnomaly.poolAtCoverageThreshold(res, 0.99);
    assertEquals(2, pool.size());
    assertEquals(0.50, pool.get(0).getCoverage(), 1e-9);
    assertEquals(0.80, pool.get(1).getCoverage(), 1e-9);
  }

  @Test
  public void poolAtCoverageThresholdEmptyInputStaysEmpty() {
    ArrayList<SampledPoint> res = new ArrayList<SampledPoint>();
    ArrayList<SampledPoint> pool = GrammarVizAnomaly.poolAtCoverageThreshold(res, 0.99);
    assertTrue(pool.isEmpty());
    assertTrue(!GrammarVizAnomaly.hasSampledPoints(res));
  }

  @Test
  public void hasSampledPointsRejectsNullAndEmpty() {
    assertTrue(!GrammarVizAnomaly.hasSampledPoints(null));
    assertTrue(!GrammarVizAnomaly.hasSampledPoints(new ArrayList<SampledPoint>()));
    ArrayList<SampledPoint> one = new ArrayList<SampledPoint>();
    one.add(point(0.1));
    assertTrue(GrammarVizAnomaly.hasSampledPoints(one));
  }
}
