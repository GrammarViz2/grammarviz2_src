package edu.hawaii.jmotif.sax.trie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for SAXTrieHitEntry.
 *
 * @author psenin
 *
 */
public class TestSAXTrieHitEntry {

  private static final String ENTRY1 = "entry1";
  private static final int POS1 = 1;

  private static final int ENTRY_SIZE = ENTRY1.length();

  private static final String ENTRY2 = "entry2";
  private static final int POS2 = 2;

  private SAXTrieHitEntry entry1;
  private SAXTrieHitEntry entry2;

  /**
   * Before JUnit loader.
   *
   * @throws Exception if error occurs.
   */
  @Before
  public void setUp() throws Exception {
    entry1 = new SAXTrieHitEntry(ENTRY_SIZE, POS1);
    entry1.setStr(ENTRY1.toCharArray());
    entry2 = new SAXTrieHitEntry(ENTRY_SIZE, POS2);
    entry2.setStr(ENTRY2.toCharArray());
  }

  /**
   * Hash code test.
   */
  @Test
  public void testHashCode() {
    assertFalse("Testing the HashCode", entry1.hashCode() == entry2.hashCode());
    SAXTrieHitEntry entry3 = new SAXTrieHitEntry(ENTRY_SIZE, POS1);
    entry3.setStr(ENTRY1.toCharArray());
    assertFalse("Testing the HashCode", entry1.hashCode() == entry2.hashCode());
  }

  /**
   * String getter test.
   */
  @Test
  public void testGetStr() {
    assertTrue("test the payload.", String.valueOf(entry1.getStr()).equalsIgnoreCase(ENTRY1));
    assertTrue("test the payload.", String.valueOf(entry2.getStr()).equalsIgnoreCase(ENTRY2));
    assertFalse("test the payload.", String.valueOf(entry1.getStr()).equalsIgnoreCase(ENTRY2));
  }

  /**
   * Frequency getter test.
   */
  @Test
  public void testGetFrequency() {
    assertEquals("test frequency", -1, entry1.getFrequency());
    assertEquals("test frequency", -1, entry2.getFrequency());
    SAXTrieHitEntry entry3 = new SAXTrieHitEntry(ENTRY_SIZE, POS1);
    entry3.setFrequency(15);
    assertEquals("test frequency", 15, entry3.getFrequency());
  }

  /**
   * Position test.
   */
  @Test
  public void testGetPosition() {
    assertEquals("test position", POS1, entry1.getPosition());
    assertEquals("test position", POS2, entry2.getPosition());
    SAXTrieHitEntry entry3 = new SAXTrieHitEntry(ENTRY_SIZE, POS1);
    entry3.setPosition(17);
    assertEquals("test position", 17, entry3.getPosition());
  }

  /**
   * Comparator test.
   */
  @Test
  public void testCompareTo() {
    assertTrue("test compare", entry1.compareTo(entry2) == 0);
    entry1.setFrequency(3);
    entry2.setFrequency(7);
    assertTrue("test compare", entry1.compareTo(entry2) < 0);
    assertTrue("test compare", entry2.compareTo(entry1) > 0);
  }

  /**
   * Equals test.
   */
  @Test
  public void testEqualsObject() {
    assertFalse("test equals", entry1.equals(entry2));
    assertFalse("test equals", entry1.equals(Integer.valueOf(2)));
    assertNotNull("test equals", entry1);

    SAXTrieHitEntry entry3 = new SAXTrieHitEntry(ENTRY_SIZE, POS1);
    entry3.setStr(ENTRY1.toCharArray());
    assertTrue("test equals", entry1.equals(entry3));
    assertTrue("test equals", entry3.equals(entry3));

    entry3.setFrequency(17);
    assertFalse("test equals", entry1.equals(entry3));

    entry3 = new SAXTrieHitEntry(ENTRY_SIZE, POS1);
    entry3.setFrequency(11);
    assertFalse("test equals", entry1.equals(entry3));

    entry3 = new SAXTrieHitEntry(ENTRY_SIZE, POS1);
    entry3.setStr("other".toCharArray());
    assertFalse("test equals", entry1.equals(entry3));
  }

}
