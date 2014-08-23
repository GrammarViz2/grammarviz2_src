package edu.hawaii.jmotif.sax.parallel;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.hawaii.jmotif.sax.SAXFactory;
import edu.hawaii.jmotif.sax.alphabet.NormalAlphabet;
import edu.hawaii.jmotif.sax.NumerosityReductionStrategy;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.util.StackTrace;

public class ParallelSAXImplementation {

  // locale, charset, etc
  //
  final static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  // logging stuff
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.DEBUG;

  // static block - we instantiate the logger
  //
  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(ParallelSAXImplementation.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  public ParallelSAXImplementation() {
    super();
  }

  /**
   * Builds a SAX representation.
   * 
   * @param timeseries
   * @param threadsNum
   * @param slidingWindowSize
   * @param paaSize
   * @param alphabetSize
   * @param nrStrategy
   * @param normalizationThreshold
   * @return
   * @throws TSException
   */
  public SAXRecords process(double[] timeseries, int threadsNum, int slidingWindowSize,
      int paaSize, int alphabetSize, NumerosityReductionStrategy nrStrategy,
      double normalizationThreshold) throws TSException {

    consoleLogger.debug("Starting the parallel SAX");

    NormalAlphabet alphabet = new NormalAlphabet();

    SAXRecords res = new SAXRecords(0);

    ExecutorService executorService = Executors.newFixedThreadPool(threadsNum);
    consoleLogger.debug("Created thread pool of " + threadsNum + " threads");

    CompletionService<SAXRecords> completionService = new ExecutorCompletionService<SAXRecords>(
        executorService);

    int totalTaskCounter = 0;

    long tstamp = System.currentTimeMillis();

    // first chunk takes on the uneven division
    //
    int evenIncrement = timeseries.length / threadsNum;
    int reminder = timeseries.length % threadsNum;
    int firstChunkSize = evenIncrement + reminder;
    consoleLogger.debug("data size " + timeseries.length + ", evenIncrement " + evenIncrement
        + ", reminder " + reminder + ", firstChunkSize " + firstChunkSize);

    // execute chunks processing
    //

    // the first chunk
    {
      int firstChunkStart = 0;
      int firstChunkEnd = firstChunkSize + slidingWindowSize - 1;
      final SAXWorker job0 = new SAXWorker(tstamp + totalTaskCounter, timeseries, firstChunkStart,
          firstChunkEnd, slidingWindowSize, paaSize, alphabetSize, nrStrategy,
          normalizationThreshold);
      completionService.submit(job0);
      consoleLogger.debug("submitted first chunk job " + tstamp);
      totalTaskCounter++;
    }

    // intermediate chunks
    while (totalTaskCounter < threadsNum - 1) {
      int intermediateChunkStart = firstChunkSize + (totalTaskCounter - 1) * evenIncrement;
      int intermediateChunkEnd = firstChunkSize + (totalTaskCounter * evenIncrement)
          + slidingWindowSize - 1;
      final SAXWorker job = new SAXWorker(tstamp + totalTaskCounter, timeseries,
          intermediateChunkStart, intermediateChunkEnd, slidingWindowSize, paaSize, alphabetSize,
          nrStrategy, normalizationThreshold);
      completionService.submit(job);
      consoleLogger.debug("submitted last chunk job " + Long.valueOf(tstamp + totalTaskCounter));
      totalTaskCounter++;
    }

    // the last chunk
    {
      int lastChunkStart = timeseries.length - evenIncrement;
      int lastChunkEnd = timeseries.length;
      final SAXWorker jobN = new SAXWorker(tstamp + totalTaskCounter, timeseries, lastChunkStart,
          lastChunkEnd, slidingWindowSize, paaSize, alphabetSize, nrStrategy,
          normalizationThreshold);
      completionService.submit(jobN);
      consoleLogger.debug("submitted last chunk job " + Long.valueOf(tstamp + totalTaskCounter));
      totalTaskCounter++;
    }

    executorService.shutdown();

    try {
      while (totalTaskCounter > 0) {

        Future<SAXRecords> finished = completionService.poll(128, TimeUnit.HOURS);

        if (null == finished) {
          // something went wrong - break from here
          System.err.println("Breaking POLL loop after 128 HOURS of waiting...");
          break;
        }
        else {
          // merge the block with junctions
          //
          SAXRecords chunkRes = finished.get();

          int idx = (int) (chunkRes.getId() - tstamp);
          consoleLogger.debug("job " + chunkRes.getId() + " of chunk " + idx + " has finished");
          if (0 == res.size()) {
            res.addAll(chunkRes);
            consoleLogger.debug("merging with empty res");
          }
          else {
            consoleLogger.debug("processing chunk " + idx + " res has results already...");
            // the very first chunk has ID=0
            //
            if (0 == idx) {
              consoleLogger.debug("this is the first chunk, merging the tail only");
              // we only need to care about the very last entry
              int tailIndex = chunkRes.getMaxIndex();
              SaxRecord chunkTail = chunkRes.getByIndex(tailIndex);

              int resHeadIndex = firstChunkSize - 1;
              while ((null == res.getByIndex(resHeadIndex))
                  && (resHeadIndex < (firstChunkSize + evenIncrement))) {
                resHeadIndex++;
              }
              if (resHeadIndex < (firstChunkSize + evenIncrement - 1)) {
                SaxRecord resHead = res.getByIndex(resHeadIndex);
                consoleLogger.debug("first index in the res " + resHeadIndex + " for "
                    + String.valueOf(resHead.getPayload()) + ", last index in head " + tailIndex
                    + " for " + String.valueOf(chunkTail.getPayload()));
                // if the last entry equals the first, drop the first
                if (nrStrategy.equals(NumerosityReductionStrategy.EXACT)
                    && Arrays.equals(chunkTail.getPayload(), res.getByIndex(resHeadIndex)
                        .getPayload())) {
                  consoleLogger.debug("res head "
                      + String.valueOf(res.getByIndex(resHeadIndex).getPayload()) + " at "
                      + resHeadIndex + " is dropped in favor of head tail "
                      + String.valueOf(chunkTail.getPayload()) + " at " + tailIndex);
                  res.dropByIndex(resHeadIndex);
                }
                else if (nrStrategy.equals(NumerosityReductionStrategy.MINDIST)
                    && (0.0 == SAXFactory.saxMinDist(chunkTail.getPayload(),
                        res.getByIndex(resHeadIndex).getPayload(),
                        alphabet.getDistanceMatrix(alphabetSize)))) {
                  consoleLogger.debug("res head "
                      + String.valueOf(res.getByIndex(resHeadIndex).getPayload()) + " at "
                      + resHeadIndex + " is dropped in favor of head tail "
                      + String.valueOf(chunkTail.getPayload()) + " at " + tailIndex);
                  res.dropByIndex(resHeadIndex);
                }
                else {
                  consoleLogger.debug("has nothing to drop");
                }
              }
              res.addAll(chunkRes);
            }
            else {
              // the other chunks have IDs >0
              //
              consoleLogger.debug("processing chunk " + idx);
              // we only need to care about the very first entry and the very last
              {
                int resLeftmostIndex = res.getMinIndex();
                int chunkLeftmostIndex = chunkRes.getMinIndex();
                SaxRecord chunkLeftmostEntry = chunkRes.getByIndex(chunkLeftmostIndex);
                consoleLogger.debug("res minIdx " + resLeftmostIndex + ", chunk head "
                    + chunkLeftmostIndex);
                // check if the result has something at the left from this chunk head
                //
                if (resLeftmostIndex < chunkLeftmostIndex) {
                  consoleLogger.debug("checking ...");
                  int leftOfChunkIndex = chunkLeftmostIndex;
                  // traverse to the leftmost entry in the result
                  while ((null == res.getByIndex(leftOfChunkIndex))
                      && (leftOfChunkIndex >= resLeftmostIndex)
                      && (leftOfChunkIndex >= chunkLeftmostIndex - evenIncrement)) {
                    leftOfChunkIndex--;
                  }
                  // need to check the distance, it should be less than a chunk size
                  //
                  if (leftOfChunkIndex >= chunkLeftmostIndex - evenIncrement) {
                    SaxRecord resLeftEntry = res.getByIndex(leftOfChunkIndex);
                    consoleLogger.debug("res entry at " + leftOfChunkIndex + " "
                        + String.valueOf(resLeftEntry.getPayload()) + " chunk entry at "
                        + chunkLeftmostIndex + " "
                        + String.valueOf(chunkLeftmostEntry.getPayload()));
                    // if the last entry equals the first, drop the first
                    if (nrStrategy.equals(NumerosityReductionStrategy.EXACT)
                        && Arrays
                            .equals(resLeftEntry.getPayload(), chunkLeftmostEntry.getPayload())) {
                      consoleLogger.debug("res entry " + String.valueOf(resLeftEntry.getPayload())
                          + " at " + leftOfChunkIndex + " is dropped in favor of chunk head "
                          + String.valueOf(chunkLeftmostEntry.getPayload()) + " at "
                          + chunkLeftmostIndex);
                      res.dropByIndex(leftOfChunkIndex);
                    }
                    else if (nrStrategy.equals(NumerosityReductionStrategy.MINDIST)
                        && (0.0 == SAXFactory.saxMinDist(resLeftEntry.getPayload(),
                            chunkLeftmostEntry.getPayload(),
                            alphabet.getDistanceMatrix(alphabetSize)))) {
                      consoleLogger.debug("res entry " + String.valueOf(resLeftEntry.getPayload())
                          + " at " + leftOfChunkIndex + " is dropped in favor of chunk head "
                          + String.valueOf(chunkLeftmostEntry.getPayload()) + " at "
                          + chunkLeftmostIndex);
                      res.dropByIndex(leftOfChunkIndex);
                    }
                  }
                }
              }
              {
                // fix the right side
                //
                int resRightmostIndex = res.getMaxIndex();
                int chunkRightmostIndex = chunkRes.getMaxIndex();
                SaxRecord chunkRightmostEntry = chunkRes.getByIndex(chunkRightmostIndex);
                consoleLogger.debug("res maxIdx " + resRightmostIndex + ", chunk tail "
                    + chunkRightmostIndex);
                // check if the result has something at the right from this chunk tail
                //
                if (resRightmostIndex > chunkRightmostIndex) {
                  int rightOfChunkIndex = chunkRightmostIndex;
                  while ((null == res.getByIndex(rightOfChunkIndex))
                      && (rightOfChunkIndex <= resRightmostIndex)
                      && (rightOfChunkIndex <= chunkRightmostIndex + evenIncrement)) {
                    rightOfChunkIndex++;
                  }
                  if (rightOfChunkIndex <= chunkRightmostIndex + evenIncrement) {
                    SaxRecord resRightEntry = res.getByIndex(rightOfChunkIndex);
                    consoleLogger.debug("res entry at " + rightOfChunkIndex + " "
                        + String.valueOf(resRightEntry.getPayload()) + " chunk entry at "
                        + chunkRightmostIndex + " "
                        + String.valueOf(chunkRightmostEntry.getPayload()));
                    // if the last entry equals the first, drop the first
                    if (nrStrategy.equals(NumerosityReductionStrategy.EXACT)
                        && Arrays.equals(resRightEntry.getPayload(),
                            chunkRightmostEntry.getPayload())) {
                      consoleLogger.debug("res entry " + String.valueOf(resRightEntry.getPayload())
                          + " at " + rightOfChunkIndex + " is dropped in favor of chunk head "
                          + String.valueOf(chunkRightmostEntry.getPayload()) + " at "
                          + chunkRightmostIndex);
                      res.dropByIndex(rightOfChunkIndex);
                    }
                    else if (nrStrategy.equals(NumerosityReductionStrategy.MINDIST)
                        && (0.0 == SAXFactory.saxMinDist(resRightEntry.getPayload(),
                            chunkRightmostEntry.getPayload(),
                            alphabet.getDistanceMatrix(alphabetSize)))) {
                      consoleLogger.debug("res entry " + String.valueOf(resRightEntry.getPayload())
                          + " at " + rightOfChunkIndex + " is dropped in favor of chunk head "
                          + String.valueOf(chunkRightmostEntry.getPayload()) + " at "
                          + chunkRightmostIndex);
                      res.dropByIndex(rightOfChunkIndex);
                    }
                  }
                }
              }
              res.addAll(chunkRes);
            }
          }
        }
        totalTaskCounter--;
      }
    }
    catch (Exception e) {
      System.err.println("Error while waiting results: " + StackTrace.toString(e));
    }
    finally {
      // wait at least 1 more hour before terminate and fail
      try {
        if (!executorService.awaitTermination(4, TimeUnit.HOURS)) {
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

    return res;
  }

}
