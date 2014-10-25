package edu.hawaii.jmotif.sax.trie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the visit registry.
 * 
 * @author psenin
 * 
 */
public class TestVisitRegistry {

  private VisitRegistry vr;

  /**
   * Set up.
   * 
   * @throws Exception if error occurs.
   */
  @Before
  public void setUp() throws Exception {
    vr = new VisitRegistry(13);
  }

  /**
   * Test the constructor.
   */
  @Test
  public void testVisitRegistry() {
    assertEquals("Test visit registry", 13, vr.getUnvisited().length);
    assertTrue("Test visit registry", 0 == vr.getVisited().length);
  }

  /**
   * Test the marking.
   */
  @Test
  public void testMarkVisited() {
    vr.markVisited(3);
    vr.markVisited(7);

    assertEquals("Test visit registry", 11, vr.getUnvisited().length);
    assertFalse("Test visit registry", vr.isNotVisited(3));
    assertFalse("Test visit registry", vr.isNotVisited(7));

    assertEquals("Test visit registry", 2, vr.getVisited().length);
    assertTrue("Test visit registry", vr.isVisited(3, 3));
    assertTrue("Test visit registry", vr.isVisited(7, 7));
  }

  /**
   * Test the position generator.
   */
  @Test
  public void testGetNextRandomUnvisitedPosition() {
    int k = vr.getNextRandomUnvisitedPosition();
    assertTrue("Test visit registry", vr.isNotVisited(k));
    assertFalse("Test visit registry", vr.isVisited(k, k));
    vr.markVisited(k);
    assertFalse("Test visit registry", vr.isNotVisited(k));

    int i = 0;
    while (0 != vr.getUnvisited().length) {
      k = vr.getNextRandomUnvisitedPosition();
      assertFalse("Test visit registry", vr.isVisited(k, k));
      vr.markVisited(k);
      assertFalse("Test visit registry", vr.isNotVisited(k));
      i++;
    }
    assertEquals("Test visit registry", 12, i);
  }

}
