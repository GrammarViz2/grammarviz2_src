package edu.hawaii.jmotif.sampler;

/**
 * User: drchaj1
 * Date: Aug 21, 2008
 * Time: 12:36:11 PM
 */

/**
 * This abstract class is a predecessor for user objective functions.
 */
public abstract class ObjectiveFunctionFast {
    /**
     * The number of function value evaluations.
     */
    protected long fEvals = 0L;

    /**
     * The number of gradient evaluations.
     */
    protected long gradEvals = 0L;

    /**
     * The number of hessian evaluations.
     */
    protected long hessEvals = 0L;

    /**
     * Zeroes evaluation counters.
     */
    public void init() {
        fEvals = 0L;
        gradEvals = 0L;
        hessEvals = 0L;
    }

    /**
     * Objective function dimension.
     *
     * @return objective function dimension
     */
    abstract public int getDim();

    /**
     * Function value at point ax.
     *
     * @param ax point where a function value is evaluated
     * @return function value at point ax
     */
    public double f(final double[] ax) {
        fEvals++;
        return Double.NaN;
    }

    /**
     * Gradient vector at point ax.
     *
     * @param ax    point where a gradient is evaluated
     * @param agrad gradient vector at point ax.
     */
    public void grad(final double[] ax, double[] agrad) {
        gradEvals++;
    }

    /**
     * Hessian matrix at point ax.
     *
     * @param ax    point where a hessian is evaluated
     * @param ahess hessian matrix at point ax.
     */
    public void hess(final double[] ax, double[][] ahess) {
        hessEvals++;
    }

    /**
     * Function value and gradient at point ax.
     *
     * @param ax    point where a function value and gradient are evaluated
     * @param agrad agrad gradient vector at point ax
     * @return function value at point ax
     */
    public double f(final double[] ax, double[] agrad) {
        grad(ax, agrad);
        return f(ax);
    }

    /**
     * Function value, gradient and hessian at point ax.
     *
     * @param ax    point where a function value and gradient are evaluated
     * @param agrad gradient vector at point ax
     * @param ahess hessian matrix at point ax.
     * @return function value at point ax
     */
    public double f(final double[] ax, double[] agrad, double[][] ahess) {
        grad(ax, agrad);
        hess(ax, ahess);
        return f(ax);
    }

    /**
     * Checks if analytic gradient is supported.
     *
     * @return true for implemented analytic gradient, otherwise false
     */
    public abstract boolean hasAnalyticGradient();

    /**
     * Checks if analytic hessian is supported.
     *
     * @return true for implemented analytic hessian, otherwise false
     */
    public abstract boolean hasAnalyticHessian();

    // getters and setters
    /**
     * Get the number of function value evaluations.
     *
     * @return number of function value evaluations
     */
    public long getFEvals() {
        return fEvals;
    }

    /**
     * Get the number of gradient evals.
     *
     * @return number of gradient evals
     */
    public long getGradEvals() {
        return gradEvals;
    }

    /**
     * Get the number of hessian evals.
     *
     * @return the number of hessian evals
     */
    public long getHessEvals() {
        return hessEvals;
    }
}
