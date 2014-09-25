package edu.hawaii.jmotif.experimentation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import javax.imageio.ImageIO;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.hawaii.jmotif.gi.GrammarRuleRecord;
import edu.hawaii.jmotif.gi.GrammarRules;
import edu.hawaii.jmotif.gi.sequitur.SequiturFactory;
import edu.hawaii.jmotif.logic.RuleInterval;
import edu.hawaii.jmotif.sax.NumerosityReductionStrategy;
import edu.hawaii.jmotif.util.StackTrace;

public class MinCoverageSearchExperiment {

  // locale, charset, etc
  //
  final static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
  private static final String CR = "\n";
  private static final Level LOGGING_LEVEL = Level.ALL;
  private static final String DATA_FILENAME = "test/data/mitdbx_mitdbx_108_1.txt";

  private static final int MIN_WINDOW_SIZE = 60;
  private static final int MAX_WINDOW_SIZE = 220;

  private static final int MIN_PAA_SIZE = 4;
  private static final int MAX_PAA_SIZE = 10;

  private static final int MIN_A_SIZE = 3;
  private static final int MAX_A_SIZE = 8;

  private static final int PLOT_HEIGHT = 120;
  private static final int PLOT_WIDTH = 400;

  private static Logger consoleLogger;

  private static double[] ts;

