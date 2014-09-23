package edu.hawaii.jmotif.sax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.hawaii.jmotif.distance.EuclideanDistance;
import edu.hawaii.jmotif.logic.RuleInterval;
import edu.hawaii.jmotif.sax.alphabet.Alphabet;
import edu.hawaii.jmotif.sax.alphabet.NormalAlphabet;
import edu.hawaii.jmotif.sax.datastructures.DiscordRecord;
import edu.hawaii.jmotif.sax.datastructures.DiscordRecords;
import edu.hawaii.jmotif.sax.datastructures.MotifRecord;
import edu.hawaii.jmotif.sax.datastructures.MotifRecords;
import edu.hawaii.jmotif.sax.datastructures.SAXRecords;
import edu.hawaii.jmotif.sax.trie.SAXTrie;
import edu.hawaii.jmotif.sax.trie.SAXTrieHitEntry;
import edu.hawaii.jmotif.sax.trie.TrieException;
import edu.hawaii.jmotif.sax.trie.VisitRegistry;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.timeseries.TSUtils;
import edu.hawaii.jmotif.timeseries.Timeseries;
import edu.hawaii.jmotif.util.StackTrace;

/**
 * Implements SAX algorithms.
 * 
 * @author Pavel Senin
 * 
 */
public final class SAXFactory {

  public static final int DEFAULT_COLLECTION_SIZE = 50;

  private static Logger consoleLogger;
  private static final Level LOGGING_LEVEL = Level.INFO;

