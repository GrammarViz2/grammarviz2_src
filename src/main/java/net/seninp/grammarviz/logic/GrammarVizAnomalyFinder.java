package net.seninp.grammarviz.logic;

import java.util.ArrayList;
import java.util.Date;
import net.seninp.gi.logic.RuleInterval;
import net.seninp.grammarviz.GrammarVizAnomaly;
import net.seninp.grammarviz.anomaly.RRAImplementation;
import net.seninp.grammarviz.anomaly.RRAIntervalBuilder;
import net.seninp.grammarviz.model.GrammarVizListener;
import net.seninp.grammarviz.model.GrammarVizMessage;
import net.seninp.grammarviz.model.GrammarVizMessageBoard;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.discord.DiscordRecord;
import net.seninp.jmotif.sax.discord.DiscordRecords;
import net.seninp.util.StackTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a runnable for the proposed in EDBT15 anomaly discovery technique.
 * 
 * @author psenin
 * 
 */
public class GrammarVizAnomalyFinder implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(GrammarVizAnomalyFinder.class);

  /** The chart data handler. */
  private GrammarVizChartData chartData;

  /** Broadcasts progress messages to listeners (replaces the deprecated Observable). */
  private final GrammarVizMessageBoard messageBoard = new GrammarVizMessageBoard();

  /**
   * Registers a listener for progress messages.
   *
   * @param listener the listener to add.
   */
  public void addListener(GrammarVizListener listener) {
    this.messageBoard.addListener(listener);
  }

  /**
   * Unregisters a progress message listener.
   *
   * @param listener the listener to remove.
   */
  public void removeListener(GrammarVizListener listener) {
    this.messageBoard.removeListener(listener);
  }

  /**
   * Constructor.
   * 
   * @param motifChartData The chartdata object -- i.e., info about the input and parameters.
   */
  public GrammarVizAnomalyFinder(GrammarVizChartData motifChartData) {
    super();
    this.chartData = motifChartData;
  }

  @Override
  public void run() {

    Date start = new Date();

    log("walking through the grammar rules...");
    ArrayList<RuleInterval> intervals;
    try {
      intervals = RRAIntervalBuilder.fromGrammarRules(this.chartData.getGrammarRules(),
          this.chartData.originalTimeSeries.length, this.chartData.getSAXPaaSize());
    }
    catch (CloneNotSupportedException e) {
      LOGGER.error("error while cloning rule intervals", e);
      log("Exception thrown: " + e.toString());
      return;
    }

    if (intervals.isEmpty()) {
      log("no viable RRA candidates (empty grammar or all gaps too short)");
      this.chartData.discords = new DiscordRecords();
      return;
    }

    int uncovered = 0;
    for (RuleInterval interval : intervals) {
      if (GrammarVizAnomaly.isUncoveredInterval(interval)) {
        uncovered++;
      }
    }
    if (uncovered > 0) {
      log("included " + uncovered + " uncovered gap interval(s) long enough for RRA search");
    }
    else {
      log("the whole timeseries is covered by rule intervals ...");
    }

    log("computing discords (this may take a while)...");
    try {
      this.chartData.discords = RRAImplementation.series2RRAAnomalies(
          this.chartData.originalTimeSeries, RRAImplementation.DEFAULT_DISCORD_COUNT, intervals,
          this.chartData.getZNormThreshold());

      for (int i = 0; i < this.chartData.discords.getSize(); i++) {
        DiscordRecord discord = this.chartData.discords.get(i);
        log("found discord: position " + discord.getPosition() + ", length "
            + discord.getLength() + ", NN distance " + discord.getNNDistance() + ", rule "
            + GrammarVizAnomaly.formatRuleIdForDisplay(discord.getRuleId()));
      }
    }
    catch (Exception e) {
      log(StackTrace.toString(e));
      LOGGER.error("error while computing discords", e);
      this.chartData.discords = new DiscordRecords();
    }

    Date end = new Date();
    log("discords found in " + SAXProcessor.timeToString(start.getTime(), end.getTime()));
  }

  private void log(String message) {
    this.messageBoard
        .fire(new GrammarVizMessage(GrammarVizMessage.STATUS_MESSAGE, "Grammarviz3: " + message));
  }
}
