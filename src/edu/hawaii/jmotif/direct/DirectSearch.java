package edu.hawaii.jmotif.direct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import edu.hawaii.jmotif.sampler.ObjectiveFunction;
import edu.hawaii.jmotif.sampler.Point;
import edu.hawaii.jmotif.sampler.SimpleStopCondition;
import edu.hawaii.jmotif.sampler.ValuePointColored;
import edu.hawaii.jmotif.sampler.ValuePointListTelemetryColored;

public class DirectSearch {

  // @Property(name = "Parameter Minimum")
  // @Range(from = -Double.MAX_VALUE, to = Double.MAX_VALUE)
  private double min = -10.0; // parameter minimum

  // @Property(name = "Parameter Maximum")
  // @Range(from = -Double.MAX_VALUE, to = Double.MAX_VALUE)
  private ArrayList<Double[]> centerPoints; // array with all rectangle centerpoints
  private ArrayList<Double[]> lengthsSide; // array with all rectangle side lengths in each
                                           // dimension
  private ArrayList<Double> diagonals; // array with distances from centerpoint to the vertices
  private ArrayList<Double> diagonalsDifferent; // array vector of all different distances, sorted
  private double[] diagonalsMinFunc; // array vector of minimum function value for each distance
  private ArrayList<Double> functionValues; // array with function values
  private ArrayList<ValuePointColored> coordinates; // array with sampled points and function value
  private int dimension;
  private int sampledPoints;
  private int rectangleCounter;
  private int indexPotentialBestRec;
  private double minFunctionValue;
  private ObjectiveFunction function;
  private double[] minBounds;
  private double[] maxBounds;
  double[] resultMinimum;
  int b = 0;
  ValuePointColored minimum = ValuePointColored.at(Point.at(0), Double.POSITIVE_INFINITY, false);

  private void update() {
    resultMinimum = minimum(functionValues);
    // getting minimum and giving it at last points
    minFunctionValue = resultMinimum[0];
    minimum.setBest(false);
    minimum = ValuePointColored.at(Point.at(0), Double.POSITIVE_INFINITY, false);
    int i = 0;
    for (ValuePointColored valuePoint : coordinates) {
      if (valuePoint.getValue() < minimum.getValue()) {
        b = i;
        minimum = valuePoint;
      }
      i++;
    }
    minimum.setBest(true);
    coordinates.remove(b);
    coordinates.add(minimum);
    double epsilon = 1E-4;
    double e = Math.max(epsilon * Math.abs(minFunctionValue), 1E-8);
    double[] temporaryArray = new double[functionValues.size()];
    for (int i2 = 0; i2 < functionValues.size(); i2++) {
      temporaryArray[i2] = (functionValues.get(i2) - minFunctionValue + e) / diagonals.get(i2);
    }
    indexPotentialBestRec = (int) minimum(temporaryArray)[1];

    diagonalsDifferent = diagonals;
    int i1 = 0;
    while (true) {
      double diagonalTmp = diagonalsDifferent.get(i1);
      int[] indx = findNegative(diagonalsDifferent, diagonalTmp);
      ArrayList<Double> diagonalCopy = diagonalsDifferent;
      diagonalsDifferent = new ArrayList<Double>();
      diagonalsDifferent.add(diagonalTmp);

      for (int i2 = 1; i2 < indx.length + 1; i2++) {
        diagonalsDifferent.add(diagonalCopy.get(indx[i2 - 1]));
      }
      if (i1 + 1 == diagonalsDifferent.size()) {
        break;
      }
      else {
        i1++;
      }
    }
    Collections.sort(diagonalsDifferent);
    diagonalsMinFunc = new double[diagonalsDifferent.size()];
    for (i1 = 0; i1 < diagonalsDifferent.size(); i1++) {
      int[] indx1 = find(diagonals, diagonalsDifferent.get(i1));
      ArrayList<Double> fTmp = new ArrayList<Double>();
      for (int i2 = 0; i2 < indx1.length; i2++) {
        fTmp.add(functionValues.get(indx1[i2]));
      }
      diagonalsMinFunc[i1] = minimum(fTmp)[0];
    }
  }

