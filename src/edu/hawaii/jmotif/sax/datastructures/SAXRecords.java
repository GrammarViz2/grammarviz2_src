package edu.hawaii.jmotif.sax.datastructures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import edu.hawaii.jmotif.logic.JmotifMapEntry;

/**
 * The collection for SAXRecords. This datastructure is used in the parallel SAX implementation.
 * 
 * @author psenin
 * 
 */
public class SAXRecords implements Iterable<SaxRecord> {

  /** The id is used to identify the chunk. */
  private long id;

  /** All the SAX records */
  private HashMap<String, SaxRecord> records;

  /** The index of occurrences, key is the position in the time series. */
  private SortedMap<Integer, SaxRecord> realTSindex;

  /** The mapping from SAX string positions to real time series positions. */
  private HashMap<Integer, Integer> stringPosToRealPos;

  /**
   * Disable this.
   */
  @SuppressWarnings("unused")
  private SAXRecords() {
    super();
  }

  /**
   * The recommended constructor.
   * 
   * @param id The structure id.
   */
  public SAXRecords(long id) {
    super();
    this.id = id;
    this.records = new HashMap<String, SaxRecord>();
    this.realTSindex = new TreeMap<Integer, SaxRecord>();
  }

  /**
   * Returns an iterator which is backed by a tree (i.e. sorted) map.
   */
  @Override
  public Iterator<SaxRecord> iterator() {
    Iterator<SaxRecord> res = this.realTSindex.values().iterator();
    return res;
  }

  /**
   * Adds a single string and index entry by creating a SAXRecord.
   * 
   * @param str The string.
   * @param idx The index.
   */
  public void add(char[] str, int idx) {
    SaxRecord rr = records.get(String.valueOf(str));
    if (null == rr) {
      rr = new SaxRecord(str, idx);
      this.records.put(String.valueOf(str), rr);
    }
    else {
      rr.addIndex(idx);
    }
    this.realTSindex.put(idx, rr);
  }

  /**
   * Gets an entry by the index.
   * 
   * @param idx The index.
   * @return The entry.
   */
  public SaxRecord getByIndex(int idx) {
    return realTSindex.get(idx);
  }

  /**
   * Drops a single entry.
   * 
   * @param idx the index.
   */
  public void dropByIndex(int idx) {
    SaxRecord entry = realTSindex.get(idx);
    if (null == entry) {
      return;
    }
    // how many things in da index
    if (1 == entry.getIndexes().size()) {
      // dropping the entry completely off
      realTSindex.remove(idx);
      records.remove(String.valueOf(entry.getPayload()));
    }
    else {
      // dropping off just a single index
      entry.removeIndex(idx);
      realTSindex.remove(idx);
    }
  }

  /**
   * Get the id.
   * 
   * @return the id.
   */
  public long getId() {
    return this.id;
  }

  /**
   * Adds all entries from the collection.
   * 
   * @param chunk The collection.
   */
  public void addAll(SAXRecords chunk) {
    for (SaxRecord rec : chunk) {

      char[] payload = rec.getPayload();
      ArrayList<Integer> indexes = rec.getIndexes();

      // treat the premier index entry
      SaxRecord rr = this.records.get(String.valueOf(payload));
      if (null == rr) {
        rr = new SaxRecord(payload, indexes.get(0));
        this.records.put(String.valueOf(payload), rr);
        this.realTSindex.put(indexes.get(0), rr);
      }
      else {
        rr.addIndex(indexes.get(0));
        this.realTSindex.put(indexes.get(0), rr);
      }

      // and here the rest of indexes
      for (int i = 1; i < indexes.size(); i++) {
        rr.addIndex(indexes.get(i));
        this.realTSindex.put(indexes.get(i), rr);
      }

    }
  }

  /**
   * Finds the minimal index value.
   * 
   * @return the minimal index value.
   */
  public int getMinIndex() {
    return Collections.min(this.realTSindex.keySet());
  }

  /**
   * Finds the maximal index value.
   * 
   * @return the maximal index value.
   */
  public int getMaxIndex() {
    return Collections.max(this.realTSindex.keySet());
  }

  /**
   * Get the collection size in indexes.
   * 
   * @return the collection size in indexes.
   */
  public int size() {
    return this.realTSindex.size();
  }

  /**
   * Get all the indexes.
   * 
   * @return all the indexes.
   */
  public Set<Integer> getIndexes() {
    return this.realTSindex.keySet();
  }

  /**
   * Get the SAX string of this whole collection.
   * 
   * @param separatorToken The separator token to use for the string.
   * 
   * @return The whole data as a string.
   */
  public String getSAXString(String separatorToken) {
    StringBuffer sb = new StringBuffer();
    ArrayList<Integer> index = new ArrayList<Integer>();
    index.addAll(this.realTSindex.keySet());
    Collections.sort(index, new Comparator<Integer>() {
      public int compare(Integer int1, Integer int2) {
        return Integer.valueOf(int1).compareTo(Integer.valueOf(int2));
      }
    });
    for (int i : index) {
      sb.append(this.realTSindex.get(i).getPayload()).append(separatorToken);
    }
    return sb.toString();
  }

  /**
   * Get all indexes.
   * 
   * @return all the indexes.
   */
  public SortedSet<Integer> getAllIndices() {
    return (SortedSet<Integer>) this.realTSindex.keySet();
  }

  /**
   * This build an index of digrams.
   * 
   * @return the map whose keys are digrams, while values are the same digram and it occurrence
   * indexes.
   */
  public HashMap<String, JmotifMapEntry<String, ArrayList<Integer>>> getDigramFrequencies() {
    // the resulting structure
    HashMap<String, JmotifMapEntry<String, ArrayList<Integer>>> res = new HashMap<String, JmotifMapEntry<String, ArrayList<Integer>>>();

    // iterating over whole set
    Entry<Integer, SaxRecord> previousSAXWord = null;
    int counter = 0;
    for (Entry<Integer, SaxRecord> currentEntry : this.realTSindex.entrySet()) {
      if (null == previousSAXWord) {
        previousSAXWord = currentEntry;
        continue;
      }

      StringBuffer sb = new StringBuffer();

      sb.append(previousSAXWord.getValue().getPayload()).append(" ")
          .append(currentEntry.getValue().getPayload());

      JmotifMapEntry<String, ArrayList<Integer>> entry = res.get(sb.toString());
      if (null == entry) {
        entry = new JmotifMapEntry<String, ArrayList<Integer>>(sb.toString(),
            new ArrayList<Integer>());
        res.put(sb.toString(), entry);
      }

      entry.getValue().add(counter);

      counter++;
      previousSAXWord = currentEntry;
    }
    return res;
  }

  /**
   * This builds an index that aids in mapping of a SAX word to the real timeseries index.
   */
  public void buildIndex() {
    this.stringPosToRealPos = new HashMap<Integer, Integer>();
    int counter = 0;
    for (Integer idx : getAllIndices()) {
      this.stringPosToRealPos.put(counter, idx);
      counter++;
    }
  }

  /**
   * This maps an index of the word in the output string to the real position in time-series.
   * 
   * @param idx the index to map.
   * @return the position in the time-series.
   */
  public Integer mapStringIndexToTSPosition(int idx) {
    return this.stringPosToRealPos.get(idx);
  }

  /**
   * Get a SAX record by the string key.
   * 
   * @param str The query string.
   * @return the record if exists.
   */
  public SaxRecord getByWord(String str) {
    return records.get(str);
  }

}
