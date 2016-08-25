/**
 * 
 */
package net.seninp.tinker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import net.seninp.tinker.Interval;

/**
 * @author psenin
 *
 */
public class TestInterval {

  private static final int START1 = 10;
  private static final int END1 = 20;
  private static final int LENGTH1 = 10;
  private static final double COVERAGE1 = 0.1;

  private static final int START2 = 5;
  private static final int END2 = 25;
  private static final double COVERAGE2 = 0.2;

  private static final int START3 = 21;
  private static final int END3 = 33;
  private static final int LENGTH3 = 12;

  private static final double DELTA = 0.1;

  private Interval i1, i2, i3;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    i1 = new Interval(START1, END1);
    i2 = new Interval(START2, END2);
    i3 = new Interval(START3, END3);
  }

  /**
   * Test method for {@link net.seninp.tinker.Interval#Interval(int, int)}.
   */
  @Test
  public void testIntervalIntInt() {
    i1 = null;
    assertNull(i1);
    i1 = new Interval(START1, END1);
    assertEquals(START1, i1.getStart());
    assertEquals(END1, i1.getEnd());
    assertEquals(LENGTH1, i1.getLength());
    assertEquals(LENGTH3, i3.getLength());
  }

  /**
   * Test method for {@link net.seninp.tinker.Interval#Interval(int, int, double)}.
   */
  @Test
  public void testIntervalIntIntDouble() {
    i1 = null;
    assertNull(i1);
    i1 = new Interval(START1, END1, COVERAGE1);
    assertEquals(COVERAGE1, i1.getCoverage(), DELTA);

    assertEquals(-1.0, i3.getCoverage(), DELTA);
  }

  /**
   * Test method for {@link net.seninp.tinker.Interval#intersects(net.seninp.tinker.Interval)}.
   */
  @Test
  public void testIntersects() {
    assertFalse(i1.intersects(i3));
    assertFalse(i3.intersects(i1));

    i3.setStart(i1.getEnd());
    assertTrue(i2.contains(i1.getEnd()));
  }

  /**
   * Test method for {@link net.seninp.tinker.Interval#contains(int)}.
   */
  @Test
  public void testContains() {
    assertTrue(i2.contains(i1.getEnd()));
  }

  /**
   * Test setters.
   */
  @Test
  public void testSetters() {
    i2 = new Interval(START2, END2, COVERAGE2);
    
    assertFalse(i2.hashCode() == i1.hashCode());
    assertFalse(i1.equals(i2));
    
    i1.setStart(START2);
    i1.setEnd(END2);
    i1.setCoverage(COVERAGE2);
    
    assertTrue(i2.hashCode() == i1.hashCode());
    assertTrue(i1.equals(i2));
  }

}
