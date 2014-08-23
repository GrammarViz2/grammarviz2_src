package edu.hawaii.jmotif.sax.datastructures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Test the SAXFrequencyEntry class.
 * 
 * @author Pavel Senin.
 * 
 */
public class TestSAXFrequencyEntry {

  private static final String str1 = "aaabbaa";
  private static final String str2 = "aaabbba";
  private static final Integer ONE = 1;
  private static final Integer ZERO = 0;

  /**
   * Test constructor and setters/getters.
   * 
   */
  @Test
  public void setUp() {
    SAXFrequencyEntry sfe1 = new SAXFrequencyEntry(str1.toCharArray(), 0);
    assertTrue("Testing constructor", String.valueOf(sfe1.getSubstring()).equalsIgnoreCase(str1));
    assertFalse("Testing constructor", String.valueOf(sfe1.getSubstring()).equalsIgnoreCase(str2));
    assertEquals("Testing constructor", (Integer) sfe1.getEntries().size(), ONE);
    assertEquals("Testing constructor", sfe1.getEntries().get(0), ZERO);

    sfe1.put(15);
    assertTrue("Testing setter", sfe1.getEntries().contains(15));
    assertFalse("Testing setter", sfe1.getEntries().contains(11));
  }

  /**
   * Test comparison.
   * 
   */
  @Test
  public void testCompare() {
    SAXFrequencyEntry sfe1 = new SAXFrequencyEntry(str1.toCharArray(), 0);
    SAXFrequencyEntry sfe2 = new SAXFrequencyEntry(str1.toCharArray(), 0);
    SAXFrequencyEntry sfe3 = new SAXFrequencyEntry(str2.toCharArray(), 0);
    assertTrue("testing equals", sfe1.equals(sfe2));
    assertEquals("testing hashCode", sfe1.hashCode(), sfe2.hashCode());
    assertSame("testing comparison", sfe1.compareTo(sfe1), 0);
    assertTrue("testing comparison", sfe1.compareTo(sfe3) == 0);

    sfe2.put(11);
    assertFalse("testing equals", sfe1.equals(sfe2));
    assertNotSame("testing hashCode", sfe1.hashCode(), sfe2.hashCode());

    assertTrue("testing comparison", sfe1.compareTo(sfe2) < 0);
  }

}
