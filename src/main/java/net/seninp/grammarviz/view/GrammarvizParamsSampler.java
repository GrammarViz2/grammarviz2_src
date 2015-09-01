package net.seninp.grammarviz.view;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Callable;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import net.seninp.gi.rulepruner.ReductionSorter;
import net.seninp.gi.rulepruner.RulePruner;
import net.seninp.gi.rulepruner.RulePrunerParameters;
import net.seninp.gi.rulepruner.SampledPoint;
import net.seninp.grammarviz.logic.GrammarVizChartData;

public class GrammarvizParamsSampler implements Callable<String> {

  private GrammarvizChartPanel parent;

  private int sampleIntervalStart;
  private int sampleIntervalEnd;

  private static final int[] boundaries = { 10, 200, 10, 2, 10, 1, 2, 10, 1 };

  // the logger business
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.DEBUG;

  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(GrammarvizParamsSampler.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  public GrammarvizParamsSampler(GrammarvizChartPanel grammarvizChartPanel) {
    this.parent = grammarvizChartPanel;
  }

  public void setSampleIntervalStart(int selectionStart) {
    this.sampleIntervalStart = selectionStart;
  }

  public void setSampleIntervalEnd(int selectionEnd) {
    this.sampleIntervalEnd = selectionEnd;
  }

  public void cancel() {
    this.parent.actionPerformed(new ActionEvent(this, 0, GrammarvizChartPanel.SELECTION_CANCELLED));
  }

  @Override
  public String call() throws Exception {

    ArrayList<SampledPoint> res = new ArrayList<SampledPoint>();

    this.parent.actionPerformed(new ActionEvent(this, 0, GrammarvizChartPanel.SELECTION_FINISHED));

    double[] ts = Arrays.copyOfRange(this.parent.tsData, sampleIntervalStart, sampleIntervalEnd);
    //
    //
    RulePruner rp = new RulePruner(ts);

    for (int WINDOW_SIZE = boundaries[0]; WINDOW_SIZE < boundaries[1]; WINDOW_SIZE += boundaries[2]) {
      for (int PAA_SIZE = boundaries[3]; PAA_SIZE < boundaries[4]; PAA_SIZE += boundaries[5]) {

        // check for invalid cases
        if (PAA_SIZE > WINDOW_SIZE) {
          continue;
        }

        for (int ALPHABET_SIZE = boundaries[6]; ALPHABET_SIZE < boundaries[7]; ALPHABET_SIZE += boundaries[8]) {

          SampledPoint p = null;

          p = rp.sample(WINDOW_SIZE, PAA_SIZE, ALPHABET_SIZE, RulePrunerParameters.SAX_NR_STRATEGY,
              RulePrunerParameters.SAX_NORM_THRESHOLD);

          if (null != p) {
            res.add(p);
          }

          if (Thread.currentThread().isInterrupted()) {
            // Cannot use InterruptedException since it's checked
            System.err.println("Ooops -- was interrupted, finilizing sampling ...");

            Collections.sort(res, new ReductionSorter());
            System.out.println("\nApparently, the best parameters are " + res.get(0).toString());
            this.parent
                .actionPerformed(new ActionEvent(this, 0, GrammarvizChartPanel.SAMPLING_SUCCEEDED));
            return res.get(0).toString();

          }

        }
      }
    }
    // bw.close();

    Collections.sort(res, new ReductionSorter());

    System.out.println("\nApparently, the best parameters are " + res.get(0).toString());
    //
    //
    this.parent.actionPerformed(new ActionEvent(this, 0, GrammarvizChartPanel.SAMPLING_SUCCEEDED));

    return res.get(0).toString();

  }

  public void interrupt() {
    // TODO Auto-generated method stub

  }

}
