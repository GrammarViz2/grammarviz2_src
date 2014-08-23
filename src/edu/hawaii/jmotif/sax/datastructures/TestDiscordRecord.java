package edu.hawaii.jmotif.sax.datastructures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 * Test the discord record.
 *
 * @author Pavel Senin
 *
 */
public class TestDiscordRecord {

  private static final double precision = 0.00001D;

  private static final Integer idx = -1;

  private static final Integer idx1 = 13;
  private static final double dist1 = 0.213D;

  private static final Integer idx2 = 17;
  private static final double dist2 = 0.875D;

  /**
   * Test the default constructor.
   */
  @Test
  public void testDiscordRecord() {
    DiscordRecord dr = new DiscordRecord();
    assertEquals("Test constructor", 0D, dr.getNNDistance(), precision);
    assertEquals("Test constructor", idx, (Integer) dr.getPosition());
  }

  /**
   * Test the constructor and setters/getters.
   */
  @Test
  public void testSetPosition() {
    DiscordRecord dr = new DiscordRecord(idx1, dist1);
    assertEquals("Test constructor", dist1, dr.getNNDistance(), precision);
    assertEquals("Test constructor", idx1, (Integer) dr.getPosition());

    dr.setNNDistance(dist2);
    dr.setPosition(idx2);
    assertEquals("Test constructor", dist2, dr.getNNDistance(), precision);
    assertEquals("Test constructor", idx2, (Integer) dr.getPosition());

    dr = new DiscordRecord(idx1, dist1);
    DiscordRecord dr2 = new DiscordRecord(idx1, dist2);

    assertTrue("Testing compareTo", dr.compareTo(dr2) < 0);
    assertTrue("Testing compareTo", dr2.compareTo(dr) > 0);
    assertFalse("Test hash", dr.hashCode() == dr2.hashCode());
    assertFalse("Test hash", dr.equals(dr2));

    dr.setNNDistance(dist2);
    assertTrue("Testing compareTo", dr2.compareTo(dr) == 0);

    dr.setPosition(idx1);
    assertTrue("Test hash", dr.hashCode() == dr2.hashCode());
    assertTrue("Test hash", dr.equals(dr2));

    try {
      assertNotNull("Testing compareTo", dr);
      assertTrue("Testing compareTo", dr.compareTo(null) > 0);
      fail("Exception wasn't thrown");
    }
    catch (NullPointerException e) {
      assert true;
    }

  }
}
