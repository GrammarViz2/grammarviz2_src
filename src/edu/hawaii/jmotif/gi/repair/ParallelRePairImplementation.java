package edu.hawaii.jmotif.gi.repair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.hawaii.jmotif.util.StackTrace;

public class ParallelRePairImplementation {

  private static final char SPACE = ' ';
  // locale, charset, etc
  //
  // private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
  // private static final Object CR = "\n";

  // logging stuff
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.WARN;

  // static block - we instantiate the logger
  //
  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(ParallelRePairImplementation.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  public ParallelRePairImplementation() {
    super();
  }

  /**
   * This builds a RE-Pair in a parallel fashion.
   * 
   * @param parallelRes
   * @param threadsNum
   * @return
   */
  public ParallelGrammarKeeper buildGrammar(ParallelGrammarKeeper grammar, int threadsNum) {

    consoleLogger.debug("Starting the parallel RE-Pair");

    int iterationCounter = 0;

    boolean contunue = true;

    while (contunue) {

      int oldStringSize = grammar.workString.size();

      // the map of extended rules to their instances - used in merging procedure
      final HashMap<String, ParallelRePairRule> extendedStringToRecord = new HashMap<String, ParallelRePairRule>();
      // the mapping of rule ID to the rule instance
      final HashMap<Integer, ParallelRePairRule> ruleNumToRecord = new HashMap<Integer, ParallelRePairRule>();
      if (!(grammar.theRules.isEmpty())) {
        for (Entry<Integer, ParallelRePairRule> e : grammar.theRules.entrySet()) {
          ruleNumToRecord.put(e.getKey(), e.getValue());
        }
      }
      // the data structure which keeps R0 strings that have been returned from workers
      final HashMap<Integer, String> chunkStrings = new HashMap<Integer, String>();
      final HashMap<Integer, ArrayList<Symbol>> chunkWorkStrings = new HashMap<Integer, ArrayList<Symbol>>();

      final ExecutorService executorService = Executors.newFixedThreadPool(threadsNum);
      final CompletionService<ParallelGrammarKeeper> completionService = new ExecutorCompletionService<ParallelGrammarKeeper>(
          executorService);

      consoleLogger.info("Iteration #" + iterationCounter + ", created thread pool of "
          + threadsNum + " threads");

      // task counter - quite important variable
      int totalTaskCounter = 0;

      // tstamp is used for jobs identification
      final long tstamp = System.currentTimeMillis();

      // first chunk takes on the uneven division
      //
      final int evenIncrement = grammar.workString.size() / threadsNum;
      final int reminder = grammar.workString.size() % threadsNum;
      final int firstChunkSize = evenIncrement + reminder;

      consoleLogger.info("data size " + grammar.workString.size() + " tokens, evenIncrement "
          + evenIncrement + ", reminder " + reminder + ", firstChunkSize " + firstChunkSize);

      // execute chunks processing
      //

      // the first chunk
      {
        final int firstChunkStart = 0;
        final int firstChunkEnd = firstChunkSize;
        final ParallelRePairWorkerSingleLevel job0 = new ParallelRePairWorkerSingleLevel(tstamp
            + totalTaskCounter, grammar, firstChunkStart, firstChunkEnd);
        completionService.submit(job0);
        consoleLogger.debug("submitted first chunk job " + tstamp);
        totalTaskCounter++;
      }

      // intermediate chunks
      while (totalTaskCounter < threadsNum - 1) {
        final int intermediateChunkStart = firstChunkSize + (totalTaskCounter - 1) * evenIncrement;
        final int intermediateChunkEnd = firstChunkSize + (totalTaskCounter * evenIncrement);
        final ParallelRePairWorkerSingleLevel job = new ParallelRePairWorkerSingleLevel(tstamp
            + totalTaskCounter, grammar, intermediateChunkStart, intermediateChunkEnd);
        completionService.submit(job);
        consoleLogger.debug("submitted intermediate chunk job "
            + Long.valueOf(tstamp + totalTaskCounter));
        totalTaskCounter++;
      }

      // the last chunk
      {
        final int lastChunkStart = grammar.workString.size() - evenIncrement;
        final int lastChunkEnd = grammar.workString.size();
        final ParallelRePairWorkerSingleLevel jobN = new ParallelRePairWorkerSingleLevel(tstamp
            + totalTaskCounter, grammar, lastChunkStart, lastChunkEnd);
        completionService.submit(jobN);
        consoleLogger.debug("submitted last chunk job " + Long.valueOf(tstamp + totalTaskCounter));
        totalTaskCounter++;
      }

      // all is in the work, shut down
      executorService.shutdown();

      // lets get jobs out
      try {

        while (totalTaskCounter > 0) {

          Future<ParallelGrammarKeeper> finished = completionService.poll(128, TimeUnit.HOURS);

          if (null == finished) {
            // something went wrong - break from here
            System.err.println("Breaking POLL loop after 128 HOURS of waiting...");
            break;
          }
          else {

            // HashMap<String, GrammarRuleRecord> extendedStringToRecord
            // HashMap<Integer, GrammarRuleRecord> ruleNumToRecord
            // HashMap<Integer, String> chunkStrings

            // merge the block with junctions
            //
            ParallelGrammarKeeper chunkRes = finished.get();
            int chunkJobIndex = (int) (chunkRes.getId() - tstamp);
            consoleLogger.debug("job " + chunkRes.getId() + " of chunk " + chunkJobIndex
                + " has finished");

            Hashtable<Integer, ParallelRePairRule> chunkGrammarRulesData = chunkRes.theRules;
            String R0String = chunkRes.r0String;

            chunkStrings.put(chunkJobIndex, R0String);
            chunkWorkStrings.put(chunkJobIndex, chunkRes.workString);

            // back to the business
            //
            chunkRes.expandRules();
            // StringBuffer debugMessage = new StringBuffer();
            // debugMessage.append("Chunk ").append(chunkJobIndex).append(" input: ")
            // .append(chunkRes.r0ExpandedString).append(CR);
            // debugMessage.append("Chunk ").append(chunkJobIndex).append(" recovered string:\n")
            // .append(chunkRes.r0ExpandedString).append(CR);
            // debugMessage.append("Chunk ").append(chunkJobIndex).append(" grammar:\nR0 -> ")
            // .append(chunkRes.r0String).append(CR);

            // these are the rule keys, they'll be used twice
            //
            ArrayList<Integer> keys = new ArrayList<Integer>(chunkGrammarRulesData.keySet());
            Collections.sort(keys);

            // for (int i = 0; i < keys.size(); i++) {
            // ParallelRePairRule r = chunkGrammarRulesData.get(keys.get(i));
            // if (null != r) {
            // debugMessage.append(r.getRuleName()).append(" -> ").append(r.toRuleString())
            // .append(" : ").append(r.expandedRuleString).append(", ").append(r.positions)
            // .append(CR);
            // }
            // }
            // System.out.print(debugMessage.toString());

            // these are guaranteed to come out in order
            //
            for (int i = 0; i < keys.size(); i++) {

              ParallelRePairRule r = chunkGrammarRulesData.get(keys.get(i));

              consoleLogger.trace("processing rule " + r.getRuleName() + " -> " + r.toRuleString()
                  + " : " + r.expandedRuleString);

              // check if such an expanded string already exists
              //
              ParallelRePairRule matchingRecord = extendedStringToRecord.get(r.expandedRuleString);

              //
              // *** [Option 1] if such extended string doesn't exists
              //
              if (null == matchingRecord) {
                consoleLogger.trace("there is no matching rule...");
                int num = r.ruleNumber;
                // check the rule naming
                //
                if (ruleNumToRecord.containsKey(num)) {
                  consoleLogger.trace(" .. but this rule num is taken by "
                      + ruleNumToRecord.get(num));
                  int newRuleNum = ruleNumToRecord.size() + 1;
                  r.ruleNumber = newRuleNum;
                  R0String = R0String.replaceAll(String.valueOf("R" + num + SPACE),
                      String.valueOf("@" + newRuleNum + SPACE));
                  consoleLogger.trace(" .. performed rename, R" + num + " -> " + r.getRuleName());
                }
                // save the record
                //
                extendedStringToRecord.put(r.expandedRuleString, r);
                ruleNumToRecord.put(r.ruleNumber, r);
              }
              else {

                //
                // ***[Option 2] the similar expanded string exists
                //
                // save all occurrences
                consoleLogger.trace("there is a matching rule: " + matchingRecord.getRuleName()
                    + " -> " + matchingRecord.toRuleString());
                matchingRecord.positions.addAll(r.positions);

                // rename the rule because the rule num could be overlapping with this grammar rule
                // num, we will simply add a special symbol
                //
                int num = r.ruleNumber;
                int newRuleNum = matchingRecord.ruleNumber;

                StringBuilder R0sb = new StringBuilder(R0String);

                int tokenIdx = 0;
                int spaceIdx = -1;

                // the very first rule
                //
                if (R0String.charAt(0) == 'R') {
                  int nextSpaceIdx = R0String.indexOf(SPACE, 1);
                  int ruleNum = Integer.parseInt(R0String.substring(1, nextSpaceIdx));
                  if (num == ruleNum) {
                    R0sb.replace(0, nextSpaceIdx, "@" + newRuleNum);
                    ParallelRePairGuard el = (ParallelRePairGuard) chunkWorkStrings.get(
                        chunkJobIndex).get(0);
                    el.rule = matchingRecord;
                  }
                }

                while ((spaceIdx = R0sb.indexOf(" ", spaceIdx + 1)) >= 0) {
                  if ((spaceIdx < R0sb.length() - 1) && (R0sb.charAt(spaceIdx + 1) == 'R')) {
                    int nextSpaceIdx = R0sb.indexOf(" ", spaceIdx + 1);
                    int ruleNum = Integer.parseInt(R0sb.substring(spaceIdx + 2, nextSpaceIdx));
                    if (num == ruleNum) {
                      R0sb.replace(spaceIdx + 1, nextSpaceIdx, "@" + newRuleNum);
                      ParallelRePairGuard el = (ParallelRePairGuard) chunkWorkStrings.get(
                          chunkJobIndex).get(tokenIdx + 1);
                      el.rule = matchingRecord;
                    }
                  }
                  tokenIdx++;
                }

                R0String = R0sb.toString();

              }
            }

            // all is finished and in the final string we may want to replace all special characters
            //
            chunkStrings.put(chunkJobIndex, R0String.replaceAll("@", "R"));
          }

          totalTaskCounter--;

        }

        // HashMap<String, GrammarRuleRecord> extendedStringToRecord
        // HashMap<Integer, GrammarRuleRecord> positionToRecord
        // HashMap<Integer, GrammarRuleRecord> ruleNumToRecord
        // HashMap<Integer, String> chunkStrings

        String mergedString = "";
        ArrayList<Symbol> mergedWorkString = new ArrayList<Symbol>();
        for (int i = 0; i < chunkStrings.size(); i++) {
          mergedString = mergedString.concat(chunkStrings.get(i));
          mergedWorkString.addAll(chunkWorkStrings.get(i));
        }

        // System.out.println("Merged grammar:");
        // System.out.println("R0 -> " + mergedString);
        // for (int i = 1; i < ruleNumToRecord.size() + 1; i++) {
        // ParallelRePairRule r = ruleNumToRecord.get(i);
        // System.out.println(r.getRuleName() + " -> " + r.toRuleString() + " : "
        // + r.expandedRuleString + ", " + r.positions);
        // }
        // System.out.println("Merged work string:\n" + stringToDisplay(mergedWorkString));

        grammar.setR0String(mergedString);
        grammar.setWorkString(mergedWorkString);

        // populate existing rules
        for (int i = 1; i < ruleNumToRecord.size() + 1; i++) {
          ParallelRePairRule r = ruleNumToRecord.get(i);
          grammar.addExistingRule(r);
        }

        // grammar.expandR0();
        // System.out.println("Recovered String:\n" + grammar.r0ExpandedString);

      }
      catch (Exception e) {
        System.err.println("Error while waiting results: " + StackTrace.toString(e));
      }
      finally {
        // wait at least 1 more hour before terminate and fail
        try {
          if (!executorService.awaitTermination(4, TimeUnit.HOURS)) {
            executorService.shutdownNow(); // Cancel currently executing tasks
            if (!executorService.awaitTermination(30, TimeUnit.MINUTES)) {
              System.err.println("Pool did not terminate... FATAL ERROR");
            }
          }
        }
        catch (InterruptedException ie) {
          System.err.println("Error while waiting interrupting: " + StackTrace.toString(ie));
          // (Re-)Cancel if current thread also interrupted
          executorService.shutdownNow();
          // Preserve interrupt status
          Thread.currentThread().interrupt();
        }

      }

      // the iteration finished -- need to make a decision about continuation
      //
      if (grammar.workString.size() == oldStringSize) {

        consoleLogger.info("finished with iterations... the final repair execution...");
        //
        // need to do the last run of repair cause in the merged string there still can be some
        // repeating digrams
        //
        // get the result instantiated
        //

        // two data structures
        //
        // 1.0. - the string
        ArrayList<Symbol> string = new ArrayList<Symbol>();
        //
        // 2.0. - the digram frequency table, digram, frequency, and the first occurrence index
        DigramFrequencies digramFrequencies = new DigramFrequencies();

        // build the input string and fill the digrams frequency table
        int stringPositionCounter = 0;

        // i is the index of a symbol in the input discretized string
        for (int i = 0; i < grammar.workString.size(); i++) {

          Symbol r = grammar.workString.get(i);

          string.add(r);

          // take care about digram frequencies
          if (stringPositionCounter > 0) {

            StringBuffer digramStr = new StringBuffer();
            digramStr.append(string.get(stringPositionCounter - 1).toString()).append(SPACE)
                .append(string.get(stringPositionCounter).toString());

            DigramFrequencyEntry entry = digramFrequencies.get(digramStr.toString());
            if (null == entry) {
              digramFrequencies.put(new DigramFrequencyEntry(digramStr.toString(), 1,
                  stringPositionCounter - 1));
            }
            else {
              digramFrequencies.incrementFrequency(entry, 1);
            }

          }

          // go on
          stringPositionCounter++;
        }

        // consoleLogger.debug("String length " + string.size() + " unique digrams "
        // + digramFrequencies.size());

        DigramFrequencyEntry entry;
        while ((entry = digramFrequencies.getTop()) != null && entry.getFrequency() >= 2) {

          // take the most frequent rule
          //
          // DigramFrequencyEntry entry = digramFrequencies.getTop();

          // consoleLogger.info("re-pair iteration, digram \"" + entry.getDigram() +
          // "\", frequency: "
          // + entry.getFrequency());

          // consoleLogger.debug("Going to substitute the digram " + entry.getDigram()
          // + " first occurring at position " + entry.getFirstOccurrence() + " with frequency "
          // + entry.getFrequency() + ", '" + string.get(entry.getFirstOccurrence()) + SPACE
          // + string.get(entry.getFirstOccurrence() + 1) + "'");

          // create new rule
          //
          ParallelRePairRule r = new ParallelRePairRule(grammar);
          r.setFirst(string.get(entry.getFirstOccurrence()));
          r.setSecond(string.get(entry.getFirstOccurrence() + 1));
          // r.assignLevel();

          // substitute each digram entry with a rule
          //
          String digramToSubstitute = entry.getDigram();
          int currentIndex = entry.getFirstOccurrence();
          while (currentIndex < string.size() - 1) {

            StringBuffer currentDigram = new StringBuffer();
            currentDigram.append(string.get(currentIndex).toString()).append(SPACE)
                .append(string.get(currentIndex + 1).toString());

            if (digramToSubstitute.equalsIgnoreCase(currentDigram.toString())) {
              // consoleLogger.debug(" next digram occurrence is at  " + currentIndex + ", '"
              // + string.get(currentIndex) + SPACE + string.get(currentIndex + 1) + "'");

              // correct entries at left and right
              if (currentIndex > 0) {
                // taking care about immediate neighbor
                removeDigramFrequencyEntry(currentIndex - 1, string, digramFrequencies);
              }
              if (currentIndex < string.size() - 2) {
                removeDigramFrequencyEntry(currentIndex + 1, string, digramFrequencies);
              }

              // create the new guard to insert
              ParallelRePairGuard g = new ParallelRePairGuard(r);
              g.setStringPosition(string.get(currentIndex).getStringPosition());
              r.addPosition(string.get(currentIndex).getStringPosition());
              substituteDigramAt(currentIndex, g, string, digramFrequencies);
              // print(digramFrequencies);
            }
            currentIndex++;
          }

          // consoleLogger.debug("*** iteration finished, top count "
          // + digramFrequencies.getTop().getFrequency());
        }

        grammar.setR0String(stringToDisplay(string));

        grammar.expandRules();

        grammar.workString = string;

        // System.out.println("Final grammar:");
        // System.out.println("R0 -> " + grammar.r0String);
        // for (int i = 1; i < grammar.theRules.size() + 1; i++) {
        // ParallelRePairRule r = grammar.theRules.get(i);
        // System.out.println(r.getRuleName() + " -> " + r.toRuleString() + " : "
        // + r.expandedRuleString + ", " + r.positions);
        // }

        grammar.expandR0();
        // System.out.println("Recovered FINAL String:\n" + grammar.r0ExpandedString);

        return grammar;
      }

      iterationCounter++;
    }

    return null;
  }

  private static String stringToDisplay(ArrayList<Symbol> string) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < string.size(); i++) {
      sb.append(string.get(i).toString()).append(SPACE);
    }
    return sb.toString();
  }

  private static void substituteDigramAt(Integer currentIndex, ParallelRePairGuard guard,
      ArrayList<Symbol> string, DigramFrequencies digramFrequencies) {

    // create entry for two new digram
    //
    StringBuffer digram = new StringBuffer();
    digram.append(string.get(currentIndex).toString()).append(SPACE)
        .append(string.get(currentIndex + 1));

    // consoleLogger.debug("  substituting the digram " + digram + " at " + currentIndex + " with "
    // + g.toString());

    // if (currentIndex > 0) {
    // // consoleLogger.debug("   previous " + string.get(currentIndex - 1).toString());
    // }
    // if (currentIndex < string.size() - 2) {
    // // consoleLogger.debug("   next " + string.get(currentIndex + 2).toString());
    // }

    // // update the new left digram frequency
    // //
    // if (currentIndex > 0) {
    // StringBuffer newDigram = new StringBuffer();
    // newDigram.append(string.get(currentIndex - 1).toString()).append(SPACE)
    // .append(guard.toString());
    // // consoleLogger.debug("   updating the frequency entry for digram " + newDigram.toString());
    // DigramFrequencyEntry entry = digramFrequencies.get(newDigram.toString());
    // if (null == entry) {
    // if (newDigram.indexOf() < 0) {
    // digramFrequencies
    // .put(new DigramFrequencyEntry(newDigram.toString(), 1, currentIndex - 1));
    // }
    // }
    // else {
    // // digramFrequencies.incrementFrequency(entry, 1);
    // // if (currentIndex - 1 < entry.getFirstOccurrence()) {
    // // entry.setFirstOccurrence(currentIndex - 1);
    // // }
    // throw new RuntimeException("Seen R in digrams!");
    // }
    // }
    //
    // // update the new right digram frequency
    // //
    // if (currentIndex < string.size() - 2) {
    // StringBuffer newDigram = new StringBuffer();
    // newDigram.append(guard.toString()).append(SPACE).append(string.get(currentIndex + 2));
    // // consoleLogger.debug("   updating the frequency entry for digram " + newDigram.toString());
    // DigramFrequencyEntry entry = digramFrequencies.get(newDigram.toString());
    // if (null == entry) {
    // if (newDigram.indexOf() < 0) {
    // digramFrequencies.put(new DigramFrequencyEntry(newDigram.toString(), 1, currentIndex));
    // }
    // }
    // else {
    // // digramFrequencies.incrementFrequency(entry, 1);
    // // if (currentIndex + 1 < entry.getFirstOccurrence()) {
    // entry.setFirstOccurrence(currentIndex);
    // // }
    // throw new RuntimeException("Seen R in digrams!");
    // }
    // }

    // remove and substitute
    //
    // 1. decrease to be substituted digram frequency
    //
    // consoleLogger.debug("   updating the frequency entry for digram " + digram.toString());
    DigramFrequencyEntry entry = digramFrequencies.get(digram.toString());
    if (1 == entry.getFrequency()) {
      // consoleLogger.debug("    removing the frequency entry");
      digramFrequencies.remove(digram.toString());
    }
    else {
      // consoleLogger.debug("    setting the frequency entry to "
      // + Integer.valueOf(entry.getFrequency() - 1));
      digramFrequencies.incrementFrequency(entry, -1);
      if (currentIndex == entry.getFirstOccurrence()) {
        // consoleLogger.debug("    this was an index entry, finding another digram index...");
        for (int i = currentIndex + 1; i < string.size() - 1; i++) {
          StringBuffer cDigram = new StringBuffer();
          cDigram.append(string.get(i).toString()).append(SPACE)
              .append(string.get(i + 1).toString());
          if (digram.toString().equals(cDigram.toString())) {
            // consoleLogger.debug("   for digram " + cDigram.toString() + " new index " + i);
            entry.setFirstOccurrence(i);
            break;
          }
        }
      }
    }
    // 2. substitute
    string.set(currentIndex, guard);
    // consoleLogger.debug("   deleting symbol " + string.get(currentIndex + 1).toString() + " at "
    // + Integer.valueOf(currentIndex + 1));
    // 3. delete
    string.remove(Integer.valueOf(currentIndex + 1).intValue());

    // need to take care about all the indexes
    // as all the indexes above _currentIndex_ shall be shifted by -1
    // NO NEED for TLinkedList<Symbol> string = new TLinkedList<Symbol>();
    // HashMap<String, int[]> digramFrequencies = new HashMap<String, int[]>();
    //
    // traverse the string to the right decreasing indexes
    for (Entry<String, DigramFrequencyEntry> e : digramFrequencies.getEntries().entrySet()) {
      int idx = e.getValue().getFirstOccurrence();
      if (idx >= currentIndex + 2) {
        // consoleLogger.debug("   shifting entry for  " + e.getValue().getDigram() + " from "
        // + e.getValue().getFirstOccurrence() + " to " + Integer.valueOf(idx - 1));
        e.getValue().setFirstOccurrence(idx - 1);
      }
    }

  }

  private static void removeDigramFrequencyEntry(int index, ArrayList<Symbol> string,
      DigramFrequencies digramFrequencies) {

    StringBuffer digramToRemove = new StringBuffer();
    digramToRemove.append(string.get(index).toString()).append(SPACE)
        .append(string.get(index + 1).toString());

    // if (digramToRemove.indexOf("R") >= 0) {
    // return;
    // // throw new RuntimeException("Unable to work with R containg digrams!");
    // }

    DigramFrequencyEntry digramEntry = digramFrequencies.get(digramToRemove.toString());
    if (null == digramEntry) {
      return;
    }

    if (digramEntry.getFrequency() == 1) {
      digramFrequencies.remove(digramToRemove.toString());
      // consoleLogger.debug("  completely removing the frequency entry for digram "
      // + digramToRemove.toString() + " at position " + index);
    }
    else {
      // consoleLogger.debug("  decreasing the frequency entry for digram "
      // + digramToRemove.toString() + " at position " + index + " from "
      // + digramEntry.getFrequency() + " to " + Integer.valueOf(digramEntry.getFrequency() - 1));
      digramFrequencies.incrementFrequency(digramEntry, -1);
      if (index == digramEntry.getFirstOccurrence()) {
        // consoleLogger.debug("  this was an index entry, finding another digram index...");
        for (int i = index + 1; i < string.size() - 1; i++) {
          StringBuffer cDigram = new StringBuffer();
          cDigram.append(string.get(i).toString()).append(SPACE)
              .append(string.get(i + 1).toString());
          if (digramToRemove.toString().equals(cDigram.toString())) {
            // consoleLogger.debug("   for digram " + cDigram.toString() + " new index " + i);
            digramEntry.setFirstOccurrence(i);
            break;
          }
        }
      }
    }

  }

  /**
   * Counts spaces in the string.
   * 
   * @param str the string to process.
   * @return number of spaces found.
   */
  @SuppressWarnings("unused")
  private static int countSpaces(String str) {
    if (null == str) {
      return -1;
    }
    int counter = 0;
    for (int i = 0; i < str.length(); i++) {
      if (str.charAt(i) == ' ') {
        counter++;
      }
    }
    return counter;
  }
}
