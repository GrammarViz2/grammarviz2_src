package net.seninp.grammarviz.anomaly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.seninp.gi.RuleInterval;
import net.seninp.jmotif.distance.EuclideanDistance;
import net.seninp.jmotif.sax.SAXProcessor;
import net.seninp.jmotif.sax.TSProcessor;
import net.seninp.jmotif.sax.algorithm.LargeWindowAlgorithm;
import net.seninp.jmotif.sax.discord.DiscordRecord;
import net.seninp.jmotif.sax.discord.DiscordRecords;
import net.seninp.jmotif.sax.registry.VisitRegistry;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class RRAImplementation {

  private static TSProcessor tp = new TSProcessor();
  private static EuclideanDistance ed = new EuclideanDistance();

  // static block - we instantiate the logger
  //
  private static Logger consoleLogger;
  private static final Level LOGGING_LEVEL = Level.INFO;
  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(RRAImplementation.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  /**
   * This method iterates over the provided list of intervals instead of all the possible SAX words
   * extracted with numerosity reduction.
   * 
   * @param series The series to find discord at.
   * @param discordCollectionSize How many discords to find.
   * @param intervals The intervals. In our implementation these come from the set of Sequitur
   * grammar rules.
   * @return Discords.
   * @throws TSException If error occurs.
   */
  public static DiscordRecords series2RRAAnomalies(double[] series, int discordCollectionSize,
      ArrayList<RuleInterval> intervals) throws Exception {

    // resulting discords collection
    DiscordRecords discords = new DiscordRecords();

    // Visit registry. The idea of the visit registry data structure is that to mark as visited all
    // the discord locations for all searches. I.e. if the discord ever found, its location is
    // marked as visited and there will be no search over it again
    VisitRegistry globalTrackVisitRegistry = new VisitRegistry(series.length);

    // we conduct the search until the number of discords is less than desired
    //
    while (discords.getSize() < discordCollectionSize) {

      consoleLogger.trace("currently known discords: " + discords.getSize() + " out of "
          + discordCollectionSize);

      Date start = new Date();
      DiscordRecord bestDiscord = findBestDiscordForIntervals(series, intervals,
          globalTrackVisitRegistry);
      Date end = new Date();

      // if the discord is null we getting out of the search
      if (bestDiscord.getNNDistance() == 0.0D || bestDiscord.getPosition() == -1) {
        consoleLogger.trace("breaking the outer search loop, discords found: " + discords.getSize()
            + " last seen discord: " + bestDiscord.toString());
        break;
      }

      bestDiscord.setInfo("position " + bestDiscord.getPosition() + ", length "
          + bestDiscord.getLength() + ", NN distance " + bestDiscord.getNNDistance()
          + ", elapsed time: " + SAXProcessor.timeToString(start.getTime(), end.getTime()) + ", "
          + bestDiscord.getInfo());
      consoleLogger.debug(bestDiscord.getInfo());

      // collect the result
      //
      discords.add(bestDiscord);

      // and maintain data structures
      //
      // RightWindowAlgorithm marker = new LargeWindowAlgorithm();
      LargeWindowAlgorithm marker = new LargeWindowAlgorithm();
      marker.markVisited(globalTrackVisitRegistry, bestDiscord.getPosition(),
          bestDiscord.getLength());
    }

    // done deal
    //
    return discords;
  }

  /**
   * 
   * @param series
   * @param globalIntervals
   * @param globalTrackVisitRegistry
   * @return
   * @throws Exception
   */
  public static DiscordRecord findBestDiscordForIntervals(double[] series,
      ArrayList<RuleInterval> globalIntervals, VisitRegistry globalTrackVisitRegistry)
      throws Exception {

    ArrayList<RuleInterval> intervals = cloneIntervals(globalIntervals);

    Collections.sort(intervals, new Comparator<RuleInterval>() {
      public int compare(RuleInterval c1, RuleInterval c2) {
        if (c1.getCoverage() > c2.getCoverage()) {
          return 1;
        }
        else if (c1.getCoverage() < c2.getCoverage()) {
          return -1;

        }
        return 0;
      }
    });

    // init variables
    int bestSoFarPosition = -1;
    double bestSoFarDistance = 0.0D;
    int bestSoFarLength = -1;
    int bestSoFarRule = -1;

    // we will iterate over words from rarest to frequent ones - this is an OUTER LOOP of the best
    // discord search
    //
    int iterationCounter = 0;
    int distanceCalls = 0;
    // int limit = frequencies.size();
    while (!intervals.isEmpty()) {

      // count iterations
      iterationCounter++;
      consoleLogger.trace("iteration " + iterationCounter + ", intervals in collection "
          + intervals.size());

      // the head of this array has the rarest word
      RuleInterval currentEntry = intervals.get(0);
      intervals.remove(0);
      consoleLogger.trace("current entry rule " + currentEntry.getId()
          + ", intervals in collection " + intervals.size());

      // make sure it is not previously found discord
      if (globalTrackVisitRegistry.isVisited(currentEntry.getStartPos(), currentEntry.getEndPos())) {
        continue;
      }

      // so, lets the search begin...
      //
      double nearestNeighborDist = Double.MAX_VALUE;
      // random is switched on
      boolean doRandomSearch = true;

      // get a copy of visited locations
      // VisitRegistry localRegistry = new VisitRegistry(series.length - currentEntry.getLength() -
      // 1);
      // localRegistry.transferVisited(globalTrackVisitRegistry);
      // VisitRegistry localRegistry = globalTrackVisitRegistry.copy();
      VisitRegistry localRegistry = new VisitRegistry(series.length - currentEntry.getLength());

      // extract the subsequence & mark visited current substring
      double[] currentSubsequence = tp.subseriesByCopy(series, currentEntry.getStartPos(),
          currentEntry.getEndPos());

      // TODO: do we really need to mark before rule too? guess so...
      localRegistry.markVisited(currentEntry.getStartPos() - currentEntry.getLength(),
          currentEntry.getEndPos());
      // localRegistry.markVisited(currentEntry.getStartPos(), currentEntry.getEndPos());

      // WE ARE GOING TO ITERATE OVER THE CURRENT WORD OCCURENCES HERE
      //

      Map<Integer, Integer> currentOccurences = listRuleOccurrences(currentEntry.getId(), intervals);
      consoleLogger.trace(" there are " + currentOccurences.size() + " occurrences for the rule "
          + currentEntry.getId() + ", iterating...");
      // what need to be checked here is which sequence is exactly producing largest distance value
      //

      // this is INNER LOOP, where we check all rule's occurrences
      //
      for (Entry<Integer, Integer> nextOccurrence : currentOccurences.entrySet()) {

        // skip the location we standing at, check if we overlap
        if (Math.abs(nextOccurrence.getKey() - currentEntry.getStartPos()) <= currentEntry
            .getLength()) {
          localRegistry.markVisited(nextOccurrence.getKey());
          continue;
        }

        // mark current next visited
        localRegistry.markVisited(nextOccurrence.getKey());

        // get the piece of the timeseries
        double[] occurrenceSubsequence = null;
        if ((nextOccurrence.getKey() + currentEntry.getLength()) >= series.length) {
          occurrenceSubsequence = tp.subseriesByCopy(series,
              series.length - currentEntry.getLength(), series.length);
        }
        else {
          occurrenceSubsequence = tp.subseriesByCopy(series, nextOccurrence.getKey(),
              nextOccurrence.getKey() + currentEntry.getLength());
        }

        // double dist = EuclideanDistance.getDTWDist(currentSubsequence,getSubSeries(series,
        // nextOccurrence.getKey(),
        // nextOccurrence.getValue()));
        double dist = ed.normalizedDistance(currentSubsequence, occurrenceSubsequence);
        distanceCalls++;

        // keep track of best so far distance
        if (dist < nearestNeighborDist) {
          nearestNeighborDist = dist;
          consoleLogger.trace(" ** current NN at interval " + nextOccurrence.getKey() + "-"
              + nextOccurrence.getValue() + ", distance: " + nearestNeighborDist);
          if (dist < bestSoFarDistance) {
            consoleLogger.trace(" ** abandoning the occurrences iterations");
            doRandomSearch = false;
            break;
          }
        }
      }

      if (Double.MAX_VALUE != nearestNeighborDist) {
        consoleLogger.trace("rule occurrence loop finished. For current rule "
            + "occurrences, smallest nearest neighbor distance: " + nearestNeighborDist);
      }
      else {
        consoleLogger
            .trace("rule occurrence loop finished. Nothing changed after iterations over current rule positions ...");
      }

      // check if we must continue with random neighbors
      if (doRandomSearch) {
        // it is heuristics here
        //
        int nextRandomVisitTarget = -1;

        int visitCounter = 0;
        // while ((nextRandomVisitTarget = localRegistry.getNextRandomUnvisitedPosition()) != -1) {
        while ((nextRandomVisitTarget = localRegistry.getNextRandomUnvisitedPosition()) != -1) {
          // consoleLogger.debug(" random position pick step " + visitCounter + " visited: "
          // + registry.getVisited().length + ", unvisited: " + registry.getUnvisited().length
          // + "; nearest neighbor at: " + nearestNeighborDist);
          //
          // if(registry.getUnvisited().length<3){
          // System.err.println("gotcha");
          // }

          // registry.markVisited(nextRandomVisitTarget);
          // marker.markVisited(registry, nextRandomVisitTarget, windowSize);
          localRegistry.markVisited(nextRandomVisitTarget);

          double[] randomTargetValues = tp.subseriesByCopy(series, nextRandomVisitTarget,
              nextRandomVisitTarget + currentEntry.getLength());
          double randomTargetDistance = ed.normalizedDistance(currentSubsequence,
              randomTargetValues);
          distanceCalls++;

          // early abandoning of the search, the current word is not
          // discord, we seen better
          if (randomTargetDistance < bestSoFarDistance) {
            nearestNeighborDist = randomTargetDistance;
            consoleLogger.trace(" ** abandoning random visits loop, seen distance "
                + nearestNeighborDist + " at iteration " + visitCounter);
            break;
          }

          // keep track
          if (randomTargetDistance < nearestNeighborDist) {
            nearestNeighborDist = randomTargetDistance;
          }

          visitCounter = visitCounter + 1;
        } // while inner loop
        consoleLogger.trace("random visits loop finished, total positions considered: "
            + visitCounter);

      } // if break loop

      if (nearestNeighborDist > bestSoFarDistance) {
        bestSoFarDistance = nearestNeighborDist;
        bestSoFarPosition = currentEntry.getStartPos();
        bestSoFarLength = currentEntry.getLength();
        bestSoFarRule = currentEntry.getId();
      }
      // if (knownWordsAndTheirDistances.containsKey(currentWord)
      // && knownWordsAndTheirDistances.get(currentWord).isAbandoned()) {
      // knownWordsAndTheirDistances.put(String.valueOf(currentWord), new DistanceEntry(
      // nearestNeighborDist, completeSearch));
      // }
      // else {
      // knownWordsAndTheirDistances.put(String.valueOf(currentWord), new DistanceEntry(
      // nearestNeighborDist, completeSearch));
      // }
      consoleLogger.trace(" . . iterated " + iterationCounter + " times, best distance:  "
          + bestSoFarDistance + " for a rule " + bestSoFarRule + " at " + bestSoFarPosition);

      iterationCounter++;
    } // outer loop

    DiscordRecord res = new DiscordRecord(bestSoFarPosition, bestSoFarDistance,
        "pos,calls,len,rule " + bestSoFarPosition + " " + distanceCalls + " " + bestSoFarLength
            + " " + bestSoFarRule);
    res.setLength(bestSoFarLength);
    res.setRuleId(bestSoFarRule);
    res.setInfo("distance calls: " + distanceCalls);

    return res;
  }

  /**
   * Finds all the Sequitur rules with a given Id and populates their start and end into the array.
   * 
   * @param id The rule Id.
   * @param intervals The rule intervals.
   * @return map of start - end.
   */
  private static Map<Integer, Integer> listRuleOccurrences(int id, ArrayList<RuleInterval> intervals) {
    HashMap<Integer, Integer> res = new HashMap<Integer, Integer>(100);
    for (RuleInterval i : intervals) {
      if (id == i.getId()) {
        res.put(i.getStartPos(), i.getEndPos());
      }
    }
    return res;
  }

  /**
   * Cloning an array. I know, I need to make the
   * 
   * @param source the source array.
   * @return the clone.
   */
  private static ArrayList<RuleInterval> cloneIntervals(ArrayList<RuleInterval> source) {
    ArrayList<RuleInterval> res = new ArrayList<RuleInterval>();
    for (RuleInterval r : source) {
      res.add(new RuleInterval(r.getId(), r.getStartPos(), r.getEndPos(), r.getCoverage()));
    }
    return res;
  }
}
