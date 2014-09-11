/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.hawaii.jmotif.sampler;

/**
 * A telemetry provider is an object able to externalize its inner values
 * using a telemery.
 * (Mediator pattern)
 *
 * All the communication to other telemetry aware objects is done by updating
 * values of the telemetry itself.
 *
 * @author ytoh
 */
public interface TelemetryProvider {
    
    /**
     * Return the telemetry to be used to externalize the state of optimization
     * execution.
     *
     * @return telemetry implementation
     */
    Telemetry[] getTelemetry();
}
