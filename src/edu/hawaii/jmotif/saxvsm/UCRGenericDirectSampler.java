package edu.hawaii.jmotif.saxvsm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import edu.hawaii.jmotif.sampler.DirectMethod;
import edu.hawaii.jmotif.sampler.ObjectiveFunction;
import edu.hawaii.jmotif.sampler.PrintConsumer;
import edu.hawaii.jmotif.sampler.Solver;
import edu.hawaii.jmotif.sampler.UCRSolver;
import edu.hawaii.jmotif.text.SAXCollectionStrategy;
import edu.hawaii.jmotif.util.StackTrace;

/**
 * Helper-runner for CBF test.
 * 
 * @author psenin
 * 
 */
public class UCRGenericDirectSampler extends UCRGenericClassifier {

  // num of threads to use
  //
  private static final int THREADS_NUM = 3;

  // data
  //
  private static String TRAINING_DATA = "in";

  // output prefix
  //
  private static String OUTPUT_PREFIX = "out";

  // SAX parameters to use
  //
  private static int WINDOW_MIN = 5;
  private static int WINDOW_MAX = 100;

  private static int PAA_MIN = 3;
  private static int PAA_MAX = 60;

  private static int ALPHABET_MIN = 2;
  private static int ALPHABET_MAX = 18;

  private static int HOLD_OUT_NUM = 1;

  private static int MAX_ITERATIONS = 20;

  private static List<String> globalResults = new ArrayList<String>();

  private UCRGenericDirectSampler() {
    super();
  }