  /**
   * Divide the rectangle containing centerPoints.get(j) into thirds along the dimension in
   * maxSideLengths, starting with the dimension with the lowest value of w[ii]
   * 
   * @param w
   * @param maxSideLengths
   * @param delta
   * @param j
   */

  private void devideRec(double[] w, int[] maxSideLengths, double delta, int j) {
    double[][] ab = sort(w);

    for (int ii = 0; ii < maxSideLengths.length; ii++) {
      int i1 = maxSideLengths[(int) ab[1][ii]];
      int index1 = rectangleCounter + 2 * (int) ab[1][ii]; // Index for new rectangle
      int index2 = rectangleCounter + 2 * (int) ab[1][ii] + 1; // Index for new rectangle
      lengthsSide.get(j)[i1] = delta / 2;
      int index = 0;
      if (index2 + 1 > index1 + 1) {
        index = index2 + 1;
      }
      else {
        index = index1 + 1;
      }

      Double[] lTmp = new Double[dimension];
      Double[] lTmp2 = new Double[dimension];
      for (int i2 = 0; i2 < lengthsSide.get(0).length; i2++) {
        lTmp[i2] = lengthsSide.get(j)[i2];
        lTmp2[i2] = lengthsSide.get(j)[i2];
      }
      if (index == lengthsSide.size() + 2) {
        lengthsSide.add(lTmp);
        lengthsSide.add(lTmp2);
      }
      else {
        Double[] lTmp3;
        int lengthsSize = lengthsSide.size();
        for (int i2 = 0; i2 < index - lengthsSize; i2++) {
          lTmp3 = new Double[dimension];
          lengthsSide.add(lTmp3);
        }
        lengthsSide.set(index1, lTmp);
        lengthsSide.set(index2, lTmp2);
      }

      diagonals.set(j, 0.0);
      Double dTmp;
      for (int i2 = 0; i2 < lengthsSide.get(j).length; i2++) {
        dTmp = diagonals.get(j) + lengthsSide.get(j)[i2] * lengthsSide.get(j)[i2];
        diagonals.set(j, dTmp);
      }
      diagonals.set(j, Math.sqrt(diagonals.get(j)));
      dTmp = diagonals.get(j);
      Double d_kop2 = diagonals.get(j);
      if (index == diagonals.size() + 2) {
        diagonals.add(dTmp);
        diagonals.add(d_kop2);
      }
      else {
        Double dTmp3;
        int size = diagonals.size();
        for (int i2 = 0; i2 < index - size; i2++) {
          dTmp3 = 0.0;
          diagonals.add(dTmp3);
        }
        diagonals.set(index1, diagonals.get(j));
        diagonals.set(index2, diagonals.get(j));
      }
    }
    rectangleCounter = rectangleCounter + 2 * maxSideLengths.length;
  }

