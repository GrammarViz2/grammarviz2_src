package net.seninp.jmotif.vector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Test the matrix routines.
 * 
 * @author Pavel Senin.
 * 
 */
public class TestMatrixFactory {

  /* Test matrix. */
  private static final double[][] a = { { 1.0, 2.0, 3.0, 4.0, 5.0 }, { 3.0, 2.0, 1.0, 2.0, 3.0 },
      { 4.0, 3.0, 1.0, 3.0, 4.0 }, { 4.0, 3.0, 1.0, 3.0, 4.0 }, { 5.0, 4.0, 3.0, 2.0, 1.0 },
      { 4.0, 3.0, 2.0, 1.0, 0.0 } };
  // some constants
  private static final int dim_row = 6;
  private static final int dim_col = 5;
  private static final double delta = 0.000001;

  /**
   * Test transpose operation.
   */
  @Test
  public void testTranspose() {
    double[][] at = MatrixFactory.transpose(a);
    for (int i = 0; i < dim_row; i++) {
      for (int j = 0; j < dim_col; j++) {
        assertEquals("test transpose", a[i][j], at[j][i], delta);
      }
    }
  }

  /**
   * Test equals and duplicate operation.
   */
  @Test
  public void testEquals() {
    double[][] b = MatrixFactory.clone(a);
    assertTrue("test equals", MatrixFactory.equals(a, b));
    b[2][3] = -3.44;
    assertFalse("test equals", MatrixFactory.equals(a, b));
  }

  /**
   * Test matrix initialization.
   */
  @Test
  public void testZeroes1() {
    double[][] refArray = new double[1][11];
    for (int i = 0; i < 11; i++) {
      refArray[0][i] = 0.0D;
    }
    double[][] zeros = MatrixFactory.zeros(11);
    assertTrue("test zeros", MatrixFactory.equals(refArray, zeros));
  }

  /**
   * Test matrix initialization.
   */
  @Test
  public void testZeroes2() {
    double[][] refArray = new double[2][11];
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 11; j++) {
        refArray[i][j] = 0.0D;
      }
    }
    double[][] zeros = MatrixFactory.zeros(2, 11);
    assertTrue("test zeros", MatrixFactory.equals(refArray, zeros));
  }

  /**
   * Test the column means routine.
   */
  @Test
  public void testColMeans() {
    // System.out.println(Matrix.toString(a));
    double[] means = MatrixFactory.colMeans(a);
    assertEquals("test col means", 3.5d, means[0], delta);
    // now add 3 NaN's into the first column and all 6 NaN's into third
    for (int i = 0; i < 3; i++) {
      a[i][0] = Double.NaN;
    }
    for (int i = 0; i < 6; i++) {
      a[i][2] = Double.NaN;
    }
    means = MatrixFactory.colMeans(a);
    assertEquals("test col means", Double.NaN, means[2], delta);

    double testVal = (a[3][0] + a[4][0] + a[5][0]) / 3.0D;
    assertEquals("test col means", testVal, means[0], delta);
  }

  /**
   * Test the reshape routine.
   */
  @Test
  public void testReshape() {
    // System.out.println(Matrix.toString(a));
    double[][] reshaped = MatrixFactory.reshape(a, 2, 15);
    // System.out.println(Matrix.toString(reshaped));
    assertEquals("test reshape", a[0][0], reshaped[0][0], delta);
    assertEquals("test reshape", a[2][1], reshaped[0][4], delta);
    assertEquals("test reshape", a[5][4], reshaped[1][14], delta);
    assertEquals("test reshape", a[4][3], reshaped[0][11], delta);
    assertEquals("test reshape", a[5][3], reshaped[1][11], delta);
  }

  /**
   * Test the toString() routine.
   */
  @Test
  public void testToString() {
    String s = MatrixFactory.toString(a);
    assertTrue("Testing tpString()",
        s.startsWith("            [  0]     [  1]     [  2]     [  3]     [  4]"));
  }

}
