package net.seninp.jmotif.vector;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Test the vector routines.
 * 
 * @author Pavel Senin.
 * 
 */
public class TestVector {

  private static final double delta = 0.000001;

  /**
   * Test the minimum finding routine.
   */
  @Test
  public void testNaN() {
    double[] options = { Double.NaN, 0.03, Double.NaN, Double.NaN, 0.01 };
    double minDistance = VectorFactory.getMinValue(options);
    assertEquals("testing NaN java facilities", 0.01, minDistance, delta);
  }

}
