/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.hawaii.jmotif.sampler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.LoggerFactory;
import edu.hawaii.jmotif.text.SAXCollectionStrategy;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Simple solver encapsulating the optimization method calculation and checking method stop
 * condition together with a max iteration stop condition.
 * 
 * @author ytoh
 */
// @Component(name="Basic solver",
// description="Simple solver encapsulating the optimization method calculation and checking method stop condition together with a max iteration stop condition.")
public class UCRSolver implements Solver, Callable<List<String>> {
  private static final Object COMMA = ",";
  private static final Level LOGGING_LEVEL = Level.ALL;
  // static final Logger logger = Logger.getLogger(BasicSolver.class);

  private Function function;
  private StopCondition[] methodConditions;
  private StopCondition[] systemConditions;
  private IterationStopCondition iterations;
  private Synchronization synchronization;
  private Consumer<? super Synchronization> synchronizationConsumer;
  private OptimizationMethod<? extends Telemetry> method;
  private BaseObjectiveFunction baseObjectiveFunction;
  // convenience shortcut
  private List<StopCondition> metConditions;

  // @Property(name="Maximum number of interations",
  // description="How many optimization steps are allowed before the optimization process is stopped.")
  // @Range(from=1, to=Integer.MAX_VALUE)
  private int maxIterations = Integer.MAX_VALUE;

  public int getMaxIterations() {
    return maxIterations;
  }

  public void setMaxIterations(int maxIterations) {
    this.maxIterations = maxIterations;
  }

  // @Property(name="Use delay")
  private boolean useDelay = false;

  public boolean isUseDelay() {
    return useDelay;
  }

  public void setUseDelay(boolean useDelay) {
    this.useDelay = useDelay;
  }

  // @Property(name="Delay between steps",description="How many miniseconds should this solver wait before doing another optimization step")
  // @Range(from=0,to=Integer.MAX_VALUE)
  private int milisDelay;
  private static Logger consoleLogger;

  public int getMilisDelay() {
    return milisDelay;
  }

  public void setMilisDelay(int milisDelay) {
    this.milisDelay = milisDelay;
  }

  public PropertyState getMilisDelayState() {
    return useDelay ? PropertyState.ENABLED : PropertyState.DISABLED;
  }

  /**
   * Constructs an instance of <code>BasicSolver</code> with a special system stop condition making
   * sure that the optimization calculation doesn't exceed a certain count.
   * 
   * @param maxIterations maximum number of main cycle iterations
   */
  public UCRSolver(int maxIterations) {
    assert maxIterations > 0;
    this.maxIterations = maxIterations;
    consoleLogger = (Logger) LoggerFactory.getLogger(UCRSolver.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  @SuppressWarnings("unchecked")
  public void init(Function function, @SuppressWarnings("rawtypes")
  OptimizationMethod method) throws Exception {
    this.function = function;
    this.method = method;

    baseObjectiveFunction = new BaseObjectiveFunction(function);
    if (!baseObjectiveFunction.hasAnalyticalGradient()) {
      baseObjectiveFunction.setNumericalGradient(new CentralDifferenceGradient());
    }

    if (!baseObjectiveFunction.hasAnalyticalHessian()) {
      baseObjectiveFunction.setNumericalHessian(new CentralDifferenceHessian());
    }

    synchronization = new Synchronization();

    method.init(baseObjectiveFunction);

    // get method specific stop conditions
    methodConditions = ArrayUtils.clone(method.getStopConditions());
    metConditions = new ArrayList<StopCondition>();

    iterations = new IterationStopCondition(maxIterations);
    systemConditions = new StopCondition[] { iterations };
  }

  public void addSystemStopCondition(StopCondition condition) {
    systemConditions = ArrayUtils.add(systemConditions, condition);
  }

  @Override
  public List<String> call() throws Exception {
    solve();
    ValuePointListTelemetryColored res = (ValuePointListTelemetryColored) method.getValue();
    List<String> resList = new ArrayList<String>();
    for (ValuePointColored v : res.getValue()) {
      resList.add(toLogStr(v, function.getSAXSamplingStrategy()));
    }
    return resList;
  }

  private String toLogStr(ValuePointColored v, SAXCollectionStrategy saxCollectionStrategy) {
    StringBuffer sb = new StringBuffer();

    // strategy
    if (saxCollectionStrategy.equals(SAXCollectionStrategy.CLASSIC)) {
      sb.append("CLASSIC,");
    }
    else if (saxCollectionStrategy.equals(SAXCollectionStrategy.EXACT)) {
      sb.append("EXACT,");
    }
    else if (saxCollectionStrategy.equals(SAXCollectionStrategy.NOREDUCTION)) {
      sb.append("NOREDUCTION,");
    }

    // coordinate
    sb.append(v.getPoint().toArray()[0]).append(COMMA);
    sb.append(v.getPoint().toArray()[1]).append(COMMA);
    sb.append(v.getPoint().toArray()[2]).append(COMMA);

    // error value
    sb.append(v.getValue());

    return sb.toString();
  }

  public void solve() throws Exception {
    int iteration = 0;
    // logger.debug("main cycle start");

    while (checkStopConditions()) {

      if (useDelay) {
        try {
          Thread.sleep(milisDelay);
        }
        catch (InterruptedException ex) {
          throw new OptimizationException("Stopping optimization", ex);
        }
      }

      consoleLogger.info("Iteration: " + iteration + " out of " + this.maxIterations + ", stats: "
          + baseObjectiveFunction.getStatistics());

      synchronization = new Synchronization(++iteration);
      if (synchronizationConsumer != null) {
        synchronizationConsumer.notifyOf(this);
      }
      iterations.nextIteration();

      // main optimization cycle
      method.optimize();
    }
  }

  /**
   * Convenience method for checking stop condition satisfaction.
   * 
   * @return true if any of the custom/system stop conditions have been met, else if no stop
   * conditions have been met
   */
  private boolean checkStopConditions() {
    // logger.debug("checking stop conditions");

    for (int i = 0; i < methodConditions.length; i++) {
      if (methodConditions[i].isConditionMet()) {
        // if(logger.isDebugEnabled()) {
        // logger.debug("condition met: " + methodConditions[i]);
        // }

        metConditions.add(methodConditions[i]);
      }
    }
    for (int i = 0; i < systemConditions.length; i++) {
      if (systemConditions[i].isConditionMet()) {
        // if(logger.isDebugEnabled()) {
        // logger.debug("condition met: " + systemConditions[i]);
        // }

        metConditions.add(systemConditions[i]);
      }
    }

    return metConditions.isEmpty();
  }

  public OptimizationResults getResults() {
    return new OptimizationResults(method.getValue(), baseObjectiveFunction.getStatistics(),
        synchronization.getValue(), metConditions);
  }

  public void addConsumer(Consumer<? super Synchronization> consumer) {
    this.synchronizationConsumer = consumer;
  }

  public Synchronization getValue() {
    return synchronization;
  }
}
