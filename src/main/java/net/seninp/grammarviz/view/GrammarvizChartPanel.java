package net.seninp.grammarviz.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.axis.TickType;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import net.seninp.gi.GrammarRuleRecord;
import net.seninp.gi.RuleInterval;
import net.seninp.grammarviz.logic.CoverageCountStrategy;
import net.seninp.grammarviz.logic.GrammarVizChartData;
import net.seninp.grammarviz.logic.UserSession;
import net.seninp.jmotif.sax.discord.DiscordRecord;

/**
 * 
 * Handles the chart panel and listens for events from Sequitur rules table.
 * 
 * @author Manfred Lerner, seninp
 * 
 */
public class GrammarvizChartPanel extends JPanel implements PropertyChangeListener,
    ChartProgressListener, ActionListener {

  /** Fancy serial. */
  private static final long serialVersionUID = -2710973854572981568L;

  // various display string constants
  //
  private static final String LABEL_SHOWING_RULES = " Data display: showing rule subsequences ";
  private static final String LABEL_SHOWING_HISTOGRAMM = " Data display: showing rules length histogramm ";
  private static final String LABEL_SHOWING_DENSITY = " Data display: showing grammar rules density, ";
  private static final String LABEL_SHOWING_PACKED_RULES = " Data display: showing packed rule subsequences ";
  private static final String LABEL_SHOWING_PERIODS = " Data display: showing periods between selected rules ";
  private static final String LABEL_SHOWING_ANOMALY = " Data display: showing anomaly ";
  private static final String LABEL_SAVING_CHART = " Data display: saving the rules density chart ";

  /** The chart container. */
  private JFreeChart chart;

  /** The timeseries plot itself. */
  private XYPlot timeseriesPlot;

  /** Current chart data instance. */
  private GrammarVizChartData chartData;

  /** JFreeChart Object holding the chart times series */
  XYSeriesCollection chartXYSeriesCollection;

  /** Position of the previous mouse click in the chart */
  double previousClickPosition = 0;

  /** The user session var - holds all parameters. */
  private UserSession session;

  // the logger business
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.DEBUG;
  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(GrammarvizChartPanel.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  /**
   * Constructor.
   */
  public GrammarvizChartPanel() {
    super();
  }

  // /**
  // * Constructor.
  // *
  // * @param chartData the chart data.
  // */
  // public SequiturChartPanel(MotifChartData chartData) {
  // this.chartData = chartData;
  // }

  /**
   * This sets the chartData and forces the panel to repaint itself showing the new chart.
   * 
   * @param chartData the data to use.
   */
  public void setChartData(GrammarVizChartData chartData, UserSession session) {
    this.chartData = chartData;
    this.resetChartPanel();
    this.session = session;
  }

  /**
   * Get the chart data object currently on show.
   * 
   * @return chartData the data in use.
   */
  public GrammarVizChartData getChartData() {
    return this.chartData;
  }

  /**
   * Get the chart.
   * 
   * @return JFreeChart object
   */
  public JFreeChart getChart() {
    return chart;
  }

  /**
   * Creates the chart panel, puts it on display.
   * 
   */
  public void resetChartPanel() {

    // this is the new "insert" - elastic boundaries chart panel
    //
    paintTheChart(this.chartData.getOriginalTimeseries());

    ChartPanel chartPanel = new ChartPanel(this.chart);

    chartPanel.setMinimumDrawWidth(0);
    chartPanel.setMinimumDrawHeight(0);
    chartPanel.setMaximumDrawWidth(1920);
    chartPanel.setMaximumDrawHeight(1200);

    chartPanel.setMouseWheelEnabled(true);

    // cleanup all the content
    //
    this.removeAll();

    // put the chart on show
    //
    this.add(chartPanel);

    // not sure if I need this
    //
    validate();
    repaint();
  }

  /**
   * Highlights the subsequence of the rule.
   * 
   * @param The rule index.
   */
  private void highlightPatternInChart(ArrayList<String> rules) {
    consoleLogger.debug("Selected rules: " + rules.toString());
    timeseriesPlot.clearDomainMarkers();
    for (String rule : rules) {
      ArrayList<RuleInterval> arrPos = chartData.getRulePositionsByRuleNum(Integer.valueOf(rule));
      consoleLogger.debug("Size: " + arrPos.size() + " - Positions: " + arrPos);
      for (RuleInterval saxPos : arrPos) {
        addMarker(timeseriesPlot, saxPos.getStartPos(), saxPos.getEndPos());
      }
    }
  }

  /**
   * Highlights the subsequence of the rule.
   * 
   * @param The rule index.
   */
  private void highlightPatternInChartPacked(String classIndex) {
    consoleLogger.debug("Selected class: " + classIndex);
    ArrayList<RuleInterval> arrPos = chartData.getSubsequencesPositionsByClassNum(Integer
        .valueOf(classIndex));
    consoleLogger.debug("Size: " + arrPos.size() + " - Positions: " + arrPos);
    timeseriesPlot.clearDomainMarkers();
    for (RuleInterval saxPos : arrPos) {
      addMarker(timeseriesPlot, saxPos.getStartPos(), saxPos.getEndPos());
    }
  }

  /**
   * Highlights intervals in between selected rule subsequences - ones which suppose to be periods.
   * 
   * @param rule The rule whose subsequences will be period boundaries.
   */
  private void highlightPeriodsBetweenPatterns(String rule) {
    consoleLogger.debug("Selected rule: " + rule);
    ArrayList<RuleInterval> arrPos = chartData.getRulePositionsByRuleNum(Integer.valueOf(rule));
    consoleLogger.debug("Size: " + arrPos.size() + " - Positions: " + arrPos);
    timeseriesPlot.clearDomainMarkers();
    for (int i = 1; i < arrPos.size(); i++) {
      RuleInterval c = arrPos.get(i - 1);
      RuleInterval p = arrPos.get(i);
      addPeriodMarker(timeseriesPlot, c.getEndPos(), p.getStartPos());
    }
  }

  private void highlightAnomaly(ArrayList<String> anomalies) {
    consoleLogger.debug("Selected anomalies: " + anomalies.toString());
    timeseriesPlot.clearDomainMarkers();
    for (String anomaly : anomalies) {
      DiscordRecord dr = this.chartData.getAnomalies().get(Integer.valueOf(anomaly));
      consoleLogger.debug(dr.toString());
      addAnomalyMarker(timeseriesPlot, dr.getPosition(), dr.getPosition() + dr.getLength());
    }
  }

  /**
   * Puts rules density on show.
   */
  private void displayRuleDensity() {

    // this is the new "insert" - elastic boundaries chart panel
    //
    paintTheChart(this.chartData.getOriginalTimeseries());
    ChartPanel chartPanel = new ChartPanel(this.chart);
    chartPanel.setMinimumDrawWidth(0);
    chartPanel.setMinimumDrawHeight(0);
    chartPanel.setMaximumDrawWidth(1920);
    chartPanel.setMaximumDrawHeight(1200);
    //
    this.removeAll();
    //
    this.add(chartPanel);

    // timeseriesPlot.clearDomainMarkers();
    int rulesNum = this.chartData.getRulesNumber();

    // find the rule density value
    int maxObservedCoverage = 0;
    int[] coverageArray = new int[chartData.getOriginalTimeseries().length];

    for (GrammarRuleRecord r : chartData.getGrammarRules()) {
      if (0 == r.ruleNumber()) {
        continue;
      }
      ArrayList<RuleInterval> arrPos = chartData.getRulePositionsByRuleNum(r.ruleNumber());
      for (RuleInterval saxPos : arrPos) {
        int start = saxPos.getStartPos();
        int end = saxPos.getEndPos();
        for (int j = start; j < end; j++) {
          if (CoverageCountStrategy.COUNT == this.session.getCountStrategy()) {
            coverageArray[j] = coverageArray[j] + 1;
          }
          else if (CoverageCountStrategy.LEVEL == this.session.getCountStrategy()) {
            coverageArray[j] = coverageArray[j] + r.getRuleLevel();
          }
          else if (CoverageCountStrategy.OCCURRENCE == this.session.getCountStrategy()) {
            coverageArray[j] = coverageArray[j] + r.getOccurrences().size();
          }
          else if (CoverageCountStrategy.YIELD == this.session.getCountStrategy()) {
            coverageArray[j] = coverageArray[j] + r.getRuleYield();
          }
          else if (CoverageCountStrategy.PRODUCT == this.session.getCountStrategy()) {
            coverageArray[j] = coverageArray[j] + r.getRuleLevel() * r.getOccurrences().size();
          }
          if (maxObservedCoverage < coverageArray[j]) {
            maxObservedCoverage = coverageArray[j];
          }
        }
      }
    }

    // since we know the maximal coverage value, we can compute the increment for a single coverage
    // interval
    double covIncrement = 1. / (double) maxObservedCoverage;

    for (int i = 0; i < rulesNum; i++) {
      GrammarRuleRecord r = chartData.getRule(i);
      if (0 == r.ruleNumber()) {
        continue;
      }
      ArrayList<RuleInterval> arrPos = chartData.getRulePositionsByRuleNum(i);
      for (RuleInterval saxPos : arrPos) {
        IntervalMarker marker = new IntervalMarker(saxPos.getStartPos(), saxPos.getEndPos());
        marker.setLabelOffsetType(LengthAdjustmentType.EXPAND);
        marker.setPaint(Color.BLUE);

        // marker.setAlpha((float) 0.05);
        if (CoverageCountStrategy.COUNT == this.session.getCountStrategy()) {
          marker.setAlpha((float) covIncrement);
        }
        else if (CoverageCountStrategy.LEVEL == this.session.getCountStrategy()) {
          marker.setAlpha((float) covIncrement * r.getRuleLevel());
        }
        else if (CoverageCountStrategy.OCCURRENCE == this.session.getCountStrategy()) {
          marker.setAlpha((float) covIncrement * r.getOccurrences().size());
        }
        else if (CoverageCountStrategy.YIELD == this.session.getCountStrategy()) {
          marker.setAlpha((float) covIncrement * r.getRuleYield());
        }
        else if (CoverageCountStrategy.PRODUCT == this.session.getCountStrategy()) {
          marker.setAlpha((float) covIncrement * (r.getRuleLevel() * r.getOccurrences().size()));
        }
        marker.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        marker.setLabelPaint(Color.green);
        marker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
        marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        timeseriesPlot.addDomainMarker(marker, Layer.BACKGROUND);
      }
    }

    // not sure if I need this
    //
    validate();
    repaint();

    // and finally save the coverage curve
    //

    this.saveRuleDensityCurve(coverageArray);

  }

  private void saveRuleDensityCurve(int[] coverageArray) {
    // write down the coverage array
    //
    try {
      String filename = session.getRuleDensityOutputFileName();
      BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filename)));
      for (int c : coverageArray) {
        bw.write(String.valueOf(c) + "\n");
      }
      bw.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void displayRulesLengthHistogram() {

    // cleanup all the content
    //
    this.removeAll();
    validate();
    repaint();

    // construct the dataset
    //

    // [1.0] extract all the rules
    ArrayList<Integer> allRules = new ArrayList<Integer>();
    for (GrammarRuleRecord r : chartData.getGrammarRules()) {
      if (0 == r.ruleNumber()) {
        continue;
      }
      for (RuleInterval interval : r.getRuleIntervals()) {
        allRules.add(interval.getLength());
      }
    }

    // [2.0] make data
    Collections.sort(allRules);
    // final int minLength = allRules.get(0);
    final int maxLength = allRules.get(allRules.size() - 1);
    final int numberOfBins = maxLength / this.chartData.getSAXWindowSize() + 1;

    double[] values = new double[allRules.size()];
    for (int i = 0; i < allRules.size(); i++) {
      values[i] = allRules.get(i).doubleValue();
    }

    HistogramDataset dataset = new HistogramDataset();
    dataset.setType(HistogramType.FREQUENCY);

    dataset.addSeries("Frequencies", values, numberOfBins, 0,
        numberOfBins * this.chartData.getSAXWindowSize());

    String plotTitle = "Rules Length Histogram";
    String xaxis = "Rule length";
    String yaxis = "Counts";

    PlotOrientation orientation = PlotOrientation.VERTICAL;
    boolean show = true;
    boolean toolTips = false;
    boolean urls = false;
    this.chart = ChartFactory.createHistogram(plotTitle, xaxis, yaxis, dataset, orientation, show,
        toolTips, urls);
    this.chart.removeLegend();

    NumberAxis myAxis = new NumberAxis(this.chart.getXYPlot().getDomainAxis().getLabel()) {

      private static final long serialVersionUID = 5839368758428973857L;

      @Override
      public List<NumberTick> refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea,
          RectangleEdge edge) {

        // List<NumberTick> allTicks = super.refreshTicks(g2, state, dataArea, edge);

        List<NumberTick> myTicks = new ArrayList<NumberTick>();

        for (int i = 0; i < numberOfBins; i++) {
          myTicks.add(new NumberTick(TickType.MAJOR, i * chartData.getSAXWindowSize(), String
              .valueOf(i * chartData.getSAXWindowSize()), TextAnchor.CENTER, TextAnchor.CENTER,
              0.0d));
          // textAnchor, rotationAnchor, angle));
        }

        // for (Object tick : allTicks) {
        // NumberTick numberTick = (NumberTick) tick;
        //
        // if (TickType.MAJOR.equals(numberTick.getTickType())
        // && (numberTick.getValue() % chartData.getSAXWindowSize() != 0)) {
        // // myTicks.add(new NumberTick(TickType.MINOR, numberTick.getValue(), "", numberTick
        // // .getTextAnchor(), numberTick.getRotationAnchor(), numberTick.getAngle()));
        // continue;
        // }
        // myTicks.add(tick);
        // }
        return myTicks;
      }
    };

    this.chart.getXYPlot().setDomainAxis(myAxis);

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

    validate();
    repaint();
  }

  /**
   * @param plot plot for the marker
   * @param startVal start postion
   * @param endVal end position
   */
  protected void addMarker(XYPlot plot, int startVal, int endVal) {
    IntervalMarker marker = new IntervalMarker(startVal, endVal);
    marker.setLabelOffsetType(LengthAdjustmentType.EXPAND);
    marker.setPaint(new Color(134, 254, 225));
    marker.setAlpha((float) 0.60);
    marker.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
    marker.setLabelPaint(Color.green);
    marker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
    marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);

    plot.addDomainMarker(marker, Layer.BACKGROUND);

    ValueMarker markStart = new ValueMarker(startVal, new Color(31, 254, 225),
        new BasicStroke(2.0f));
    ValueMarker markEnd = new ValueMarker(endVal, new Color(31, 254, 225), new BasicStroke(2.0f));
    plot.addDomainMarker(markStart, Layer.BACKGROUND);
    plot.addDomainMarker(markEnd, Layer.BACKGROUND);
  }

  /**
   * Adds a periodicity marker.
   * 
   * @param plot plot for the marker
   * @param startVal start postion
   * @param endVal end position
   */
  protected void addPeriodMarker(XYPlot plot, int startVal, int endVal) {

    IntervalMarker marker = new IntervalMarker(startVal, endVal);

    marker.setLabelOffsetType(LengthAdjustmentType.EXPAND);
    marker.setPaint(new Color(134, 254, 225));
    marker.setAlpha((float) 0.60);
    marker.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
    marker.setLabelPaint(Color.blue);
    marker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
    marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);

    marker.setPaint(Color.blue);

    plot.addDomainMarker(marker, Layer.BACKGROUND);
  }

  /**
   * Adds an anomaly marker.
   * 
   * @param plot plot for the marker
   * @param startVal start postion
   * @param endVal end position
   */
  protected void addAnomalyMarker(XYPlot plot, int startVal, int endVal) {

    IntervalMarker marker = new IntervalMarker(startVal, endVal);

    marker.setLabelOffsetType(LengthAdjustmentType.EXPAND);
    marker.setPaint(new Color(134, 254, 225));
    marker.setAlpha((float) 0.60);
    marker.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
    marker.setLabelPaint(Color.pink);
    marker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
    marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);

    marker.setPaint(Color.pink);

    plot.addDomainMarker(marker, Layer.BACKGROUND);
  }

  /**
   * Plot the timeseries at the panel.
   * 
   * @param tsData The time series data.
   */
  public void showTimeSeries(double[] tsData) {
    paintTheChart(tsData);
    ChartPanel chartPanel = new ChartPanel(this.chart);
    chartPanel.setMinimumDrawWidth(0);
    chartPanel.setMinimumDrawHeight(0);
    chartPanel.setMaximumDrawWidth(1920);
    chartPanel.setMaximumDrawHeight(1200);
    this.removeAll();
    this.add(chartPanel);
    validate();
    repaint();
  }

  /**
   * create the chart for the original time series
   * 
   * @param tsData the data to plot.
   * 
   * @return a JFreeChart object of the chart
   */
  private void paintTheChart(double[] tsData) {

    // making the data
    //
    XYSeries dataset = new XYSeries("Series");
    for (int i = 0; i < tsData.length; i++) {
      dataset.add(i, (float) tsData[i]);
    }
    chartXYSeriesCollection = new XYSeriesCollection(dataset);

    // set the renderer
    //
    XYLineAndShapeRenderer xyRenderer = new XYLineAndShapeRenderer(true, false);
    xyRenderer.setSeriesPaint(0, new Color(0, 0, 0));
    xyRenderer.setBaseStroke(new BasicStroke(3));

    // X - the time axis
    //
    NumberAxis timeAxis = new NumberAxis("Time. (zoom: select with mouse; panning: Ctrl+mouse)");

    // Y axis
    //
    NumberAxis valueAxis = new NumberAxis("Values");
    valueAxis.setAutoRangeIncludesZero(false);

    // put these into collection of dots
    //
    this.timeseriesPlot = new XYPlot(chartXYSeriesCollection, timeAxis, valueAxis, xyRenderer);
    
    // enabling panning
    //
    this.timeseriesPlot.setDomainPannable(true);
    this.timeseriesPlot.setRangePannable(true);

    // finally, create the chart
    this.chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, timeseriesPlot, false);

    // set the progress listener to react to mouse clicks in the chart
    this.chart.addProgressListener(this);

  }

  public void chartProgress(ChartProgressEvent chartprogressevent) {
    if (chartprogressevent.getType() != 2)
      return;

    XYPlot xyplot = (XYPlot) chart.getPlot();

    double pos = xyplot.getDomainCrosshairValue();

    // this is needed because the call of highlightPatternInChart triggers a ChartProgessEvent
    if (previousClickPosition == pos)
      return;

    // SAXString sax = new SAXString(chartData.getFreqData(), " ");
    // String rule = sax.getRuleFromPosition(chartData, (int) pos);
    // if (rule != null) {
    // firePropertyChange(SequiturMessage.MAIN_CHART_CLICKED_MESSAGE, "", rule);
    // System.out.println("Clicked Property Change fired with rule: " + rule);
    // }

    previousClickPosition = pos;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (GrammarvizRulesPanel.FIRING_PROPERTY.equalsIgnoreCase(evt.getPropertyName())) {
      @SuppressWarnings("unchecked")
      ArrayList<String> newlySelectedRaws = (ArrayList<String>) evt.getNewValue();
      highlightPatternInChart(newlySelectedRaws);
      TitledBorder tb = (TitledBorder) this.getBorder();
      tb.setTitle(LABEL_SHOWING_RULES);
      this.repaint();
    }
    if (PackedRulesPanel.FIRING_PROPERTY_PACKED.equalsIgnoreCase(evt.getPropertyName())) {
      String newlySelectedRaw = (String) evt.getNewValue();
      highlightPatternInChartPacked(newlySelectedRaw);
      TitledBorder tb = (TitledBorder) this.getBorder();
      tb.setTitle(LABEL_SHOWING_PACKED_RULES);
      this.repaint();
    }
    if (RulesPeriodicityPanel.FIRING_PROPERTY_PERIOD.equalsIgnoreCase(evt.getPropertyName())) {
      String newlySelectedRaw = (String) evt.getNewValue();
      highlightPeriodsBetweenPatterns(newlySelectedRaw);
      TitledBorder tb = (TitledBorder) this.getBorder();
      tb.setTitle(LABEL_SHOWING_PERIODS);
      this.repaint();
    }
    if (GrammarVizAnomaliesPanel.FIRING_PROPERTY_ANOMALY.equalsIgnoreCase(evt.getPropertyName())) {
      @SuppressWarnings("unchecked")
      ArrayList<String> newlySelectedRaws = (ArrayList<String>) evt.getNewValue();
      highlightAnomaly(newlySelectedRaws);
      TitledBorder tb = (TitledBorder) this.getBorder();
      tb.setTitle(LABEL_SHOWING_ANOMALY);
      this.repaint();
    }

  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (GrammarVizView.DISPLAY_DENSITY_DATA.equalsIgnoreCase(e.getActionCommand())) {
      TitledBorder tb = (TitledBorder) this.getBorder();
      tb.setTitle(LABEL_SHOWING_DENSITY + "coverage count strategy: "
          + this.session.getCountStrategy().toString() + " ");
      this.repaint();
      displayRuleDensity();
    }
    else if (GrammarVizView.DISPLAY_LENGTH_HISTOGRAM.equalsIgnoreCase(e.getActionCommand())) {
      TitledBorder tb = (TitledBorder) this.getBorder();
      tb.setTitle(LABEL_SHOWING_HISTOGRAMM);
      this.repaint();
      displayRulesLengthHistogram();
    }
    else if (GrammarVizView.SAVE_CHART.equalsIgnoreCase(e.getActionCommand())) {
      TitledBorder tb = (TitledBorder) this.getBorder();
      tb.setTitle(LABEL_SAVING_CHART);
      this.repaint();
      saveCurrentChart();
    }

  }

  private void saveCurrentChart() {
    String fileName = new SimpleDateFormat("yyyyMMddhhmmssSS'.png'").format(new Date());
    try {

      NumberAxis domain = (NumberAxis) this.timeseriesPlot.getDomainAxis();
      Range domainRange = domain.getRange();

      NumberAxis range = (NumberAxis) this.timeseriesPlot.getRangeAxis();
      Range rangeRange = range.getRange();

      String annotationString = "W:" + this.chartData.getSAXWindowSize() + ", P:"
          + this.chartData.getSAXPaaSize() + ", A:" + this.chartData.getSAXAlphabetSize();

      XYTextAnnotation a = new XYTextAnnotation(annotationString, domainRange.getLowerBound()
          + domainRange.getLength() / 100, rangeRange.getLowerBound() + rangeRange.getLength() / 5
          * 3.5);

      a.setTextAnchor(TextAnchor.BOTTOM_LEFT);

      a.setPaint(Color.RED);
      a.setOutlinePaint(Color.BLACK);
      a.setOutlineVisible(true);

      a.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));

      // XYPointerAnnotation a = new XYPointerAnnotation("Bam!", domainRange.getLowerBound()
      // + domainRange.getLength() / 10, rangeRange.getLowerBound() + rangeRange.getLength() / 5
      // * 4, 5 * Math.PI / 8);

      this.timeseriesPlot.addAnnotation(a);

      // this.paintTheChart();

      ChartUtilities.saveChartAsPNG(new File(fileName), this.chart, 1400, 425);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

}
