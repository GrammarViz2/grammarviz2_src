package edu.hawaii.jmotif.sampler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Carnuss
 * Date: 3.5.2010
 * Time: 12:42:20
 *  * A thread-safe implementation of the <code>Telemetry</code> interface capable
 * of externalizing a list of <code>ValuePointColored</code>s.
 * @see ValuePointColored
 */
public class ValuePointListTelemetryColored implements Telemetry<List<ValuePointColored>>{
     // a reference to the current published telemetry
    private final List<ValuePointColored> valuePoints;


    public ValuePointListTelemetryColored() {
        this.valuePoints = new ArrayList<ValuePointColored>(0);
    }
   /**
     * A defensive copy is made upon insertion of new telemetry to assure that
     * changes to the original list are not reflected in the telemetry.
     *
     * @param valuePoints
     */
    public ValuePointListTelemetryColored(List<ValuePointColored> valuePoints) {
        this.valuePoints = new ArrayList<ValuePointColored>(valuePoints);
    }
     /**
     * The liste returned is a copy of the internal telemetry provided.
     * It is a copy so clients can freely modify it and there is no need
     * to worry about changes.
     *
     * @return list of ValuePoints
     * a fresh copy of the represented current state
     */
    public List<ValuePointColored> getValue() {
        return new ArrayList<ValuePointColored>(valuePoints);
    }
 public String toString() {
        ValuePointColored minimum = ValuePointColored.at(Point.at(0), Double.POSITIVE_INFINITY,false);

        for (ValuePointColored valuePoint : valuePoints) {
            if(valuePoint.getValue() < minimum.getValue()) {
                minimum = valuePoint;
            }
        }

        return String.format("%d point with minimim: %s", 1, minimum);
    }
}