  /**
   * Determine where to sample within rectangle j and how to divide the rectangle into
   * subrectangles. Update minFunctionValue and set m=m+delta_m, where delta_m is the number of new
   * points sampled.
   * 
   * @param j
   */
  private void samplingPotentialRec(int j) {
    double max_L = lengthsSide.get(j)[0], delta;
    int[] maxSideLengths;

    for (int i1 = 0; i1 < lengthsSide.get(j).length; i1++) {
      max_L = Math.max(max_L, lengthsSide.get(j)[i1]);
    }
    // Identify the array maxSideLengths of dimensions with the maximum side length.
    maxSideLengths = find(lengthsSide.get(j), max_L);
    delta = 2 * max_L / 3;
    double[] w = new double[0];
    double i1;
    double[] e_i;
    double f_m2, f_m1;
    // Sample the function at the points c +- delta*e_i for all ii in maxSideLengths.
    for (int ii = 0; ii < maxSideLengths.length; ii++) {
      Double[] c_m1 = new Double[dimension];
      double[] x_m1 = new double[dimension];
      Double[] c_m2 = new Double[dimension];
      double[] x_m2 = new double[dimension];
      i1 = maxSideLengths[ii];
      e_i = new double[dimension];
      e_i[(int) i1] = 1;
      // Centerpoint for new rectangle
      for (int i2 = 0; i2 < centerPoints.get(j).length; i2++) {
        c_m1[i2] = centerPoints.get(j)[i2] + delta * e_i[i2];
      }
      // Transform c_m1 to original search space
      for (int i2 = 0; i2 < c_m1.length; i2++) {
        x_m1[i2] = minBounds[i2] + c_m1[i2] * (maxBounds[i2] - minBounds[i2]);
      }

      // Function value at x_m1
      Point pointToSample1 = Point.at(x_m1);
      f_m1 = function.valueAt(pointToSample1);
      // add to all points
      coordinates.add(ValuePointColored.at(pointToSample1, f_m1, false));
      sampledPoints = sampledPoints + 1;
      // Centerpoint for new rectangle
      for (int i2 = 0; i2 < centerPoints.get(j).length; i2++) {
        c_m2[i2] = centerPoints.get(j)[i2] - delta * e_i[i2];
      }
      // Transform c_m2 to original search space
      for (int i2 = 0; i2 < c_m2.length; i2++) {
        x_m2[i2] = minBounds[i2] + c_m2[i2] * (maxBounds[i2] - minBounds[i2]);
      }

      // Function value at x_m2
      Point pointToSample2 = Point.at(x_m2);
      f_m2 = function.valueAt(pointToSample2);
      // add to all points
      coordinates.add(ValuePointColored.at(pointToSample2, f_m2, false));
      sampledPoints = sampledPoints + 1;

      double[] w_pom;
      w_pom = w;
      w = new double[ii + 1];
      System.arraycopy(w_pom, 0, w, 0, w_pom.length);
      w[ii] = Math.min(f_m2, f_m1);

      centerPoints.add(c_m1);
      centerPoints.add(c_m2);
      functionValues.add(f_m1);
      functionValues.add(f_m2);

      // System.out.println(Arrays.toString(x_m1) + ", " + f_m1);
      // System.out.println(Arrays.toString(x_m2) + ", " + f_m2);
    }
    devideRec(w, maxSideLengths, delta, j);

  }

  /**
   * returns array with elements which pole[i]==cislo points sampled.
   * 
   * @param pole
   * @param cislo
   * @return
   */
  private int[] find(Double[] pole, double cislo) {

    int pocet = 0, pom = 0;
    double tolle = 1E-16;
    for (int i = 0; i < pole.length; i++) {
      if (Math.abs(pole[i] - cislo) <= tolle) {
        pocet++;
      }
    }
    int[] pole1 = new int[pocet];
    for (int i = 0; i < pole.length; i++) {
      if (Math.abs(pole[i] - cislo) <= tolle) {
        pole1[pom] = i;
        pom++;
      }
    }
    return pole1;
  }

  /**
   * returns array with elements which pole[i]==cislo points sampled.
   * 
   * @param pole
   * @param cislo
   * @return
   */
  private int[] find(ArrayList<Double> pole, double cislo) {

    int pocet = 0, pom = 0;
    double tolle = 1E-16;
    for (int i = 0; i < pole.size(); i++) {
      if (Math.abs(pole.get(i) - cislo) <= tolle) {
        pocet++;
      }
    }
    int[] pole1 = new int[pocet];
    for (int i = 0; i < pole.size(); i++) {
      if (Math.abs(pole.get(i) - cislo) <= tolle) {
        pole1[pom] = i;
        pom++;
      }
    }
    return pole1;
  }

  /**
   * returns array with elements which pole[i]!=cislo points sampled.
   * 
   * @param pole
   * @param cislo
   * @return
   */
  private int[] findNegative(ArrayList<Double> pole, double cislo) {
    double tolle = 1E-16;
    int pocet = 0, pom = 0;
    for (int i = 0; i < pole.size(); i++) {
      if (Math.abs(pole.get(i) - cislo) > tolle) {
        pocet++;
      }
    }
    int[] pole1 = new int[pocet];
    for (int i = 0; i < pole.size(); i++) {
      if (Math.abs(pole.get(i) - cislo) > tolle) {
        pole1[pom] = i;
        pom++;
      }
    }
    return pole1;
  }

