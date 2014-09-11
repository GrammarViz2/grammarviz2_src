/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.hawaii.jmotif.sampler;

/**
 *
 * @author ytoh
 */
public interface Producer<T> {

    void addConsumer(Consumer<? super T> consumer);

    T getValue();
}
