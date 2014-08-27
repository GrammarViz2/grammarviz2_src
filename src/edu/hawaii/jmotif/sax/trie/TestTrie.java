package edu.hawaii.jmotif.sax.trie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * test the trie structure.
 * 
 * @author Pavel Senin
 * 
 */
public class TestTrie {

  private SAXTrieTree testTrie;

  private static final String testStr1 = "abc";
  private static final String testStr2 = "acb";

  /**
   * Test setup.
   * 
   * @throws Exception if error occurs.
   */
  @Before
  public void setUp() throws Exception {
    testTrie = new SAXTrieTree(3);
  }

  /**
   * Test a shallow tree.
   * 
   * @throws TrieException if goes wrong.
   */
  @Test
  public void testTwoLetterDepth() throws TrieException {
    SAXTrieTree twoLettersTrie = new SAXTrieTree(2);
    twoLettersTrie.addOccurence("ab", 3);
    twoLettersTrie.addOccurence("ab", 2);
    twoLettersTrie.addOccurence("bb", 1);
    assertTrue("Testing a trie", twoLettersTrie.getOccurrences("ab").contains(3));
    assertTrue("Testing a trie", twoLettersTrie.getOccurrences("bb").contains(1));

    List<Integer> oc = null;
    try {
      oc = twoLettersTrie.getOccurrences("aaa");
      fail("Exception is not thrown here.");
    }
    catch (TrieException e) {
      assertNull("Testing a trie", oc);
    }

    oc = null;
    try {
      oc = twoLettersTrie.getOccurrences("ac");
      fail("Exception is not thrown here.");
    }
    catch (TrieException e) {
      assertNull("Testing a trie", oc);
    }

    try {
      twoLettersTrie.addOccurence("bbc", 1);
      fail("Exception is not thrown here.");
    }
    catch (TrieException e) {
      assert true;
    }

  }

  /**
   * Test the structure.
   * 
   * @throws TrieException if error occurs.
   */
  @Test
  public void testTrie() throws TrieException {
    assertNotNull("Test the trie constructor", testTrie);

    assertEquals("Test empty occurrences", 0, testTrie.getOccurrences(testStr1).size());
    assertEquals("Test empty occurrences", 0, testTrie.getOccurrences(testStr2).size());

    testTrie.addOccurence(testStr1, 15);
    assertEquals("Test non-empty occurrences", 1, testTrie.getOccurrences(testStr1).size());
    assertEquals("Test empty occurrences", 0, testTrie.getOccurrences(testStr2).size());

    testTrie.addOccurence(testStr1, 15);
    assertEquals("Test non-empty occurrences", 1, testTrie.getOccurrences(testStr1).size());
    assertTrue("Test non-empty occurrences", testTrie.getOccurrences(testStr1).contains(15));
    assertEquals("Test empty occurrences", 0, testTrie.getOccurrences(testStr2).size());

    testTrie.addOccurence(testStr1, 3);
    assertEquals("Test non-empty occurrences", 2, testTrie.getOccurrences(testStr1).size());
    assertTrue("Test non-empty occurrences", testTrie.getOccurrences(testStr1).contains(15));
    assertTrue("Test non-empty occurrences", testTrie.getOccurrences(testStr1).contains(3));
    assertEquals("Test empty occurrences", 0, testTrie.getOccurrences(testStr2).size());

    testTrie.addOccurence(testStr2, 3);
    testTrie.addOccurence(testStr2, 5);
    testTrie.addOccurence(testStr2, 7);
    assertEquals("Test non-empty occurrences", 2, testTrie.getOccurrences(testStr1).size());
    assertTrue("Test non-empty occurrences", testTrie.getOccurrences(testStr2).contains(5));
    assertFalse("Test non-empty occurrences", testTrie.getOccurrences(testStr2).contains(6));
    assertEquals("Test empty occurrences", 3, testTrie.getOccurrences(testStr2).size());
  }
}