  /**
   * @param args
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws Exception {

    TRAINING_DATA = args[0];

    OUTPUT_PREFIX = args[1];

    WINDOW_MIN = Integer.valueOf(args[2]);
    WINDOW_MAX = Integer.valueOf(args[3]);

    PAA_MIN = Integer.valueOf(args[4]);
    PAA_MAX = Integer.valueOf(args[5]);

    ALPHABET_MIN = Integer.valueOf(args[6]);
    ALPHABET_MAX = Integer.valueOf(args[7]);

    HOLD_OUT_NUM = Integer.valueOf(args[8]);

    consoleLogger.info("data file: " + TRAINING_DATA);
    consoleLogger.info("output prefix: " + OUTPUT_PREFIX);

    consoleLogger.info("Window: " + WINDOW_MIN + ", " + WINDOW_MAX);

    consoleLogger.info("PAA: " + PAA_MIN + ", " + PAA_MAX);

    consoleLogger.info("Alphabet: " + ALPHABET_MIN + ", " + ALPHABET_MAX);

    consoleLogger.info("Holding out: " + HOLD_OUT_NUM);

    Map<String, List<double[]>> trainData = UCRUtils.readUCRData(TRAINING_DATA);
    consoleLogger.debug("reading file: " + TRAINING_DATA);
    consoleLogger.debug("trainData classes: " + trainData.size() + ", series length: "
        + trainData.entrySet().iterator().next().getValue().get(0).length);
    for (Entry<String, List<double[]>> e : trainData.entrySet()) {
      consoleLogger.debug(" training class: " + e.getKey() + " series: " + e.getValue().size());
    }

    double[] parametersLowest = { Double.valueOf(WINDOW_MIN), Double.valueOf(PAA_MIN),
        Double.valueOf(ALPHABET_MIN) };
    double[] parametersHighest = { Double.valueOf(WINDOW_MAX), Double.valueOf(PAA_MAX),
        Double.valueOf(ALPHABET_MAX) };

    ExecutorService executorService = Executors.newFixedThreadPool(THREADS_NUM);
    CompletionService<List<String>> completionService = new ExecutorCompletionService<List<String>>(
        executorService);
    int totalTaskCounter = 0;

    // create and submit the job for NOREDUCTION
    //
    ObjectiveFunction noredFunction = new UCRLOOCVErrorFunction();
    noredFunction.setStrategy(SAXCollectionStrategy.NOREDUCTION);
    PrintConsumer noredConsumer = new PrintConsumer(SAXCollectionStrategy.NOREDUCTION);
    noredFunction.setUpperBounds(parametersHighest);
    noredFunction.setLowerBounds(parametersLowest);
    noredFunction.setData(trainData, HOLD_OUT_NUM);
    DirectMethod noredMethod = new DirectMethod();
    noredMethod.addConsumer(noredConsumer);

    Solver noredSolver = new UCRSolver(MAX_ITERATIONS);
    noredSolver.init(noredFunction, noredMethod);

    completionService.submit((Callable<List<String>>) noredSolver);
    totalTaskCounter++;

    // create and submit the job for EXACT
    //
    ObjectiveFunction exactFunction = new UCRLOOCVErrorFunction();
    exactFunction.setStrategy(SAXCollectionStrategy.EXACT);
    PrintConsumer exactConsumer = new PrintConsumer(SAXCollectionStrategy.EXACT);
    exactFunction.setUpperBounds(parametersHighest);
    exactFunction.setLowerBounds(parametersLowest);
    exactFunction.setData(trainData, HOLD_OUT_NUM);
    DirectMethod exactMethod = new DirectMethod();
    exactMethod.addConsumer(exactConsumer);

    Solver exactSolver = new UCRSolver(MAX_ITERATIONS);
    exactSolver.init(exactFunction, exactMethod);

    completionService.submit((Callable<List<String>>) exactSolver);
    totalTaskCounter++;

    // create and submit the job for CLASSIC
    //
    ObjectiveFunction classicFunction = new UCRLOOCVErrorFunction();
    classicFunction.setStrategy(SAXCollectionStrategy.CLASSIC);
    PrintConsumer classicConsumer = new PrintConsumer(SAXCollectionStrategy.CLASSIC);
    classicFunction.setUpperBounds(parametersHighest);
    classicFunction.setLowerBounds(parametersLowest);
    classicFunction.setData(trainData, HOLD_OUT_NUM);
    DirectMethod classicMethod = new DirectMethod();
    classicMethod.addConsumer(classicConsumer);

    Solver classicSolver = new UCRSolver(MAX_ITERATIONS);
    classicSolver.init(classicFunction, classicMethod);

    completionService.submit((Callable<List<String>>) classicSolver);
    totalTaskCounter++;

    // waiting for completion, shutdown pool disabling new tasks from being submitted
    executorService.shutdown();
    consoleLogger.info("Submitted " + totalTaskCounter + " jobs, shutting down the pool");

    // waiting for jobs to finish
    //
    //
    try {

      while (totalTaskCounter > 0) {
        //
        // poll with a wait up to FOUR hours
        Future<List<String>> finished = completionService.poll(128, TimeUnit.HOURS);
        if (null == finished) {
          // something went wrong - break from here
          System.err.println("Breaking POLL loop after 128 HOURS of waiting...");
          break;
        }
        else {
          List<String> res = finished.get();
          globalResults.addAll(res);
          totalTaskCounter--;
        }
      }
      consoleLogger.info("All jobs completed.");

    }
    catch (Exception e) {
      System.err.println("Error while waiting results: " + StackTrace.toString(e));
    }
    finally {
      // wait at least 1 more hour before terminate and fail
      try {
        if (!executorService.awaitTermination(1, TimeUnit.HOURS)) {
          executorService.shutdownNow(); // Cancel currently executing tasks
          if (!executorService.awaitTermination(30, TimeUnit.MINUTES))
            System.err.println("Pool did not terminate... FATAL ERROR");
        }
      }
      catch (InterruptedException ie) {
        System.err.println("Error while waiting interrupting: " + StackTrace.toString(ie));
        // (Re-)Cancel if current thread also interrupted
        executorService.shutdownNow();
        // Preserve interrupt status
        Thread.currentThread().interrupt();
      }

    }

    BufferedWriter bw = new BufferedWriter(new FileWriter(OUTPUT_PREFIX + ".csv"));

    for (String line : globalResults) {
      bw.write(line + CR);
    }
    bw.close();

  }
}
