package net.seninp.jmotif.text;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

public class BigramBag {

  private String label;

  private HashMap<Bigram, Integer> bigrams;

  public BigramBag(String label) {
    super();
    this.label = label.substring(0);
    this.bigrams = new HashMap<Bigram, Integer>();
  }

  public void mergeWith(BigramBag otherBag) {
    for (Entry<Bigram, Integer> entry : otherBag.getBigrams().entrySet())
      if (this.bigrams.containsKey(entry.getKey())) {
        int newValue = this.bigrams.get(entry.getKey()) + entry.getValue();
        this.bigrams.put(entry.getKey(), newValue);
      }
      else {
        this.bigrams.put(entry.getKey(), entry.getValue());
      }
  }

  public HashMap<Bigram, Integer> getBigrams() {
    return this.bigrams;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void add(Bigram bgram) {
    int frequency = 1;
    if (this.bigrams.containsKey(bgram)) {
      frequency = this.bigrams.get(bgram) + 1;
    }
    this.bigrams.put(bgram, frequency);
  }

  public Collection<Bigram> getBigramSet() {
    return this.bigrams.keySet();
  }

  public int getTotalWordCount() {
    int res = 0;
    for (int count : this.bigrams.values()) {
      res = res + count;
    }
    return res;
  }

  public boolean contains(Bigram word) {
    return this.bigrams.keySet().contains(word);
  }

  public HashMap<Bigram, Double> getBigramsAsDoubles() {
    HashMap<Bigram, Double> res = new HashMap<Bigram, Double>();
    for (Entry<Bigram, Integer> e : this.bigrams.entrySet()) {
      res.put(e.getKey(), e.getValue().doubleValue());
    }
    return res;
  }

}
