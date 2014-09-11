/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.hawaii.jmotif.sampler;

import java.util.List;
import java.util.Map;
import edu.hawaii.jmotif.text.SAXCollectionStrategy;

/**
 * @author ytoh
 */
public class RosenbrockFunction implements Function, FunctionGradient, FunctionHessian,
    ObjectiveFunction {

  private int n = 2;

  private double[] lowerBounds = { -5., -5. };
  private double[] upperBounds = { 5., 5. };

  public double valueAt(Point point) {
    double[] ax = point.toArray();

    /*
     * double t1 = ax[1] - ax[0] * ax[0]; double t2 = 1.0 - ax[0]; return 100 * t1 * t1 + t2 * t2;
     */

    double sum = 0.0;

    for (int i = 0; i < n - 1; i++)
      sum += 100 * (ax[i + 1] - ax[i] * ax[i]) * (ax[i + 1] - ax[i] * ax[i]) + (1 - ax[i])
          * (1 - ax[i]);

    return sum;
  }

  public int getDimension() {
    return n;
  }

  public Gradient gradientAt(Point point) {
    double[] ax = point.toArray();
    double[] gradient = new double[ax.length];

    /*
     * double t1 = ax[1] - ax[0] * ax[0]; gradient[0] = -400.0 * ax[0] * t1 - 2 * (1 - ax[0]);
     * gradient[1] = 200 * t1;
     */

    gradient[0] = -2 * (1 - ax[0]) - 400 * ax[0] * (ax[1] - ax[0] * ax[0]);

    if (n > 2)
      for (int i = 1; i < n - 1; i++)
        gradient[i] = -2 * (1 - ax[i]) + 200 * (ax[i] - ax[i - 1] * ax[i - 1]) - 400 * ax[i]
            * (ax[i + 1] - ax[i] * ax[i]);

    gradient[n - 1] = 200 * (ax[n - 1] - ax[n - 2] * ax[n - 2]);

    return Gradient.valueOf(gradient);
  }

  public Hessian hessianAt(Point point) {
    double[] ax = point.toArray();
    double[][] hessian = new double[ax.length][ax.length];
    /*
     * hessian[0][0] = 1200.0 * ax[0] * ax[0] - 400.0 * ax[1] + 2; hessian[0][1] = -400.0 * ax[0];
     * hessian[1][0] = hessian[0][1]; hessian[1][1] = 200.0;
     */

    hessian[0][0] = 2 + 1200 * ax[0] * ax[0] - 400 * ax[1];
    hessian[0][1] = -400 * ax[0];

    if (n > 2)
      for (int i = 1; i < n - 1; i++) {
        hessian[i][i - 1] = -400 * ax[i - 1];
        hessian[i][i] = 202 + 1200 * ax[i] * ax[i] - 400 * ax[i + 1];
        hessian[i][i + 1] = -400 * ax[i];
      }

    hessian[n - 1][n - 2] = -400 * ax[n - 2];
    hessian[n - 1][n - 1] = 200;

    return Hessian.valueOf(hessian);
  }

  public int getN() {
    return n;
  }

  public void setN(int n) {
    this.n = n;
  }

  @Override
  public boolean hasAnalyticalGradient() {
    return true;
  }

  @Override
  public boolean hasAnalyticalHessian() {
    return true;
  }

  @Override
  public boolean isDynamic() {
    return false;
  }

  @Override
  public double[] getMinimum() {
    return lowerBounds;
  }

  @Override
  public double[] getMaximum() {
    return upperBounds;
  }

  @Override
  public void resetGenerationCount() {
    // TODO Auto-generated method stub

  }

  @Override
  public void nextGeneration() {
    // TODO Auto-generated method stub

  }

  @Override
  public void setGeneration(int currentGeneration) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean inBounds(Point position) {
    return (lowerBounds[0] <= position.toArray()[0]) && (position.toArray()[0] <= upperBounds[0])
        && (lowerBounds[1] <= position.toArray()[1]) && (position.toArray()[1] <= upperBounds[1]);
  }

  @Override
  public void setUpperBounds(double[] parametersHighest) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void setLowerBounds(double[] parametersLowest) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void setStrategy(SAXCollectionStrategy noreduction) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void setData(Map<String, List<double[]>> trainData, int i) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public SAXCollectionStrategy getSAXSamplingStrategy() {
    // TODO Auto-generated method stub
    return null;
  }

}
