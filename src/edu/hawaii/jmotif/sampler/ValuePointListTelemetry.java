/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.hawaii.jmotif.sampler;

import java.util.ArrayList;
import java.util.List;

/**
 * A thread-safe implementation of the <code>Telemetry</code> interface capable
 * of externalizing a list of <code>ValuePoint</code>s.
 * @see ValuePoint
 *
 * @author ytoh
 */
public final class ValuePointListTelemetry implements Telemetry<List<ValuePoint>> {
    
    //  telemetry representation
    private final List<ValuePoint> valuePoints;

    /**
     *
     */
    public ValuePointListTelemetry() {
        this.valuePoints = new ArrayList<ValuePoint>(0);
    }

    /**
     * A defensive copy is made upon insertion of new telemetry to assure that
     * changes to the original list are not reflected in the telemetry.
     *
     * @param valuePoints
     */
    public ValuePointListTelemetry(List<ValuePoint> valuePoints) {
        this.valuePoints = new ArrayList<ValuePoint>(valuePoints);
    }

    /**
     * The liste returned is a copy of the internal telemetry provided.
     * It is a copy so clients can freely modify it and there is no need
     * to worry about changes.
     *
     * @return list of ValuePoints
     * a fresh copy of the represented current state
     */
    public List<ValuePoint> getValue() {
        return new ArrayList<ValuePoint>(valuePoints);
    }

    @Override
    public String toString() {
        ValuePoint minimum = ValuePoint.at(Point.at(0), Double.POSITIVE_INFINITY);

        for (ValuePoint valuePoint : valuePoints) {
            if(valuePoint.getValue() < minimum.getValue()) {
                minimum = valuePoint;
            }
        }

        return String.format("%d point with minimim: %s", valuePoints.size(), minimum);
    }
}
