package edu.hawaii.jmotif.sax.trie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the leaf node implementation.
 *
 * @author Pavel Senin.
 *
 */
public class TestLeafNode {

  private TrieLeafNode testNode, testNode2, testNode3;

  private static final String nodeName = "leaf1";

  private static final int[] occurences = { 3, 5 };

  private List<Integer> newTestOccurences;

  /**
   * Test setup.
   *
   * @throws Exception if error occurs.
   */
  @Before
  public void setUp() throws Exception {
    testNode = new TrieLeafNode("leaf1");
    newTestOccurences = new ArrayList<Integer>();
    for (int i : occurences) {
      newTestOccurences.add(i);
    }

    testNode2 = new TrieLeafNode();
    testNode3 = new TrieLeafNode(null);
  }

  /**
   * Test the returned node type.
   */
  @Test
  public void testGetType() {
    assertEquals("Test node label", TrieNodeType.LEAF, testNode.getType());
    assertEquals("Test node label", nodeName, testNode.getLabel());

    assertTrue("Testing default constructor", testNode2.getLabel().isEmpty());
    assertTrue("Testing default constructor", testNode3.getLabel().isEmpty());
  }

  /**
   * Test the node leaf methods.
   */
  @Test
  public void testLeaf() {
    assertEquals("Test locations", 0, testNode.getOccurences().size());

    testNode.addOccurrence(1);
    assertEquals("Test locations", 1, testNode.getOccurences().size());

    testNode.addOccurrences(newTestOccurences);
    assertEquals("Test locations", 3, testNode.getOccurences().size());
  }

}
