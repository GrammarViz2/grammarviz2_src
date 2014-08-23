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
public class TestTimeinterval {

  private static final double value1 = 0.022D;
  private static final long start1 = 23L;
  private static final long end1 = 25L;

  private static final double value2 = 0.065D;
  private static final long start2 = 66L;
  private static final long end2 = 66L;

  private Timeinterval tInterval1;
  private Timeinterval tInterval2;

  /**
   * Test fixture.
   * 
   */
  @Before
  public void setUp() {
    tInterval1 = new Timeinterval(value1, start1, end1);
    tInterval2 = new Timeinterval(value2, start2, end2);
  }

  /**
   * Test constructor, hashcode and equals method.
   */
  @Test
  public void testTInterval() {
    Timeinterval tInterval3 = new Timeinterval(value1, start1, end1);
    assertEquals("testing hash", tInterval1.hashCode(), tInterval3.hashCode());
    assertNotSame("testing hash", tInterval1.hashCode(), tInterval2.hashCode());

    assertEquals("testing equals", tInterval1, tInterval3);
    assertFalse("testing equals", tInterval2.equals(tInterval3));
    assertFalse("testing equals", tInterval1.equals(tInterval2));
    assertFalse("testing equals", tInterval1.equals(Integer.valueOf(15)));
  }

  /**
   * Test value.
   */
  @Test
  public void testValue() {
    assertEquals("testing value", (Double) value1, (Double) tInterval1.getValue());
    assertEquals("testing value", (Double) value2, (Double) tInterval2.getValue());
    tInterval2.setValue(11.3D);
    assertEquals("testing value", (Double) 11.3D, (Double) tInterval2.getValue());
  }

  /**
   * Test tstamp.
   */
  @Test
  public void testTstamp() {

    assertEquals("testing start", (Long) start1, (Long) tInterval1.getStart());
    assertEquals("testing end", (Long) end1, (Long) tInterval1.getEnd());

    assertEquals("testing start", (Long) start2, (Long) tInterval2.getStart());
    assertEquals("testing end", (Long) end2, (Long) tInterval2.getEnd());

    tInterval2.setStart(113L);
    tInterval2.setEnd(254L);
    assertEquals("testing value", (Long) 113L, (Long) tInterval2.getStart());
    assertEquals("testing value", (Long) 254L, (Long) tInterval2.getEnd());

    long l = Integer.MAX_VALUE;
    tInterval1.setStart(l * 2);
    tInterval1.setEnd(l * 4);
    Timeinterval tInterval3 = new Timeinterval(tInterval1.getValue(), tInterval1.getStart(),
        tInterval1.getEnd());

    assertEquals("testing equals", tInterval1, tInterval3);
    assertFalse("testing equals", tInterval2.equals(tInterval3));

    assertEquals("Testing hash code", tInterval1.hashCode(), tInterval3.hashCode());
    assertNotSame("Testing hash code", tInterval2.hashCode(), tInterval3.hashCode());
  }

  /**
   * Test comparator.
   */
  @Test
  public void testCompare() {
    Timeinterval p1 = new Timeinterval(value1, start1, end1);
    Timeinterval p2 = new Timeinterval(value1, start1 + 2, end1);
    Timeinterval p3 = new Timeinterval(value1, start1 + 4, end1);
    assertEquals("testing compare", -1, p1.compareTo(p2));
    assertEquals("testing compare", 0, p1.compareTo(p1));
    assertEquals("testing compare", 1, p2.compareTo(p1));
    assertEquals("testing compare", -1, p2.compareTo(p3));

    p2.setStart(p1.getStart());
    p2.setValue(p1.getValue() - 1);
    assertEquals("testing compare", -1, p2.compareTo(p1));
    assertEquals("testing compare", 1, p1.compareTo(p2));

    p2.setValue(p1.getValue());
    p2.setEnd(p1.getEnd() + 1);
    assertEquals("testing compare", -1, p1.compareTo(p2));
    p2.setEnd(p1.getEnd() - 1);
    assertEquals("testing compare", 1, p1.compareTo(p2));
  }

}
