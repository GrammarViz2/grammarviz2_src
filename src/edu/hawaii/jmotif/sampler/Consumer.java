/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.hawaii.jmotif.sampler;

/**
 *
 * @author ytoh
 */
public interface Consumer<T> {

    void notifyOf(Producer<? extends T> producer);
}
