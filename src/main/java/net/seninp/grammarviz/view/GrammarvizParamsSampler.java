package net.seninp.grammarviz.view;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.seninp.gi.GIAlgorithm;
import net.seninp.gi.rulepruner.ReductionSorter;
import net.seninp.gi.rulepruner.RulePruner;
import net.seninp.gi.rulepruner.SampledPoint;
import net.seninp.grammarviz.GrammarSizeSorter;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;

public class GrammarvizParamsSampler implements Callable<String> {

  private GrammarvizChartPanel parent;

  // static block - we instantiate the logger
  //
  private static final Logger LOGGER = LoggerFactory.getLogger(GrammarvizParamsSampler.class);

  public GrammarvizParamsSampler(GrammarvizChartPanel grammarvizChartPanel) {
    this.parent = grammarvizChartPanel;
  }

  public void cancel() {
    this.parent.actionPerformed(new ActionEvent(this, 0, GrammarvizChartPanel.SELECTION_CANCELLED));
  }

  @Override
  public String call() throws Exception {

    ArrayList<SampledPoint> res = new ArrayList<SampledPoint>();

    this.parent.actionPerformed(new ActionEvent(this, 0, GrammarvizChartPanel.SELECTION_FINISHED));

    double[] ts = Arrays.copyOfRange(this.parent.tsData, this.parent.session.samplingStart,
        this.parent.session.samplingEnd);

    RulePruner rp = new RulePruner(ts);
    int[] boundaries = Arrays.copyOf(this.parent.session.boundaries,
        this.parent.session.boundaries.length);

    //
    //
    LOGGER.info("starting sampling loop on interval [" + this.parent.session.samplingStart + ", "
        + this.parent.session.samplingEnd + "] of length "
        + Integer.valueOf(this.parent.session.samplingEnd - this.parent.session.samplingStart));
    LOGGER
        .info("window range: " + boundaries[0] + " - " + boundaries[1] + ", step " + boundaries[2]);
    LOGGER.info("PAA range: " + boundaries[3] + " - " + boundaries[4] + ", step " + boundaries[5]);
    LOGGER.info(
        "Alphabet range: " + boundaries[6] + " - " + boundaries[7] + ", step " + boundaries[8]);
    //
    //

    // need to take care about the sliding window size and adjust it
    //
    int samplingIntervalLength = this.parent.session.samplingEnd
        - this.parent.session.samplingStart;

    int winLimit = Math.min(samplingIntervalLength, boundaries[1]);

    // run the grid; success XOR failure always fires an event so the UI never hangs
    //
    try {
      sampleGrid(rp, boundaries, winLimit, this.parent.session.giAlgorithm,
          this.parent.session.numerosityReductionStrategy,
          this.parent.session.normalizationThreshold, res);
      return finishSampling(res, false);
    }
    catch (InterruptedException e) {
      LOGGER.info("sampler interrupted -- selecting best of " + res.size() + " sampled points");
      return finishSampling(res, true);
    }
    catch (Exception e) {
      LOGGER.error("sampler failed", e);
      this.parent.actionPerformed(new ActionEvent(this, 0, GrammarvizChartPanel.SAMPLING_FAILED));
      return "";
    }
  }

