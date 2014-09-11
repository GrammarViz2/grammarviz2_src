/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.hawaii.jmotif.sampler;

/**
 * <code>OptimizationException</code> signals a problem during optimization.
 * An exception during optimization should most likely stop the execution thread
 * that is why <code>OptimizationException</code> is a subclass
 * of <code>RuntimeException</code>.
 *
 * @author ytoh
 */
public class OptimizationException extends RuntimeException {

    /**
   * 
   */
  private static final long serialVersionUID = 1284129641036611338L;

    /**
     * Creates a new instance of <code>OptimizationException</code> without detail message.
     */
    public OptimizationException() {}


    /**
     * Constructs an instance of <code>OptimizationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public OptimizationException(String msg) {
        super(msg);
    }

    /**
     * Creates a new instance of <code>OptimizationException</code> with the specified cause.
     *
     * @param cause the cause of this exception
     */
    public OptimizationException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance of <code>OptimizationException</code> with a cause and a message.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public OptimizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