  /**
   * returns array with elements which pole[i]==pole1[a] points sampled.
   * 
   * @param pole
   * @param pole1
   * @return
   */
  private Integer[] find(int[] pole, int[] pole1) {
    int pocet = 0, pom = 0;
    for (int i = 0; i < pole.length; i++) {
      for (int i1 = 0; i1 < pole1.length; i1++) {
        if (pole[i] == pole1[i1]) {
          pocet++;
        }
      }
    }
    Integer[] pole2 = new Integer[pocet];
    for (int i = 0; i < pole.length; i++) {
      for (int i1 = 0; i1 < pole1.length; i1++) {
        if (pole[i] == pole1[i1]) {
          pole2[pom] = pole1[i1];
          pom++;
        }
      }
    }
    return pole2;
  }

  /**
   * returns sorted array and the original indicies
   * 
   * @param pole
   * @return
   */
  private double[][] sort(double[] pole) {
    double[][] pole1 = new double[3][pole.length];
    double[][] pole2 = new double[2][pole.length];
    System.arraycopy(pole, 0, pole1[0], 0, pole.length);
    Arrays.sort(pole);
    for (int i = 0; i < pole.length; i++) {
      for (int i1 = 0; i1 < pole.length; i1++) {
        if (pole[i] == pole1[0][i1] && pole1[2][i1] != 1) {
          pole1[2][i1] = 1;
          pole1[1][i] = i1;
          break;
        }
      }
    }
    pole2[0] = pole;
    pole2[1] = pole1[1];
    return pole2;
  }

  /**
   * returns array with minimum and the original indicies
   * 
   * @param pole
   * @return
   */
  private double[] minimum(double[] pole) {
    double[] pole1 = new double[2];
    double min = pole[0];
    pole1[1] = 0;
    pole1[0] = min;
    for (int i = 0; i < pole.length; i++) {
      if (min > pole[i]) {
        min = pole[i];
        pole1[1] = i;
        pole1[0] = min;
      }
    }

    return pole1;
  }

  /**
   * returns array with minimum and the original indicies
   * 
   * @param pole
   * @return
   */
  private double[] minimum(ArrayList<Double> pole) {
    double[] pole1 = new double[2];
    double min = pole.get(0);
    pole1[1] = 0;
    pole1[0] = min;
    for (int i = 0; i < pole.size(); i++) {
      if (min > pole.get(i)) {
        min = pole.get(i);
        pole1[1] = i;
        pole1[0] = min;
      }
    }

    return pole1;
  }

  /**
   * Identify the set of all potentially optimal rectangles
   * 
   * @return
   */
  private ArrayList<Integer> identifyPotentiallyRec() {
    double tolle2 = 1E-12;
    ArrayList<Integer> s_2 = new ArrayList<Integer>();
    ArrayList<Integer> s_3 = new ArrayList<Integer>();
    int[] indx;
    indx = find(diagonalsDifferent, diagonals.get(indexPotentialBestRec));
    ArrayList<Integer> s_1 = new ArrayList<Integer>();
    Integer[] indx2;
    for (int i1 = indx[0]; i1 < diagonalsDifferent.size(); i1++) {
      int[] indx3 = (find(functionValues, diagonalsMinFunc[i1]));
      int[] indx4 = (find(diagonals, diagonalsDifferent.get(i1)));
      indx2 = find(indx3, indx4);

      s_1.addAll(Arrays.asList(indx2));
    }
    // s_1 now includes all rectangles i, with diagonals[i] >= diagonals(indexPotentialBestRec)
    if (diagonalsDifferent.size() - indx[0] > 2) {
      double a1 = diagonals.get(indexPotentialBestRec), a2 = diagonalsDifferent
          .get(diagonalsDifferent.size() - 1), b1 = functionValues.get(indexPotentialBestRec), b2 = diagonalsMinFunc[diagonalsDifferent
          .size() - 1];
      // The line is defined by: y = slope*x + const
      double slope = (b2 - b1) / (a2 - a1);
      double consta = b1 - slope * a1;
      for (int i1 = 0; i1 < s_1.size(); i1++) {
        int j = s_1.get(i1).intValue();
        if (functionValues.get(j) <= slope * diagonals.get(j) + consta + tolle2) {
          s_2.add(j);
        }
      }
      // s_2 now contains all points in S_1 which lies on or below the line
      // Find the points on the convex hull defined by the points in s_2
      double[] xx = new double[s_2.size()];
      double[] yy = new double[s_2.size()];
      for (int i1 = 0; i1 < xx.length; i1++) {
        xx[i1] = diagonals.get(s_2.get(i1).intValue());
        yy[i1] = functionValues.get(s_2.get(i1).intValue());
      }
      double[] h = conhull(xx, yy);
      for (int i1 = 0; i1 < h.length; i1++) {
        s_3.add(s_2.get((int) h[i1]));
      }
    }
    else {
      s_3 = s_1;
    }
    return s_3;
  }

