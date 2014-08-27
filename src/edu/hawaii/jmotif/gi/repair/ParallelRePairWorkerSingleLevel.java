package edu.hawaii.jmotif.gi.repair;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

/**
 * Implements a Re-Pair worker which combines digram at the single level. The idea is that it can be
 * executed recursively to build a grammar.
 * 
 * @author psenin
 * 
 */
public class ParallelRePairWorkerSingleLevel implements Callable<ParallelGrammarKeeper> {

  private static final char SPACE = ' ';
  private long id;
  private int startIdx;
  private int endIdx;
  private ParallelGrammarKeeper grammar;

  // // logging stuff
  // //
  // private static Logger consoleLogger;
  // private static Level LOGGING_LEVEL = Level.INFO;
  //
  // // static block - we instantiate the logger
  // //
  // static {
  // consoleLogger = (Logger) LoggerFactory.getLogger(ParallelSAXImplementation.class);
  // consoleLogger.setLevel(LOGGING_LEVEL);
  // }

  /**
   * Constructor.
   * 
   * @param id The job id.
   * @param grammar The data structure to draw sequences from.
   * @param firstWord The first word index.
   * @param lastWord The last word index.
   */
  public ParallelRePairWorkerSingleLevel(long id, ParallelGrammarKeeper grammar, int firstWord,
      int lastWord) {
    this.id = id;
    this.grammar = grammar;
    this.startIdx = firstWord;
    this.endIdx = lastWord;
  }

  @Override
  public ParallelGrammarKeeper call() throws Exception {

    // get the result instantiated
    //
    ParallelGrammarKeeper res = new ParallelGrammarKeeper(this.id);
    res.numRules.set(this.grammar.numRules.intValue());

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
    for (int i = this.startIdx; i < this.endIdx; i++) {

      Symbol r = this.grammar.workString.get(i);

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
    // print(digramFrequencies);

    // consoleLogger.debug("String length " + string.size() + " unique digrams "
    // + digramFrequencies.size());

    res.setR0ExpandedString(stringToDisplay(string));

    DigramFrequencyEntry entry;
    while ((entry = digramFrequencies.getTop()) != null && entry.getFrequency() >= 2) {

      // take the most frequent rule
      //
      // DigramFrequencyEntry entry = digramFrequencies.getTop();

      // consoleLogger.info("re-pair iteration, digram \"" + entry.getDigram() + "\", frequency: "
      // + entry.getFrequency());

      // consoleLogger.debug("Going to substitute the digram " + entry.getDigram()
      // + " first occurring at position " + entry.getFirstOccurrence() + " with frequency "
      // + entry.getFrequency() + ", '" + string.get(entry.getFirstOccurrence()) + SPACE
      // + string.get(entry.getFirstOccurrence() + 1) + "'");

      // create new rule
      //
      ParallelRePairRule r = new ParallelRePairRule(res);
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

    res.workString = string;
    res.setR0String(stringToDisplay(string));

    // res.expandRules();

    return res;
  }

  private static void substituteDigramAt(Integer currentIndex, ParallelRePairGuard guard,
      ArrayList<Symbol> string, DigramFrequencies digramFrequencies) {

    // create entry for two new digram
    // TRACE
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

  private static String stringToDisplay(ArrayList<Symbol> string) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < string.size(); i++) {
      sb.append(string.get(i).toString()).append(SPACE);
    }
    return sb.toString();
  }

}
