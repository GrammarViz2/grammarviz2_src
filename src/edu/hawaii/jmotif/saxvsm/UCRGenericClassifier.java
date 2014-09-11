package edu.hawaii.jmotif.saxvsm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import edu.hawaii.jmotif.text.Bigram;
import edu.hawaii.jmotif.text.BigramBag;
import edu.hawaii.jmotif.text.SAXCollectionStrategy;
import edu.hawaii.jmotif.text.TextUtils;
import edu.hawaii.jmotif.text.WordBag;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.timeseries.TSUtils;
import edu.hawaii.jmotif.util.StackTrace;

/**
 * Helper-runner for CBF test.
 * 
 * @author psenin
 * 
 */
public class UCRGenericClassifier {

  protected final static int CLASSIC = 0;
  protected final static int EXACT = 1;
  protected final static int NOREDUCTION = 2;

  // output stuff
  //
  protected static final String COMMA = ",";
  protected static final String CR = "\n";

  // logger
  //
  protected static Logger consoleLogger;
  private static String LOGGING_LEVEL = "FINE";

  /**
   * This implements k-leave out classification. It iterates over possible sets of parameters
   * running training on the subset of N-k series, while validating over k series. Result is going
   * to be the map of experiment abbreviation (window_paa_alphabet) and an entry combining mean
   * error value and a set of SAX parameters.
   * 
   * @param threadsNum How many threads to use.
   * @param windowSizes possible sliding window sizes.
   * @param paaSizes possible PAA sizes.
   * @param alphabetSizes possible alphabet sizes.
   * @param strategy the bag building strategy to employ.
   * @param trainData training data.
   * @param validationSampleSize validation sample size.
   * @return
   * @throws IndexOutOfBoundsException if error occurs.
   * @throws TSException if error occurs.
   */
  protected static List<String> trainKNNFoldJMotifThreaded(int threadsNum, int[] windowSizes,
      int[] paaSizes, int[] alphabetSizes, SAXCollectionStrategy strategy,
      Map<String, List<double[]>> trainData, int validationSampleSize)
      throws IndexOutOfBoundsException, TSException {

    // make a result map
    //
    // here keys are parameters like window _ PAA _ Alphabet
    //
    //
    List<String> results = new ArrayList<String>();

    // create thread pool for processing these users
    //
    ExecutorService executorService = Executors.newFixedThreadPool(threadsNum);
    CompletionService<String> completionService = new ExecutorCompletionService<String>(
        executorService);
    int totalTaskCounter = 0;

    // here is a loop over SAX parameters, strategy is fixed
    //
    for (int windowSize : windowSizes) {
      for (int paaSize : paaSizes) {
        for (int alphabetSize : alphabetSizes) {

          // make sure to brake if PAA greater than window
          if (windowSize < paaSize + 1) {
            continue;
          }

          // create and submit the job
          final UCRKNNloocvJob job = new UCRKNNloocvJob(trainData, validationSampleSize,
              windowSize, paaSize, alphabetSize, strategy);

          completionService.submit(job);
          totalTaskCounter++;

        }
      }
    }

    // waiting for completion, shutdown pool disabling new tasks from being submitted
    executorService.shutdown();
    consoleLogger.info("Submitted " + totalTaskCounter + " jobs, shutting down the pool");

    try {

      while (totalTaskCounter > 0) {
        //
        // poll with a wait up to FOUR hours
        Future<String> finished = completionService.poll(96, TimeUnit.HOURS);
        if (null == finished) {
          // something went wrong - break from here
          System.err.println("Breaking POLL loop after 48 HOURS of waiting...");
          break;
        }
        else {
          String res = finished.get();
          if (!(res.startsWith("ok_"))) {
            System.err.println("Exception caught: " + finished.get());
            break;
          }
          else {
            String record = res.substring(3);
            consoleLogger.info(record);
            results.add(record);
          }
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

    return results;

  }

  /**
   * This implements k-leave out classification. It iterates over possible sets of parameters
   * running training on the subset of N-k series, while validating over k series. Result is going
   * to be the map of experiment abbreviation (window_paa_alphabet) and an entry combining mean
   * error value and a set of SAX parameters.
   * 
   * @param windowSizes possible sliding window sizes.
   * @param paaSizes possible PAA sizes.
   * @param alphabetSizes possible alphabet sizes.
   * @param strategy the bag building strategy to employ.
   * @param trainData training data.
   * @param validationSampleSize validation sample size.
   * @return
   * @throws IndexOutOfBoundsException if error occurs.
   * @throws TSException if error occurs.
   */
  protected static List<String> trainKNNFoldJMotif(int[] windowSizes, int[] paaSizes,
      int[] alphabetSizes, SAXCollectionStrategy strategy, Map<String, List<double[]>> trainData,
      int validationSampleSize) throws IndexOutOfBoundsException, TSException {

    // make a result map
    //
    // here keys are parameters like window _ PAA _ Alphabet
    //
    //
    List<String> results = new ArrayList<String>();

    // here is a loop over SAX parameters, strategy is fixed
    //
    for (int windowSize : windowSizes) {
      for (int paaSize : paaSizes) {
        for (int alphabetSize : alphabetSizes) {

          // make sure to brake if PAA greater than window
          if (windowSize < paaSize + 1) {
            continue;
          }

          // parameters
          int[] params = new int[4];
          params[0] = windowSize;
          params[1] = paaSize;
          params[2] = alphabetSize;
          params[3] = strategy.index();

          // push into stack all the samples we are going to validate for
          Stack<KNNOptimizedStackEntry> samples2go = new Stack<KNNOptimizedStackEntry>();
          for (Entry<String, List<double[]>> e : trainData.entrySet()) {
            String key = e.getKey();
            int index = 0;
            for (double[] sample : e.getValue()) {
              samples2go.push(new KNNOptimizedStackEntry(key, sample, index));
              index++;
            }
          }

          // total counter
          int totalSamples = samples2go.size();

          // missclassified counter
          int missclassifiedSamples = 0;

          // cache for bags
          HashMap<String, WordBag> cache = new HashMap<String, WordBag>();

          // while something in stack
          while (!samples2go.isEmpty()) {

            // extracting validation samples
            //
            List<KNNOptimizedStackEntry> currentValidationSample = new ArrayList<KNNOptimizedStackEntry>();
            Set<Integer> currentValidationIndexes = new TreeSet<Integer>();
            for (int i = 0; i < validationSampleSize; i++) {
              if (samples2go.isEmpty()) {
                break;
              }
              KNNOptimizedStackEntry sample = samples2go.pop();
              String cKey = sample.getKey();
              if (i > 0) {
                if (!(cKey.equalsIgnoreCase(currentValidationSample.get(i - 1).getKey()))) {
                  samples2go.push(sample);
                  break;
                }
              }
              currentValidationSample.add(sample);
              currentValidationIndexes.add(sample.getIndex());
            }

            // check if something in the validation sample
            //
            if (currentValidationSample.isEmpty()) {
              break;
            }

            String validationKey = currentValidationSample.get(0).getKey();

            // re-build bags if there is a need or pop them from the stack
            //
            for (Entry<String, List<double[]>> e : trainData.entrySet()) {

              // if there is a hit - need to rebuild that bag and replace it in the cache
              if (e.getKey().equalsIgnoreCase(validationKey)) {
                WordBag bag = new WordBag(validationKey);
                int index = -1;
                for (double[] series : e.getValue()) {
                  index++;
                  if (currentValidationIndexes.contains(index)) {
                    // if (sampleContainsSeries(currentValidationSample, series)) {
                    // System.out.println("bingo! ");
                    // }
                    // else {
                    // System.out.println("Wrong! ");
                    // System.exit(10);
                    // }
                    continue;
                  }
                  WordBag cb = TextUtils.seriesToWordBag("tmp", series, params);
                  bag.mergeWith(cb);
                }
                cache.put(validationKey, bag);
              }

              // else we just check if a bag is in place, if not - we put it in
              else {
                if (!cache.containsKey(e.getKey())) {
                  WordBag bag = new WordBag(e.getKey());
                  for (double[] series : e.getValue()) {
                    WordBag cb = TextUtils.seriesToWordBag("tmp", series, params);
                    bag.mergeWith(cb);
                  }
                  cache.put(e.getKey(), bag);
                }
              }

            } // end of cache update loop

            // all stuff from the cache will build a classifier vectors
            //

            // compute TFIDF statistics for training set
            HashMap<String, HashMap<String, Double>> tfidf = TextUtils.computeTFIDF(cache.values());

            // normalize to unit vectors to avoid false discrimination by vector magnitude
            tfidf = TextUtils.normalizeToUnitVectors(tfidf);

            // Classifying...
            //
            // is this sample correctly classified?
            for (KNNOptimizedStackEntry e : currentValidationSample) {
              int res = TextUtils.classify(e.getKey(), e.getValue(), tfidf, params);
              if (0 == res) {
                missclassifiedSamples = missclassifiedSamples + 1;
              }
            }

          }

          double error = Integer.valueOf(missclassifiedSamples).doubleValue()
              / Integer.valueOf(totalSamples).doubleValue();
          results.add(toLogStr(params, 1.0D - error, error));
          consoleLogger.fine(toLogStr(params, 1.0D - error, error));

        }
      }
    }

    return results;

  }

  protected static void run2GrammClassificationExperiment(String trainingDataName,
      String testDataName, int windowSize, int[] paa_sizes, int[] alphabet_sizes,
      SAXCollectionStrategy strategy, String outFname) throws IOException,
      IndexOutOfBoundsException, TSException {

    BufferedWriter bw = new BufferedWriter(new FileWriter(outFname));

    // reading training and test collections
    //
    Map<String, List<double[]>> trainData = UCRUtils.readUCRData(trainingDataName);
    consoleLogger.fine("trainData classes: " + trainData.size() + ", series length: "
        + trainData.entrySet().iterator().next().getValue().get(0).length);
    for (Entry<String, List<double[]>> e : trainData.entrySet()) {
      consoleLogger.fine(" training class: " + e.getKey() + " series: " + e.getValue().size());
    }

    Map<String, List<double[]>> testData = UCRUtils.readUCRData(testDataName);
    consoleLogger.fine("testData classes: " + testData.size());
    for (Entry<String, List<double[]>> e : testData.entrySet()) {
      consoleLogger.fine(" test class: " + e.getKey() + " series: " + e.getValue().size());
    }

    for (int paaSize : paa_sizes) {
      for (int alphabetSize : alphabet_sizes) {

        if (windowSize < paaSize + 1) {
          continue;
        }

        int[][] params = new int[1][4];
        params[0][0] = windowSize;
        params[0][1] = paaSize;
        params[0][2] = alphabetSize;
        params[0][3] = strategy.index();

        // making training bags collection
        List<BigramBag> bags = TextUtils.labeledSeries2BigramBags(trainData, params);

        HashMap<String, HashMap<Bigram, Double>> tfidf = TextUtils.computeTFIDF(bags);
        tfidf = TextUtils.normalizeBigramsToUnitVectors(tfidf);

        int totalTestSample = 0;
        int totalPositiveTests = 0;

        for (String currenClassUnderTest : testData.keySet()) {
          List<double[]> testD = testData.get(currenClassUnderTest);

          int positives = 0;
          for (double[] series : testD) {
            positives = positives
                + TextUtils.classifyBigrams(currenClassUnderTest, series, tfidf, params);
            totalTestSample++;
          }
          totalPositiveTests = totalPositiveTests + positives;

        }

        double accuracy = (double) totalPositiveTests / (double) totalTestSample;
        double error = 1.0d - accuracy;

        String str = windowSize + COMMA + paaSize + COMMA + alphabetSize + COMMA + accuracy + COMMA
            + error;

        bw.write(str + CR);
        consoleLogger.fine(str);

      }
    }
    bw.close();
  }

  protected static void runClassificationExperiment(String trainingDataName, String testDataName,
      Integer windowSize, int[] paa_sizes, int[] alphabet_sizes, SAXCollectionStrategy strategy,
      String outFname) throws IOException, IndexOutOfBoundsException, TSException {

    BufferedWriter bw = new BufferedWriter(new FileWriter(outFname));

    // reading training and test collections
    //
    Map<String, List<double[]>> trainData = UCRUtils.readUCRData(trainingDataName);
    consoleLogger.fine("trainData classes: " + trainData.size() + ", series length: "
        + trainData.entrySet().iterator().next().getValue().get(0).length);
    for (Entry<String, List<double[]>> e : trainData.entrySet()) {
      consoleLogger.fine(" training class: " + e.getKey() + " series: " + e.getValue().size());
    }

    Map<String, List<double[]>> testData = UCRUtils.readUCRData(testDataName);
    consoleLogger.fine("testData classes: " + testData.size());
    for (Entry<String, List<double[]>> e : testData.entrySet()) {
      consoleLogger.fine(" test class: " + e.getKey() + " series: " + e.getValue().size());
    }

    for (int paaSize : paa_sizes) {
      for (int alphabetSize : alphabet_sizes) {

        if (windowSize < paaSize + 1) {
          continue;
        }

        int[] params = new int[4];
        params[0] = windowSize;
        params[1] = paaSize;
        params[2] = alphabetSize;
        params[3] = strategy.index();

        // making training bags collection
        List<WordBag> bags = TextUtils.labeledSeries2WordBags(trainData, params);

        HashMap<String, HashMap<String, Double>> tfidf = TextUtils.computeTFIDF(bags);
        tfidf = TextUtils.normalizeToUnitVectors(tfidf);

        int totalTestSample = 0;
        int totalPositiveTests = 0;

        for (String currenClassUnderTest : testData.keySet()) {
          List<double[]> testD = testData.get(currenClassUnderTest);

          int positives = 0;
          for (double[] series : testD) {
            positives = positives + TextUtils.classify(currenClassUnderTest, series, tfidf, params);
            totalTestSample++;
          }
          totalPositiveTests = totalPositiveTests + positives;

        }

        double accuracy = (double) totalPositiveTests / (double) totalTestSample;
        double error = 1.0d - accuracy;

        String str = windowSize + COMMA + paaSize + COMMA + alphabetSize + COMMA + accuracy + COMMA
            + error;

        bw.write(str + CR);
        consoleLogger.fine(str);

      }
    }
    bw.close();
  }

  protected static void runKNNExperiment(Map<String, List<double[]>> trainData,
      Map<String, List<double[]>> testData, Integer windowSize, int paaSize, int alphabetSize,
      SAXCollectionStrategy strategy, String outFname) throws IOException,
      IndexOutOfBoundsException, TSException {

    BufferedWriter bw = new BufferedWriter(new FileWriter(outFname));

    // make parameters array
    //
    int[][] params = new int[1][4];
    params[0][0] = windowSize;
    params[0][1] = paaSize;
    params[0][2] = alphabetSize;
    params[0][3] = strategy.index();

    // figuring out a total test collection size
    //
    int totalTestSample = 0;
    for (Entry<String, List<double[]>> e : testData.entrySet()) {
      totalTestSample = totalTestSample + e.getValue().size();
    }

    // build huge TFIDF table for all of trainData
    //
    long start = System.currentTimeMillis();
    List<WordBag> trainBags = new ArrayList<WordBag>();
    for (Entry<String, List<double[]>> referenceSet : trainData.entrySet()) {
      int counter = 0;
      for (double[] series : referenceSet.getValue()) {
        WordBag newBag = TextUtils.seriesToWordBag(
            referenceSet.getKey() + "_" + String.valueOf(counter), series, params[0]);
        trainBags.add(newBag);
        counter++;
      }
    }
    HashMap<String, HashMap<String, Double>> tfidf = TextUtils.computeTFIDF(trainBags);
    tfidf = TextUtils.normalizeToUnitVectors(tfidf);
    consoleLogger.fine("TFIDF statistics table is built in "
        + timeToString(start, System.currentTimeMillis()));

    // ################ begin classification
    //
    int totalPositiveTests = 0;

    int queryCounter = 0;

    // #### here we iterate over all TEST series, class by class, series by series
    //
    for (Entry<String, List<double[]>> querySet : testData.entrySet()) {
      for (double[] querySeries : querySet.getValue()) {

        consoleLogger.fine("classifying query " + queryCounter + " of class " + querySet.getKey());

        // this holds the closest neighbor out of all training data with its class
        //
        double bestDistance = Double.MIN_VALUE;
        String bestClass = "";

        // the query word bag
        WordBag queryBag = TextUtils.seriesToWordBag("query", querySeries, params[0]);

        for (Entry<String, HashMap<String, Double>> neighbor : tfidf.entrySet()) {

          double similarity = TextUtils.cosineSimilarity(queryBag, neighbor.getValue());

          if (similarity > bestDistance) {
            bestDistance = similarity;
            bestClass = neighbor.getKey();
            consoleLogger.fine(" + closest class: " + bestClass + " distance: " + bestDistance);
          }
        }

        // best distance inner loop - over references

        if (bestClass.substring(0, bestClass.indexOf('_')).equalsIgnoreCase(querySet.getKey())) {
          totalPositiveTests++;
          consoleLogger.fine(" * hit!");
        }
        else {
          consoleLogger.fine(" ? miss!");
        }

        queryCounter++;
      }
    }

    double accuracy = (double) totalPositiveTests / (double) totalTestSample;
    double error = 1.0d - accuracy;

    System.out.println(accuracy + "," + error + "\n");
    bw.write(accuracy + "," + error + "\n");
    bw.close();
  }

  protected static void runKNNExperiment(String trainingDataName, String testDataName,
      Integer windowSize, int paa_size, int alphabet_size, SAXCollectionStrategy strategy,
      String outFname) throws IOException, IndexOutOfBoundsException, TSException {

    // reading training and test collections
    //
    Map<String, List<double[]>> trainData = UCRUtils.readUCRData(trainingDataName);
    consoleLogger.fine("trainData classes: " + trainData.size() + ", series length: "
        + trainData.entrySet().iterator().next().getValue().get(0).length);
    for (Entry<String, List<double[]>> e : trainData.entrySet()) {
      consoleLogger.fine(" training class: " + e.getKey() + " series: " + e.getValue().size());
    }

    int totalTestSample = 0;
    Map<String, List<double[]>> testData = UCRUtils.readUCRData(testDataName);
    consoleLogger.fine("testData classes: " + testData.size());
    for (Entry<String, List<double[]>> e : testData.entrySet()) {
      consoleLogger.fine(" test class: " + e.getKey() + " series: " + e.getValue().size());
      totalTestSample = totalTestSample + e.getValue().size();
    }

    runKNNExperiment(trainData, testData, windowSize, paa_size, alphabet_size, strategy, outFname);
  }

  protected static int[] makeArray(int minValue, int maxValue, int incrementValue) {
    ArrayList<Integer> preRes = new ArrayList<Integer>();
    int curValue = minValue;
    do {
      preRes.add(curValue);
      curValue = curValue + incrementValue;
    }
    while (curValue <= maxValue);
    int[] res = new int[preRes.size()];
    for (int i = 0; i < preRes.size(); i++) {
      res[i] = preRes.get(i).intValue();
    }
    return res;
  }

  /**
   * This implements k-leave out classification. It iterates over possible sets of parameters
   * running training on the subset of N-k series, while validating over k series. Result is going
   * to be the map of experiment abbreviation (window_paa_alphabet) and an entry combining mean
   * error value and a set of SAX parameters.
   * 
   * @param windowSizes possible sliding window sizes.
   * @param paaSizes possible PAA sizes.
   * @param alphabetSizes possible alphabet sizes.
   * @param strategy the bag building strategy to employ.
   * @param trainData training data.
   * @param validationSampleSize validation sample size.
   * @return
   * @throws IndexOutOfBoundsException if error occurs.
   * @throws TSException if error occurs.
   */
  protected static Map<String, Entry<Double, int[][]>> trainKNNFold(int[] windowSizes,
      int[] paaSizes, int[] alphabetSizes, SAXCollectionStrategy strategy,
      Map<String, List<double[]>> trainData, int validationSampleSize)
      throws IndexOutOfBoundsException, TSException {

    // make a result map
    HashMap<String, Entry<Double, int[][]>> results = new HashMap<String, Map.Entry<Double, int[][]>>();

    // minimal subclass length
    //
    int minLen = Integer.MAX_VALUE;
    for (Entry<String, List<double[]>> e : trainData.entrySet()) {
      if (minLen > e.getValue().size()) {
        minLen = e.getValue().size();
      }
    }

    int slicesNum = minLen / validationSampleSize;
    HashMap<String, Integer> classIncerements = new HashMap<String, Integer>();
    for (Entry<String, List<double[]>> e : trainData.entrySet()) {
      Integer currSliceSize = e.getValue().size() / slicesNum;
      classIncerements.put(e.getKey(), currSliceSize);
    }

    for (int windowSize : windowSizes) {
      for (int paaSize : paaSizes) {
        for (int alphabetSize : alphabetSizes) {

          if (windowSize < paaSize + 1) {
            continue;
          }

          // get the iteration number
          int slices = minLen / validationSampleSize;

          // init the iteration's error rates array
          double[] errors = new double[slices];

          // iterate over possible sets
          for (int currentSlice = 0; currentSlice < slices; currentSlice++) {

            // training subset
            Map<String, List<double[]>> innerTrainData = remove(trainData, classIncerements,
                currentSlice);

            // validation subset
            Map<String, List<double[]>> innerTestData = extract(trainData, classIncerements,
                currentSlice);

            // sometimes classes are of different sizes; we took care about not getting out of
            // boundaries, but we need to take care about the last iteration
            if (currentSlice == slices - 1) {
              innerTrainData = removeMax(trainData, classIncerements, currentSlice);
              innerTestData = extractMax(trainData, classIncerements, currentSlice);
            }

            // making training bags collection
            List<WordBag> bags = TextUtils.labeledSeries2WordBags(innerTrainData, paaSize,
                alphabetSize, windowSize, strategy);

            // compute TFIDF statistics for training set
            HashMap<String, HashMap<String, Double>> tfidf = TextUtils.computeTFIDF(bags);

            // normalize to unit vectors to avoid false discrimination by vector magnitude
            tfidf = TextUtils.normalizeToUnitVectors(tfidf);

            // init counters
            int totalTestSample = 0;
            int totalPositiveTests = 0;

            // let's see the error rate for this fold
            // iterating over class labels
            for (String label : tfidf.keySet()) {

              List<double[]> testD = innerTestData.get(label);

              int positives = 0;

              for (double[] series : testD) {
                positives = positives
                    + TextUtils.classify(label, series, tfidf, paaSize, alphabetSize, windowSize,
                        strategy);
                totalTestSample++;
              }
              totalPositiveTests = totalPositiveTests + positives;

            }

            // compute accuracy and the error rate
            double accuracy = (double) totalPositiveTests / (double) totalTestSample;
            double error = 1.0d - accuracy;

            // save the error rate value
            errors[currentSlice] = error;
          }

          // here cross-validation stuff finished
          //
          int[][] params = new int[2][3];
          params[0][0] = windowSize;
          params[0][1] = paaSize;
          params[0][2] = alphabetSize;

          results.put(
              String.valueOf(windowSize) + "_" + String.valueOf(paaSize) + "_"
                  + String.valueOf(alphabetSize),
              new KNNStackEntry<Double, int[][]>(TSUtils.mean(errors), params));

          consoleLogger.fine("params " + Arrays.toString(params[0]) + ", max. error: "
              + TSUtils.max(errors) + ", mean error: " + TSUtils.mean(errors) + ", min. error: "
              + TSUtils.min(errors));

        }
      }
    }

    return results;

  }

  /**
   * Extract subset.
   * 
   * @param trainData
   * @param classIncerements
   * @param currentSlice
   * @return
   */
  private static Map<String, List<double[]>> extract(Map<String, List<double[]>> trainData,
      HashMap<String, Integer> classIncerements, int currentSlice) {
    Map<String, List<double[]>> res = new HashMap<String, List<double[]>>();
    for (Entry<String, List<double[]>> e : trainData.entrySet()) {
      String className = e.getKey();
      Integer classSliseSize = classIncerements.get(className);
      List<double[]> classSample = new ArrayList<double[]>();
      int lowBound = classSliseSize * currentSlice;
      int highBound = classSliseSize * (currentSlice + 1);
      for (int i = lowBound; i < highBound; i++) {
        classSample.add(e.getValue().get(i));
      }
      res.put(className, classSample);
    }
    return res;
  }

  private static Map<String, List<double[]>> extractMax(Map<String, List<double[]>> trainData,
      HashMap<String, Integer> classIncerements, int currentSlice) {
    Map<String, List<double[]>> res = new HashMap<String, List<double[]>>();
    for (Entry<String, List<double[]>> e : trainData.entrySet()) {
      String className = e.getKey();
      Integer classSliseSize = classIncerements.get(className);
      List<double[]> classSample = new ArrayList<double[]>();
      int lowBound = classSliseSize * currentSlice;
      int highBound = e.getValue().size();
      for (int i = lowBound; i < highBound; i++) {
        classSample.add(e.getValue().get(i));
      }
      res.put(className, classSample);
    }
    return res;
  }

  /**
   * Remove subset.
   * 
   * @param trainData
   * @param classIncerements
   * @param currentSlice
   * @return
   */
  private static Map<String, List<double[]>> remove(Map<String, List<double[]>> trainData,
      HashMap<String, Integer> classIncerements, int currentSlice) {
    Map<String, List<double[]>> res = new HashMap<String, List<double[]>>();
    for (Entry<String, List<double[]>> e : trainData.entrySet()) {
      String className = e.getKey();
      Integer classSliseSize = classIncerements.get(className);
      List<double[]> classSample = new ArrayList<double[]>();
      int lowBound = classSliseSize * currentSlice;
      int highBound = classSliseSize * (currentSlice + 1);
      for (int i = 0; i < e.getValue().size(); i++) {
        if (lowBound <= i && i < highBound) {
          continue;
        }
        classSample.add(e.getValue().get(i));
      }
      res.put(className, classSample);
    }
    return res;
  }

  private static Map<String, List<double[]>> removeMax(Map<String, List<double[]>> trainData,
      HashMap<String, Integer> classIncerements, int currentSlice) {
    Map<String, List<double[]>> res = new HashMap<String, List<double[]>>();
    for (Entry<String, List<double[]>> e : trainData.entrySet()) {
      String className = e.getKey();
      Integer classSliseSize = classIncerements.get(className);
      List<double[]> classSample = new ArrayList<double[]>();
      int lowBound = classSliseSize * currentSlice;
      int highBound = e.getValue().size();
      for (int i = 0; i < e.getValue().size(); i++) {
        if (lowBound <= i && i < highBound) {
          continue;
        }
        classSample.add(e.getValue().get(i));
      }
      res.put(className, classSample);
    }
    return res;
  }

  private static String timeToString(long start, long finish) {

    StringBuffer sb = new StringBuffer();
    long diff = finish - start;

    final long secondInMillis = 1000;
    final long minuteInMillis = secondInMillis * 60;
    final long hourInMillis = minuteInMillis * 60;
    // final long dayInMillis = hourInMillis * 24;
    // final long yearInMillis = dayInMillis * 365;

    // long elapsedYears = diff / yearInMillis;
    // diff = diff % yearInMillis;
    // long elapsedDays = diff / dayInMillis;
    // diff = diff % dayInMillis;
    long elapsedHours = diff / hourInMillis;
    if (elapsedHours > 0) {
      sb.append(String.valueOf(elapsedHours) + "h ");
    }
    diff = diff % hourInMillis;

    long elapsedMinutes = diff / minuteInMillis;
    if (elapsedMinutes > 0) {
      sb.append(String.valueOf(elapsedMinutes) + "m ");
    }
    diff = diff % minuteInMillis;

    long elapsedSeconds = diff / secondInMillis;
    if (elapsedSeconds > 0) {
      sb.append(String.valueOf(elapsedSeconds) + "s ");
    }

    diff = diff % secondInMillis;
    if (diff > 0) {
      sb.append(String.valueOf(diff) + "ms");
    }

    return sb.toString();
  }

  protected static String getStrategyPrefix(SAXCollectionStrategy strategy) {
    String strategyP = "noreduction";
    if (SAXCollectionStrategy.EXACT.equals(strategy)) {
      strategyP = "exact";
    }
    if (SAXCollectionStrategy.CLASSIC.equals(strategy)) {
      strategy = SAXCollectionStrategy.CLASSIC;
      strategyP = "classic";
    }
    return strategyP;
  }

  protected static String toLogStr(int[] p, double accuracy, double error) {
    StringBuffer sb = new StringBuffer();
    if (SAXCollectionStrategy.CLASSIC.index() == p[3]) {
      sb.append("CLASSIC,");
    }
    else if (SAXCollectionStrategy.EXACT.index() == p[3]) {
      sb.append("EXACT,");
    }
    else if (SAXCollectionStrategy.NOREDUCTION.index() == p[3]) {
      sb.append("NOREDUCTION,");
    }
    sb.append(p[0]).append(COMMA);
    sb.append(p[1]).append(COMMA);
    sb.append(p[2]).append(COMMA);
    sb.append(accuracy).append(COMMA);
    sb.append(error);

    return sb.toString();
  }

  protected static String toLogStr(int[][] params, SAXCollectionStrategy strategy, double accuracy,
      double error) {
    StringBuffer sb = new StringBuffer();
    if (strategy.equals(SAXCollectionStrategy.CLASSIC)) {
      sb.append("CLASSIC,");
    }
    else if (strategy.equals(SAXCollectionStrategy.EXACT)) {
      sb.append("EXACT,");
    }
    else if (strategy.equals(SAXCollectionStrategy.NOREDUCTION)) {
      sb.append("NOREDUCTION,");
    }
    sb.append(params[0][0]).append(COMMA);
    sb.append(params[0][1]).append(COMMA);
    sb.append(params[0][2]).append(COMMA);
    sb.append(Double.valueOf(accuracy).toString()).append(COMMA);
    sb.append(Double.valueOf(error).toString());

    return sb.toString();
  }
}