  /**
   * conhull returns all points on the convex hull, even redundant ones.
   * 
   * @param x
   * @param y
   * @return
   */
  private double[] conhull(double[] x, double[] y) {
    int m = x.length;
    double[] h;
    int start = 0, flag = 0, v, w, a, b, c, leftturn, j, k;
    double determinant;
    if (x.length != y.length) {
      System.out.println("Input dimension must agree");
      return null;
    }
    if (m == 2) {
      h = new double[2];
      h[0] = 0;
      h[1] = 1;
      return h;
    }
    if (m == 1) {
      h = new double[1];
      h[0] = 0;
      return h;
    }
    v = start;
    w = x.length - 1;
    h = new double[x.length];
    for (int i = 0; i < x.length; i++) {
      h[i] = i + 1;
    }
    while ((next(v, m) != 0) || (flag == 0)) {
      if (next(v, m) == w) {
        flag = 1;
      }
      // getting three points
      a = v;
      b = next(v, m);
      c = next(next(v, m), m);
      determinant = (x[a] * y[b] * 1) + (x[b] * y[c] * 1) + (x[c] * y[a] * 1) - (1 * y[b] * x[c])
          - (1 * y[c] * x[a]) - (1 * y[a] * x[b]);

      if (determinant >= 0) {
        leftturn = 1;
      }
      else {
        leftturn = 0;
      }
      if (leftturn == 1) {
        v = next(v, m);
      }
      else {
        j = next(v, m);
        k = 0;
        double[] x1 = new double[x.length - 1];
        for (int i = 0; i < x1.length; i++) {
          if (j == i) {

            k++;
          }
          x1[i] = x[k];
          k++;
        }
        x = x1;
        k = 0;
        x1 = new double[y.length - 1];
        for (int i = 0; i < x1.length; i++) {
          if (j == i) {

            k++;
          }
          x1[i] = y[k];
          k++;
        }
        y = x1;
        k = 0;
        x1 = new double[h.length - 1];
        for (int i = 0; i < x1.length; i++) {
          if (j == i) {

            k++;
          }
          x1[i] = h[k];
          k++;
        }
        h = x1;
        m = m - 1;
        w = w - 1;
        v = pred(v, m);
      }
    }
    for (int i = 0; i < h.length; i++) {
      h[i] = h[i] - 1;
    }
    return h;
  }

  /**
   * returns next point if the last then the first
   * 
   * @param v
   * @param m
   * @return
   */
  private int next(int v, int m) {
    if ((v + 1) == m) {
      return 0;
    }
    else {
      if ((v + 1) < m) {
        return (v + 1);
      }
      else {
        return -1;
      }
    }
  }

  /**
   * returns previous point if the first then the last
   * 
   * @param v
   * @param m
   * @return
   */
  private int pred(int v, int m) {
    if ((v + 1) == 1) {
      return m - 1;
    }
    else {
      if ((v + 1) > 1) {
        return (v - 1);
      }
      else {
        return -1;
      }
    }
  }
}
