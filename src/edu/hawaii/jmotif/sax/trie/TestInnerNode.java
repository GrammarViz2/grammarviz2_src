package edu.hawaii.jmotif.sax.trie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * Inner node test.
 *
 * @author Senin Pavel
 *
 */
public class TestInnerNode {

  private TrieInnerNode testNode1, testNode2, testNode3;

  private static final String name1 = "node1";
  private static final String name2 = "node2";
  private static final String name3 = "node3";

  /**
   * Test setup.
   *
   * @throws Exception if error occurs.
   */
  @Before
  public void setUp() throws Exception {
    testNode1 = new TrieInnerNode(name1);
    testNode2 = new TrieInnerNode();
    testNode3 = new TrieInnerNode(null);
  }

  /**
   * Test the returned node type.
   */
  @Test
  public void testGetType() {
    assertEquals("Testing the node type", TrieNodeType.INNER, testNode1.getType());

    assertTrue("Testing default constructor", testNode2.getLabel().isEmpty());
    assertTrue("Testing default constructor", testNode3.getLabel().isEmpty());
  }

  /**
   * Test the descendants list manipulations.
   */
  @Test
  public void testDescendant() {

    assertEquals("Testing descendants list", 0, testNode1.getDescendants().size());

    TrieInnerNode testNode2 = new TrieInnerNode();
    testNode2.setLabel(name2);

    TrieInnerNode testNode3 = new TrieInnerNode(name3);

    testNode1.addNext(testNode2);
    assertEquals("Testing descendants list", 1, testNode1.getDescendants().size());

    testNode1.addNext(testNode3);
    assertEquals("Testing descendants list", 2, testNode1.getDescendants().size());

    assertSame("Test the descendant retrieval by name", testNode2, testNode1.getDescendant(name2));
    assertNull("Test the descendant retrieval by name", testNode1.getDescendant("fake name"));

    assertNotNull("Test the descendant deletion", testNode1.getDescendant(name3));
    testNode1.delete(name3);
    assertNull("Test the descendant deletion", testNode1.getDescendant(name3));
  }
}
