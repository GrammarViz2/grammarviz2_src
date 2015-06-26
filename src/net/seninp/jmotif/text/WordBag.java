package net.seninp.jmotif.text;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implements a word bag. Word bag is the container for pairs word-frequency. It is not text, it is
 * usually a processed text.
 * 
 * @author psenin
 * 
 */
public class WordBag implements Cloneable {

  private static final String CR = "\n";
  private HashMap<String, AtomicInteger> words;
  private String label;
  private int cachedMax;
  private boolean changed = true;
  private double cachedAverage;

  /**
   * Constructor.
   * 
   * @param bagLabel The name for the collection.
   */
  public WordBag(String bagLabel) {
    super();
    this.label = bagLabel.substring(0);
    this.words = new HashMap<String, AtomicInteger>();
  }

  /**
   * Constructor
   * 
   * @param bagName The name for the collection.
   * @param words The words data for the collection.
   */
  public WordBag(String bagName, HashMap<String, Integer> words) {
    this.label = bagName;
    this.words = new HashMap<String, AtomicInteger>();
    for (Entry<String, Integer> e : words.entrySet()) {
      this.words.put(e.getKey(), new AtomicInteger(e.getValue()));
    }
  }

  /**
   * Set the new label on this bag.
   * 
   * @param newBagLabel The new label.
   */
  public synchronized void setLabel(String newBagLabel) {
    this.label = newBagLabel;
  }

  /**
   * Get the wordbag id or name.
   * 
   * @return the label string.
   */
  public synchronized String getLabel() {
    return this.label;
  }

  /**
   * Add the word into the bag.
   * 
   * @param word The word to add.
   */
  public synchronized void addWord(String word) {
    this.changed = true;
    if (this.words.containsKey(word)) {
      this.words.get(word).incrementAndGet();
    }
    else {
      this.words.put(word, new AtomicInteger(1));
    }
  }

  /**
   * Add the word into the dictionary.
   * 
   * @param word The word.
   * @param frequency Word's frequency.
   */
  public synchronized void addWord(String word, Integer frequency) {
    this.changed = true;
    if (this.words.containsKey(word)) {
      int newFreq = this.words.get(word).intValue() + frequency;
      if (0 >= newFreq) {
        this.words.remove(word);
      }
      else {
        this.words.get(word).set(newFreq);
      }
    }
    else {
      if (frequency > 0) {
        this.words.put(word, new AtomicInteger(frequency));
      }else{
        System.out.println("!!! oops");
      }
    }
  }

  /**
   * Get the word occurrence frequency, if word is not in returns 0.
   * 
   * @param word The word to look for.
   * @return The word frequency.
   */
  public synchronized Integer getWordFrequency(String word) {
    if (this.words.containsKey(word)) {
      return this.words.get(word).intValue();
    }
    return 0;
  }

  /**
   * Quick check if the word is in the text.
   * 
   * @param word The word to check for.
   * @return True if the word seen in text.
   */
  public synchronized boolean contains(String word) {
    return this.words.keySet().contains(word);
  }

  /**
   * Get the words set.
   * 
   * @return The words set.
   */
  public synchronized Collection<String> getWordSet() {
    return this.words.keySet();
  }

  /**
   * Get the words collection along with frequencies.
   * 
   * @return The map of words as keys with frequencies as values.
   */
  public synchronized HashMap<String, Double> getWordsAsDoubles() {
    HashMap<String, Double> res = new HashMap<String, Double>();
    for (Entry<String, AtomicInteger> e : this.words.entrySet()) {
      res.put(e.getKey(), e.getValue().doubleValue());
    }
    return res;
  }

  /**
   * Get the words collection along with frequencies.
   * 
   * @return The map of words as keys with frequencies as values.
   */
  public synchronized HashMap<String, Integer> getWords() {
    HashMap<String, Integer> res = new HashMap<String, Integer>(this.words.size());
    for (Entry<String, AtomicInteger> e : this.words.entrySet()) {
      res.put(e.getKey(), e.getValue().intValue());
    }
    return res;
  }

  /**
   * Get the words collection along with frequencies.
   * 
   * @return The map of words as keys with frequencies as values.
   */
  public synchronized HashMap<String, AtomicInteger> getInternalWords() {
    return this.words;
  }

  /**
   * Integral of all frequency values.
   * 
   * @return sum of all frequency values.
   */
  public synchronized int getTotalWordCount() {
    int res = 0;
    for (AtomicInteger count : this.words.values()) {
      res = res + count.intValue();
    }
    return res;
  }

  /**
   * Implements merge operation for this bag with some other bag. Resulting words set is the union
   * of words from two bags, and resulting frequencies are the sum of two frequencies.
   * 
   * @param otherBag The bag to merge with.
   */
  public synchronized void mergeWith(WordBag otherBag) {
    this.changed = true;
    for (Entry<String, Integer> entry : otherBag.getWords().entrySet())
      if (this.words.containsKey(entry.getKey())) {
        this.words.get(entry.getKey()).set(
            words.get(entry.getKey()).intValue()
                + otherBag.getWordFrequency(entry.getKey()).intValue());
      }
      else {
        this.words.put(entry.getKey(), new AtomicInteger(entry.getValue()));
      }
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(this.label).append(CR);
    for (Entry<String, AtomicInteger> entry : this.words.entrySet()) {
      sb.append(entry.getKey()).append("\t").append(entry.getValue().intValue()).append(CR);
    }
    return sb.toString();
  }

  public String toColumn() {
    StringBuffer sb = new StringBuffer();
    for (Entry<String, AtomicInteger> entry : this.words.entrySet()) {
      for (int i = 0; i < entry.getValue().intValue(); i++) {
        sb.append(entry.getKey()).append(CR);
      }
    }
    return sb.toString();
  }

  /**
   * Get the maximal observed frequency. Useful for normalized tf.
   * 
   * @return
   */
  public synchronized int getMaxFrequency() {
    if (changed) {
      this.cachedMax = 0;
      for (AtomicInteger num : this.words.values()) {
        if (this.cachedMax < num.intValue()) {
          this.cachedMax = num.intValue();
        }
      }
      this.changed = false;
      return this.cachedMax;
    }
    return this.cachedMax;
  }

  public double getAverageFrequency() {
    if (changed) {
      int res = 0;
      for (AtomicInteger num : this.words.values()) {
        res = res + num.intValue();
      }
      this.cachedAverage = (double) res / (double) this.words.size();
      this.changed = false;
      return this.cachedAverage;
    }
    return this.cachedAverage;
  }

  @Override
  public WordBag clone() {
    WordBag res = new WordBag(this.label);
    for (Entry<String, AtomicInteger> w : this.words.entrySet()) {
      res.addWord(w.getKey(), w.getValue().intValue());
    }
    return res;
  }
}
