package edu.hawaii.jmotif.sax.datastructures;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 * The SAX data structure. Implements optimized storage for the SAX data.
 * 
 * @author Pavel Senin.
 * 
 */
public class SAXFrequencyData implements Iterable<SAXFrequencyEntry> {

  /** This map keeps SAX words and arrays of their indexes. */
  private HashMap<String, SAXFrequencyEntry> stringToEntries;

  /** This is a reverse index it is not built unless the method needed it is called. */
  private HashMap<Integer, String> indexesToWords;

  /** This is a simple index of all indexes, but it is also not built until called for. */
  private ArrayList<Integer> allIndexes;

  /**
   * Constructor.
   */
  public SAXFrequencyData() {
    super();
    this.stringToEntries = new HashMap<String, SAXFrequencyEntry>();
    this.indexesToWords = new HashMap<Integer, String>();
    this.allIndexes = new ArrayList<Integer>();
  }

  /**
   * Put the substring with it's index into the storage.
   * 
   * @param substring The substring value.
   * @param idx The substring entry index.
   */
  public void put(char[] substring, int idx) {
    SAXFrequencyEntry sfe = this.stringToEntries.get(String.valueOf(substring));
    if (null == sfe) {
      this.stringToEntries.put(String.valueOf(substring), new SAXFrequencyEntry(substring, idx));
    }
    else {
      sfe.put(idx);
    }
  }

  /**
   * Get the internal hash size.
   * 
   * @return The number of substrings in the data structure.
   */
  public Integer size() {
    return this.stringToEntries.size();
  }

  /**
   * Check if the data includes substring.
   * 
   * @param substring The query substring.
   * @return TRUE is contains, FALSE if not.
   */
  public boolean contains(String substring) {
    return this.stringToEntries.containsKey(substring);
  }

  /**
   * Get the entry information.
   * 
   * @param substring The key get entry for.
   * @return The entry containing the substring occurrence frequency information.
   */
  public SAXFrequencyEntry get(String substring) {
    return this.stringToEntries.get(substring);
  }

  /**
   * Get the set of sorted by the occurrence array size.
   * 
   * @return The set of sorted by the occurrence frequency entries.
   */
  protected List<SAXFrequencyEntry> getSortedFrequencies() {
    List<SAXFrequencyEntry> l = new ArrayList<SAXFrequencyEntry>();
    l.addAll(this.stringToEntries.values());
    Collections.sort(l);
    return l;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<SAXFrequencyEntry> iterator() {
    return this.stringToEntries.values().iterator();
  }

  /**
   * Get all SAX subsequences as one string separated by a specified string.
   * 
   * @param separator The separator.
   * @return SAX all SAX words as a string.
   * @throws IOException
   */
  public String getSAXString(String separator) throws IOException {

    // hash mapping, position -> word
    //
    this.indexesToWords = new HashMap<Integer, String>();

    // all timeseries indexes where word is mapped to
    //
    allIndexes = new ArrayList<Integer>();

    // iterate over all the frequency entries filling up above data structures
    //
    Iterator<SAXFrequencyEntry> freqIterator = iterator();
    while (freqIterator.hasNext()) {

      SAXFrequencyEntry freqEntry = freqIterator.next();
      ArrayList<Integer> entryOccurrences = freqEntry.getEntries();

      // save words
      for (int index : entryOccurrences) {
        indexesToWords.put(index, String.valueOf(freqEntry.getSubstring()));
      }

      // save indexes
      this.allIndexes.addAll(entryOccurrences);

    }

    // sort by the position
    //
    Collections.sort(allIndexes, new Comparator<Integer>() {
      public int compare(Integer int1, Integer int2) {
        return Integer.valueOf(int1).compareTo(Integer.valueOf(int2));
      }
    });

    // make a string
    //
    StringBuilder sb = new StringBuilder();
    for (int index : allIndexes) {
      sb.append(indexesToWords.get(index));
      sb.append(separator);
    }

    return sb.toString();

  }

  /**
   * Get all SAX subsequences as one string separated by a specified string.
   * 
   * @param separator The separator.
   * @return SAX all SAX words as a string.
   * @throws IOException
   */
  public String getSAXStringWithLog(String separator, String prefix) throws IOException {

    // hash mapping, position -> word
    //
    this.indexesToWords = new HashMap<Integer, String>();

    // all timeseries indexes where word is mapped to
    //
    allIndexes = new ArrayList<Integer>();

    // iterate over all the frequency entries filling up above data structures
    //
    Iterator<SAXFrequencyEntry> freqIterator = iterator();
    while (freqIterator.hasNext()) {

      SAXFrequencyEntry freqEntry = freqIterator.next();
      ArrayList<Integer> entryOccurrences = freqEntry.getEntries();

      // save words
      for (int index : entryOccurrences) {
        indexesToWords.put(index, String.valueOf(freqEntry.getSubstring()));
      }

      // save indexes
      this.allIndexes.addAll(entryOccurrences);

    }

    // sort by the position
    //
    Collections.sort(allIndexes, new Comparator<Integer>() {
      public int compare(Integer int1, Integer int2) {
        return Integer.valueOf(int1).compareTo(Integer.valueOf(int2));
      }
    });

    BufferedWriter bw = new BufferedWriter(new FileWriter(new File(prefix + "words_stat.txt")));

    // make a string
    //
    StringBuilder sb = new StringBuilder();
    for (int index : allIndexes) {
      sb.append(indexesToWords.get(index));
      sb.append(separator);
      bw.write(index + "," + indexesToWords.get(index) + "\n");
    }
    bw.close();

    return sb.toString();

  }

  public HashMap<Integer, String> getPositionsAndWords() {
    return indexesToWords;
  }

  public ArrayList<Integer> getAllIndices() {
    return this.allIndexes;
  }

  public void save(BufferedWriter bw) {
    StringBuffer sb = new StringBuffer();

    sb.append(this.allIndexes.toString().replace("[", "").replace("]", "").replace(",", ""));
    sb.append("\n//\n");

    for (Entry<Integer, String> e : this.indexesToWords.entrySet()) {
      sb.append(e.getKey() + " " + e.getValue() + "\n");
    }
    sb.append("///\n");

    for (Entry<String, SAXFrequencyEntry> e : this.stringToEntries.entrySet()) {
      sb.append(e.getKey() + " " + e.getValue() + "\n");
    }

    try {
      bw.write(sb.toString());
    }
    catch (IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

  }

  public void read(BufferedReader br) throws IOException {

    String line = br.readLine();
    String[] split = line.split(" ");
    this.allIndexes = new ArrayList<Integer>(split.length);
    for (String s : split) {
      this.allIndexes.add(Integer.valueOf(s));
    }
    line = br.readLine();
    line = br.readLine();

    this.indexesToWords = new HashMap<Integer, String>();
    while (!line.startsWith("///")) {
      System.out.println(line);
      split = line.split(" ");
      this.indexesToWords.put(Integer.valueOf(split[0].trim()), split[1].trim());
      line = br.readLine();
    }

    this.stringToEntries = new HashMap<String, SAXFrequencyEntry>();
    while ((line = br.readLine()) != null) {
      split = line.split(" -> ");
      String[] split1 = split[0].split(" ");
      String[] split2 = split[1].split(" ");
      SAXFrequencyEntry e = new SAXFrequencyEntry(split1[1].toCharArray(),
          Integer.valueOf(split2[0]));
      for (int i = 0; i < split2.length; i++) {
        e.put(Integer.valueOf(split2[i]));
      }
      this.stringToEntries.put(split1[0].trim(), e);
    }

  }

}
