/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.hawaii.jmotif.sampler;

import edu.hawaii.jmotif.direct.ValuePoint;

/**
 * A thread-safe implementation of the <code>Telemetry</code> interface capable
 * of externalizing a single <code>ValuePoint</code>.
 * @see ValuePoint
 *
 * @author ytoh
 */
public final class ValuePointTelemetry implements Telemetry<ValuePoint> {
    
    // a reference to the current published telemetry
    private final ValuePoint valuePoint;

    /**
     *
     */
    public ValuePointTelemetry() {
        valuePoint = ValuePoint.getDefault();
    }

    /**
     * Publish a <code>ValuePoint</code> as the internal state/telemetry.
     *
     * @param value
     * an instance of an immutable ValuePoint
     */
    public ValuePointTelemetry(ValuePoint value) {
        this.valuePoint = value;
    }

    /**
     * @return value
     * an immutable instance of <code>ValuePoint</code> = point coordinates + value at these
     * coordinates
     */
    public ValuePoint getValue() {
        return valuePoint;
    }

    @Override
    public String toString() {
        return valuePoint.toString();
    }
}
