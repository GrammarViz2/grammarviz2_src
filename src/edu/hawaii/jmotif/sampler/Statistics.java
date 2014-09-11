/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.hawaii.jmotif.sampler;

/**
 * Optimization computation statistics gathering class.
 *
 * <p>Statistics are gathered by {@link cz.cvut.felk.cig.jcool.core.ObjectiveFunction}s.
 * and presented as immutable end results of the optimization process.</p>
 *
 * @author ytoh
 */
public final class Statistics {
    // the number of times the function was evaluated for its value
    private final int valueAt;
    // the number of times the function was evaluated for its gradient
    private final int gradientAt;
    // the number of times the function was evaluated for its hessian
    private final int hessianAt;

    /**
     * Create an instance of <code>Statistics</code> wrapping the evaluation
     * counts.
     *
     * @param valueAt number of valueAt evaluations
     * @param gradientAt number of gradientAt evaluations
     * @param hessianAt number of hessianAt evaluations
     */
    public Statistics(int valueAt, int gradientAt, int hessianAt) {
        this.valueAt    = valueAt;
        this.gradientAt = gradientAt;
        this.hessianAt  = hessianAt;
    }

    /**
     * Creates a new instance of a {@link StatisticsBuilder} convenience class
     * providing simple methods for <code>Statistics</code> building.
     *
     * @return instance of <code>StatisticsBuilder</code>
     */
    public static StatisticsBuilder newInstance() {
        return new StatisticsBuilder();
    }

    /**
     * Returns the number invocations of {@link cz.cvut.felk.cig.jcool.core.ObjectiveFunction#gradientAt(cz.cvut.felk.cig.jcool.core.Point)}
     *
     * @return number of gradient evaluations
     */
    public int getGradientAt() {
        return gradientAt;
    }

    /**
     * Returns the number invocations of {@link cz.cvut.felk.cig.jcool.core.ObjectiveFunction#hessianAt(cz.cvut.felk.cig.jcool.core.Point)}
     *
     * @return number of hessian evaluations
     */
    public int getHessianAt() {
        return hessianAt;
    }

    /**
     * Returns the number invocations of {@link cz.cvut.felk.cig.jcool.core.ObjectiveFunction#valueAt(cz.cvut.felk.cig.jcool.core.Point)}
     *
     * @return number of value evaluations
     */
    public int getValueAt() {
        return valueAt;
    }

    @Override
    public String toString() {
        return String.format("Evaluation count: valueAt=%d, gradientAt=%d, hessianAt=%d", valueAt, gradientAt, hessianAt);
    }

    /**
     * <code>StatisticsBuilder</code> is used to create {@link Statistics} instances
     * by providing convenience methods for incrementing counts.
     *
     * (Builder pattern)
     */
    public static final class StatisticsBuilder {
        private int valueAt;
        private int gradientAt;
        private int hessianAt;

        private StatisticsBuilder() {
            this.valueAt    = 0;
            this.gradientAt = 0;
            this.hessianAt  = 0;
        }

        /**
         * Increment the internal value of function evaluations (value)
         */
        public void incrementValueAt() {
            valueAt++;
        }

        /**
         * Increment the internal value of function evaluations (gradient)
         */
        public void incrementGradientAt() {
            gradientAt++;
        }

        /**
         * Increment the internal value of function evaluations (hessian)
         */
        public void incrementHessianAt() {
            hessianAt++;
        }

        /**
         * Create an immutable instance as a snapshot of the statistics currently
         * held by this builder.
         *
         * @return an immutable {@link Statistics} instance.
         */
        public Statistics build() {
            return new Statistics(valueAt, gradientAt, hessianAt);
        }
    }
}
