package edu.hawaii.jmotif.sax.trie;

import java.util.ArrayList;
import java.util.List;

/**
 * The trie structure following the Keogh's and Lin discords discovery article. It is an extension
 * of the Trie class by adding the array of all occurrences in order to speed-up search process to
 * the Magic.
 * 
 * @author Pavel Senin
 * 
 */
public class SAXTrie {

  /** The alphabet size used here. */
  private Integer alphabetSize;

  /** The trie structure. */
  private SAXTrieTree trie;

  /** The frequencies table. */
  private ArrayList<SAXTrieHitEntry> frequencies;

  /**
   * Constructor.
   * 
   * @param seqLength The sequence length - this parameter is needed for the proper array
   * allocation.
   * @param alphabetSize The alphabet size to use for the trie tree construction.
   * @throws TrieException If error occurs.
   */
  public SAXTrie(Integer seqLength, Integer alphabetSize) throws TrieException {
    if (alphabetSize > 10) {
      throw new TrieException(
          "Unable to create a Trie data structiure of size greater than 10! Size of "
              + alphabetSize + " requested.");
    }
    else {
      this.alphabetSize = alphabetSize;
      this.trie = new SAXTrieTree(this.alphabetSize);
      this.frequencies = new ArrayList<SAXTrieHitEntry>(seqLength);
      // init array
      for (int i = 0; i < seqLength; i++) {
        this.frequencies.add(new SAXTrieHitEntry(alphabetSize, i));
      }
    }
  }

  /**
   * Put an actual entrance in the magic trie.
   * 
   * @param str The string.
   * @param idx The position.
   * @throws TrieException If error occurs.
   */
  public void put(String str, int idx) throws TrieException {

    // update the string entry
    this.frequencies.get(idx).setStr(str.toCharArray());

    // update the trie
    List<Integer> allOccurences = this.trie.addOccurence(str, idx);

    // populate updated frequencies
    for (Integer i : allOccurences) {
      this.frequencies.get(i).setFrequency(allOccurences.size());
    }

  }

  /**
   * Get the stored SAX string frequency from the specific position.
   * 
   * @param currPosition The position.
   * @return stored SAX frequency record.
   */
  public SAXTrieHitEntry getSAX(int currPosition) {
    return this.frequencies.get(currPosition);
  }

  /**
   * Get all the occurences for the specific substring.
   * 
   * @param str The substring.
   * @return List of position.
   * @throws TrieException if error occurs.
   */
  public List<Integer> getOccurences(char[] str) throws TrieException {
    return this.trie.getOccurrences(String.valueOf(str));
  }

  /**
   * Get all frequencies stored in this instance. It will not return the occurrences. You need to
   * ask for them.
   * 
   * @return all the internal storage.
   */
  public ArrayList<SAXTrieHitEntry> getFrequencies() {
    // make a copy
    //
    ArrayList<SAXTrieHitEntry> res = new ArrayList<SAXTrieHitEntry>();
    for (SAXTrieHitEntry e : this.frequencies) {
      res.add(e.clone());
    }
    return res;
  }

  public int getAlphabetSize() {
    return this.alphabetSize;
  }
}
