package net.seninp.grammarviz.view;

import java.awt.event.ActionEvent;
import net.seninp.grammarviz.logic.GrammarVizChartData;

public class GrammarvizParamsSampler {

  private GrammarvizChartPanel parent;

  private int sampleIntervalStart;
  private int sampleIntervalEnd;

  public GrammarvizParamsSampler(GrammarvizChartPanel grammarvizChartPanel) {
    this.parent = grammarvizChartPanel;
  }

  public void sample() {
    this.parent.actionPerformed(new ActionEvent(this, 0, GrammarvizChartPanel.SELECTION_FINISHED));
    this.parent.actionPerformed(new ActionEvent(this, 0, GrammarvizChartPanel.SAMPLING_SUCCEEDED));
  }

  public void setTimeSeries(double[] originalTimeseries) {
    // TODO Auto-generated method stub

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

}
