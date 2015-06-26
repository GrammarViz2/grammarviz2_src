package net.seninp.jmotif.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import org.junit.Test;

/**
 * Test the word bag class.
 * 
 * @author psenin
 * 
 */
public class TestWordBag {

  private static final String TEST_BAG_NAME = "TEST001";

  private static final String[] TEST_WORDS = { "word0", "word1", "word2", "word3", "word4" };

  /**
   * Test the constructor.
   */
  @Test
  public void testConstructor() {
    WordBag bag = new WordBag(TEST_BAG_NAME);
    assertEquals(TEST_BAG_NAME, bag.getLabel());
    assertTrue(0 == bag.getWordFrequency("word"));
    assertTrue(bag.getWords().isEmpty());
    assertTrue(bag.getWordSet().isEmpty());
  }

  /**
   * Test the word bag functionality.
   */
  @Test
  public void testBag() {
    WordBag bag = new WordBag(TEST_BAG_NAME);
    for (int i = 0; i < TEST_WORDS.length; i++) {
      bag.addWord(TEST_WORDS[i], i + 1);
    }

    assertTrue(3 == bag.getWordFrequency(TEST_WORDS[2]));
    assertTrue(5 == bag.getWordFrequency(TEST_WORDS[4]));

    HashMap<String, Integer> words = bag.getWords();
    assertTrue(5 == words.size());
    assertSame(4, words.get("word3"));

    assertTrue(bag.contains("word4"));
  }

}
