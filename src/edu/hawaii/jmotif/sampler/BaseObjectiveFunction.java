/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.hawaii.jmotif.sampler;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import edu.hawaii.jmotif.sampler.Statistics.StatisticsBuilder;
import edu.hawaii.jmotif.text.SAXCollectionStrategy;

/**
 * Function decorator (Decorator pattern)
 * implementing:
 *
 * <li>gathering statistics about function invocations</li>
 * <li>providing analytical gradient/hessian detection</li>
 * <li>returning analytical or numerical gradient/hessian or
 * NullObject gradient/hessian</li>
 *  
 * (Strategy pattern) Numerical gradient/hessian calculation strategy
 *
 * @author ytoh
 */
public class BaseObjectiveFunction implements ObjectiveFunction {

    protected Function          function                = null;
    private FunctionGradient    gradient                = null;
    private FunctionHessian     hessian                 = null;
    private boolean             hasAnalyticalGradient   = false;
    private boolean             hasAnalyticalHessian    = false;
    private NumericalGradient   numericalGradient       = null;
    private NumericalHessian    numericalHessian        = null;
    private StatisticsBuilder   statistics              = null;
    protected FunctionBounds    bounds                  = null;
    private boolean             isFunctionBound         = false;
    private FunctionDynamics    dynamics                = null;
    private boolean             isFunctionDynamic       = false;

    /**
     * Constructs an <code>ObjectiveFunction</code> able of analyzing the supplied
     * function, gathering statistics and calculating numerical gradient/hessian
     * after a certain strategy.
     *
     * @param function to analyze and wrap
     */
    public BaseObjectiveFunction(Function function) {
        this.function = function;
        this.statistics = Statistics.newInstance();

        if (function instanceof FunctionGradient) {
            hasAnalyticalGradient = true;
            gradient = (FunctionGradient) function;
        }

        if (function instanceof FunctionHessian) {
            hasAnalyticalHessian = true;
            hessian = (FunctionHessian) function;
        }

        if(function instanceof FunctionBounds) {
            bounds = (FunctionBounds) function;
            isFunctionBound = true;
        }

        if(function instanceof FunctionDynamics) {
            dynamics = (FunctionDynamics) function;
            isFunctionDynamic = true;
        }
    }

    /**
     * Set the prefered gradient calculation strategy if no analytical gradient
     * is provided.
     *
     * @param numericalGradient gradient calculation strategy
     */
    public void setNumericalGradient(NumericalGradient numericalGradient) {
        this.numericalGradient = numericalGradient;
    }

    /**
     * Set the prefered hessian calculation strategy if no analytical hessian
     * is provided.
     *
     * @param numericalHessian hessian calculation strategy
     */
    public void setNumericalHessian(NumericalHessian numericalHessian) {
        this.numericalHessian = numericalHessian;
    }

    public boolean hasAnalyticalGradient() {
        return hasAnalyticalGradient;
    }

    public boolean hasAnalyticalHessian() {
        return hasAnalyticalHessian;
    }

    public boolean isDynamic() {
        return isFunctionDynamic;
    }

    public boolean inBounds(Point point) throws ArrayIndexOutOfBoundsException{
        if (isFunctionBound){
            double[] positions = point.toArray();
            double[] minima = bounds.getMinimum();
            double[] maxima = bounds.getMaximum();
            for (int i = 0; i < function.getDimension(); i++){
                if ( positions[i] < minima[i] || positions[i] > maxima[i]){
                    return false;
                }
            }
        }
        return true;
    }

    public double valueAt(Point point) {
        // count invocations
        statistics.incrementValueAt();

        return function.valueAt(point);
    }

    public int getDimension() {
        return function.getDimension();
    }

    public Gradient gradientAt(Point point) {
        // count invocations
        statistics.incrementGradientAt();

        if (hasAnalyticalGradient) {
            return gradient.gradientAt(point);
        }

        // return calculated gradient according to certain strategy
        if(numericalGradient != null) {
            return numericalGradient.gradientAt(this, point);
        }

        throw new OptimizationException("Cannot compute gradient - no analytical/numerical gradient available");
    }

    public Hessian hessianAt(Point point) {
        // count invocations
        statistics.incrementHessianAt();

        if (hasAnalyticalHessian) {
            return hessian.hessianAt(point);
        }

        // return calculated hessian according to certain strategy
        if(numericalHessian != null) {
            return numericalHessian.hessianAt(this, point);
        }

        throw new OptimizationException("Cannot compute hessian - no analytical/numerical hessian available");
    }

    /**
     * Retrieve a snapshot of the current function invocation statistics represented
     * as an immutable {@link Statistics} instance.
     *
     * <p>The returned <code>Statistics</code> instance can be queried about
     * the number of <code>valueAt</code>, <code>gradientAt</code> and <code>hessianAt</code>
     * invocations.</p>
     *
     * @return invocation count statistics
     */
    public Statistics getStatistics() {
        return statistics.build();
    }

    public double[] getMinimum() {
        if(isFunctionBound) {
            return bounds.getMinimum();
        }

        double[] minimum = new double[function.getDimension()];
        Arrays.fill(minimum, -Double.MAX_VALUE);
        return minimum;
    }

    public double[] getMaximum() {
        if(isFunctionBound) {
            return bounds.getMaximum();
        }

        double[] maximum = new double[function.getDimension()];
        Arrays.fill(maximum, Double.MAX_VALUE);
        return maximum;
    }

    public void resetGenerationCount() {
        if (isFunctionDynamic){
            dynamics.resetGenerationCount();
        }
    }

    public void nextGeneration() {
        if (isFunctionDynamic){
            dynamics.nextGeneration();
        }
    }

    public void setGeneration(int currentGeneration) {
        if (isFunctionDynamic){
            dynamics.setGeneration(currentGeneration);
        }
    }

    @Override
    public SAXCollectionStrategy getSAXSamplingStrategy() {
      // TODO Auto-generated method stub
      return null;
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
} 