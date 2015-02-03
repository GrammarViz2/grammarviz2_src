package edu.hawaii.jmotif.grammarviz.logic;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Observable;
import edu.hawaii.jmotif.gi.GrammarRuleRecord;
import edu.hawaii.jmotif.grammarviz.model.SequiturMessage;
import edu.hawaii.jmotif.logic.RuleInterval;
import edu.hawaii.jmotif.sax.LargeWindowAlgorithm;
import edu.hawaii.jmotif.sax.SAXFactory;
import edu.hawaii.jmotif.sax.datastructures.DiscordRecord;
import edu.hawaii.jmotif.sax.datastructures.DiscordRecords;
import edu.hawaii.jmotif.sax.trie.VisitRegistry;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.util.StackTrace;

/**
 * Implements a runnable for the proposed in EDBT15 anomaly discovery technique.
 * 
 * @author psenin
 * 
 */
public class GrammarVizAnomalyFinder extends Observable implements Runnable {

  /** The chart data handler. */
  private MotifChartData chartData;

  /**
   * Constructor.
   * 
   * @param motifChartData The chartdata object -- i.e., info about the input and parameters.
   */
  public GrammarVizAnomalyFinder(MotifChartData motifChartData) {
    super();
    this.chartData = motifChartData;
  }

  @Override
  public void run() {

    // save the timestamp
    Date start = new Date();

    this.setChanged();
    notifyObservers("computing coverage...");

    int[] coverageCurve = new int[this.chartData.originalTimeSeries.length];
    for (GrammarRuleRecord ruleEntry : this.chartData.getGrammarRules()) {
      if (0 == ruleEntry.ruleNumber()) {
        continue;
      }
      ArrayList<RuleInterval> intervals = getRulePositionsByRuleNum(ruleEntry.ruleNumber());
      for (RuleInterval interval : intervals) {
        for (int j = interval.getStartPos(); j < interval.getEndPos(); j++) {
          coverageCurve[j]++;
        }
      }
    }

    // SAX Sequitur exact
    //
    // build an array of rules with their average coverage
    //
    HashMap<RuleDescriptor, ArrayList<RuleInterval>> rules = new HashMap<RuleDescriptor, ArrayList<RuleInterval>>();
    for (GrammarRuleRecord r : this.chartData.getGrammarRules()) {
      if (0 == r.ruleNumber()) {
        continue;
      }
      ArrayList<RuleInterval> intervals = getRulePositionsByRuleNum(r.ruleNumber());
      rules.put(
          new RuleDescriptor(r.ruleNumber(), r.getRuleName(), r.getRuleString(), r.getMeanLength(),
              r.getRuleUseFrequency()), intervals);
    }

    // populate all intervals with their coverage
    //
    ArrayList<RuleInterval> intervals = new ArrayList<RuleInterval>();
    for (Entry<RuleDescriptor, ArrayList<RuleInterval>> e : rules.entrySet()) {
      for (RuleInterval ri : e.getValue()) {
        ri.setCoverage(e.getKey().getRuleFrequency());
        ri.setId(e.getKey().getRuleIndex());
        intervals.add(ri);
      }
    }

    // check if somewhere there is a ZERO coverage!
    //
    for (int i = 0; i < coverageCurve.length; i++) {
      if (0 == coverageCurve[i]) {
        int j = i;
        while ((j < coverageCurve.length - 1) && (0 == coverageCurve[j])) {
          j++;
        }
        if (Math.abs(i - j) > 1) {
          intervals.add(new RuleInterval(0, i, j, 0.0d));
        }
        i = j;
      }
    }

    log("running RRA on the set of rule intervals...");

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
        bestDiscord = SAXFactory.findBestDiscordForIntervals(this.chartData.originalTimeSeries,
            intervals, globalTrackVisitRegistry);
        Date end = new Date();

        // if the discord is null we getting out of the search
        if (bestDiscord.getNNDistance() == 0.0D || bestDiscord.getPosition() == -1) {
          log("breaking the discords search loop, discords found: "
              + this.chartData.discords.getSize() + " last seen discord: " + bestDiscord.toString());
          break;
        }

        log("found discord: position " + bestDiscord.getPosition() + ", length "
            + bestDiscord.getLength() + ", NN distance " + bestDiscord.getNNDistance()
            + ", elapsed time: " + SAXFactory.timeToString(start.getTime(), end.getTime()) + ", "
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
      catch (TSException e) {
        log(StackTrace.toString(e));
        e.printStackTrace();
      }

    }
    // end of discords code
    //
    Date end = new Date();

    log("discords found in " + SAXFactory.timeToString(start.getTime(), end.getTime()));

  }

  private void log(String message) {
    this.setChanged();
    notifyObservers(new SequiturMessage(SequiturMessage.STATUS_MESSAGE, "SAXSequitur: " + message));
  }

  /**
   * Recovers start and stop coordinates ofRule's subsequences.
   * 
   * @param ruleIdx The rule index.
   * @return The array of all intervals corresponding to this rule.
   */
  public ArrayList<RuleInterval> getRulePositionsByRuleNum(Integer ruleIdx) {
    return this.chartData.getGrammarRules().get(ruleIdx).getRuleIntervals();
  }
}
