/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.hawaii.jmotif.sampler;

/**
 * Synchronization is a solver produced {@link Telemetry} that holds
 * the number of the current iteration (optimization step).
 *
 * <p><code>Synchronization</code> objects are immutable.</p>
 *
 * @author ytoh
 */
public final class Synchronization implements Telemetry<Integer> {
    // number of the current optimization step
    private final int iteration;

    /**
     * Creates a default instance of <code>Synchronization</code> with <code>0</code>
     * as the number of the current iteration.
     */
    public Synchronization() {
        this(0);
    }

    /**
     * Creates an instance of <code>Synchronization</code> wrapping the number
     * of the current iteration.
     *
     * @param iteration current optimization step number
     */
    public Synchronization(int iteration) {
        this.iteration = iteration;
    }

    public Integer getValue() {
        return iteration;
    }

    @Override
    public String toString() {
        return String.format("Iteration: %d", iteration);
    }
}