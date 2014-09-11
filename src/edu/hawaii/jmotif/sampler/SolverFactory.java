/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.hawaii.jmotif.sampler;


/**
 * A factory encapsulating the details of solver instantiation and initializations.
 * 
 * @author ytoh
 */
public class SolverFactory {
  // static final Logger logger = ConLogger.getLogger(SolverFactory.class);

  /**
   * Instantiates a solver that checks the optimization calculation and terminates it after a
   * certain number of iterations.
   * 
   * @param maxIterations
   * @return a solver stopping the calculation after <code>maxIterations<code>
   * iterations
   */
  public static Solver getNewInstance(int maxIterations) {
    return new UCRSolver(maxIterations);
  }

  /**
   * Instantiates a solver that checks the optimization calculation and terminates it after a
   * certain number of iterations or a certain time.
   * 
   * @param maxIterations
   * @param timeout
   * @return a solver stopping the calculation after <code>maxIterations<code>
   * or after <code>timenout</code> miliseconds
   */
  public static Solver getNewInstance(int maxIterations, long timeout) {
    // if(logger.isDebugEnabled()) {
    // logger.debug("creating a decorated solver with " + timeout + "ms timeout");
    // }

    return new TimeoutSolver(getNewInstance(maxIterations), timeout);
  }
}
