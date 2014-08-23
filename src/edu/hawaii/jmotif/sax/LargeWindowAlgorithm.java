package edu.hawaii.jmotif.sax;

import edu.hawaii.jmotif.sax.trie.VisitRegistry;

public class LargeWindowAlgorithm implements SlidingWindowMarkerAlgorithm {

  @Override
  public void markVisited(VisitRegistry registry, int startPosition, int intervalLength) {
    // mark to the right
    for (int i = 0; i < intervalLength; i++) {
      if (startPosition + i > registry.size() - 1) {
        break;
      }
      registry.markVisited(startPosition + i);
    }
    // grow left
    for (int i = 0; i <= intervalLength; i++) {
      if (startPosition - i < 0) {
        break;
      }
      registry.markVisited(startPosition - i);
    }

  }
}