  // static block - we instantiate the logger
  //
  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(MinCoverageSearchExperiment.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  public static void main(String[] args) throws Exception {

    ts = loadData(DATA_FILENAME);

    ArrayList<String> filenames = new ArrayList<String>();

    for (int winSize = MIN_WINDOW_SIZE; winSize < MAX_WINDOW_SIZE; winSize = winSize + 40) {
      for (int paaSize = MIN_PAA_SIZE; paaSize < MAX_PAA_SIZE; paaSize++) {
        for (int aSize = MIN_A_SIZE; aSize < MAX_A_SIZE; aSize++) {

          // get the TS converted into the rule Intervals
          //
          Date tStart = new Date();
          GrammarRules rules = SequiturFactory.series2SequiturRules(ts, winSize, paaSize, aSize,
              NumerosityReductionStrategy.EXACT, 0.05);
          Date tRulesEnd = new Date();

          // populate all intervals with their coverage
          //
          ArrayList<RuleInterval> intervals = new ArrayList<RuleInterval>();
          for (GrammarRuleRecord rule : rules) {
            if (rule.getRuleYield() > 2) {
              continue;
            }
            for (RuleInterval ri : rule.getRuleIntervals()) {
              ri.setCoverage(rule.getRuleIntervals().size());
              ri.setId(rule.ruleNumber());
              intervals.add(ri);
            }
          }

          // get the chart of that
          //
          JFreeChart chart = paintTheChart(ts);

          int[] coverageArray = new int[ts.length];

          for (GrammarRuleRecord r : rules) {
            if (0 == r.ruleNumber()) {
              continue;
            }
            ArrayList<RuleInterval> arrPos = r.getRuleIntervals();
            for (RuleInterval saxPos : arrPos) {
              int start = saxPos.getStartPos();
              int end = saxPos.getEndPos();
              for (int j = start; j < end; j++) {
                coverageArray[j] = coverageArray[j] + 1;
              }
            }
          }

          // find the rule density value
          int maxObservedCoverage = 0;
          int minObservedCoverage = Integer.MAX_VALUE;
          for (int i = winSize; i < coverageArray.length - winSize; i++) {
            // update the min and max coverage values
            if (maxObservedCoverage < coverageArray[i]) {
              maxObservedCoverage = coverageArray[i];
            }
            if (minObservedCoverage > coverageArray[i]) {
              minObservedCoverage = coverageArray[i];
            }
          }

          double covIncrement = 1. / (double) (maxObservedCoverage - minObservedCoverage);

          for (int i = 0; i < rules.size(); i++) {
            GrammarRuleRecord r = rules.get(i);
            if (0 == r.ruleNumber()) {
              continue;
            }
            ArrayList<RuleInterval> arrPos = rules.get(i).getRuleIntervals();
            for (RuleInterval saxPos : arrPos) {

              IntervalMarker marker = new IntervalMarker(saxPos.getStartPos(), saxPos.getEndPos());
              marker.setLabelOffsetType(LengthAdjustmentType.EXPAND);
              marker.setPaint(Color.BLUE);

              marker.setAlpha((float) covIncrement);

              marker.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
              marker.setLabelPaint(Color.green);
              marker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
              marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);

              ((XYPlot) chart.getPlot()).addDomainMarker(marker, Layer.BACKGROUND);

            }
          }

          String annotationString = "W:" + winSize + ", P:" + paaSize + ", A:" + aSize
              + ", MIN_COV: " + minObservedCoverage;
          if (minObservedCoverage == 0) {
            annotationString = "*#* " + annotationString;
          }

          NumberAxis domain = (NumberAxis) ((XYPlot) chart.getPlot()).getDomainAxis();
          Range domainRange = domain.getRange();

          NumberAxis range = (NumberAxis) ((XYPlot) chart.getPlot()).getRangeAxis();
          Range rangeRange = range.getRange();

          XYTextAnnotation a = new XYTextAnnotation(annotationString, domainRange.getLowerBound()
              + domainRange.getLength() / 100, rangeRange.getLowerBound() + rangeRange.getLength()
              / 5 * 3.5);

          a.setTextAnchor(TextAnchor.BOTTOM_LEFT);

          a.setPaint(Color.RED);
          a.setOutlinePaint(Color.BLACK);
          a.setOutlineVisible(true);

          a.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));

          // XYPointerAnnotation a = new XYPointerAnnotation("Bam!", domainRange.getLowerBound()
          // + domainRange.getLength() / 10, rangeRange.getLowerBound() + rangeRange.getLength() / 5
          // * 4, 5 * Math.PI / 8);

          ((XYPlot) chart.getPlot()).addAnnotation(a);

          String fname = "ecg0606_" + ((Integer) winSize).toString() + "_"
              + ((Integer) paaSize).toString() + "_" + ((Integer) aSize).toString() + ".png";

          ChartUtilities.saveChartAsPNG(new File(fname), chart, PLOT_WIDTH, PLOT_HEIGHT);

          filenames.add(fname);

        }
      }
    }

    BufferedImage result = new BufferedImage(2000, (filenames.size() / 5) * PLOT_HEIGHT, // work
                                                                                         // these
        // out
        BufferedImage.TYPE_INT_RGB);
    Graphics g = result.getGraphics();

    int x = 0;
    int y = 0;
    for (int i = 0; i < filenames.size(); i++) {
      String image = filenames.get(i);
      BufferedImage bi = ImageIO.read(new File(image));
      g.drawImage(bi, x, y, null);
      x += PLOT_WIDTH;
      if (x >= result.getWidth()) {
        x = 0;
        y += PLOT_HEIGHT;
      }
    }

    ImageIO.write(result, "png", new File("result_mitdbx_108_1.png"));

  }

  private static JFreeChart paintTheChart(double[] tsData) {

    // making the data
    //
    XYSeries dataset = new XYSeries("Series");
    for (int i = 0; i < tsData.length; i++) {
      dataset.add(i, (float) tsData[i]);
    }
    XYSeriesCollection chartXYSeriesCollection = new XYSeriesCollection(dataset);

    // set the renderer
    //
    XYLineAndShapeRenderer xyRenderer = new XYLineAndShapeRenderer(true, false);
    xyRenderer.setSeriesPaint(0, new Color(0, 0, 0));
    xyRenderer.setBaseStroke(new BasicStroke(3));

    // X - the time axis
    //
    NumberAxis timeAxis = new NumberAxis();
    timeAxis.setLabel("Time");

    // Y axis
    //
    NumberAxis valueAxis = new NumberAxis("Values");
    valueAxis.setAutoRangeIncludesZero(false);
    valueAxis.setLabel("Values");

    // put these into collection of dots
    //
    XYPlot timeseriesPlot = new XYPlot(chartXYSeriesCollection, timeAxis, valueAxis, xyRenderer);

    // finally, create the chart
    JFreeChart chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, timeseriesPlot, false);

    // set the progress listener to react to mouse clicks in the chart
    return chart;

  }

  /**
   * This reads the data
   * 
   * @param fname The filename.
   * @return
   */
  private static double[] loadData(String fname) {

    consoleLogger.info("reading from " + fname);

    long lineCounter = 0;
    double ts[] = new double[1];

    Path path = Paths.get(fname);

    ArrayList<Double> data = new ArrayList<Double>();

    try {

      BufferedReader reader = Files.newBufferedReader(path, DEFAULT_CHARSET);

      String line = null;
      while ((line = reader.readLine()) != null) {
        String[] lineSplit = line.trim().split("\\s+");
        for (int i = 0; i < lineSplit.length; i++) {
          double value = new BigDecimal(lineSplit[i]).doubleValue();
          data.add(value);
        }
        lineCounter++;
      }
      reader.close();
    }
    catch (Exception e) {
      System.err.println(StackTrace.toString(e));
    }
    finally {
      assert true;
    }

    if (!(data.isEmpty())) {
      ts = new double[data.size()];
      for (int i = 0; i < data.size(); i++) {
        ts[i] = data.get(i);
      }
    }

    consoleLogger.info("loaded " + data.size() + " points from " + lineCounter + " lines in "
        + fname);
    return ts;

  }

}
