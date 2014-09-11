/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.hawaii.jmotif.sampler;

/**
 * 
 * @author ytoh
 */
public interface TelemetryVisualization<T extends Telemetry> extends Consumer<Iteration<T>>,
    Visualization<T> {

  /**
   * 
   * @param function
   * @param method
   */
  void init(Function function);
}