  /**
   * Pure (Swing-free, testable) grid scan: samples every valid (window, PAA, alphabet) triple
   * over the inclusive ranges in {@code boundaries}, appending each successful
   * {@link SampledPoint} to {@code res}. A single point that fails to sample (e.g. an
   * out-of-range alphabet throws) is logged and skipped so it does not abort the whole grid;
   * a user interrupt aborts the scan via {@link InterruptedException}.
   *
   * @param rp the rule pruner bound to the (sub)series.
   * @param boundaries the 9-element grid: window {min,max,step}, PAA {min,max,step},
   *        alphabet {min,max,step}.
   * @param winLimit the effective window upper bound (min of the series length and the window
   *        max), so the window never exceeds the data.
   * @param giAlgorithm the grammar-induction algorithm to score with.
   * @param nrStrategy the numerosity-reduction strategy.
   * @param normThreshold the normalization threshold.
   * @param res the (typically empty) list the sampled points are appended to.
   * @throws InterruptedException if the current thread is interrupted during the scan.
   */
  static void sampleGrid(RulePruner rp, int[] boundaries, int winLimit, GIAlgorithm giAlgorithm,
      NumerosityReductionStrategy nrStrategy, double normThreshold, ArrayList<SampledPoint> res)
      throws InterruptedException {

    // inclusive MAX bounds (<=) so the user's typed MAX is actually evaluated
    for (int windowSize = boundaries[0]; windowSize <= winLimit; windowSize += boundaries[2]) {

      for (int paaSize = boundaries[3]; paaSize <= boundaries[4]; paaSize += boundaries[5]) {

        // check for invalid cases
        if (paaSize > windowSize) {
          continue;
        }

        for (int alphabetSize = boundaries[6]; alphabetSize <= boundaries[7]; alphabetSize += boundaries[8]) {

          if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("sampler interrupted by the user");
          }

          SampledPoint p = null;
          try {
            p = rp.sample(windowSize, paaSize, alphabetSize, giAlgorithm, nrStrategy,
                normThreshold);
          }
          catch (InterruptedException e) {
            throw e;
          }
          catch (Exception e) {
            // one bad parameter point (e.g. an out-of-range alphabet) must not abort the
            // whole grid -- skip it and keep sampling the rest
            LOGGER.warn("skipping point [w=" + windowSize + ", p=" + paaSize + ", a=" + alphabetSize
                + "]: " + e.getMessage());
            continue;
          }

          if (null != p) {
            res.add(p);
          }
        }
      }
    }
  }

  /**
   * Picks the best sampled point, writes it into the session, and fires the appropriate UI
   * event. Always fires SAMPLING_SUCCEEDED (point chosen) or SAMPLING_FAILED (nothing to
   * choose) so the caller's button is never left stuck.
   *
   * @param res the sampled points (may be empty).
   * @param interrupted whether the run was interrupted (user pressed Stop).
   * @return the chosen "window paa alphabet" string, or "" if none.
   */
  private String finishSampling(ArrayList<SampledPoint> res, boolean interrupted) {

    SampledPoint best = selectBest(res);

    if (null == best) {
      LOGGER.warn("sampler produced no valid points for the selected interval and ranges");
      this.parent.actionPerformed(new ActionEvent(this, 0, GrammarvizChartPanel.SAMPLING_FAILED));
      return "";
    }

    parent.session.saxWindow = best.getWindow();
    parent.session.saxPAA = best.getPAA();
    parent.session.saxAlphabet = best.getAlphabet();

    LOGGER.info((interrupted ? "interrupted; " : "") + "best parameters are " + best.toString());

    // signal whether the threshold was actually met so the view can inform the user
    String event = best.getCoverage() >= this.parent.session.minimalCoverThreshold
        ? GrammarvizChartPanel.SAMPLING_SUCCEEDED
        : GrammarvizChartPanel.SAMPLING_BELOW_THRESHOLD;
    this.parent.actionPerformed(new ActionEvent(this, 0, event));

    return best.getWindow() + " " + best.getPAA() + " " + best.getAlphabet();
  }

  /**
   * Selects the "best" sampled point: among points whose pruned grammar covers at least the
   * user's minimal-cover threshold, the one with the smallest surviving-rule fraction
   * (reduction), breaking ties by grammar size for determinism. If no point meets the
   * threshold, falls back to ranking all sampled points the same way (and logs it).
   *
   * @param res the sampled points.
   * @return the best point, or {@code null} if {@code res} is empty.
   */
  private SampledPoint selectBest(ArrayList<SampledPoint> res) {
    return selectBest(res, this.parent.session.minimalCoverThreshold);
  }

  /**
   * Pure (Swing-free, testable) core of the selection: among points whose pruned grammar
   * covers at least {@code minCover}, returns the one with the smallest reduction (surviving-
   * rule fraction), breaking ties by grammar size. Falls back to ranking all points if none
   * meet the threshold.
   *
   * @param res the sampled points (not mutated except for sorting; may be empty).
   * @param minCover the minimal rule-cover threshold.
   * @return the chosen point, or {@code null} if {@code res} is empty.
   */
  static SampledPoint selectBest(ArrayList<SampledPoint> res, double minCover) {

    if (res.isEmpty()) {
      return null;
    }

    ArrayList<SampledPoint> covered = new ArrayList<SampledPoint>();
    for (SampledPoint p : res) {
      if (p.getCoverage() >= minCover) {
        covered.add(p);
      }
    }

    ArrayList<SampledPoint> pool = covered.isEmpty() ? res : covered;
    if (covered.isEmpty()) {
      LOGGER.warn("no parameter set reached cover threshold " + minCover + " over " + res.size()
          + " samples; falling back to best-reduction over all points");
    }

    // primary key: smallest reduction (GUI's existing ranking); secondary: smallest grammar
    // size, to make the choice among reduction-ties deterministic
    pool.sort(new ReductionSorter().thenComparing(new GrammarSizeSorter()));

    return pool.get(0);
  }

}
