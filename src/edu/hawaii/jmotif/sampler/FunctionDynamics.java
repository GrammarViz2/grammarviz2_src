package edu.hawaii.jmotif.sampler;

/**
 * Created by IntelliJ IDEA.
 * User: SuperLooser
 * Date: 3.2.2011
 * Time: 20:28:40
 * Interface that provides necessary methods for receiving information about generation change in evolutionary process.
 * This interface is not only meant for Function, it can be implemented in any component that might want to know about evolutionary process, e.g. EvolutionaryOperator,...
 */
public interface FunctionDynamics {

    /**
     * Erases local counter and whole evolutionary process can start over.
     */
    public void resetGenerationCount();

    /**
     * Increments internal generation count by one, meaning that new generation has been created. 
     */
    public void nextGeneration();

    /**
     * Auxiliary method that sets internal generation count to given number.
     * Just in case of complex evolutionary processing that does not uses all operators in one generation.
     * @param currentGeneration - current generation number.
     */
    public void setGeneration(int currentGeneration);
}
