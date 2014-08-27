package edu.hawaii.jmotif.gi.repair;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.hawaii.jmotif.sax.datastructures.SAXRecords;
import edu.hawaii.jmotif.sax.datastructures.SaxRecord;

public final class RePairFactory {

  private static final char SPACE = ' ';

  // logging stuff
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.WARN;
  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(RePairFactory.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  /**
   * Disable constructor.
   */
  private RePairFactory() {
    assert true;
  }

  public static RePairRule buildGrammar(SAXRecords saxRecords) {

    consoleLogger.debug("Starting RePair with an input string of " + saxRecords.getIndexes().size()
        + " words.");

    RePairRule.numRules = new AtomicInteger(0);
    RePairRule.theRules = new Hashtable<Integer, RePairRule>();

    RePairRule theRule = new RePairRule();

    // get all indexes and sort them
    Set<Integer> index = saxRecords.getIndexes();
    Integer[] sortedSAXWords = index.toArray(new Integer[index.size()]);

    // two data structures
    //
    // 1.0. - the string
    ArrayList<Symbol> string = new ArrayList<Symbol>();
    // LinkedList<Symbol> string = new LinkedList<Symbol>();

    //
    // 2.0. - the digram frequency table, digram, frequency, and the first occurrence index
    DigramFrequencies digramFrequencies = new DigramFrequencies();

    // build data structures
    int stringPositionCounter = 0;
    for (Integer saxWordPosition : sortedSAXWords) {
      // i is the index of a symbol in the input discretized string
      // counter is the index in the grammar rule R0 string
      SaxRecord r = saxRecords.getByIndex(saxWordPosition);
      Symbol symbol = new Symbol(r, stringPositionCounter);
      // put it into the string
      string.add(symbol);
      // and into the index
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

    consoleLogger.debug("String length " + string.size() + " unique digrams "
        + digramFrequencies.size());

    DigramFrequencyEntry entry;
    while ((entry = digramFrequencies.getTop()) != null && entry.getFrequency() > 2) {

      // take the most frequent rule
      //
      // Entry<String, int[]> entry = entries.get(0);
      // DigramFrequencyEntry entry = digramFrequencies.getTop();

      consoleLogger.info("re-pair iteration, digram \"" + entry.getDigram() + "\", frequency: "
          + entry.getFrequency());

      consoleLogger.debug("Going to substitute the digram " + entry.getDigram()
          + " first occurring at position " + entry.getFirstOccurrence() + " with frequency "
          + entry.getFrequency() + ", '" + string.get(entry.getFirstOccurrence()) + SPACE
          + string.get(entry.getFirstOccurrence() + 1) + "'");

      // create new rule
      //
      RePairRule r = new RePairRule();
      r.setFirst(string.get(entry.getFirstOccurrence()));
      r.setSecond(string.get(entry.getFirstOccurrence() + 1));
      r.assignLevel();

      // substitute each digram entry with a rule
      //
      String digramToSubstitute = entry.getDigram();
      int currentIndex = entry.getFirstOccurrence();
      while (currentIndex < string.size() - 1) {

        StringBuffer currentDigram = new StringBuffer();
        currentDigram.append(string.get(currentIndex).toString()).append(SPACE)
            .append(string.get(currentIndex + 1).toString());

        if (digramToSubstitute.equalsIgnoreCase(currentDigram.toString())) {
          consoleLogger.debug(" next digram occurrence is at  " + currentIndex + ", '"
              + string.get(currentIndex) + SPACE + string.get(currentIndex + 1) + "'");

          // correct entries at left and right
          if (currentIndex > 0) {
            // taking care about immediate neighbor
            removeDigramFrequencyEntry(currentIndex - 1, string, digramFrequencies);
          }
          if (currentIndex < string.size() - 2) {
            removeDigramFrequencyEntry(currentIndex + 1, string, digramFrequencies);
          }

          // create the new guard to insert
          RePairGuard g = new RePairGuard(r);
          g.setStringPosition(string.get(currentIndex).getStringPosition());
          r.addPosition(string.get(currentIndex).getStringPosition());
          substituteDigramAt(currentIndex, g, string, digramFrequencies);

        }
        currentIndex++;
      }

      // // sort the entries of digram table by the size of indexes
      // entries = new ArrayList<Entry<String, int[]>>();
      // entries.addAll(digramFrequencies.entrySet());
      // Collections.sort(entries, new Comparator<Entry<String, int[]>>() {
      // @Override
      // public int compare(Entry<String, int[]> o1, Entry<String, int[]> o2) {
      // return -Integer.valueOf(o1.getValue()[0]).compareTo(Integer.valueOf(o2.getValue()[0]));
      // }
      // });

      consoleLogger.debug("*** iteration finished, top count "
          + digramFrequencies.getTop().getFrequency());
    }
    RePairRule.setRuleString(stringToDisplay(string));
    return theRule;
  }

  private static void substituteDigramAt(Integer currentIndex, RePairGuard g,
      ArrayList<Symbol> string, DigramFrequencies digramFrequencies) {

    // create entry for two new digram
    //
    StringBuffer digram = new StringBuffer();
    digram.append(string.get(currentIndex).toString()).append(SPACE)
        .append(string.get(currentIndex + 1));

    consoleLogger.debug("  substituting the digram " + digram + " at " + currentIndex + " with "
        + g.toString());

    if (currentIndex > 0) {
      consoleLogger.debug("   previous " + string.get(currentIndex - 1).toString());
    }
    if (currentIndex < string.size() - 2) {
      consoleLogger.debug("   next " + string.get(currentIndex + 2).toString());
    }

    // update the new left digram frequency
    //
    if (currentIndex > 0) {
      StringBuffer newDigram = new StringBuffer();
      newDigram.append(string.get(currentIndex - 1).toString()).append(SPACE).append(g.toString());
      consoleLogger.debug("   updating the frequency entry for digram " + newDigram.toString());
      DigramFrequencyEntry entry = digramFrequencies.get(newDigram.toString());
      if (null == entry) {
        digramFrequencies.put(new DigramFrequencyEntry(newDigram.toString(), 1, currentIndex - 1));
      }
      else {
        digramFrequencies.incrementFrequency(entry, 1);
        if (currentIndex - 1 < entry.getFirstOccurrence()) {
          entry.setFirstOccurrence(currentIndex - 1);
        }
      }
    }

    // update the new right digram frequency
    //
    if (currentIndex < string.size() - 2) {
      StringBuffer newDigram = new StringBuffer();
      newDigram.append(g.toString()).append(SPACE).append(string.get(currentIndex + 2));
      consoleLogger.debug("   updating the frequency entry for digram " + newDigram.toString());
      DigramFrequencyEntry entry = digramFrequencies.get(newDigram.toString());
      if (null == entry) {
        digramFrequencies.put(new DigramFrequencyEntry(newDigram.toString(), 1, currentIndex));
      }
      else {
        digramFrequencies.incrementFrequency(entry, 1);
        if (currentIndex + 1 < entry.getFirstOccurrence()) {
          entry.setFirstOccurrence(currentIndex);
        }
      }
    }

    // remove and substitute
    //
    // 1. decrease to be substituted digram frequency
    //
    consoleLogger.debug("   updating the frequency entry for digram " + digram.toString());
    DigramFrequencyEntry entry = digramFrequencies.get(digram.toString());
    if (1 == entry.getFrequency()) {
      consoleLogger.debug("    removing the frequency entry");
      digramFrequencies.remove(digram.toString());
    }
    else {
      consoleLogger.debug("    setting the frequency entry to "
          + Integer.valueOf(entry.getFrequency() - 1));
      digramFrequencies.incrementFrequency(entry, -1);
      if (currentIndex == entry.getFirstOccurrence()) {
        consoleLogger.debug("    this was an index entry, finding another digram index...");
        for (int i = currentIndex + 1; i < string.size() - 1; i++) {
          StringBuffer cDigram = new StringBuffer();
          cDigram.append(string.get(i).toString()).append(SPACE)
              .append(string.get(i + 1).toString());
          if (digram.toString().equals(cDigram.toString())) {
            consoleLogger.debug("   for digram " + cDigram.toString() + " new index " + i);
            entry.setFirstOccurrence(i);
            break;
          }
        }
      }
    }
    // 2. substitute
    string.set(currentIndex, g);
    consoleLogger.debug("   deleting symbol " + string.get(currentIndex + 1).toString() + " at "
        + Integer.valueOf(currentIndex + 1));
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

    DigramFrequencyEntry digramEntry = digramFrequencies.get(digramToRemove.toString());

    if (digramEntry.getFrequency() == 1) {
      digramFrequencies.remove(digramToRemove.toString());
      consoleLogger.debug("  completely removing the frequency entry for digram "
          + digramToRemove.toString() + " at position " + index);
    }
    else {
      consoleLogger.debug("  decreasing the frequency entry for digram "
          + digramToRemove.toString() + " at position " + index + " from "
          + digramEntry.getFrequency() + " to " + Integer.valueOf(digramEntry.getFrequency() - 1));
      digramFrequencies.incrementFrequency(digramEntry, -1);
      if (index == digramEntry.getFirstOccurrence()) {
        consoleLogger.debug("  this was an index entry, finding another digram index...");
        for (int i = index + 1; i < string.size() - 1; i++) {
          StringBuffer cDigram = new StringBuffer();
          cDigram.append(string.get(i).toString()).append(SPACE)
              .append(string.get(i + 1).toString());
          if (digramToRemove.toString().equals(cDigram.toString())) {
            consoleLogger.debug("   for digram " + cDigram.toString() + " new index " + i);
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
