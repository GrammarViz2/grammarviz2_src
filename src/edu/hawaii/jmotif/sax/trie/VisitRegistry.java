package edu.hawaii.jmotif.sax.trie;

import java.util.Arrays;
import java.util.Random;

/**
 * The convenient way to keep track of visited locations.
 * 
 * @author Pavel Senin.
 */
public class VisitRegistry {

  protected byte[] registry; // 1 visited, 0 unvisited
  private int unvisitedCount;
  private Random randomizer = new Random(System.currentTimeMillis());
  private int capacity;

  /**
   * Constructor.
   * 
   * @param capacity The initial capacity.
   */
  public VisitRegistry(int capacity) {

    this.capacity = capacity;

    this.registry = new byte[capacity];

    this.unvisitedCount = capacity;

  }

  protected VisitRegistry() {

  }

  /**
   * Mark as visited certain location.
   * 
   * @param i The location to mark.
   */
  public void markVisited(int i) {
    if (i >= 0 && i < this.capacity) {
      if (0 == this.registry[i]) {
        this.unvisitedCount--;
        this.registry[i] = 1;
      }
    }
  }

  /**
   * Marks as visited a range of locations.
   * 
   * @param i The location to mark.
   */
  public void markVisited(int start, int end) {
    for (int i = start; i < end; i++) {
      this.markVisited(i);
    }
  }

  /**
   * Get the next random unvisited position.
   * 
   * @return The next unvisited position.
   */
  public int getNextRandomUnvisitedPosition() {
    if (0 == this.unvisitedCount) {
      return -1;
    }
    // the idea is to count unvisited, get a random position and map it back to the array
    //
    // int unvisitedCount = 0;
    // for (int i = 0; i < capacity; i++) {
    // if (0 == this.registry[i]) {
    // unvisitedCount++;
    // }
    // }
    // if (0 == unvisitedCount) {
    // if (0 == this.unvisitedCount) {
    // // it is impossible to get unvisited, all marked as visited
    // return -1;
    // }
    // else {
    // // int randomIndex = this.randomizer.nextInt(unvisitedCount);
    // int randomIndex = this.randomizer.nextInt(this.unvisitedCount);
    // int counter = 0;
    // for (int i = 0; i < capacity; i++) {
    // if (0 == this.registry[i]) {
    // if (randomIndex == counter) {
    // return i;
    // }
    // counter++;
    // }
    // }
    // }
    int i = this.randomizer.nextInt(capacity);
    while (1 == registry[i]) {
      i = this.randomizer.nextInt(capacity);
    }
    return i;
  }

  /**
   * Check if position is not visited.
   * 
   * @param i The index.
   * @return true if not visited.
   */
  public boolean isNotVisited(int i) {
    return (0 == this.registry[i]);
  }

  /**
   * Check if position was visited.
   * 
   * @param i The position.
   * @return True if visited.
   */
  public boolean isVisited(Integer i) {
    return (1 == this.registry[i]);
  }

  /**
   * Get the list of unvisited positions.
   * 
   * @return list of unvisited positions.
   */
  public int[] getUnvisited() {
    int count = 0;
    for (int i = 0; i < capacity; i++) {
      if (0 == this.registry[i]) {
        count++;
      }
    }
    int[] res = new int[count];
    int cp = 0;
    for (int i = 0; i < capacity; i++) {
      if (0 == this.registry[i]) {
        res[cp] = i;
        cp++;
      }
    }
    return res;
  }

  /**
   * Get the list of visited positions.
   * 
   * @return list of visited positions.
   */
  public int[] getVisited() {
    int count = 0;
    for (int i = 0; i < capacity; i++) {
      if (1 == this.registry[i]) {
        count++;
      }
    }
    int[] res = new int[count];
    int cp = 0;
    for (int i = 0; i < capacity; i++) {
      if (1 == this.registry[i]) {
        res[cp] = i;
        cp++;
      }
    }
    return res;
  }

  /**
   * Transfers all visited entries to this registry.
   * 
   * @param discordRegistry The discords registry to copy from.
   */
  public void transferVisited(VisitRegistry discordRegistry) {
    // discordRegistry.registry = Arrays.copyOfRange(this.registry, 0, this.registry.length);
    for (int v : discordRegistry.getVisited()) {
      this.markVisited(v);
    }
  }

  public VisitRegistry copy() {
    VisitRegistry res = new VisitRegistry();
    res.capacity = this.capacity;
    res.unvisitedCount = this.unvisitedCount;
    res.registry = Arrays.copyOfRange(this.registry, 0, this.registry.length);
    return res;
  }

  public int size() {
    return this.registry.length;
  }

}