  private static final double NORMALIZATION_THRESHOLD = 0.005D;

  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(SAXFactory.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  /**
   * Constructor.
   */
  private SAXFactory() {
    super();
  }

  /**
   * Convert the timeseries into SAX string representation, normalizes each of the pieces before SAX
   * conversion. NOSKIP means that ALL SAX words reported.
   * 
   * @param ts The timeseries given.
   * @param windowSize The sliding window size used.
   * @param paaSize The number of the points used in the PAA reduction of the time series.
   * @param cuts The alphabet cuts to use.
   * @return The SAX representation of the timeseries.
   * @throws TSException If error occurs.
   * @throws CloneNotSupportedException
   */

  public static SAXRecords ts2saxZnormByCutsNoSkip(Timeseries ts, int windowSize, int paaSize,
      double[] cuts) throws TSException, CloneNotSupportedException {

    // Initialize symbolic result data
    SAXRecords res = new SAXRecords();

    // scan across the time series extract sub sequences, and converting
    // them to strings
    for (int i = 0; i < ts.size() - (windowSize - 1); i++) {

      // fix the current subsection
      Timeseries subSection = ts.subsection(i, i + windowSize - 1);

      // Z normalize it
      subSection = TSUtils.zNormalize(subSection);

      // perform PAA conversion if needed
      Timeseries paa;
      try {
        paa = TSUtils.paa(subSection, paaSize);
      }
      catch (CloneNotSupportedException e) {
        throw new TSException("Unable to clone: " + StackTrace.toString(e));
      }

      // Convert the PAA to a string.
      char[] currentString = TSUtils.ts2StringWithNaNByCuts(paa, cuts);

      res.add(currentString, i);
    }
    return res;
  }

  /**
   * Convert the timeseries into SAX string representation, normalizes each of the pieces before SAX
   * conversion. Not all SAX words reported, if the new SAX word is the same as current it will not
   * be reported.
   * 
   * @param ts The timeseries given.
   * @param windowSize The sliding window size used.
   * @param paaSize The number of the points used in the PAA reduction of the time series.
   * @param cuts The alphabet cuts to use.
   * @return The SAX representation of the timeseries.
   * @throws TSException If error occurs.
   * @throws CloneNotSupportedException
   */

  public static SAXRecords ts2saxZnormByCuts(Timeseries ts, int windowSize, int paaSize,
      double[] cuts) throws TSException, CloneNotSupportedException {

    // Initialize symbolic result data
    SAXRecords res = new SAXRecords();
    String previousString = "";

    // scan across the time series extract sub sequences, and converting
    // them to strings
    for (int i = 0; i < ts.size() - (windowSize - 1); i++) {

      // fix the current subsection
      Timeseries subSection = ts.subsection(i, i + windowSize - 1);

      // Z normalize it
      subSection = TSUtils.zNormalize(subSection);

      // perform PAA conversion if needed
      Timeseries paa;
      try {
        paa = TSUtils.paa(subSection, paaSize);
      }
      catch (CloneNotSupportedException e) {
        throw new TSException("Unable to clone: " + StackTrace.toString(e));
      }

      // Convert the PAA to a string.
      char[] currentString = TSUtils.ts2StringWithNaNByCuts(paa, cuts);

      // check if previous one was the same, if so, ignore that (don't
      // know why though, but guess
      // cause we didn't advance much on the timeseries itself)
      // if (4728 < i && i < 4732) {
      // System.out.println(i);
      // System.out.println("series "
      // + Arrays.toString(ts.subsection(i, i + windowSize - 1).values()));
      // System.out.println("norm " + Arrays.toString(subSection.values()));
      // System.out.println("paa " + Arrays.toString(paa.values()));
      // System.out.println("str " + String.valueOf(currentString));
      // }
      if (!previousString.isEmpty() && previousString.equalsIgnoreCase(new String(currentString))) {
        continue;
      }
      previousString = new String(currentString);
      res.add(currentString, i);
    }
    return res;
  }

  /**
   * Convert the timeseries into SAX string representation, normalizes each of the pieces before SAX
   * conversion. NOSKIP means that ALL SAX words reported.
   * 
   * @param s The timeseries given.
   * @param windowSize The sliding window size used.
   * @param paaSize The number of the points used in the PAA reduction of the time series.
   * @param cuts The alphabet cuts to use.
   * @return The SAX representation of the timeseries.
   * @throws TSException If error occurs.
   * @throws CloneNotSupportedException
   */
  public static SAXRecords ts2saxZnormByCutsNoSkip(double[] s, int windowSize, int paaSize,
      double[] cuts) throws TSException, CloneNotSupportedException {
    long[] ticks = new long[s.length];
    for (int i = 0; i < s.length; i++) {
      ticks[i] = i;
    }
    Timeseries ts = new Timeseries(s, ticks);
    return ts2saxZnormByCutsNoSkip(ts, windowSize, paaSize, cuts);
  }

  /**
   * Convert the timeseries into SAX string representation, normalizes each of the pieces before SAX
   * conversion. Not all SAX words reported, if the new SAX word is the same as current it will not
   * be reported.
   * 
   * @param s The timeseries given.
   * @param windowSize The sliding window size used.
   * @param paaSize The number of the points used in the PAA reduction of the time series.
   * @param cuts The alphabet cuts to use.
   * @return The SAX representation of the timeseries.
   * @throws TSException If error occurs.
   * @throws CloneNotSupportedException
   */
  public static SAXRecords ts2saxZnormByCuts(double[] s, int windowSize, int paaSize, double[] cuts)
      throws TSException, CloneNotSupportedException {
    long[] ticks = new long[s.length];
    for (int i = 0; i < s.length; i++) {
      ticks[i] = i;
    }
    Timeseries ts = new Timeseries(s, ticks);
    return ts2saxZnormByCuts(ts, windowSize, paaSize, cuts);
  }

  /**
   * Convert the timeseries into SAX string representation - NO SLIDING WINDOW, i.e CHUNKING -
   * normalizes each of the pieces before SAX conversion. Not all SAX words reported, if the new SAX
   * word is the same as current it will not be reported.
   * 
   * @param s The timeseries given.
   * @param windowSize The sliding window size used.
   * @param paaSize The number of the points used in the PAA reduction of the time series.
   * @param cuts The alphabet cuts to use.
   * @return The SAX representation of the timeseries.
   * @throws TSException If error occurs.
   * @throws CloneNotSupportedException
   */
  public static SAXRecords ts2saxZnormByCutsNoSliding(double[] s, int windowSize, int paaSize,
      double[] cuts) throws TSException, CloneNotSupportedException {

    // Initialize symbolic result data
    SAXRecords res = new SAXRecords();
    String previousString = "";

    // scan across the time series extract sub sequences, and converting
    // them to strings
    int i = 0;
    while (i <= s.length - windowSize - 1) {
      // fix the current subsection
      double[] subSection = Arrays.copyOfRange(s, i, i + windowSize);

      // Z normalize it
      subSection = TSUtils.zNormalize(subSection);

      // perform PAA conversion if needed
      double[] paa = TSUtils.paa(subSection, paaSize);

      // Convert the PAA to a string.
      char[] currentString = TSUtils.ts2String(paa, cuts);

      // check if previous one was the same, if so, ignore that (don't
      // know why though, but guess
      // cause we didn't advance much on the timeseries itself)
      if (!previousString.isEmpty() && previousString.equalsIgnoreCase(new String(currentString))) {
        i = i + windowSize;
        continue;
      }

      previousString = new String(currentString);
      res.add(currentString, i);

      // dont forget the counter
      //
      i = i + windowSize;
    }
    if (i < s.length) {

    }
    return res;
  }

  /**
   * Convert the timeseries into SAX string representation - NO SLIDING WINDOW, i.e CHUNKING -
   * normalizes each of the pieces before SAX conversion. Not all SAX words reported, if the new SAX
   * word is the same as current it will not be reported.
   * 
   * @param s The timeseries given.
   * @param windowSize The sliding window size used.
   * @param paaSize The number of the points used in the PAA reduction of the time series.
   * @param cuts The alphabet cuts to use.
   * @return The SAX representation of the timeseries.
   * @throws TSException If error occurs.
   * @throws CloneNotSupportedException
   */
  public static SAXRecords ts2saxZnormByCutsNoSlidingNoSkip(double[] s, int windowSize,
      int paaSize, double[] cuts) throws TSException, CloneNotSupportedException {

    // Initialize symbolic result data
    SAXRecords res = new SAXRecords();

    // scan across the time series extract sub sequences, and converting
    // them to strings
    int i = 0;
    while (i <= s.length - windowSize - 1) {
      // fix the current subsection
      double[] subSection = Arrays.copyOfRange(s, i, i + windowSize);

      // Z normalize it
      subSection = TSUtils.zNormalize(subSection);

      // perform PAA conversion if needed
      double[] paa = TSUtils.paa(subSection, paaSize);

      // Convert the PAA to a string.
      char[] currentString = TSUtils.ts2String(paa, cuts);

      res.add(currentString, i);

      // dont forget the counter
      //
      i = i + windowSize;
    }
    if (i < s.length) {

    }
    return res;
  }

  /**
   * Convert the timeseries into SAX string representation. It doesn't normalize anything.
   * 
   * @param ts The timeseries given.
   * @param windowSize The sliding window size used.
   * @param paaSize The number of the points used in the PAA reduction of the time series.
   * @param cuts The alphabet cuts to use.
   * @return The SAX representation of the timeseries.
   * @throws TSException If error occurs.
   */

  public static SAXRecords ts2saxNoZnormByCuts(Timeseries ts, int windowSize, int paaSize,
      double[] cuts) throws TSException {

    // Initialize symbolic result data
    SAXRecords res = new SAXRecords();
    String previousString = "";

    // scan across the time series extract sub sequences, and converting
    // them to strings
    for (int i = 0; i < ts.size() - (windowSize - 1); i++) {

      // fix the current subsection
      Timeseries subSection = ts.subsection(i, i + windowSize - 1);

      // Z normalize it
      // subSection = TSUtils.normalize(subSection);

      // perform PAA conversion if needed
      Timeseries paa;
      try {
        paa = TSUtils.paa(subSection, paaSize);
      }
      catch (CloneNotSupportedException e) {
        throw new TSException("Unable to clone: " + StackTrace.toString(e));
      }

      // Convert the PAA to a string.
      char[] currentString = TSUtils.ts2StringWithNaNByCuts(paa, cuts);

      // check if previous one was the same, if so, ignore that (don't
      // know why though, but guess
      // cause we didn't advance much on the timeseries itself)
      if (!(previousString.isEmpty()) && previousString.equalsIgnoreCase(new String(currentString))) {
        continue;
      }
      previousString = new String(currentString);
      res.add(currentString, i);
    }
    return res;
  }

  /**
   * Convert the timeseries into SAX string representation.
   * 
   * @param ts The timeseries given.
   * @param windowSize The sliding window size used.
   * @param paaSize The number of the points used in the PAA reduction of the time series.
   * @param alphabet The alphabet to use.
   * @param alphabetSize The alphabet size used.
   * @return The SAX representation of the timeseries.
   * @throws TSException If error occurs.
   * @throws CloneNotSupportedException
   */

  public static SAXRecords ts2saxZNorm(Timeseries ts, int windowSize, int paaSize,
      Alphabet alphabet, int alphabetSize) throws TSException, CloneNotSupportedException {

    if (alphabetSize > alphabet.getMaxSize()) {
      throw new TSException("Unable to set the alphabet size greater than " + alphabet.getMaxSize());
    }

    return ts2saxZnormByCuts(ts, windowSize, paaSize, alphabet.getCuts(alphabetSize));

  }

  /**
   * Convert the timeseries into SAX string representation.
   * 
   * @param ts The timeseries given.
   * @param windowSize The sliding window size used.
   * @param paaSize The number of the points used in the PAA reduction of the time series.
   * @param alphabet The alphabet to use.
   * @param alphabetSize The alphabet size used.
   * @return The SAX representation of the timeseries.
   * @throws TSException If error occurs.
   */

  public static SAXRecords ts2saxNoZnorm(Timeseries ts, int windowSize, int paaSize,
      Alphabet alphabet, int alphabetSize) throws TSException {

    if (alphabetSize > alphabet.getMaxSize()) {
      throw new TSException("Unable to set the alphabet size greater than " + alphabet.getMaxSize());
    }

    return ts2saxNoZnormByCuts(ts, windowSize, paaSize, alphabet.getCuts(alphabetSize));

  }

  public static SAXRecords data2sax(double[] ts, int slidingWindowSize, int paaSize,
      int alphabetSize) throws TSException {
    NormalAlphabet normalA = new NormalAlphabet();
    String previousString = "";
    SAXRecords res = new SAXRecords();
    for (int i = 0; i < ts.length - (slidingWindowSize - 1); i++) {
      double[] subSection = Arrays.copyOfRange(ts, i, i + slidingWindowSize);
      if (TSUtils.stDev(subSection) > NORMALIZATION_THRESHOLD) {
        subSection = TSUtils.zNormalize(subSection);
      }
      double[] paa = TSUtils.optimizedPaa(subSection, paaSize);
      char[] currentString = TSUtils.ts2String(paa, normalA.getCuts(alphabetSize));
      if (!(previousString.isEmpty()) && previousString.equalsIgnoreCase(new String(currentString))) {
        continue;
      }
      previousString = new String(currentString);
      res.add(currentString, i);
    }
    return res;
  }

  /**
   * Convert the timeseries into SAX string representation.
   * 
   * @param ts The timeseries given.
   * @param paaSize The number of the points used in the PAA reduction of the time series.
   * @param alphabet The alphabet to use.
   * @param alphabetSize The alphabet size used.
   * @return The SAX representation of the timeseries.
   * @throws TSException If error occurs.
   * @throws CloneNotSupportedException
   */
  public static String ts2string(Timeseries ts, int paaSize, Alphabet alphabet, int alphabetSize)
      throws TSException, CloneNotSupportedException {

    if (alphabetSize > alphabet.getMaxSize()) {
      throw new TSException("Unable to set the alphabet size greater than " + alphabet.getMaxSize());
    }

    int tsLength = ts.size();
    if (tsLength == paaSize) {
      return new String(TSUtils.ts2String(TSUtils.zNormalize(ts), alphabet, alphabetSize));
    }
    else {
      // perform PAA conversion
      Timeseries PAA;
      try {
        PAA = TSUtils.paa(TSUtils.zNormalize(ts), paaSize);
      }
      catch (CloneNotSupportedException e) {
        throw new TSException("Unable to clone: " + StackTrace.toString(e));
      }
      return new String(TSUtils.ts2String(PAA, alphabet, alphabetSize));
    }
  }

  /**
   * Build the SAX trie out of the series.
   * 
   * @param tsData The timeseries.
   * @param windowSize PAA window size to use.
   * @param alphabetSize The SAX alphabet size.
   * @return Discords found within the series.
   * @throws TrieException if error occurs.
   * @throws TSException if error occurs.
   */
  public static DiscordRecords ts2Discords(double[] tsData, int windowSize, int alphabetSize)
      throws TrieException, TSException {

    // make alphabet available
    NormalAlphabet normalA = new NormalAlphabet();

    // get a trie instance
    SAXTrie trie = new SAXTrie(tsData.length - windowSize, alphabetSize);

    // build the trie sliding over the series
    //
    int currPosition = 0;
    while ((currPosition + windowSize) < tsData.length) {
      // get the window SAX representation
      double[] subSeries = TSUtils.subseriesByCopy(tsData, currPosition, currPosition + windowSize);
      char[] saxVals = getSaxVals(subSeries, windowSize, normalA.getCuts(alphabetSize));
      // add result to the structure
      trie.put(String.valueOf(saxVals), currPosition);
      // increment the position
      currPosition++;
    }

    // delegate the job to discords extraction engine
    DiscordRecords discords = getDiscords(tsData, windowSize, trie, DEFAULT_COLLECTION_SIZE,
        new LargeWindowAlgorithm());

    return discords;
  }

  /**
   * Compute the distance between the two strings, this function use the numbers associated with
   * ASCII codes, i.e. distance between a and b would be 1.
   * 
   * @param a The first string.
   * @param b The second string.
   * @return The pairwise distance.
   * @throws TSException if length are differ.
   */
  public static int strDistance(char[] a, char[] b) throws TSException {
    if (a.length == b.length) {
      int distance = 0;
      for (int i = 0; i < a.length; i++) {
        int tDist = Math.abs(Character.getNumericValue(a[i]) - Character.getNumericValue(b[i]));
        if (tDist > 1) {
          distance += tDist;
        }
      }
      return distance;
    }
    else {
      throw new TSException("Unable to compute SAX distance, string lengths are not equal");
    }
  }

  /**
   * Compute the distance between the two chars based on the ASCII symbol codes.
   * 
   * @param a The first char.
   * @param b The second char.
   * @return The distance.
   */
  public static int strDistance(char a, char b) {
    return Math.abs(Character.getNumericValue(a) - Character.getNumericValue(b));
  }

  /**
   * This function implements SAX MINDIST function which uses alphabet based distance matrix.
   * 
   * @param a The SAX string.
   * @param b The SAX string.
   * @param distanceMatrix The distance matrix to use.
   * @return distance between strings.
   * @throws TSException If error occurs.
   */
  public static double saxMinDist(char[] a, char[] b, double[][] distanceMatrix) throws TSException {
    if (a.length == b.length) {
      double dist = 0.0D;
      for (int i = 0; i < a.length; i++) {
        if (Character.isLetter(a[i]) && Character.isLetter(b[i])) {
          int numA = Character.getNumericValue(a[i]) - 10;
          int numB = Character.getNumericValue(b[i]) - 10;
          if (numA > 19 || numA < 0 || numB > 19 || numB < 0) {
            throw new TSException("The character index greater than 19 or less than 0!");
          }
          double localDist = distanceMatrix[numA][numB];
          dist += localDist;
        }
        else {
          throw new TSException("Non-literal character found!");
        }
      }
      return dist;
    }
    else {
      throw new TSException("Data arrays lengths are not equal!");
    }
  }

  public MotifRecords series2Motifs(double[] series, int windowSize, int alphabetSize,
      int motifsNumToReport, SlidingWindowMarkerAlgorithm markerAlgorithm) throws TrieException,
      TSException {
    // init the SAX structures
    //
    SAXTrie trie = new SAXTrie(series.length - windowSize, alphabetSize);

    StringBuilder sb = new StringBuilder(128);
    sb.append("data size: ").append(series.length);

    double max = TSUtils.max(series);
    sb.append("; max: ").append(max);

    double min = TSUtils.min(series);
    sb.append("; min: ").append(min);

    double mean = TSUtils.mean(series);
    sb.append("; mean: ").append(mean);

    int nans = TSUtils.countNaN(series);
    sb.append("; NaNs: ").append(nans);

    consoleLogger.debug(sb.toString());
    consoleLogger.debug("window size: " + windowSize + ", alphabet size: " + alphabetSize
        + ", SAX Trie size: " + (series.length - windowSize));

    Alphabet normalA = new NormalAlphabet();

    Date start = new Date();
    // build the trie
    //
    int currPosition = 0;
    while ((currPosition + windowSize) < series.length) {
      // get the window SAX representation
      double[] subSeries = TSUtils.subseriesByCopy(series, currPosition, currPosition + windowSize);
      char[] saxVals = getSaxVals(subSeries, windowSize, normalA.getCuts(alphabetSize));
      // add result to the structure
      trie.put(String.valueOf(saxVals), currPosition);
      // increment the position
      currPosition++;
    }
    Date end = new Date();
    consoleLogger.debug("trie built in: " + timeToString(start.getTime(), end.getTime()));

    start = new Date();
    MotifRecords motifs = getMotifs(trie, motifsNumToReport);
    end = new Date();

    consoleLogger.debug("motifs retrieved in: " + timeToString(start.getTime(), end.getTime()));

    return motifs;
  }

  /**
   * Build the SAX trie out of the series and reports discords.
   * 
   * @param series The timeseries.
   * @param windowSize PAA window size to use.
   * @param alphabetSize The SAX alphabet size.
   * @param discordsNumToReport how many discords to report.
   * @return Discords found within the series.
   * @throws TrieException if error occurs.
   * @throws TSException if error occurs.
   */
  public static DiscordRecords series2Discords(double[] series, int windowSize, int alphabetSize,
      int discordsNumToReport, SlidingWindowMarkerAlgorithm markerAlgorithm) throws TrieException,
      TSException {

    // get the Alphabet
    //
    NormalAlphabet normalA = new NormalAlphabet();

    Date start = new Date();
    // instantiate the trie
    //
    SAXTrie trie = new SAXTrie(series.length - windowSize, alphabetSize);
    Date trieInitEnd = new Date();

    consoleLogger.info("Trie built in : " + timeToString(start.getTime(), trieInitEnd.getTime()));

    // fill the trie with data
    //
    int currPosition = 0;
    while ((currPosition + windowSize) < series.length) {

      // get the subsequence
      double[] subSeries = TSUtils.subseriesByCopy(series, currPosition, currPosition + windowSize);

      // convert to string
      char[] saxVals = getSaxVals(subSeries, windowSize, normalA.getCuts(alphabetSize));

      // add to trie
      trie.put(String.valueOf(saxVals), currPosition);

      // increment the position
      currPosition++;
    }

    Date trieEnd = new Date();
    consoleLogger.debug("Time series processed in : "
        + timeToString(trieInitEnd.getTime(), trieEnd.getTime()));

    int reportNum = DEFAULT_COLLECTION_SIZE;
    if (discordsNumToReport > 0 && discordsNumToReport < 50) {
      reportNum = discordsNumToReport;
    }

    DiscordRecords discords = getDiscords(series, windowSize, trie, reportNum, markerAlgorithm);

    Date end = new Date();

    consoleLogger.debug("discords search finished in : "
        + timeToString(start.getTime(), end.getTime()));

    return discords;
  }

  /**
   * The discords extraction method.
   * 
   * Here I need to keep a continuous stack of knowledge with information not only about distance,
   * but about abandoning or conducting a full search for words. Thus, I will not be doing the same
   * expensive search on the rarest word all over again.
   * 
   * @param series The series we work with.
   * @param windowSize The series window size.
   * @param marker The algorithm for marking visited locations.
   * @param trie
   * @param discordCollectionSize
   * @return
   * @throws TSException
   * @throws TrieException
   */
  private static DiscordRecords getDiscords(double[] series, int windowSize, SAXTrie trie,
      int discordCollectionSize, SlidingWindowMarkerAlgorithm marker) throws TSException,
      TrieException {

    // resulting discords collection
    DiscordRecords discords = new DiscordRecords();

    // visit registry. the idea is to mark as visited all the discord
    // locations for all searches. in other words, if the discord was found, its location is marked
    // as visited and there will be no search IT CANT SPAN BEYOND series.length - windowSize
    VisitRegistry globalTrackVisitRegistry = new VisitRegistry(series.length - windowSize);

    // the collection of seen words and their best so far distances
    // in the collection, in addition to pairs <word, distance> I store a
    // semaphore which indicates whether the full search was conducted with this word,
    // or it was abandoned at some point, so we do not know the true near neighbor
    //
    TreeMap<String, DistanceEntry> knownWordsAndTheirCurrentDistances = new TreeMap<String, DistanceEntry>();

    // the words already in the discords collection, so we do not have to
    // re-consider them
    //
    // TreeSet<String> completeWords = new TreeSet<String>();

    // we conduct the search until the number of discords is less than
    // desired
    //
    while (discords.getSize() < discordCollectionSize) {

      consoleLogger.debug("currently known discords: " + discords.getSize() + " out of "
          + discordCollectionSize);

      Date start = new Date();
      DiscordRecord bestDiscord = findBestDiscord(series, windowSize, trie,
          knownWordsAndTheirCurrentDistances, globalTrackVisitRegistry, marker);
      Date end = new Date();

      // if the discord is null we getting out of the search
      if (bestDiscord.getNNDistance() == 0.0D || bestDiscord.getPosition() == -1) {
        consoleLogger.debug("breaking the outer search loop, discords found: " + discords.getSize()
            + " last seen discord: " + bestDiscord.toString());
        break;
      }

      bestDiscord.setInfo("position " + bestDiscord.getPosition() + ", NN distance "
          + bestDiscord.getNNDistance() + ", elapsed time: "
          + timeToString(start.getTime(), end.getTime()) + ", " + bestDiscord.getInfo());
      consoleLogger.debug(bestDiscord.getInfo());

      // collect the result
      //
      discords.add(bestDiscord);

      // and maintain data structures
      //
      marker.markVisited(globalTrackVisitRegistry, bestDiscord.getPosition(), windowSize);

      // completeWords.add(String.valueOf(bestDiscord.getPayload()));
    }

    // done deal
    //
    return discords;
  }

  /**
   * This method reports the best found discord. Note, that this discord is approximately the best.
   * Due to the fuzzy-logic search with randomization and aggressive labeling of the magic array
   * locations.
   * 
   * @param series The series we are looking for discord in.
   * @param windowSize The sliding window size.
   * @param trie The trie (index of the series).
   * @param knownWordsAndTheirDistances The best known distances for certain word. I use the early
   * search abandoning optimization in oder to reduce complexity.
   * @param visitedLocations The magic array.
   * @return The best discord instance.
   * @throws TSException If error occurs.
   * @throws TrieException If error occurs.
   */
  private static DiscordRecord findBestDiscord(double[] series, int windowSize, SAXTrie trie,
      TreeMap<String, DistanceEntry> knownWordsAndTheirDistances, VisitRegistry visitedLocations,
      SlidingWindowMarkerAlgorithm marker) throws TSException, TrieException {

    // we extract all seen words from the trie and sort them by the frequency decrease
    ArrayList<SAXTrieHitEntry> frequencies = trie.getFrequencies();
    Collections.sort(frequencies);

    // init tracking variables
    int bestSoFarPosition = -1;
    double bestSoFarDistance = 0.0D;
    String bestSoFarString = "";

    // discord search stats
    int iterationCounter = 0;
    int distanceCalls = 0;

    // while not all sequences are considered
    while (!frequencies.isEmpty()) {
      iterationCounter++;

      // the head of this array has the rarest word
      SAXTrieHitEntry currentEntry = frequencies.get(0);
      String currentWord = String.valueOf(currentEntry.getStr());
      int outerLoopCandidatePosition = currentEntry.getPosition();

      frequencies.remove(0);
      currentEntry = null;

      // make sure it is not previously found discord passed through the parameters array
      if (visitedLocations.isVisited(outerLoopCandidatePosition)) {
        continue;
      }

      // *** THIS IS AN OUTER LOOP - over the current motif
      consoleLogger.debug("conducting search for " + currentWord + " at "
          + outerLoopCandidatePosition + ", iteration " + iterationCounter + ", to go: "
          + frequencies.size());

      // let the search begin
      double nearestNeighborDist = Double.MAX_VALUE;
      boolean doRandomSearch = true;

      // get a copy of global restrictions on visited locations
      VisitRegistry registry = new VisitRegistry(series.length - windowSize);

      // extract the subsequence & mark visited current substring
      double[] currentSubsequence = TSUtils.subseriesByCopy(series, outerLoopCandidatePosition,
          outerLoopCandidatePosition + windowSize);
      marker.markVisited(registry, outerLoopCandidatePosition, windowSize);

      // WE ARE GOING TO ITERATE OVER THE CURRENT WORD OCCURENCES HERE
      List<Integer> currentOccurences = trie.getOccurences(currentWord.toCharArray());
      consoleLogger.debug(currentWord + " has " + currentOccurences.size()
          + " occurrences, iterating...");

      for (Integer nextOccurrence : currentOccurences) {

        // check this subsequence as visited
        registry.markVisited(nextOccurrence);

        // just in case there is an overlap
        if (Math.abs(nextOccurrence.intValue() - outerLoopCandidatePosition) <= windowSize) {
          continue;
        }

        // get the subsequence and the distance
        double[] occurrenceSubsequence = TSUtils.subseriesByCopy(series, nextOccurrence,
            nextOccurrence + windowSize);
        double dist = EuclideanDistance.distance(currentSubsequence, occurrenceSubsequence);
        distanceCalls++;

        // keep track of best so far distance
        if (dist < nearestNeighborDist) {
          nearestNeighborDist = dist;
          consoleLogger.debug(" ** current NN at " + nextOccurrence + ", distance: "
              + nearestNeighborDist);
          if (dist < bestSoFarDistance) {
            consoleLogger.debug(" ** abandoning the occurrences loop, distance " + dist
                + " is less than best so far " + bestSoFarDistance);
            doRandomSearch = false;
            break;
          }
        }
      }

      if (!(Double.isInfinite(nearestNeighborDist))) {
        consoleLogger.debug("for " + currentWord
            + " occurrences, smallest nearest neighbor distance: " + nearestNeighborDist);
      }
      else {
        consoleLogger.debug("nothing changed after iterations over current word positions ...");
      }

      // check if we must continue with random neighbors
      if (doRandomSearch) {
        // it is heuristics here
        //
        int nextRandomSubsequencePosition = -1;

        int visitCounter = 0;

        // while there are unvisited locations
        while ((nextRandomSubsequencePosition = registry.getNextRandomUnvisitedPosition()) != -1) {
          registry.markVisited(nextRandomSubsequencePosition);

          double[] randomSubsequence = TSUtils.subseriesByCopy(series,
              nextRandomSubsequencePosition, nextRandomSubsequencePosition + windowSize);
          double randomSubsequenceDistance = EuclideanDistance.distance(currentSubsequence,
              randomSubsequence);
          distanceCalls++;

          // early abandoning of the search:
          // the current word is not discord, we have seen better
          if (randomSubsequenceDistance < bestSoFarDistance) {
            nearestNeighborDist = randomSubsequenceDistance;
            consoleLogger.debug(" ** abandoning random visits loop, seen distance "
                + nearestNeighborDist + " at iteration " + visitCounter);
            break;
          }

          // keep track
          if (randomSubsequenceDistance < nearestNeighborDist) {
            nearestNeighborDist = randomSubsequenceDistance;
          }

          visitCounter = visitCounter + 1;
        } // while inner loop
        consoleLogger.debug("random visits loop finished, total positions considered: "
            + visitCounter);

      } // if break loop

      if (nearestNeighborDist > bestSoFarDistance) {
        consoleLogger.debug("beat best so far distance, updating from " + bestSoFarDistance
            + " to  " + nearestNeighborDist);
        bestSoFarDistance = nearestNeighborDist;
        bestSoFarPosition = outerLoopCandidatePosition;
        bestSoFarString = currentWord;
      }

      consoleLogger.debug(" . . iterated " + iterationCounter + " times, best distance:  "
          + bestSoFarDistance + " for a string " + bestSoFarString + " at " + bestSoFarPosition);

    } // outer loop

    consoleLogger.debug("Distance calls: " + distanceCalls);
    DiscordRecord res = new DiscordRecord(bestSoFarPosition, bestSoFarDistance, bestSoFarString);
    res.setInfo("distance calls: " + distanceCalls);
    return res;
  }

  /**
   * Build the SAX trie out of the series and reports discords.
   * 
   * @param series The timeseries.
   * @param windowSize sliding window size to use.
   * @param paaSize PAA value to use.
   * @param alphabetSize The SAX alphabet size.
   * @param discordsNumToReport how many discords to report.
   * @return Discords found within the series.
   * @throws TrieException if error occurs.
   * @throws TSException if error occurs.
   */
  public static DiscordRecords series2DiscordsWithHash(double[] series, int windowSize,
      int paaSize, int alphabetSize, int discordsNumToReport,
      SlidingWindowMarkerAlgorithm markerAlgorithm) throws TrieException, TSException {

    // get the Alphabet
    //
    NormalAlphabet normalA = new NormalAlphabet();

    // instantiate the hash
    //
    HashMap<String, ArrayList<Integer>> hash = new HashMap<String, ArrayList<Integer>>();

    // discretize the timeseries
    //
    Date start = new Date();

    int currPosition = 0;
    while ((currPosition + windowSize) < series.length) {

      // get the subsequence
      double[] subSeries = TSUtils.subseriesByCopy(series, currPosition, currPosition + windowSize);

      // convert to string
      String saxVals = String.valueOf(getSaxVals(subSeries, windowSize,
          normalA.getCuts(alphabetSize)));

      // add to hash
      if (!(hash.containsKey(saxVals))) {
        hash.put(saxVals, new ArrayList<Integer>());
      }
      hash.get(String.valueOf(saxVals)).add(currPosition);

      // increment the position
      currPosition++;
    }

    Date trieEnd = new Date();
    consoleLogger.debug("Time series processed in : "
        + timeToString(start.getTime(), trieEnd.getTime()));

    int reportNum = DEFAULT_COLLECTION_SIZE;
    if (discordsNumToReport > 0 && discordsNumToReport < 50) {
      reportNum = discordsNumToReport;
    }

    DiscordRecords discords = getDiscordsWithHash(series, windowSize, hash, reportNum,
        markerAlgorithm);

    Date end = new Date();

    consoleLogger.debug("discords search finished in : "
        + timeToString(start.getTime(), end.getTime()));

    return discords;
  }

  private static DiscordRecords getDiscordsWithHash(double[] series, int windowSize,
      HashMap<String, ArrayList<Integer>> hash, int discordCollectionSize,
      SlidingWindowMarkerAlgorithm markerAlgorithm) throws TSException, TrieException {

    // resulting discords collection
    DiscordRecords discords = new DiscordRecords();

    // visit registry. the idea is to mark as visited all the discord
    // locations for all searches. in other words, if the discord was found, its location is marked
    // as visited and there will be no search IT CANT SPAN BEYOND series.length - windowSize
    VisitRegistry globalTrackVisitRegistry = new VisitRegistry(series.length - windowSize);

    // the collection of seen words and their best so far distances
    // in the collection, in addition to pairs <word, distance> I store a
    // semaphore which indicates whether the full search was conducted with this word,
    // or it was abandoned at some point, so we do not know the true near neighbor
    //
    TreeMap<String, DistanceEntry> knownWordsAndTheirCurrentDistances = new TreeMap<String, DistanceEntry>();

    // the words already in the discords collection, so we do not have to
    // re-consider them
    //
    // TreeSet<String> completeWords = new TreeSet<String>();

    // we conduct the search until the number of discords is less than
    // desired
    //
    while (discords.getSize() < discordCollectionSize) {

      consoleLogger.trace("currently known discords: " + discords.getSize() + " out of "
          + discordCollectionSize);

      Date start = new Date();
      DiscordRecord bestDiscord = findBestDiscordWithHash(series, windowSize, hash,
          knownWordsAndTheirCurrentDistances, globalTrackVisitRegistry, markerAlgorithm);
      Date end = new Date();

      // if the discord is null we getting out of the search
      if (bestDiscord.getNNDistance() == 0.0D || bestDiscord.getPosition() == -1) {
        consoleLogger.trace("breaking the outer search loop, discords found: " + discords.getSize()
            + " last seen discord: " + bestDiscord.toString());
        break;
      }

      bestDiscord.setInfo("position " + bestDiscord.getPosition() + ", NN distance "
          + bestDiscord.getNNDistance() + ", elapsed time: "
          + timeToString(start.getTime(), end.getTime()) + ", " + bestDiscord.getInfo());
      consoleLogger.debug(bestDiscord.getInfo());

      // collect the result
      //
      discords.add(bestDiscord);

      // and maintain data structures
      //
      markerAlgorithm.markVisited(globalTrackVisitRegistry, bestDiscord.getPosition(), windowSize);

      // completeWords.add(String.valueOf(bestDiscord.getPayload()));
    }

    // done deal
    //
    return discords;
  }

  private static DiscordRecord findBestDiscordWithHash(double[] series, int windowSize,
      HashMap<String, ArrayList<Integer>> hash,
      TreeMap<String, DistanceEntry> knownWordsAndTheirDistances, VisitRegistry visitedLocations,
      SlidingWindowMarkerAlgorithm marker) throws TSException, TrieException {

    // we extract all seen words from the trie and sort them by the frequency decrease
    ArrayList<SAXTrieHitEntry> frequencies = hashToFrequencies(hash);
    Collections.sort(frequencies);

    // init tracking variables
    int bestSoFarPosition = -1;
    double bestSoFarDistance = 0.0D;
    String bestSoFarString = "";

    // discord search stats
    int iterationCounter = 0;
    int distanceCalls = 0;

    // try {
    // BufferedWriter bw = new BufferedWriter(new FileWriter(new File(System.currentTimeMillis()
    // + "_discordrun.csv")));

    // while not all sequences are considered
    while (!frequencies.isEmpty()) {
      iterationCounter++;

      // the head of this array has the rarest word
      SAXTrieHitEntry currentEntry = frequencies.get(0);
      String currentWord = String.valueOf(currentEntry.getStr());
      int outerLoopCandidatePosition = currentEntry.getPosition();

      frequencies.remove(0);
      currentEntry = null;

      // make sure it is not previously found discord passed through the parameters array
      if (visitedLocations.isVisited(outerLoopCandidatePosition)) {
        continue;
      }

      // bw.write(currentWord + "," + currentFrequency + "," + outerLoopCandidatePosition + ","
      // + windowSize + ",");

      // *** THIS IS AN OUTER LOOP - over the current motif
      consoleLogger.trace("conducting search for " + currentWord + " at "
          + outerLoopCandidatePosition + ", iteration " + iterationCounter + ", to go: "
          + frequencies.size());

      // let the search begin
      double nearestNeighborDist = Double.MAX_VALUE;
      boolean doRandomSearch = true;

      // get a copy of global restrictions on visited locations
      VisitRegistry registry = new VisitRegistry(series.length - windowSize);

      // extract the subsequence & mark visited current substring
      double[] currentSubsequence = TSUtils.subseriesByCopy(series, outerLoopCandidatePosition,
          outerLoopCandidatePosition + windowSize);
      marker.markVisited(registry, outerLoopCandidatePosition, windowSize);

      // WE ARE GOING TO ITERATE OVER THE CURRENT WORD OCCURENCES HERE
      List<Integer> currentOccurences = hash.get(currentWord);
      consoleLogger.trace(currentWord + " has " + currentOccurences.size()
          + " occurrences, iterating...");

      for (Integer nextOccurrence : currentOccurences) {

        // check this subsequence as visited
        registry.markVisited(nextOccurrence);

        // just in case there is an overlap
        if (Math.abs(nextOccurrence.intValue() - outerLoopCandidatePosition) <= windowSize) {
          continue;
        }

        // get the subsequence and the distance
        double[] occurrenceSubsequence = TSUtils.subseriesByCopy(series, nextOccurrence,
            nextOccurrence + windowSize);
        double dist = EuclideanDistance.distance(currentSubsequence, occurrenceSubsequence);
        distanceCalls++;

        // keep track of best so far distance
        if (dist < nearestNeighborDist) {
          nearestNeighborDist = dist;
          consoleLogger.trace(" ** current NN at " + nextOccurrence + ", distance: "
              + nearestNeighborDist);
          if (dist < bestSoFarDistance) {
            consoleLogger.trace(" ** abandoning the occurrences loop, distance " + dist
                + " is less than best so far " + bestSoFarDistance);
            doRandomSearch = false;
            break;
          }
        }
      }

      if (!(Double.isInfinite(nearestNeighborDist))) {
        consoleLogger.trace("for " + currentWord
            + " occurrences, smallest nearest neighbor distance: " + nearestNeighborDist);
      }
      else {
        consoleLogger.trace("nothing changed after iterations over current word positions ...");
      }

      // check if we must continue with random neighbors
      if (doRandomSearch) {
        // it is heuristics here
        //
        int nextRandomSubsequencePosition = -1;

        int visitCounter = 0;

        // while there are unvisited locations
        while ((nextRandomSubsequencePosition = registry.getNextRandomUnvisitedPosition()) != -1) {
          registry.markVisited(nextRandomSubsequencePosition);

          double[] randomSubsequence = TSUtils.subseriesByCopy(series,
              nextRandomSubsequencePosition, nextRandomSubsequencePosition + windowSize);
          double randomSubsequenceDistance = EuclideanDistance.distance(currentSubsequence,
              randomSubsequence);
          distanceCalls++;

          // early abandoning of the search:
          // the current word is not discord, we have seen better
          if (randomSubsequenceDistance < bestSoFarDistance) {
            nearestNeighborDist = randomSubsequenceDistance;
            consoleLogger.trace(" ** abandoning random visits loop, seen distance "
                + nearestNeighborDist + " at iteration " + visitCounter);
            break;
          }

          // keep track
          if (randomSubsequenceDistance < nearestNeighborDist) {
            nearestNeighborDist = randomSubsequenceDistance;
          }

          visitCounter = visitCounter + 1;
        } // while inner loop
        consoleLogger.trace("random visits loop finished, total positions considered: "
            + visitCounter);

      } // if break loop

      if (nearestNeighborDist > bestSoFarDistance) {
        consoleLogger.trace("beat best so far distance, updating from " + bestSoFarDistance
            + " to  " + nearestNeighborDist);
        bestSoFarDistance = nearestNeighborDist;
        bestSoFarPosition = outerLoopCandidatePosition;
        bestSoFarString = currentWord;
      }

      consoleLogger.trace(" . . iterated " + iterationCounter + " times, best distance:  "
          + bestSoFarDistance + " for a string " + bestSoFarString + " at " + bestSoFarPosition);

      // bw.write(bestSoFarDistance + "," + iterationCounter + "\n");

    } // outer loop

    consoleLogger.trace("Distance calls: " + distanceCalls);
    DiscordRecord res = new DiscordRecord(bestSoFarPosition, bestSoFarDistance, bestSoFarString);
    res.setInfo("distance calls: " + distanceCalls);

    // bw.close();

    return res;

    // }
    // catch (IOException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }

    // return null;

  }

  /**
   * Translates the hash table into sortable array of substrings.
   * 
   * @param hash
   * @return
   */
  private static ArrayList<SAXTrieHitEntry> hashToFrequencies(
      HashMap<String, ArrayList<Integer>> hash) {
    ArrayList<SAXTrieHitEntry> res = new ArrayList<SAXTrieHitEntry>();
    for (Entry<String, ArrayList<Integer>> e : hash.entrySet()) {
      char[] payload = e.getKey().toCharArray();
      int frequency = e.getValue().size();
      for (Integer i : e.getValue()) {
        res.add(new SAXTrieHitEntry(i, payload, frequency));
      }
    }
    return res;
  }

  /**
   * Get N top motifs from trie.
   * 
   * @param trie The trie.
   * @param maxMotifsNum The number of motifs to report.
   * @return The motifs collection.
   * @throws TrieException If error occurs.
   */
  private static MotifRecords getMotifs(SAXTrie trie, int maxMotifsNum) throws TrieException {

    MotifRecords res = new MotifRecords(maxMotifsNum);

    ArrayList<SAXTrieHitEntry> frequencies = trie.getFrequencies();

    Collections.sort(frequencies);

    // all sorted - from one end we have unique words - those discords
    // from the other end - we have motifs - the most frequent entries
    //
    // what I'll do here - is to populate non-trivial frequent entries into
    // the resulting container
    //

    // picking those non-trivial patterns this method job
    // non-trivial here means the one which are not the same letters
    //

    Set<SAXTrieHitEntry> seen = new TreeSet<SAXTrieHitEntry>();

    int counter = 0;
    // iterating backward - collection is sorted
    for (int i = frequencies.size() - 1; i >= 0; i--) {
      SAXTrieHitEntry entry = frequencies.get(i);
      if (entry.isTrivial(2) || seen.contains(entry) || (2 > entry.getFrequency())) {
        if ((2 > entry.getFrequency())) {
          break;
        }
        continue;
      }
      else {
        counter += 1;
        res.add(new MotifRecord(entry.getStr(), trie.getOccurences(entry.getStr())));
        seen.add(entry);
        if (counter > maxMotifsNum) {
          break;
        }
      }
    }
    return res;
  }

  /**
   * Convert real-valued series into symbolic representation.
   * 
   * @param vals Real valued timeseries.
   * @param windowSize The PAA window size.
   * @param cuts The cut values array used for SAX transform.
   * @return The symbolic representation of the given real time-series.
   * @throws TSException If error occurs.
   */
  public static char[] getSaxVals(double[] vals, int windowSize, double[] cuts) throws TSException {
    char[] saxVals;
    double std = TSUtils.stDev(vals);
    if (std > NORMALIZATION_THRESHOLD) {
      if (windowSize == cuts.length + 1) {
        saxVals = TSUtils.ts2String(TSUtils.zNormalize(vals), cuts);
      }
      else {
        saxVals = TSUtils.ts2String(TSUtils.zNormalize(TSUtils.paa(vals, cuts.length + 1)), cuts);
      }
    }
    else {
      if (windowSize == cuts.length + 1) {
        saxVals = TSUtils.ts2String(vals, cuts);
      }
      else {
        saxVals = TSUtils.ts2String(TSUtils.paa(vals, cuts.length + 1), cuts);
      }
    }
    return saxVals;
  }

  /**
   * Extracts sub-series from series.
   * 
   * @param data The series.
   * @param start The start position.
   * @param end The end position
   * @return sub-series from start to end.
   */
  // public static double[] getSubSeries(double[] data, int start, int end) {
  // return Arrays.copyOfRange(data, start, end);
  // // double[] vals = new double[end - start];
  // // for (int i = start; i < end; i++) {
  // // vals[i] = data[i];
  // // }
  // // return vals;
  // }

  /**
   * Generic method to convert the milliseconds into the elapsed time string.
   * 
   * @param start Start timestamp.
   * @param finish End timestamp.
   * @return String representation of the elapsed time.
   */
  public static String timeToString(long start, long finish) {
    long diff = finish - start;

    long secondInMillis = 1000;
    long minuteInMillis = secondInMillis * 60;
    long hourInMillis = minuteInMillis * 60;
    long dayInMillis = hourInMillis * 24;
    long yearInMillis = dayInMillis * 365;

    @SuppressWarnings("unused")
    long elapsedYears = diff / yearInMillis;
    diff = diff % yearInMillis;

    @SuppressWarnings("unused")
    long elapsedDays = diff / dayInMillis;
    diff = diff % dayInMillis;

    // @SuppressWarnings("unused")
    long elapsedHours = diff / hourInMillis;
    diff = diff % hourInMillis;

    long elapsedMinutes = diff / minuteInMillis;
    diff = diff % minuteInMillis;

    long elapsedSeconds = diff / secondInMillis;
    diff = diff % secondInMillis;

    long elapsedMilliseconds = diff % secondInMillis;

    return elapsedHours + "h " + elapsedMinutes + "m " + elapsedSeconds + "s "
        + elapsedMilliseconds + "ms";
  }

  /**
   * This method iterates over the provided list of intervals instead of all the possible SAX words
   * extracted with numerosity reduction.
   * 
   * @param series The series to find discord at.
   * @param discordCollectionSize How many discords to find.
   * @param intervals The intervals. In our implementation these come from the set of Sequitur
   * grammar rules.
   * @return Discords.
   * @throws TSException If error occurs.
   */
  public static DiscordRecords series2SAXSequiturAnomalies(double[] series,
      int discordCollectionSize, ArrayList<RuleInterval> intervals) throws TSException {

    // resulting discords collection
    DiscordRecords discords = new DiscordRecords();

    // Visit registry. The idea of the visit registry data structure is that to mark as visited all
    // the discord locations for all searches. I.e. if the discord ever found, its location is
    // marked as visited and there will be no search over it again
    VisitRegistry globalTrackVisitRegistry = new VisitRegistry(series.length);

    // we conduct the search until the number of discords is less than desired
    //
    while (discords.getSize() < discordCollectionSize) {

      consoleLogger.trace("currently known discords: " + discords.getSize() + " out of "
          + discordCollectionSize);

      Date start = new Date();
      DiscordRecord bestDiscord = findBestDiscordForIntervals(series, intervals,
          globalTrackVisitRegistry);
      Date end = new Date();

      // if the discord is null we getting out of the search
      if (bestDiscord.getNNDistance() == 0.0D || bestDiscord.getPosition() == -1) {
        consoleLogger.trace("breaking the outer search loop, discords found: " + discords.getSize()
            + " last seen discord: " + bestDiscord.toString());
        break;
      }

      bestDiscord.setInfo("position " + bestDiscord.getPosition() + ", length "
          + bestDiscord.getLength() + ", NN distance " + bestDiscord.getNNDistance()
          + ", elapsed time: " + timeToString(start.getTime(), end.getTime()) + ", "
          + bestDiscord.getInfo());
      consoleLogger.debug(bestDiscord.getInfo());

      // collect the result
      //
      discords.add(bestDiscord);

      // and maintain data structures
      //
      // RightWindowAlgorithm marker = new LargeWindowAlgorithm();
      LargeWindowAlgorithm marker = new LargeWindowAlgorithm();
      marker.markVisited(globalTrackVisitRegistry, bestDiscord.getPosition(),
          bestDiscord.getLength());
    }

    // done deal
    //
    return discords;
  }

  public static DiscordRecord findBestDiscordForIntervals(double[] series,
      ArrayList<RuleInterval> globalIntervals, VisitRegistry globalTrackVisitRegistry)
      throws TSException {

    ArrayList<RuleInterval> intervals = cloneIntervals(globalIntervals);

    Collections.sort(intervals, new Comparator<RuleInterval>() {
      public int compare(RuleInterval c1, RuleInterval c2) {
        if (c1.getCoverage() > c2.getCoverage()) {
          return 1;
        }
        else if (c1.getCoverage() < c2.getCoverage()) {
          return -1;

        }
        return 0;
      }
    });

    // init variables
    int bestSoFarPosition = -1;
    double bestSoFarDistance = 0.0D;
    int bestSoFarLength = -1;
    int bestSoFarRule = -1;

    // we will iterate over words from rarest to frequent ones - this is an OUTER LOOP of the best
    // discord search
    //
    int iterationCounter = 0;
    int distanceCalls = 0;
    // int limit = frequencies.size();
    while (!intervals.isEmpty()) {

      // count iterations
      iterationCounter++;
      consoleLogger.trace("iteration " + iterationCounter + ", intervals in collection "
          + intervals.size());

      // the head of this array has the rarest word
      RuleInterval currentEntry = intervals.get(0);
      intervals.remove(0);
      consoleLogger.trace("current entry rule " + currentEntry.getId()
          + ", intervals in collection " + intervals.size());

      // make sure it is not previously found discord
      if (globalTrackVisitRegistry.isVisited(currentEntry.getStartPos())) {
        continue;
      }

      // so, lets the search begin...
      //
      double nearestNeighborDist = Double.MAX_VALUE;
      // random is switched on
      boolean doRandomSearch = true;

      // get a copy of visited locations
      // VisitRegistry localRegistry = new VisitRegistry(series.length - currentEntry.getLength() -
      // 1);
      // localRegistry.transferVisited(globalTrackVisitRegistry);
      // VisitRegistry localRegistry = globalTrackVisitRegistry.copy();
      VisitRegistry localRegistry = new VisitRegistry(series.length - currentEntry.getLength());

      // extract the subsequence & mark visited current substring
      double[] currentSubsequence = TSUtils.subseriesByCopy(series, currentEntry.getStartPos(),
          currentEntry.getEndPos());

      // TODO: do we really need to mark before rule too? guess so...
      localRegistry.markVisited(currentEntry.getStartPos() - currentEntry.getLength(),
          currentEntry.getEndPos());
      // localRegistry.markVisited(currentEntry.getStartPos(), currentEntry.getEndPos());

      // WE ARE GOING TO ITERATE OVER THE CURRENT WORD OCCURENCES HERE
      //

      Map<Integer, Integer> currentOccurences = listRuleOccurrences(currentEntry.getId(), intervals);
      consoleLogger.trace(" there are " + currentOccurences.size() + " occurrences for the rule "
          + currentEntry.getId() + ", iterating...");
      // what need to be checked here is which sequence is exactly producing largest distance value
      //

      // this is INNER LOOP, where we check all rule's occurrences
      //
      for (Entry<Integer, Integer> nextOccurrence : currentOccurences.entrySet()) {

        // skip the location we standing at, check if we overlap
        if (Math.abs(nextOccurrence.getKey() - currentEntry.getStartPos()) <= currentEntry
            .getLength()) {
          localRegistry.markVisited(nextOccurrence.getKey());
          continue;
        }

        // mark current next visited
        localRegistry.markVisited(nextOccurrence.getKey());

        // get the piece of the timeseries
        double[] occurrenceSubsequence = null;
        if ((nextOccurrence.getKey() + currentEntry.getLength()) >= series.length) {
          occurrenceSubsequence = TSUtils.subseriesByCopy(series,
              series.length - currentEntry.getLength(), series.length);
        }
        else {
          occurrenceSubsequence = TSUtils.subseriesByCopy(series, nextOccurrence.getKey(),
              nextOccurrence.getKey() + currentEntry.getLength());
        }

        // double dist = EuclideanDistance.getDTWDist(currentSubsequence,getSubSeries(series,
        // nextOccurrence.getKey(),
        // nextOccurrence.getValue()));
        double dist = EuclideanDistance.normalizedDistance(currentSubsequence,
            occurrenceSubsequence);
        distanceCalls++;

        // keep track of best so far distance
        if (dist < nearestNeighborDist) {
          nearestNeighborDist = dist;
          consoleLogger.trace(" ** current NN at interval " + nextOccurrence.getKey() + "-"
              + nextOccurrence.getValue() + ", distance: " + nearestNeighborDist);
          if (dist < bestSoFarDistance) {
            consoleLogger.trace(" ** abandoning the occurrences iterations");
            doRandomSearch = false;
            break;
          }
        }
      }

      if (Double.MAX_VALUE != nearestNeighborDist) {
        consoleLogger.trace("rule occurrence loop finished. For current rule "
            + "occurrences, smallest nearest neighbor distance: " + nearestNeighborDist);
      }
      else {
        consoleLogger
            .trace("rule occurrence loop finished. Nothing changed after iterations over current rule positions ...");
      }

      // check if we must continue with random neighbors
      if (doRandomSearch) {
        // it is heuristics here
        //
        int nextRandomVisitTarget = -1;

        int visitCounter = 0;
        // while ((nextRandomVisitTarget = localRegistry.getNextRandomUnvisitedPosition()) != -1) {
        while ((nextRandomVisitTarget = localRegistry.getNextRandomUnvisitedPosition()) != -1) {
          // consoleLogger.debug(" random position pick step " + visitCounter + " visited: "
          // + registry.getVisited().length + ", unvisited: " + registry.getUnvisited().length
          // + "; nearest neighbor at: " + nearestNeighborDist);
          //
          // if(registry.getUnvisited().length<3){
          // System.err.println("gotcha");
          // }

          // registry.markVisited(nextRandomVisitTarget);
          // marker.markVisited(registry, nextRandomVisitTarget, windowSize);
          localRegistry.markVisited(nextRandomVisitTarget);

          double[] randomTargetValues = TSUtils.subseriesByCopy(series, nextRandomVisitTarget,
              nextRandomVisitTarget + currentEntry.getLength());
          double randomTargetDistance = EuclideanDistance.normalizedDistance(currentSubsequence,
              randomTargetValues);
          distanceCalls++;

          // early abandoning of the search, the current word is not
          // discord, we seen better
          if (randomTargetDistance < bestSoFarDistance) {
            nearestNeighborDist = randomTargetDistance;
            consoleLogger.trace(" ** abandoning random visits loop, seen distance "
                + nearestNeighborDist + " at iteration " + visitCounter);
            break;
          }

          // keep track
          if (randomTargetDistance < nearestNeighborDist) {
            nearestNeighborDist = randomTargetDistance;
          }

          visitCounter = visitCounter + 1;
        } // while inner loop
        consoleLogger.trace("random visits loop finished, total positions considered: "
            + visitCounter);

      } // if break loop

      if (nearestNeighborDist > bestSoFarDistance) {
        bestSoFarDistance = nearestNeighborDist;
        bestSoFarPosition = currentEntry.getStartPos();
        bestSoFarLength = currentEntry.getLength();
        bestSoFarRule = currentEntry.getId();
      }
      // if (knownWordsAndTheirDistances.containsKey(currentWord)
      // && knownWordsAndTheirDistances.get(currentWord).isAbandoned()) {
      // knownWordsAndTheirDistances.put(String.valueOf(currentWord), new DistanceEntry(
      // nearestNeighborDist, completeSearch));
      // }
      // else {
      // knownWordsAndTheirDistances.put(String.valueOf(currentWord), new DistanceEntry(
      // nearestNeighborDist, completeSearch));
      // }
      consoleLogger.trace(" . . iterated " + iterationCounter + " times, best distance:  "
          + bestSoFarDistance + " for a rule " + bestSoFarRule + " at " + bestSoFarPosition);

      iterationCounter++;
    } // outer loop

    DiscordRecord res = new DiscordRecord(bestSoFarPosition, bestSoFarDistance,
        "pos,calls,len,rule " + bestSoFarPosition + " " + distanceCalls + " " + bestSoFarLength
            + " " + bestSoFarRule);
    res.setLength(bestSoFarLength);
    res.setRule(bestSoFarRule);
    res.setInfo("distance calls: " + distanceCalls);

    return res;
  }

  /**
   * Cloning an array. I know, I need to make the
   * 
   * @param source the source array.
   * @return the clone.
   */
  private static ArrayList<RuleInterval> cloneIntervals(ArrayList<RuleInterval> source) {
    ArrayList<RuleInterval> res = new ArrayList<RuleInterval>();
    for (RuleInterval r : source) {
      res.add(new RuleInterval(r.getId(), r.getStartPos(), r.getEndPos(), r.getCoverage()));
    }
    return res;
  }

  /**
   * Finds all the Sequitur rules with a given Id and populates their start and end into the array.
   * 
   * @param id The rule Id.
   * @param intervals The rule intervals.
   * @return map of start - end.
   */
  private static Map<Integer, Integer> listRuleOccurrences(int id, ArrayList<RuleInterval> intervals) {
    HashMap<Integer, Integer> res = new HashMap<Integer, Integer>(100);
    for (RuleInterval i : intervals) {
      if (id == i.getId()) {
        res.put(i.getStartPos(), i.getEndPos());
      }
    }
    return res;
  }
}
