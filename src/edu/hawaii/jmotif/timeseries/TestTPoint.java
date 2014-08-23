package edu.hawaii.jmotif.timeseries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import org.junit.Before;
import org.junit.Test;

/**
 * Test TPoint class.
 *
 * @author Pavel Senin.
 *
 */
public class TestTPoint {

  private static final double value1 = 0.022D;
  private static final long tstamp1 = 23L;

  private static final double value2 = 0.065D;
  private static final long tstamp2 = 66L;

  private TPoint tPoint1;
  private TPoint tPoint2;

  /**
   * Test fixture.
   *
   */
  @Before
  public void setUp() {
    tPoint1 = new TPoint(value1, tstamp1);
    tPoint2 = new TPoint(value2, tstamp2);
  }

  /**
   * Test constructor, hashcode and equals method.
   */
  @Test
  public void testTPoint() {
    TPoint tPoint3 = new TPoint(value1, tstamp1);
    assertEquals("testing hash", tPoint1.hashCode(), tPoint3.hashCode());
    assertNotSame("testing hash", tPoint1.hashCode(), tPoint2.hashCode());

    assertEquals("testing equals", tPoint1, tPoint3);
    assertFalse("testing equals", tPoint2.equals(tPoint3));
    assertFalse("testing equals", tPoint1.equals(tPoint2));
    assertFalse("testing equals", tPoint1.equals(Integer.valueOf(15)));
  }

  /**
   * Test value.
   */
  @Test
  public void testValue() {
    assertEquals("testing value", (Double) value1, (Double) tPoint1.value());
    assertEquals("testing value", (Double) value2, (Double) tPoint2.value());
  }

  /**
   * Test tstamp.
   */
  @Test
  public void testTstamp() {
    assertEquals("testing value", (Long) tstamp1, (Long) tPoint1.tstamp());
    assertEquals("testing value", (Long) tstamp2, (Long) tPoint2.tstamp());

    tPoint2.setTstamp(113L);
    assertEquals("testing value", (Long) 113L, (Long) tPoint2.tstamp());

    long l = Integer.MAX_VALUE;
    tPoint1.setTstamp(l * 2);
    TPoint tPoint3 = new TPoint(tPoint1.value(), tPoint1.tstamp());

    assertEquals("testing equals", tPoint1, tPoint3);
    assertFalse("testing equals", tPoint2.equals(tPoint3));

    assertEquals("Testing hash code", tPoint1.hashCode(), tPoint3.hashCode());
    assertNotSame("Testing hash code", tPoint2.hashCode(), tPoint3.hashCode());
  }

  /**
   * Test comparator.
   */
  @Test
  public void testCompare() {
    TPoint p1 = new TPoint(value1, tstamp1);
    TPoint p2 = new TPoint(value1, tstamp1 + 2);
    TPoint p3 = new TPoint(value1, tstamp1 + 4);
    assertEquals("testing compare", -1, p1.compareTo(p2));
    assertEquals("testing compare", 0, p1.compareTo(p1));
    assertEquals("testing compare", 1, p2.compareTo(p1));
    assertEquals("testing compare", -1, p2.compareTo(p3));

    p2.setTstamp(p1.tstamp());
    p2.setValue(p1.value() - 1);
    assertEquals("testing compare", -1, p2.compareTo(p1));
    assertEquals("testing compare", 1, p1.compareTo(p2));

  }

}
