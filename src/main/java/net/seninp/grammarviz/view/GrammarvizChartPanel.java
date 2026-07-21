package net.seninp.grammarviz.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
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
import org.jfree.chart.ui.Layer;
import org.jfree.chart.ui.LengthAdjustmentType;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.seninp.gi.logic.GrammarRuleRecord;
import net.seninp.gi.logic.RuleInterval;
import net.seninp.gi.GIAlgorithm;
import net.seninp.gi.rulepruner.SampledPoint;
import net.seninp.grammarviz.logic.CoverageCountStrategy;
import net.seninp.grammarviz.session.UserSession;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.discord.DiscordRecord;

/**
 * 
 * Handles the chart panel and listens for events from Sequitur rules table.
 * 
 * @author Manfred Lerner, seninp
 * 
 */
public class GrammarvizChartPanel extends JPanel
    implements PropertyChangeListener, ChartProgressListener, ActionListener {

  /** Fancy serial. */
  private static final long serialVersionUID = -2710973854572981568L;

  // various display string constants
  //
  private static final String LABEL_DEFAULT = " Data display ";
  private static final String LABEL_SHOWING_RULES = " Data display: showing rule subsequences ";
  private static final String LABEL_SHOWING_HISTOGRAMM = " Data display: showing rules length histogramm ";
  private static final String LABEL_SHOWING_DENSITY = " Data display: showing grammar rules density, ";
  private static final String LABEL_SHOWING_PACKED_RULES = " Data display: showing packed rule subsequences ";
  private static final String LABEL_SHOWING_PERIODS = " Data display: showing periods between selected rules ";
  private static final String LABEL_SHOWING_ANOMALY = " Data display: showing anomaly ";
  private static final String LABEL_SAVING_CHART = " Data display: saving chart ";

  private static final String LABEL_SELECT_INTERVAL = " Select the timeseries interval for guessing ";

  public static final String SELECTION_CANCELLED = "interval_selection_cancelled";

  public static final String SAMPLING_SUCCEEDED = "parameters_sampling_succeeded";

  /** Best parameters were applied, but none reached the user's minimal-cover threshold. */
  public static final String SAMPLING_BELOW_THRESHOLD = "parameters_sampling_below_threshold";

  /** The sampler produced no usable parameters (empty grid or error). */
  public static final String SAMPLING_FAILED = "parameters_sampling_failed";

  public static final String SELECTION_FINISHED = "interval_selection_finished";

  private enum GuessPhase {
    IDLE, SELECTING, SAMPLING
  }

  static final class GuessSamplingContext {
    final double[] tsSlice;
    final int samplingStart;
    final int samplingEnd;
    final int[] boundaries;
    final GIAlgorithm giAlgorithm;
    final NumerosityReductionStrategy nrStrategy;
    final double normalizationThreshold;
    final double minimalCoverThreshold;

    GuessSamplingContext(double[] tsSlice, int samplingStart, int samplingEnd, int[] boundaries,
        GIAlgorithm giAlgorithm, NumerosityReductionStrategy nrStrategy,
        double normalizationThreshold, double minimalCoverThreshold) {
      this.tsSlice = tsSlice;
      this.samplingStart = samplingStart;
      this.samplingEnd = samplingEnd;
      this.boundaries = boundaries;
      this.giAlgorithm = giAlgorithm;
      this.nrStrategy = nrStrategy;
      this.normalizationThreshold = normalizationThreshold;
      this.minimalCoverThreshold = minimalCoverThreshold;
    }
  }

  /** Current chart data instance. */
  protected double[] tsData;

  /** The chart container. */
  private JFreeChart chart;

  /** The timeseries plot itself. */
  private XYPlot timeseriesPlot;

  /** Stable references to the time-series chart, so save/export always targets the series
   * view even when the histogram has temporarily replaced {@link #chart}. */
  private JFreeChart seriesChart;
  private XYPlot seriesPlot;

  /** JFreeChart Object holding the chart times series */
  XYSeriesCollection chartXYSeriesCollection;

  /** Position of the previous mouse click in the chart */
  double previousClickPosition = 0;

  /** The user session var - holds all parameters. */
  protected UserSession session;

  /** The inner panel which displays the chart in place. */
  private ChartPanel chartPanel;

  private Thread guessRefreshThread;

  private volatile GuessPhase guessPhase = GuessPhase.IDLE;

  private final AtomicLong guessSessionId = new AtomicLong(0);

  private MouseMarker activeMouseMarker;

  private ChartPanel markerHostPanel;

  private ExecutorService samplerExecutor;

  private Future<String> samplerFuture;

  private GuessSamplingContext activeSamplingContext;

  private final ActionListener guessStopListener = new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
      cancelActiveSampling();
    }
  };

  private JButton setOperationalButton;

  private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();

  // static block - we instantiate the logger
  //
  private static final Logger LOGGER = LoggerFactory.getLogger(GrammarRulesPanel.class);

  /**
   * Constructor.
   */
  public GrammarvizChartPanel() {
    super();
  }

  /**
   * This sets the chartData and forces the panel to repaint itself showing the new chart.
   * 
   * @param session the user session.
   */
  public void setSession(UserSession session) {
    this.session = session;
    this.resetChartPanel();
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

    // Marker detach happens only in finishGuessSessionTerminal, not here (other features call reset).

    // this is the new "insert" - elastic boundaries chart panel
    //
    if (null == this.session.chartData && null != this.tsData) {
      paintTheChart(this.tsData);
    }
    else {
      paintTheChart(this.session.chartData.getOriginalTimeseries());
    }

    // reset the border label
    //
    TitledBorder tb = (TitledBorder) this.getBorder();
    tb.setTitle(LABEL_DEFAULT);

    // instantiate and adjust the chart panel
    //
    chartPanel = new ChartPanel(this.chart);

    chartPanel.setMaximumDrawHeight(this.getParent().getHeight());
    chartPanel.setMaximumDrawWidth(this.getParent().getWidth());
    chartPanel.setMinimumDrawWidth(0);
    chartPanel.setMinimumDrawHeight(0);

    chartPanel.setMouseWheelEnabled(true);

    // cleanup all the content
    //
    this.removeAll();

    // put the chart on show
    //
    this.add(chartPanel);

    // make sure all is set
    //
    this.revalidate();
  }

  /**
   * Highlights the subsequence of the rule.
   * 
   * @param The rule index.
   */
  private void highlightPatternInChart(ArrayList<String> rules) {
    LOGGER.debug("Selected rules: " + rules.toString());
    timeseriesPlot.clearDomainMarkers();
    for (String rule : rules) {
      int ruleId = Integer.valueOf(rule);
      if (0 == ruleId) {
        continue;
      }
      ArrayList<RuleInterval> arrPos = this.session.chartData.getRulePositionsByRuleNum(ruleId);
      LOGGER.debug("Size: " + arrPos.size() + " - Positions: " + arrPos);
      for (RuleInterval saxPos : arrPos) {
        addMarker(timeseriesPlot, saxPos.getStart(), saxPos.getEnd());
      }
    }
  }

  /**
   * Highlights the subsequence of the rule.
   * 
   * @param The rule index.
   */
  private void highlightPatternInChartPacked(ArrayList<String> rules) {
    LOGGER.debug("Selected class: " + rules.toString());
    timeseriesPlot.clearDomainMarkers();
    for (String rule : rules) {
      int ruleId = Integer.valueOf(rule);
      // if (0 == ruleId) {
      // continue;
      // }
      ArrayList<RuleInterval> arrPos = this.session.chartData
          .getSubsequencesPositionsByClassNum(Integer.valueOf(ruleId));
      LOGGER.debug("Size: " + arrPos.size() + " - Positions: " + arrPos);
      for (RuleInterval saxPos : arrPos) {
        addMarker(timeseriesPlot, saxPos.getStart(), saxPos.getEnd());
      }
    }
  }

  /**
   * Highlights intervals in between selected rule subsequences - ones which suppose to be periods.
   * 
   * @param rule The rule whose subsequences will be period boundaries.
   */
  private void highlightPeriodsBetweenPatterns(String rule) {
    LOGGER.debug("Selected rule: " + rule);
    ArrayList<RuleInterval> arrPos = this.session.chartData
        .getRulePositionsByRuleNum(Integer.valueOf(rule));
    LOGGER.debug("Size: " + arrPos.size() + " - Positions: " + arrPos);
    timeseriesPlot.clearDomainMarkers();
    for (int i = 1; i < arrPos.size(); i++) {
      RuleInterval c = arrPos.get(i - 1);
      RuleInterval p = arrPos.get(i);
      addPeriodMarker(timeseriesPlot, c.getEnd(), p.getStart());
    }
  }

  private void highlightAnomaly(ArrayList<String> anomalies) {
    LOGGER.debug("Selected anomalies: " + anomalies.toString());
    timeseriesPlot.clearDomainMarkers();
    for (String anomaly : anomalies) {
      DiscordRecord dr = this.session.chartData.getAnomalies().get(Integer.valueOf(anomaly));
      LOGGER.debug(dr.toString());
      addAnomalyMarker(timeseriesPlot, dr.getPosition(), dr.getPosition() + dr.getLength());
    }
  }

  /**
   * Puts rules density on show.
   */
  private void displayRuleDensity() {

    // this is the new "insert" - elastic boundaries chart panel
    //
    // paintTheChart(this.session.chartData.getOriginalTimeseries());
    // chartPanel = new ChartPanel(this.chart);
    // chartPanel.setMaximumDrawHeight(this.getParent().getHeight());
    // chartPanel.setMaximumDrawWidth(this.getParent().getWidth());
    // chartPanel.setMinimumDrawWidth(0);
    // chartPanel.setMinimumDrawHeight(0);
    // chartPanel.revalidate();
    //
    this.removeAll();
    this.add(chartPanel);

    // init vars
    //
    int maxObservedCoverage = Integer.MIN_VALUE;
    int minObservedCoverage = Integer.MAX_VALUE;
    int[] coverageArray = new int[this.session.chartData.getOriginalTimeseries().length];

    for (GrammarRuleRecord r : this.session.chartData.getGrammarRules()) {

      if (0 == r.ruleNumber()) { // skip R0
        continue;
      }

      ArrayList<RuleInterval> occurrences = this.session.chartData
          .getRulePositionsByRuleNum(r.ruleNumber());

      for (RuleInterval i : occurrences) {

        int start = i.getStart();
        int end = i.getEnd();

        for (int j = start; j < end; j++) {
          if (CoverageCountStrategy.COUNT.equals(this.session.countStrategy)) {
            coverageArray[j] = coverageArray[j] + 1;
          }
          else if (CoverageCountStrategy.LEVEL.equals(this.session.countStrategy)) {
            coverageArray[j] = coverageArray[j] + r.getRuleLevel();
          }
          else if (CoverageCountStrategy.OCCURRENCE.equals(this.session.countStrategy)) {
            coverageArray[j] = coverageArray[j] + r.getOccurrences().size();
          }
          else if (CoverageCountStrategy.YIELD.equals(this.session.countStrategy)) {
            coverageArray[j] = coverageArray[j] + r.getRuleYield();
          }
          else if (CoverageCountStrategy.PRODUCT.equals(this.session.countStrategy)) {
            coverageArray[j] = coverageArray[j] + r.getRuleLevel() * r.getOccurrences().size();
          }

          if (maxObservedCoverage < coverageArray[j]) {
            maxObservedCoverage = coverageArray[j];
          }

          if (minObservedCoverage > coverageArray[j]) {
            minObservedCoverage = coverageArray[j];
          }

        }
      }
    }

    // since we know the maximal coverage value, we can compute the increment for a single coverage
    // interval

    // guard against a degenerate/empty coverage array: if no rule contributed,
    // maxObservedCoverage stays Integer.MIN_VALUE and 1.0 / max would yield a
    // bogus (near-zero, negative) alpha -- nothing meaningful to render. Clear any
    // stale density overlay and tell the user, rather than leaving the chart looking
    // unchanged (which reads like a no-op / bug).
    if (maxObservedCoverage <= 0) {
      this.timeseriesPlot.clearDomainMarkers();
      this.timeseriesPlot.clearAnnotations();
      revalidate();
      repaint();
      LOGGER.info("no rule coverage to display (empty or R0-only grammar)");
      SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
          "No rule coverage to display.\n"
              + "The current grammar has no rules beyond R0 -- try different SAX parameters.",
          "Nothing to show", JOptionPane.INFORMATION_MESSAGE));
      return;
    }
    double covIncrement = 1.0 / (double) maxObservedCoverage;

    for (GrammarRuleRecord r : this.session.chartData.getGrammarRules()) {
      if (0 == r.ruleNumber()) { // skip the R0
        continue;
      }

      ArrayList<RuleInterval> occurrences = r.getRuleIntervals();

      for (RuleInterval i : occurrences) {
        IntervalMarker marker = new IntervalMarker(i.getStart(), i.getEnd());
        marker.setLabelOffsetType(LengthAdjustmentType.EXPAND);
        marker.setPaint(Color.BLUE);

        // marker.setAlpha((float) 0.05);
        if (CoverageCountStrategy.COUNT.equals(this.session.countStrategy)) {
          marker.setAlpha((float) covIncrement);
        }
        else if (CoverageCountStrategy.LEVEL.equals(this.session.countStrategy)) {
          marker.setAlpha((float) covIncrement * r.getRuleLevel());
        }
        else if (CoverageCountStrategy.OCCURRENCE.equals(this.session.countStrategy)) {
          marker.setAlpha((float) covIncrement * r.getOccurrences().size());
        }
        else if (CoverageCountStrategy.YIELD.equals(this.session.countStrategy)) {
          marker.setAlpha((float) covIncrement * r.getRuleYield());
        }
        else if (CoverageCountStrategy.PRODUCT.equals(this.session.countStrategy)) {
          marker.setAlpha((float) covIncrement * (r.getRuleLevel() * r.getOccurrences().size()));
        }

        marker.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        marker.setLabelPaint(Color.green);
        marker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
        marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        timeseriesPlot.addDomainMarker(marker, Layer.BACKGROUND);

      }
    }

    int sum = 0;
    for (int d : coverageArray)
      sum += d;
    double meanCoverage = 1.0d * sum / coverageArray.length;

    DecimalFormat df = new DecimalFormat("#.00");
    String annotationString = "min C:" + minObservedCoverage + ", max C:" + maxObservedCoverage
        + ", mean C:" + df.format(meanCoverage);

    NumberAxis domain = (NumberAxis) this.timeseriesPlot.getDomainAxis();
    Range domainRange = domain.getRange();

    NumberAxis range = (NumberAxis) this.timeseriesPlot.getRangeAxis();
    Range rangeRange = range.getRange();

    XYTextAnnotation a = new XYTextAnnotation(annotationString,
        domainRange.getLowerBound() + domainRange.getLength() / 100,
        rangeRange.getLowerBound() + 0.5);

    a.setTextAnchor(TextAnchor.BOTTOM_LEFT);

    a.setPaint(Color.RED);
    a.setOutlinePaint(Color.BLACK);
    a.setOutlineVisible(true);

    a.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));

    this.timeseriesPlot.addAnnotation(a);

    // not sure if I need this
    //
    revalidate();
    repaint();

    // and finally save the coverage curve
    //
    this.saveRuleDensityCurve(coverageArray);

  }

  private void saveRuleDensityCurve(int[] coverageArray) {
    final int[] snapshot = Arrays.copyOf(coverageArray, coverageArray.length);
    final String filename = session.ruleDensityOutputFileName;
    Thread writer = new Thread(() -> {
      try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filename)))) {
        for (int c : snapshot) {
          bw.write(String.valueOf(c) + "\n");
        }
        LOGGER.info("rule coverage file written: " + filename);
      }
      catch (IOException e) {
        LOGGER.error("error while writing the rule coverage file", e);
      }
    }, "rule-density-writer");
    writer.setDaemon(true);
    writer.start();
  }

  private void displayRulesLengthHistogram() {

    // cleanup all the content
    //
    this.removeAll();
    revalidate();
    repaint();

    // construct the dataset
    //

    // [1.0] extract all the rules
    ArrayList<Integer> allRules = new ArrayList<Integer>();
    for (GrammarRuleRecord r : this.session.chartData.getGrammarRules()) {
      if (0 == r.ruleNumber()) {
        continue;
      }
      for (RuleInterval interval : r.getRuleIntervals()) {
        allRules.add(interval.getLength());
      }
    }

    if (allRules.isEmpty()) {
      TitledBorder tb = (TitledBorder) this.getBorder();
      tb.setTitle(LABEL_DEFAULT);
      revalidate();
      repaint();
      LOGGER.info("no rules to histogram (empty or R0-only grammar)");
      SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
          "No rules to histogram.\n"
              + "The current grammar has no rules beyond R0 -- try different SAX parameters.",
          "Nothing to show", JOptionPane.INFORMATION_MESSAGE));
      return;
    }

    // [2.0] make data
    Collections.sort(allRules);
    // final int minLength = allRules.get(0);
    final int maxLength = allRules.get(allRules.size() - 1);
    final int numberOfBins = maxLength / this.session.chartData.getSAXWindowSize() + 1;

    double[] values = new double[allRules.size()];
    for (int i = 0; i < allRules.size(); i++) {
      values[i] = allRules.get(i).doubleValue();
    }

    HistogramDataset dataset = new HistogramDataset();
    dataset.setType(HistogramType.FREQUENCY);

    dataset.addSeries("Frequencies", values, numberOfBins, 0,
        numberOfBins * this.session.chartData.getSAXWindowSize());

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
          myTicks.add(new NumberTick(TickType.MAJOR, i * session.chartData.getSAXWindowSize(),
              String.valueOf(i * session.chartData.getSAXWindowSize()), TextAnchor.CENTER,
              TextAnchor.CENTER, 0.0d));
          // textAnchor, rotationAnchor, angle));
        }

        // for (Object tick : allTicks) {
        // NumberTick numberTick = (NumberTick) tick;
        //
        // if (TickType.MAJOR.equals(numberTick.getTickType())
        // && (numberTick.getValue() % this.session.chartData.getSAXWindowSize() != 0)) {
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

    chartPanel = new ChartPanel(this.chart);
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

    revalidate();
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
    this.tsData = tsData;
    paintTheChart(tsData);
    chartPanel = new ChartPanel(this.chart);
    this.removeAll();
    this.add(chartPanel);
    revalidate();
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
    xyRenderer.setDefaultStroke(new BasicStroke(3));

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
    this.chart.setNotify(true);

    this.seriesChart = this.chart;
    this.seriesPlot = this.timeseriesPlot;

  }

  public void chartProgress(ChartProgressEvent chartprogressevent) {

    if (chartprogressevent.getType() != 2)
      return;

    XYPlot xyplot = (XYPlot) chart.getPlot();

    double pos = xyplot.getDomainCrosshairValue();

    // this is needed because the call of highlightPatternInChart triggers a ChartProgessEvent
    if (previousClickPosition == pos) {
      return;
    }

    // SAXString sax = new SAXString(this.session.chartData.getFreqData(), " ");
    // String rule = sax.getRuleFromPosition(this.session.chartData, (int) pos);
    // if (rule != null) {
    // firePropertyChange(SequiturMessage.MAIN_CHART_CLICKED_MESSAGE, "", rule);
    // System.out.println("Clicked Property Change fired with rule: " + rule);
    // }

    previousClickPosition = pos;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (GrammarRulesPanel.FIRING_PROPERTY.equalsIgnoreCase(evt.getPropertyName())) {
      @SuppressWarnings("unchecked")
      ArrayList<String> newlySelectedRows = (ArrayList<String>) evt.getNewValue();
      highlightPatternInChart(newlySelectedRows);
      TitledBorder tb = (TitledBorder) this.getBorder();
      tb.setTitle(LABEL_SHOWING_RULES);
      revalidate();
      repaint();
    }
    if (PackedRulesPanel.FIRING_PROPERTY_PACKED.equalsIgnoreCase(evt.getPropertyName())) {
      @SuppressWarnings("unchecked")
      ArrayList<String> newlySelectedRows = (ArrayList<String>) evt.getNewValue();
      // String newlySelectedRaw = (String) evt.getNewValue();
      highlightPatternInChartPacked(newlySelectedRows);
      TitledBorder tb = (TitledBorder) this.getBorder();
      tb.setTitle(LABEL_SHOWING_PACKED_RULES);
      revalidate();
      repaint();
    }
    if (RulesPeriodicityPanel.FIRING_PROPERTY_PERIOD.equalsIgnoreCase(evt.getPropertyName())) {
      String newlySelectedRaw = (String) evt.getNewValue();
      highlightPeriodsBetweenPatterns(newlySelectedRaw);
      TitledBorder tb = (TitledBorder) this.getBorder();
      tb.setTitle(LABEL_SHOWING_PERIODS);
      revalidate();
      repaint();
    }
    if (GrammarVizAnomaliesPanel.FIRING_PROPERTY_ANOMALY.equalsIgnoreCase(evt.getPropertyName())) {
      @SuppressWarnings("unchecked")
      ArrayList<String> newlySelectedRaws = (ArrayList<String>) evt.getNewValue();
      highlightAnomaly(newlySelectedRaws);
      TitledBorder tb = (TitledBorder) this.getBorder();
      tb.setTitle(LABEL_SHOWING_ANOMALY);
      revalidate();
      repaint();
    }

  }

  boolean isGuessActive() {
    return guessPhase != GuessPhase.IDLE;
  }

  boolean tryBeginGuessSession() {
    if (guessPhase != GuessPhase.IDLE) {
      LOGGER.info("guess already active, ignoring");
      return false;
    }
    guessSessionId.incrementAndGet();
    guessPhase = GuessPhase.SELECTING;
    return true;
  }

  void attachMouseMarker() {
    activeMouseMarker = new MouseMarker(chartPanel);
    markerHostPanel = chartPanel;
    markerHostPanel.addMouseListener(activeMouseMarker);
    markerHostPanel.addMouseMotionListener(activeMouseMarker);
  }

  void detachMouseMarker() {
    if (activeMouseMarker != null && markerHostPanel != null) {
      markerHostPanel.removeMouseListener(activeMouseMarker);
      markerHostPanel.removeMouseMotionListener(activeMouseMarker);
    }
    activeMouseMarker = null;
    markerHostPanel = null;
  }

  GuessSamplingContext captureSamplingContext() {
    double[] source = (null != session.chartData)
        ? session.chartData.getOriginalTimeseries()
        : tsData;
    double[] tsSlice = Arrays.copyOfRange(source, session.samplingStart, session.samplingEnd);
    int[] boundaries = Arrays.copyOf(session.boundaries, 9);
    return new GuessSamplingContext(tsSlice, session.samplingStart, session.samplingEnd,
        boundaries, session.giAlgorithm, session.numerosityReductionStrategy,
        session.normalizationThreshold, session.minimalCoverThreshold);
  }

  void dispatchGuessEvent(String actionCommand, long sessionId) {
    SwingUtilities.invokeLater(() -> {
      if (sessionId != guessSessionId.get()) {
        return;
      }
      actionPerformed(new ActionEvent(this, 0, actionCommand));
    });
  }

  void applySamplingResult(long sessionId, SampledPoint best, boolean interrupted) {
    SwingUtilities.invokeLater(() -> {
      if (sessionId != guessSessionId.get()) {
        return;
      }
      session.saxWindow = best.getWindow();
      session.saxPAA = best.getPAA();
      session.saxAlphabet = best.getAlphabet();
      String event = best.getCoverage() >= activeSamplingContext.minimalCoverThreshold
          ? SAMPLING_SUCCEEDED
          : SAMPLING_BELOW_THRESHOLD;
      if (interrupted) {
        LOGGER.info("interrupted; applied best parameters from partial sampling");
      }
      finishGuessSessionTerminal(event);
    });
  }

  void finishGuessSessionTerminal(String terminalEvent) {
    // idempotent: once a session has been finalized (phase back to IDLE) any later/duplicate
    // terminal event for it is a no-op, so a session can never be torn down twice
    if (guessPhase == GuessPhase.IDLE) {
      return;
    }
    detachMouseMarker();

    if (guessPhase == GuessPhase.SAMPLING) {
      setOperationalButton.removeActionListener(guessStopListener);
      setOperationalButton.setText("Guess");
    }

    if (SELECTION_CANCELLED.equalsIgnoreCase(terminalEvent)
        || SAMPLING_FAILED.equalsIgnoreCase(terminalEvent)) {
      resetChartPanel();
    }
    else if (SAMPLING_SUCCEEDED.equalsIgnoreCase(terminalEvent)
        || SAMPLING_BELOW_THRESHOLD.equalsIgnoreCase(terminalEvent)) {
      resetChartPanel();
      session.notifyParametersChangeListeners();
    }

    if (SAMPLING_BELOW_THRESHOLD.equalsIgnoreCase(terminalEvent)) {
      final double cover = activeSamplingContext.minimalCoverThreshold;
      JOptionPane.showMessageDialog(this,
          "No parameter set reached the minimal rule cover threshold (" + cover + ").\n"
              + "Applied the best available parameters (window " + session.saxWindow + ", PAA "
              + session.saxPAA + ", alphabet " + session.saxAlphabet + ") instead.",
          "Cover threshold not reached", JOptionPane.INFORMATION_MESSAGE);
    }
    else if (SAMPLING_FAILED.equalsIgnoreCase(terminalEvent)) {
      JOptionPane.showMessageDialog(this,
          "No valid SAX parameters were found for the selected interval and ranges.\n"
              + "Try a longer interval or wider window/PAA/alphabet ranges.",
          "Parameter guessing failed", JOptionPane.WARNING_MESSAGE);
    }

    ActionEvent event = new ActionEvent(this, 0, GrammarVizView.RESET_GUESS_BUTTON_LISTENER);
    for (ActionListener l : this.listeners) {
      l.actionPerformed(event);
    }

    guessPhase = GuessPhase.IDLE;
    activeSamplingContext = null;
    guessRefreshThread = null;
    samplerExecutor = null;
    samplerFuture = null;
  }

  void cancelActiveSampling() {
    if (guessPhase != GuessPhase.SAMPLING) {
      return;
    }
    if (samplerFuture != null) {
      samplerFuture.cancel(true);
    }
    if (samplerExecutor != null) {
      samplerExecutor.shutdownNow();
    }
    if (guessRefreshThread != null) {
      guessRefreshThread.interrupt();
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {

    if (GrammarVizView.DISPLAY_DENSITY_DATA.equalsIgnoreCase(e.getActionCommand())) {
      TitledBorder tb = (TitledBorder) this.getBorder();
      tb.setTitle(LABEL_SHOWING_DENSITY + "coverage count strategy: "
          + this.session.countStrategy.toString() + " ");
      revalidate();
      repaint();
      displayRuleDensity();
    }

    else if (GrammarVizView.DISPLAY_LENGTH_HISTOGRAM.equalsIgnoreCase(e.getActionCommand())) {
      TitledBorder tb = (TitledBorder) this.getBorder();
      tb.setTitle(LABEL_SHOWING_HISTOGRAMM);
      revalidate();
      displayRulesLengthHistogram();
    }

    else if (GrammarVizView.SAVE_CHART.equalsIgnoreCase(e.getActionCommand())) {
      TitledBorder tb = (TitledBorder) this.getBorder();
      tb.setTitle(LABEL_SAVING_CHART);
      revalidate();
      repaint();
      saveCurrentChart();
    }
    // GUESS PARAMETERS procedure
    //
    else if (GrammarVizView.GUESS_PARAMETERS.equalsIgnoreCase(e.getActionCommand())) {

      if (!tryBeginGuessSession()) {
        return;
      }

      LOGGER.info("Starting the sampling dialog...");

      // re-draw the plot, so selection wouldn't get weird...
      //
      this.resetChartPanel();

      // setting the new label on the chart-surrounding panel
      //
      TitledBorder tb = (TitledBorder) this.getBorder();
      tb.setTitle(LABEL_SELECT_INTERVAL);

      // clear whatever markers are on the chart panel
      //
      timeseriesPlot.clearDomainMarkers();

      // disabling zoom on the panel
      //
      chartPanel.setRangeZoomable(false);
      chartPanel.setDomainZoomable(false);

      JOptionPane.showMessageDialog(this,
          "Select the sampling range (preferrably the normal signal)\n"
              + "by dragging the mouse pointer from left to right.",
          null, JOptionPane.WARNING_MESSAGE);

      attachMouseMarker();

      final long sessionId = guessSessionId.get();
      final Object selectionLock = new Object();
      final JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
      activeMouseMarker.setLockObject(selectionLock);

      guessRefreshThread = new Thread(new Runnable() {
        public void run() {
          synchronized (selectionLock) {
            while (!activeMouseMarker.isSelectionReleased()) {
              try {
                selectionLock.wait();
              }
              catch (InterruptedException e1) {
                Thread.currentThread().interrupt();
                // no sampler exists yet, so nothing else will recover the UI -- end the session
                dispatchGuessEvent(SELECTION_CANCELLED, sessionId);
                return;
              }
            }
            activeMouseMarker.clearSelectionReleased();
          }

          final boolean[] cancelled = { false };
          try {
            SwingUtilities.invokeAndWait(() -> {
              int tsLength = (null != session.chartData)
                  ? session.chartData.getOriginalTimeseries().length
                  : tsData.length;
              int selStart = (int) Math.floor(activeMouseMarker.getSelectionStart());
              int selEnd = (int) Math.ceil(activeMouseMarker.getSelectionEnd());
              session.samplingStart = Math.max(0, Math.min(selStart, tsLength));
              session.samplingEnd = Math.max(0, Math.min(selEnd, tsLength));

              GrammarvizGuesserPane parametersPanel = new GrammarvizGuesserPane(session);
              GrammarvizGuesserDialog parametersDialog = new GrammarvizGuesserDialog(topFrame,
                  parametersPanel, session);
              parametersDialog.wasCancelled = false;
              parametersDialog.setVisible(true);
              cancelled[0] = parametersDialog.wasCancelled;
            });
          }
          catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
            // no sampler exists yet, so nothing else will recover the UI -- end the session
            dispatchGuessEvent(SELECTION_CANCELLED, sessionId);
            return;
          }
          catch (InvocationTargetException e1) {
            LOGGER.error("error showing the guesser dialog", e1);
            dispatchGuessEvent(SAMPLING_FAILED, sessionId);
            return;
          }

          if (cancelled[0]) {
            LOGGER.info("Selection process has been cancelled...");
            dispatchGuessEvent(SELECTION_CANCELLED, sessionId);
            return;
          }

          final GuessSamplingContext[] ctxHolder = new GuessSamplingContext[1];
          try {
            SwingUtilities.invokeAndWait(() -> {
              ctxHolder[0] = captureSamplingContext();
            });
          }
          catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
            // sampler not submitted yet, so nothing else will recover the UI -- end the session
            dispatchGuessEvent(SELECTION_CANCELLED, sessionId);
            return;
          }
          catch (InvocationTargetException e1) {
            LOGGER.error("error capturing sampling context", e1);
            dispatchGuessEvent(SAMPLING_FAILED, sessionId);
            return;
          }

          activeSamplingContext = ctxHolder[0];
          guessPhase = GuessPhase.SAMPLING;

          samplerExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "params-sampler");
            t.setDaemon(true);
            return t;
          });

          GrammarvizParamsSampler sampler = new GrammarvizParamsSampler(
              GrammarvizChartPanel.this, ctxHolder[0], sessionId);
          samplerFuture = samplerExecutor.submit(sampler);

          SwingUtilities.invokeLater(() -> {
            // ignore if this session was superseded, or if a fast sampler already drove the
            // terminal handler (which restored the "Guess" label) before this runnable ran --
            // otherwise we would re-apply "Stop!" and re-attach the listener on a finished session
            if (sessionId != guessSessionId.get() || guessPhase != GuessPhase.SAMPLING) {
              return;
            }
            setOperationalButton.setText("Stop!");
            setOperationalButton.removeActionListener(guessStopListener);
            setOperationalButton.addActionListener(guessStopListener);
            setOperationalButton.revalidate();
            setOperationalButton.repaint();
          });

          samplerExecutor.shutdown();
          try {
            if (!samplerExecutor.awaitTermination(10, TimeUnit.MINUTES)) {
              samplerExecutor.shutdownNow();
              if (!samplerExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                LOGGER.error("executor pool did not terminate");
              }
            }
          }
          catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
            return;
          }
          finally {
            if (samplerExecutor != null && !samplerExecutor.isTerminated()) {
              samplerExecutor.shutdownNow();
            }
            samplerExecutor = null;
            samplerFuture = null;
          }
        }
      });
      guessRefreshThread.setDaemon(true);
      guessRefreshThread.setName("guess-refresh");
      guessRefreshThread.start();
    }
    else if (GrammarvizChartPanel.SELECTION_CANCELLED.equalsIgnoreCase(e.getActionCommand())) {
      LOGGER.info("selection cancelled...");
      finishGuessSessionTerminal(SELECTION_CANCELLED);
    }
    else if (GrammarvizChartPanel.SELECTION_FINISHED.equalsIgnoreCase(e.getActionCommand())) {
      LOGGER.info("selection finished...");
      this.resetChartPanel();
    }
    else if (GrammarvizChartPanel.SAMPLING_SUCCEEDED.equalsIgnoreCase(e.getActionCommand())) {
      finishGuessSessionTerminal(SAMPLING_SUCCEEDED);
    }
    else if (GrammarvizChartPanel.SAMPLING_BELOW_THRESHOLD
        .equalsIgnoreCase(e.getActionCommand())) {
      finishGuessSessionTerminal(SAMPLING_BELOW_THRESHOLD);
    }
    else if (GrammarvizChartPanel.SAMPLING_FAILED.equalsIgnoreCase(e.getActionCommand())) {
      finishGuessSessionTerminal(SAMPLING_FAILED);
    }

  }

  /**
   * Quick and dirty hack for saving the current chart -- because normally the chart parameters need
   * to be defined and modifiable by the user.
   */
  private void saveCurrentChart() {
    if (null == this.seriesChart || null == this.seriesPlot) {
      LOGGER.error("no time-series chart available to save");
      return;
    }

    final String fileName = new SimpleDateFormat("yyyyMMddhhmmssSS'.png'").format(new Date());
    try {

      NumberAxis domain = (NumberAxis) this.seriesPlot.getDomainAxis();
      Range domainRange = domain.getRange();

      NumberAxis range = (NumberAxis) this.seriesPlot.getRangeAxis();
      Range rangeRange = range.getRange();

      String annotationString = "W:" + this.session.chartData.getSAXWindowSize() + ", P:"
          + this.session.chartData.getSAXPaaSize() + ", A:"
          + this.session.chartData.getSAXAlphabetSize();

      XYTextAnnotation a = new XYTextAnnotation(annotationString,
          domainRange.getLowerBound() + domainRange.getLength() / 100,
          rangeRange.getLowerBound() + rangeRange.getLength() / 5 * 3.5);

      a.setTextAnchor(TextAnchor.BOTTOM_LEFT);

      a.setPaint(Color.RED);
      a.setOutlinePaint(Color.BLACK);
      a.setOutlineVisible(true);

      a.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));

      this.seriesPlot.addAnnotation(a);

      // write the PNG on the EDT: seriesChart may be the live on-screen chart, and
      // ChartUtils.saveChartAsPNG reads it -- doing that on a background thread would race
      // the EDT's repaint of the same JFreeChart. The save is fast, so keep it synchronous.
      ChartUtils.saveChartAsPNG(new File(fileName), this.seriesChart, 900, 600);
      LOGGER.info("chart saved to " + fileName);
    }
    catch (Exception e) {
      LOGGER.error("error while saving the chart as PNG", e);
    }
  }

  public void setOperationalButton(JButton guessParametersButton) {
    this.setOperationalButton = guessParametersButton;
  }

  public void addActionListener(ActionListener listener) {
    this.listeners.add(listener);
  }

  // working around the scaling chart issue
  //
  public void bindToTheFrameSize() {
    this.getTopLevelAncestor().addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        if (null != chartPanel) {
          // System.err.println("component resized");
          chartPanel.setMaximumDrawHeight(e.getComponent().getHeight());
          chartPanel.setMaximumDrawWidth(e.getComponent().getWidth());
          chartPanel.setMinimumDrawWidth(0);
          chartPanel.setMinimumDrawHeight(0);
          chartPanel.revalidate();
        }
      }
    });
  }

}
