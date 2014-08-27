package edu.hawaii.jmotif.sax.datastructures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import org.junit.Test;

/**
 * Test data structures used in the SAX implementation.
 * 
 * @author Pavel Senin.
 * 
 */
public class TestSaxRecord {

  private static final Integer iNum1 = 7;
  private static final Integer iNum2 = 3;
  private static final String str11 = "abggfecbb";

  private static final String str1 = "aaabbaa";
  private static final String str2 = "aaabbba";
  private static final Integer ONE = 1;
  private static final Integer ZERO = 0;

  /**
   * Test the SAX frequency structure.
   */
  @Test
  public void testSAXFrequencyEntry() {
    SaxRecord se = new SaxRecord(str11.toCharArray(), iNum2);

    assertTrue("Testing SAXRecord", str11.equalsIgnoreCase(String.valueOf(se.getPayload())));

    ArrayList<Integer> freqs = se.getIndexes();
    assertEquals("Testing SAXRecord", 1, freqs.size());
    assertEquals("Testing SAXRecord", iNum2, freqs.get(0));

    se.addIndex(iNum1);
    ArrayList<Integer> freqs1 = se.getIndexes();
    assertEquals("Testing SAXRecord", 2, freqs1.size());
    assertEquals("Testing SAXRecord", iNum2, freqs1.get(0));
    assertEquals("Testing SAXRecord", iNum1, freqs1.get(1));

    se.addIndex(iNum2);
    ArrayList<Integer> freqs2 = se.getIndexes();
    assertEquals("Testing SAXRecord", 2, freqs2.size());
    assertEquals("Testing SAXRecord", iNum2, freqs2.get(0));
    assertEquals("Testing SAXRecord", iNum1, freqs2.get(1));
  }

  /**
   * Test constructor and setters/getters.
   * 
   */
  @Test
  public void setUp() {
    SaxRecord sfe1 = new SaxRecord(str1.toCharArray(), 0);
    assertTrue("Testing constructor", String.valueOf(sfe1.getPayload()).equalsIgnoreCase(str1));
    assertFalse("Testing constructor", String.valueOf(sfe1.getPayload()).equalsIgnoreCase(str2));
    assertEquals("Testing constructor", (Integer) sfe1.getIndexes().size(), ONE);
    assertEquals("Testing constructor", sfe1.getIndexes().get(0), ZERO);

    sfe1.addIndex(15);
    assertTrue("Testing setter", sfe1.getIndexes().contains(15));
    assertFalse("Testing setter", sfe1.getIndexes().contains(11));
  }

  /**
   * Test comparison.
   * 
   */
  @Test
  public void testCompare() {
    SaxRecord sfe1 = new SaxRecord(str1.toCharArray(), 0);
    SaxRecord sfe2 = new SaxRecord(str1.toCharArray(), 0);
    SaxRecord sfe3 = new SaxRecord(str2.toCharArray(), 0);
    assertTrue("testing equals", sfe1.equals(sfe2));
    assertEquals("testing hashCode", sfe1.hashCode(), sfe2.hashCode());
    assertSame("testing comparison", sfe1.compareTo(sfe1), 0);
    assertTrue("testing comparison", sfe1.compareTo(sfe3) == 0);

    sfe2.addIndex(11);
    assertFalse("testing equals", sfe1.equals(sfe2));
    assertNotSame("testing hashCode", sfe1.hashCode(), sfe2.hashCode());

    assertTrue("testing comparison", sfe1.compareTo(sfe2) < 0);
  }

}
