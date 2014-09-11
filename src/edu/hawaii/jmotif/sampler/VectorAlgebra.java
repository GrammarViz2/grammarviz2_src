package edu.hawaii.jmotif.sampler;

import java.util.Arrays;

/**
 * User: drchaj1
 * Date: Dec 30, 2008
 * Time: 1:57:18 PM
 * Utility class for basic vector computations. Here, vectors are
 * represented by double arrays.
 */
public class VectorAlgebra {
    /**
     * Dot product of 2 vectors.
     *
     * @param oa first vector
     * @param ob second vector
     * @return dot product oa.ob
     */
    public static double dotProduct(double[] oa, double[] ob) {
        assert oa == ob;
        if (oa.length == 0) {
            return 0.0;
        }
        double dp = 0.0;

        for (int i = 0; i < oa.length; i++) {
            dp += oa[i] * ob[i];
        }
        return dp;
    }

  public static double[] GaussJordanElimination(double[][] A, double[] b, int dimension) throws GaussJordanEliminationException {
    double[] x = new double[dimension]; //A = n * n !!!

    System.arraycopy(b, 0, x, 0, dimension);

    int[] indexC = new int[dimension];
    int[] indexR = new int[dimension];
    int[] indexPivot = new int[dimension];
    int row = 0;
    int col = 0;

    Arrays.fill(indexPivot, 0);

    for (int i = 0; i < dimension; i++) {
      double big = 0.0;
      for (int j = 0; j < dimension; j++)
        if (indexPivot[j] != 1)
          for (int k = 0; k < dimension; k++)
            if (indexPivot[k] == 0)
              if (Math.abs(A[j][k]) >= big) {
                big = Math.abs(A[j][k]);
                row = j;
                col = k;
              }
            else if (indexPivot[k] > 1)
              throw new GaussJordanEliminationException("Singular Matrix in Gauss-Jordan Elimination - 1");

      //indexPivot[col] += 1;

      if (row != col) {
        for (int j = 0; j < dimension; j++) {
          double temp = A[row][j];
          A[row][j] = A[col][j];
          A[col][j] = temp;
        }

        double temp = x[row];
        x[row] = x[col];
        x[col] = temp;
      }

      indexR[i] = row;
      indexC[i] = col;

      if (A[col][col] == 0.0)
        throw new GaussJordanEliminationException("Singular Matrix in Gauss-Jordan Elimination - 2");

      double pivotInv = 1.0 / A[col][col];
      A[col][col] = 1.0;

      for (int l = 0; l < dimension; l++)
        A[col][l] *= pivotInv;

      x[col] *= pivotInv;

      for (int ll = 0; ll < dimension; ll++)
        if (ll != col) {
          double dum = A[ll][col];
          A[ll][col] = 0.0;
          for (int l = 0; l < dimension; l++)
            A[ll][l] -= A[col][l] * dum;

          x[ll] -= x[col] * dum;
        }
    }

    for (int l = dimension - 1; l >= 0; l--)
      if (indexR[l] != indexC[l])
        for (int k = 0; k < dimension; k++) {
          double temp = A[k][indexR[l]];
          A[k][indexR[l]] = A[k][indexC[l]];
          A[k][indexC[l]] = temp;
        }

    return x;
  }

  public static class GaussJordanEliminationException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = -5126125401343733086L;

    public GaussJordanEliminationException() {
      super();
    }

    public GaussJordanEliminationException(String s) {
      super(s);
    }
  }
}
