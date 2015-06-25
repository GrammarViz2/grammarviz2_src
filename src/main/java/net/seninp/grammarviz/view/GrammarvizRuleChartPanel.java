package net.seninp.grammarviz.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JPanel;
import net.seninp.gi.RuleInterval;
import net.seninp.grammarviz.controller.GrammarVizController;
import net.seninp.grammarviz.logic.MotifChartData;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.discord.DiscordRecord;
import net.seninp.util.StackTrace;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class GrammarvizRuleChartPanel extends JPanel implements PropertyChangeListener {

  /** Fancy serial. */
  private static final long serialVersionUID = 5334407476500195779L;

  /** The chart container. */
  private JFreeChart chart;

  /** The plot itself. */
  private XYPlot plot;

  /** Current chart data instance. */
  private MotifChartData chartData;

  private TSProcessor tp;
  private GrammarVizController controller;

  /**
   * Constructor.
   */
  public GrammarvizRuleChartPanel() {
    super();
    tp = new TSProcessor();
  }

  /**
   * Adds a controler instance to get normalization value from.
   * 
   * @param controller the controller instance.
   */
  public void setController(GrammarVizController controller) {
    this.controller = controller;
  }

  public void setChartData(MotifChartData chartData) {
    this.chartData = chartData;
    resetChartPanel();
  }

  /**
   * Create the chart for the original time series.
   * 
   * @return a JFreeChart object of the chart
   * @throws TSException
   */
  private void chartIntervals(ArrayList<double[]> intervals) throws Exception {

    // making the data
    //
    XYSeriesCollection collection = new XYSeriesCollection();
    int counter = 0;
    for (double[] series : intervals) {
      collection.addSeries(toSeries(counter++, series));
    }

    // set the renderer
    //
    XYLineAndShapeRenderer xyRenderer = new XYLineAndShapeRenderer(true, false);
    xyRenderer.setSeriesPaint(0, new Color(0, 0, 0));
    xyRenderer.setBaseStroke(new BasicStroke(3));

    // X - the time axis
    //
    NumberAxis timeAxis = new NumberAxis();
    // timeAxis.setLabel("Time");

    // Y axis
    //
    NumberAxis valueAxis = new NumberAxis();
    valueAxis.setAutoRangeIncludesZero(false);
    // valueAxis.setLabel("Values");

    // put these into collection of dots
    //
    this.plot = new XYPlot(collection, timeAxis, valueAxis, xyRenderer);

    // finally, create the chart
    this.chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, plot, false);

    // and put it on the show
    ChartPanel chartPanel = new ChartPanel(this.chart);
    chartPanel.setMinimumDrawWidth(0);
    chartPanel.setMinimumDrawHeight(0);
    chartPanel.setMaximumDrawWidth(1920);
    chartPanel.setMaximumDrawHeight(1200);

    // cleanup all the content
    //
    this.removeAll();

    // put the chart on show
    //
    this.add(chartPanel);

    // not sure if I need this
    //
    this.validate();
    this.repaint();

  }

  /**
   * Converts an array to a normalized XYSeries to be digested with JFreeChart.
   * 
   * @param index
   * @param series
   * @return
   * @throws TSException
   */
  private XYSeries toSeries(int index, double[] series) throws Exception {
    double[] normalizedSubseries = tp.znorm(series, controller.getSession()
        .getNormalizationThreshold());
    XYSeries res = new XYSeries("series" + String.valueOf(index));
    for (int i = 0; i < normalizedSubseries.length; i++) {
      res.add(i, normalizedSubseries[i]);
    }
    return res;
  }

  /**
   * Highlight the original time series sequences of a rule.
   * 
   * @param index index of the rule in the sequitur table.
   */
  protected void chartIntervalsForRule(String rule) {
    try {
      ArrayList<RuleInterval> arrPos = chartData.getRulePositionsByRuleNum(Integer.valueOf(rule));
      ArrayList<double[]> intervals = new ArrayList<double[]>();
      for (RuleInterval saxPos : arrPos) {
        intervals.add(extractInterval(saxPos.getStartPos(), saxPos.getEndPos()));
      }
      chartIntervals(intervals);
    }
    catch (Exception e) {
      System.err.println(StackTrace.toString(e));
    }
  }

  /**
   * Highlight the original time series sequences of a sub-sequences class.
   * 
   * @param index index of the class in the sub-sequences class table.
   */
  protected void chartIntervalsForClass(String classIndex) {
    try {
      ArrayList<RuleInterval> arrPos = chartData.getSubsequencesPositionsByClassNum(Integer
          .valueOf(classIndex));
      ArrayList<double[]> intervals = new ArrayList<double[]>();
      for (RuleInterval saxPos : arrPos) {
        intervals.add(extractInterval(saxPos.getStartPos(), saxPos.getEndPos()));
      }
      chartIntervals(intervals);
    }
    catch (Exception e) {
      System.err.println(StackTrace.toString(e));
    }
  }

  /**
   * Charts a subsequence for a selected row in the anomaly table.
   * 
   * @param selectedRow
   */
  private void chartIntervalForAnomaly(String selectedRow) {
    // find the anomaly
    try {
      DiscordRecord dr = this.chartData.getAnomalies().get(Integer.valueOf(selectedRow));
      ArrayList<double[]> intervals = new ArrayList<double[]>();
      intervals.add(extractInterval(dr.getPosition(), dr.getPosition() + dr.getLength()));
      chartIntervals(intervals);
    }
    catch (Exception e) {
      System.err.println(StackTrace.toString(e));
    }
  }

  /**
   * Extracts a subsequence of the original time series.
   * 
   * @param startPos the start position.
   * @param endPos the end position.
   * @return the subsequence.
   * @throws Exception if error occurs.
   */
  private double[] extractInterval(int startPos, int endPos) throws Exception {
    if (this.chartData.getOriginalTimeseries().length <= (endPos - startPos)) {
      return Arrays.copyOf(this.chartData.getOriginalTimeseries(),
          this.chartData.getOriginalTimeseries().length);
    }
    return Arrays.copyOfRange(this.chartData.getOriginalTimeseries(), startPos, endPos);
  }

  /**
   * Clears the chart panel of the content.
   */
  public void resetChartPanel() {
    this.removeAll();
    this.validate();
    this.repaint();
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (GrammarvizRulesPanel.FIRING_PROPERTY.equalsIgnoreCase(evt.getPropertyName())) {
      String newlySelectedRaw = (String) evt.getNewValue();
      chartIntervalsForRule(newlySelectedRaw);
    }
    else if (PackedRulesPanel.FIRING_PROPERTY_PACKED.equalsIgnoreCase(evt.getPropertyName())) {
      String newlySelectedRaw = (String) evt.getNewValue();
      chartIntervalsForClass(newlySelectedRaw);

    }
    else if (RulesPeriodicityPanel.FIRING_PROPERTY_PERIOD.equalsIgnoreCase(evt.getPropertyName())) {
      String newlySelectedRaw = (String) evt.getNewValue();
      chartIntervalsForRule(newlySelectedRaw);
    }
    else if (AnomaliesPanel.FIRING_PROPERTY_ANOMALY.equalsIgnoreCase(evt.getPropertyName())) {
      String newlySelectedRaw = (String) evt.getNewValue();
      chartIntervalForAnomaly(newlySelectedRaw);
    }

  }

  /**
   * Clears the panel.
   */
  public void clear() {
    resetChartPanel();
  }

}
