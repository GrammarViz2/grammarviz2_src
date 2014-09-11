/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.hawaii.jmotif.sampler;

/**
 * 
 * @author ytoh
 */
public final class Iteration<T> implements Telemetry<T> {

  private final T value;
  private final int iteration;

  public Iteration(T value, int iteration) {
    this.value = value;
    this.iteration = iteration;
  }

  public int getIteration() {
    return iteration;
  }

  public T getValue() {
    return value;
  }
}
