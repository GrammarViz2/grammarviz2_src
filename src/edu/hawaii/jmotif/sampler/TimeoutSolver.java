/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.hawaii.jmotif.sampler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A simple solver encapsulation running the optimization calculation in 
 * a separate thread and terminating it after a given time to prevent infinite 
 * looping or locks. (Decorator pattern)
 *
 * @author ytoh
 */
//@Component(name="Timeout solver")
public class TimeoutSolver implements Solver {
//    static final Logger logger = Logger.getLogger(TimeoutSolver.class);

    private ScheduledExecutorService scheduler;
    private Solver solver;
    private TimeoutStopCondition stopCondition;

//    @Property(name="Maximum solving time [s]")
//    @Range(from=1, to=Double.MAX_VALUE)
    private double timeout;

    public double getTimeout() {
        return timeout;
    }

    public void setTimeout(double timeout) {
        this.timeout = timeout;
    }
    /**
     * Constructs an instance of <code>TimeoutSolver</code> wrapping the given
     * solver and initializing the mechanism to terminate the optimization
     * calculation if it runs longer then the specified timeout period.
     *
     * @param solver worker solver to be wrapped
     * @param timeout maximum calculation period
     */
    TimeoutSolver(Solver solver, long timeout) {
        this.solver        = solver;
        this.timeout       = timeout;
    }

    public void init(Function function, OptimizationMethod method) throws Exception {
//        logger.debug("initialization of timeout solver");
        
        this.scheduler     = Executors.newSingleThreadScheduledExecutor();
        this.stopCondition = new TimeoutStopCondition((long)timeout);

        solver.init(function, method);
        solver.addSystemStopCondition(stopCondition);
    }

    @SuppressWarnings("unchecked")
    public void solve() throws Exception {

        final Callable innerSolver = new Callable() {
            public Object call() throws Exception {
                stopCondition.start();
                solver.solve();
                return null;
            }
        };

        final ScheduledFuture<?> solverHandle = scheduler.schedule(innerSolver, 0L, TimeUnit.SECONDS);

        try {
            solverHandle.get((long)timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
//            logger.error("Optimization stopped: User interrupt.", ex);
            
        } catch (ExecutionException ex) {
//            logger.error("Error occurred while optimizing", ex);
        } catch (TimeoutException ex) {
//            logger.warn("Optimization stopped: Time limit reached. (" + timeout + "ms)");
            solverHandle.cancel(true);
        } finally {
            scheduler.shutdown();
        }
    }

    public OptimizationResults getResults() {
        if(stopCondition.isConditionMet()) { // optimization process reached its max running time
            OptimizationResults innerResults = solver.getResults();
            // we need to add the timeout stop conditions to the list of met stop conditions
            List<StopCondition> conditions = new ArrayList<StopCondition>(innerResults.getMetConditions());
            conditions.add(stopCondition);
            return new OptimizationResults(innerResults.getSolution(), innerResults.getStatistics(), innerResults.getNumberOfIterations(), conditions);
        } else { // optimization process stopped before the specified timeout
            return solver.getResults();
        }
    }

    public void addSystemStopCondition(StopCondition condition) {
        solver.addSystemStopCondition(condition);
    }

    public void addConsumer(Consumer<? super Synchronization> consumer) {
        solver.addConsumer(consumer);
    }

    public Synchronization getValue() {
        return solver.getValue();
    }
}
