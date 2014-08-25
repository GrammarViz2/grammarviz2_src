package edu.hawaii.jmotif.sax.datastructures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the discord records collection implementation.
 * 
 * @author Pavel Senin.
 * 
 */
public class TestDiscordRecords {

  private static final int ds1Pos = 11;
  private static final double ds1Dist = 0.11D;

  private static final int ds2Pos = 21;
  private static final double ds2Dist = 0.21D;

  private static final int ds3Pos = 31;
  private static final double ds3Dist = 0.31D;

  private static final int ds4Pos = 41;
  private static final double ds4Dist = 0.41D;

  private static final double precision = 0.0001D;

  private DiscordRecord discord1, discord2, discord3;
  private DiscordRecord discord4;

  /**
   * Test setup.
   * 
   * @throws Exception if error occurs.
   */
  @Before
  public void setUp() throws Exception {
    discord1 = new DiscordRecord(ds1Pos, ds1Dist);
    discord2 = new DiscordRecord(ds2Pos, ds2Dist);
    discord3 = new DiscordRecord(ds3Pos, ds3Dist);
    discord4 = new DiscordRecord(ds4Pos, ds4Dist);
  }

  /**
   * Test discord records.
   */
  @Test
  public void testDiscordRecords() {
    DiscordRecords ds = new DiscordRecords();
    assertTrue("Test constructor", ds.getTopHits(10).isEmpty());

    ds.add(discord1);
    assertEquals("Test constructor", 1, ds.getTopHits(10).size());
    assertSame("Test constructor", discord1, ds.getTopHits(10).get(0));

    ds.add(discord2);
    assertEquals("Test constructor", 2, ds.getTopHits(10).size());
    assertSame("Test constructor", discord1, ds.getTopHits(2).get(1));

    // this part reproduces the error found by Sergey & Christian, thank you guys!
    // was fixed 30-05-2012
    ds.add(discord3);
    ds.add(discord4);
    List<DiscordRecord> topHits = ds.getTopHits(2);
    assertEquals("Test constructor", 2, topHits.size());
    assertSame("Test constructor", discord2, topHits.get(0));

  }

  /**
   * Test discord records.
   */
  @Test
  public void testDiscordRecordsInt() {
    DiscordRecords ds = new DiscordRecords();
    assertTrue("Test constructor", ds.getTopHits(10).isEmpty());

    ds.add(discord1);
    assertEquals("Test constructor", 1, ds.getTopHits(10).size());
    assertSame("Test constructor", discord1, ds.getTopHits(10).get(0));

    ds.add(discord2);
    assertEquals("Test constructor", 2, ds.getTopHits(10).size());
    assertSame("Test constructor", discord2, ds.getTopHits(10).get(0));
  }

  /**
   * Test the minimal distance calculation routine.
   */
  @Test
  public void testGetMinDistance() {
    DiscordRecords ds = new DiscordRecords();
    ds.add(discord1);
    ds.add(discord2);
    ds.add(discord3);
    assertEquals("Test constructor", ds3Dist, ds.getMinDistance(), precision);
  }

}
