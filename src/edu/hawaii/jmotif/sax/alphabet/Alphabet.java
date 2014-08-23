package edu.hawaii.jmotif.sax.alphabet;

import edu.hawaii.jmotif.timeseries.TSException;

/**
 * The Alphabet class template.
 * 
 * @author Pavel Senin.
 * 
 */
public abstract class Alphabet {

  /**
   * get the max size of the alphabet.
   * 
   * @return maximum size of the alphabet.
   */
  public abstract Integer getMaxSize();

  /**
   * Get cut intervals corresponding to the alphabet size.
   * 
   * @param size The alphabet size.
   * @return cut intervals for the alphabet.
   * @throws TSException if error occurs.
   */
  public abstract double[] getCuts(Integer size) throws TSException;

  /**
   * Get the distance matrix for the alphabet size.
   * 
   * @param size The alphabet size.
   * @return The distance matrix.
   * @throws TSException if error occurs.
   */
  public abstract double[][] getDistanceMatrix(Integer size) throws TSException;

}