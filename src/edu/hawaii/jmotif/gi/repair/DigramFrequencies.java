package edu.hawaii.jmotif.gi.repair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

public class DigramFrequencies {

  private HashMap<String, DigramFrequencyEntry> digramsToEntries;
  private SortedMap<Integer, ArrayList<DigramFrequencyEntry>> bucketsToEntries;

  public DigramFrequencies() {
    super();
    digramsToEntries = new HashMap<String, DigramFrequencyEntry>();
    bucketsToEntries = new TreeMap<Integer, ArrayList<DigramFrequencyEntry>>();
  }

  public void put(DigramFrequencyEntry digramFrequencyEntry) {
    this.digramsToEntries.put(digramFrequencyEntry.getDigram(), digramFrequencyEntry);
    Integer freq = digramFrequencyEntry.getFrequency();
    ArrayList<DigramFrequencyEntry> bucket = this.bucketsToEntries.get(freq);
    if (null == bucket) {
      bucket = new ArrayList<DigramFrequencyEntry>();
      this.bucketsToEntries.put(freq, bucket);
    }
    bucket.add(digramFrequencyEntry);
  }

  public DigramFrequencyEntry get(String string) {
    return this.digramsToEntries.get(string);
  }

  public void incrementFrequency(DigramFrequencyEntry entry, int index) {

    ArrayList<DigramFrequencyEntry> oldBucket = this.bucketsToEntries.get(entry.getFrequency());
    oldBucket.remove(entry);
    if (0 == oldBucket.size() || oldBucket.isEmpty()) {
      this.bucketsToEntries.remove(entry.getFrequency());
    }

    int newFreq = entry.getFrequency() + index;
    entry.setFrequency(newFreq);

    ArrayList<DigramFrequencyEntry> bucket = this.bucketsToEntries.get(newFreq);
    if (null == bucket) {
      bucket = new ArrayList<DigramFrequencyEntry>();
      this.bucketsToEntries.put(newFreq, bucket);
    }
    bucket.add(entry);
  }

  public DigramFrequencyEntry getTop() {
    // System.out.println("** calling top on collection "
    // + Arrays.toString(bucketsToEntries.keySet().toArray(
    // new Integer[bucketsToEntries.keySet().size()])));
    if (bucketsToEntries.keySet().isEmpty()) {
      return null;
    }
    else {
      Integer maxBucket = Collections.max(bucketsToEntries.keySet());
      return bucketsToEntries.get(maxBucket).get(0);
    }
  }

  public void remove(String digram) {
    DigramFrequencyEntry entry = this.digramsToEntries.get(digram);
    int freq = entry.getFrequency();
    ArrayList<DigramFrequencyEntry> bucket = this.bucketsToEntries.get(freq);
    if (!bucket.remove(entry)) {
      throw (new RuntimeException("There was an error!"));
    }
    if (0 == bucket.size() || bucket.isEmpty()) {
      this.bucketsToEntries.remove(entry.getFrequency());
    }
    this.digramsToEntries.remove(entry);
    entry = null;
  }

  public int size() {
    return this.digramsToEntries.size();
  }

  public HashMap<String, DigramFrequencyEntry> getEntries() {
    return digramsToEntries;
  }

}
