package net.seninp.grammarviz.logic;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Observable;
import net.seninp.gi.GrammarRuleRecord;
import net.seninp.gi.RuleInterval;
import net.seninp.grammarviz.anomaly.RRAImplementation;
import net.seninp.grammarviz.model.GrammarVizMessage;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.discord.DiscordRecord;
import net.seninp.jmotif.sax.discord.DiscordRecords;
import net.seninp.jmotif.sax.registry.LargeWindowAlgorithm;
import net.seninp.jmotif.sax.registry.VisitRegistry;
import net.seninp.util.StackTrace;

/**
 * Implements a runnable for the proposed in EDBT15 anomaly discovery technique.
 * 
 * @author psenin
 * 
 */
public class GrammarVizAnomalyFinder extends Observable implements Runnable {

  /** The chart data handler. */
  private GrammarVizChartData chartData;

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

    // save the timestamp
    Date start = new Date();

    // [1] build an array of rules along with their use frequency
    //
    HashMap<RuleDescriptor, ArrayList<RuleInterval>> rules = new HashMap<RuleDescriptor, ArrayList<RuleInterval>>();

    for (GrammarRuleRecord r : this.chartData.getGrammarRules()) {
      if (0 == r.ruleNumber()) {
        continue;
      }
      ArrayList<RuleInterval> intervals = getRulePositionsByRuleNum(r.ruleNumber());
      rules.put(new RuleDescriptor(r.ruleNumber(), r.getRuleName(), r.getRuleString(),
          r.getMeanLength(), r.getRuleUseFrequency()), intervals);
    }

    // [2] populate all intervals with their coverage
    //
    ArrayList<RuleInterval> intervals = new ArrayList<RuleInterval>();
    for (Entry<RuleDescriptor, ArrayList<RuleInterval>> e : rules.entrySet()) {
      for (RuleInterval ri : e.getValue()) {
        // ri.setCoverage(e.getKey().getRuleFrequency());
        ri.setCoverage(e.getValue().size());
        ri.setId(e.getKey().getRuleIndex());
        intervals.add(ri);
      }
    }

    // [3] compute the coverage
    //
    this.setChanged();
    notifyObservers("computing coverage...");

    int[] coverageArray = new int[this.chartData.originalTimeSeries.length];

    for (GrammarRuleRecord ruleEntry : this.chartData.getGrammarRules()) {
      if (0 == ruleEntry.ruleNumber()) {
        continue;
      }
      ArrayList<RuleInterval> ruleIntervals = getRulePositionsByRuleNum(ruleEntry.ruleNumber());
      for (RuleInterval interval : ruleIntervals) {
        for (int j = interval.getStartPos(); j < interval.getEndPos(); j++) {
          coverageArray[j]++;
        }
      }
    }

    // [3] check if somewhere there is a ZERO coverage!
    //
    for (int i = 0; i < coverageArray.length; i++) {
      if (0 == coverageArray[i]) {
        int j = i;
        while ((j < coverageArray.length - 1) && (0 == coverageArray[j])) {
          j++;
        }
        if (Math.abs(i - j) > 1) {
          intervals.add(new RuleInterval(0, i, j, 0.0d));
        }
        i = j;
      }
    }

    List<RuleInterval> zeros = getZeroIntervals(coverageArray);
    if (zeros.size() > 0) {
      log("found " + zeros.size() + " intervals not covered by rules: " + intervalsToString(zeros));
      intervals.addAll(zeros);
    }
    else {
      log("the whole timeseries covered by rule intervals ...");
    }

    // run HOTSAX with this intervals set
    //

    // resulting discords collection
    this.chartData.discords = new DiscordRecords();

    // Visit registry. The idea of the visit registry data structure is that to mark as visited all
    // the discord locations for all searches. I.e. if the discord ever found, its location is
    // marked as visited and there will be no search over it again
    VisitRegistry globalTrackVisitRegistry = new VisitRegistry(
        this.chartData.originalTimeSeries.length);

    // we conduct the search until the number of discords is less than desired
    //
    while (this.chartData.discords.getSize() < 10) {

      start = new Date();
      DiscordRecord bestDiscord;
      try {
        bestDiscord = RRAImplementation.findBestDiscordForIntervals(
            this.chartData.originalTimeSeries, intervals, globalTrackVisitRegistry);
        Date end = new Date();

        // if the discord is null we getting out of the search
        if (bestDiscord.getNNDistance() == 0.0D || bestDiscord.getPosition() == -1) {
          log("breaking the discords search loop, discords found: "
              + this.chartData.discords.getSize() + " last seen discord: "
              + bestDiscord.toString());
          break;
        }

        log("found discord: position " + bestDiscord.getPosition() + ", length "
            + bestDiscord.getLength() + ", NN distance " + bestDiscord.getNNDistance()
            + ", elapsed time: " + SAXProcessor.timeToString(start.getTime(), end.getTime()) + ", "
            + bestDiscord.getInfo());

        // collect the result
        //
        this.chartData.discords.add(bestDiscord);

        // and maintain data structures
        //
        // RightWindowAlgorithm marker = new LargeWindowAlgorithm();
        LargeWindowAlgorithm marker = new LargeWindowAlgorithm();
        marker.markVisited(globalTrackVisitRegistry, bestDiscord.getPosition(),
            bestDiscord.getLength());
      }
      catch (Exception e) {
        log(StackTrace.toString(e));
        e.printStackTrace();
      }

    }
    // end of discords code
    //
    Date end = new Date();

    log("discords found in " + SAXProcessor.timeToString(start.getTime(), end.getTime()));

  }

  private void log(String message) {
    this.setChanged();
    notifyObservers(
        new GrammarVizMessage(GrammarVizMessage.STATUS_MESSAGE, "SAXSequitur: " + message));
  }

  /**
   * Run a quick scan along the timeseries coverage to find a zeroed intervals.
   * 
   * @param coverageArray the coverage to analyze.
   * @return set of zeroed intervals (if found).
   */
  private List<RuleInterval> getZeroIntervals(int[] coverageArray) {
    ArrayList<RuleInterval> res = new ArrayList<RuleInterval>();
    int start = -1;
    boolean inInterval = false;
    int intervalsCounter = -1;
    for (int i = 0; i < coverageArray.length; i++) {
      if (0 == coverageArray[i] && !inInterval) {
        start = i;
        inInterval = true;
      }
      if (coverageArray[i] > 0 && inInterval) {
        res.add(new RuleInterval(intervalsCounter, start, i, 0));
        inInterval = false;
        intervalsCounter--;
      }
    }
    return res;
  }

  /**
   * Recovers start and stop coordinates ofRule's subsequences.
   * 
   * @param ruleIdx The rule index.
   * @return The array of all intervals corresponding to this rule.
   */
  private ArrayList<RuleInterval> getRulePositionsByRuleNum(Integer ruleIdx) {
    return this.chartData.getGrammarRules().get(ruleIdx).getRuleIntervals();
  }

  /**
   * Makes a zeroed interval to appear nicely in output.
   * 
   * @param zeros the list of zeros.
   * @return the intervals list as a string.
   */
  private String intervalsToString(List<RuleInterval> zeros) {
    StringBuilder sb = new StringBuilder();
    for (RuleInterval i : zeros) {
      sb.append(i.toString()).append(",");
    }
    return sb.toString();
  }
}
