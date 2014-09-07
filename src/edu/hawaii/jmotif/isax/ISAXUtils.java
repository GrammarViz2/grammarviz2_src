package edu.hawaii.jmotif.isax;

import java.util.ArrayList;
import edu.hawaii.jmotif.sax.SAXException;
import edu.hawaii.jmotif.sax.alphabet.Alphabet;
import edu.hawaii.jmotif.sax.alphabet.NormalAlphabet;
import edu.hawaii.jmotif.timeseries.TPoint;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.timeseries.TSUtils;
import edu.hawaii.jmotif.timeseries.Timeseries;

class SAXBreakpoints {
  public double B_Li = 0;
  public double B_Ui = 0;
}

/**
 * ISAXUtils is a lib for creating various types of SAX Sequences and calculating the
 * MINDIST_PAA_ISAX distance
 * 
 * - currently the MINDIST_PAA_SAX function is broken. feel free to review and help fix it.
 * 
 * - Some C# code for the MINDIST_PAA_SAX function is in the comments.
 * 
 * - need MINDIST_PAA_SAX for exact search and knn search
 * 
 * - is there a faster way to do knnSearch ?
 * 
 * @author jpatterson
 * 
 */

public class ISAXUtils {

  /**
   * Creates a Timeseries of length len; typically used in Unit Tests.
   * 
   * @param len Length of new timeseries
   * @return the Timeseries generated
   */
  public static Timeseries generateRandomTS(int len) {

    Timeseries ts = new Timeseries();

    for (int x = 0; x < len; x++) {

      int r = (int) (Math.random() * 100) % 10;

      ts.add(new TPoint(r, x));

    }

    return ts;

  }

  /**
   * Create an iSAX Sequence
   * 
   * @param ts
   * @param base_cardinality
   * @param word_length
   * @return
   * @throws TSException
   */
  public static Sequence CreateiSAXSequence(Timeseries ts, int base_cardinality, int word_length)
      throws TSException {

    // create SAX representation
    NormalAlphabet alphabet = new NormalAlphabet();
    int paaSize = word_length;

    Timeseries PAA;
    try {
      PAA = TSUtils.paa(TSUtils.zNormalize(ts), paaSize);
    }
    catch (CloneNotSupportedException e) {
      throw new TSException("Unable to clone: "); // + StackTrace.toString(e));
    }

    Sequence iSAX = new Sequence(ts.size());

    // transpose into numeric iSAX representation

    int[] arCuts = TSUtils.ts2Index(PAA, alphabet, base_cardinality);

    for (int x = 0; x < arCuts.length; x++) {
      iSAX.getSymbols().add(new Symbol(arCuts[x], base_cardinality));
    }

    return iSAX;

  }

  /**
   * 
   * Uses another Sequence's cardinality to generate the SAX representation.
   * 
   * @param ts
   * @return
   */
  public static Sequence CreateiSAXSequenceBasedOnCardinality(Timeseries ts, Sequence seq)
      throws TSException {

    // create SAX representation
    NormalAlphabet alphabet = new NormalAlphabet();
    int paaSize = seq.getSymbols().size();

    // perform PAA conversion
    Timeseries PAA;
    try {
      PAA = TSUtils.paa(TSUtils.zNormalize(ts), paaSize);
    }
    catch (CloneNotSupportedException e) {
      throw new TSException("Unable to clone: "); // + StackTrace.toString(e));
    }

    Sequence iSAX = new Sequence(ts.size());

    // transpose into numeric iSAX representation

    for (int x = 0; x < seq.getSymbols().size(); x++) {

      int[] arCuts = TSUtils.ts2Index(PAA, alphabet, seq.getSymbols().get(x).cardinality);

      // System.out.println( x + " > " + arCuts[x] );
      iSAX.getSymbols().add(new Symbol(arCuts[x], seq.getSymbols().get(x).cardinality));
    }

    return iSAX;

  }

  /**
   * 
   * Uses another Sequence's cardinality to generate the SAX representation.
   * 
   * @param ts
   * @return
   */
  public static Sequence CreateiSAXSequenceBasedOnCardinality(Timeseries ts,
      ArrayList<Integer> arCards) throws TSException {

    // create SAX representation
    NormalAlphabet alphabet = new NormalAlphabet();
    int paaSize = arCards.size();

    // perform PAA conversion
    Timeseries PAA;
    try {
      PAA = TSUtils.paa(TSUtils.zNormalize(ts), paaSize);
    }
    catch (CloneNotSupportedException e) {
      throw new TSException("Unable to clone: "); // + StackTrace.toString(e));
    }

    Sequence iSAX = new Sequence(ts.size());

    // transpose into numeric iSAX representation

    try {
      for (int x = 0; x < arCards.size(); x++) {

        int[] arCuts = TSUtils.ts2Index(PAA, alphabet, arCards.get(x));

        iSAX.getSymbols().add(new Symbol(arCuts[x], arCards.get(x)));
      }
    }
    catch (TSException e) {
      iSAX = null;
      System.out.println("Failed to build SAX for card: " + arCards);
    }

    return iSAX;

  }

  /**
   * Creates an iSAX Sequence from a DNA segment.
   * 
   * 
   * @param dna_segment
   * @param base_cardinality
   * @param word_length
   * @return
   * @throws TSException
   */
  public static Sequence CreateiSAXSequenceFromDNA(String dna_segment, int base_cardinality,
      int word_length) throws TSException {

    /*
     * For i = 1 to length(DNAstring) If DNAstringi = A, then Ti+1 = Ti + 2 If DNAstringi = G, then
     * Ti+1 = Ti + 1 If DNAstringi = C, then Ti+1 = Ti - 1 If DNAstringi = T, then Ti+1 = Ti - 2 End
     */
    Timeseries ts_dna = new Timeseries();

    String dna_scrubbed = dna_segment.toLowerCase().trim();

    // System.out.println( "seq-len: " + dna_scrubbed + ", len: " + dna_scrubbed.length() );

    for (int x = 0; x < dna_scrubbed.length(); x++) {

      if (dna_scrubbed.charAt(x) == 'a') {
        ts_dna.add(new TPoint(2, x));
      }
      if (dna_scrubbed.charAt(x) == 'g') {
        ts_dna.add(new TPoint(1, x));
      }
      if (dna_scrubbed.charAt(x) == 'c') {
        ts_dna.add(new TPoint(-1, x));
      }
      if (dna_scrubbed.charAt(x) == 't') {
        ts_dna.add(new TPoint(-2, x));
      }

    }

    // create SAX representation
    NormalAlphabet alphabet = new NormalAlphabet();
    int paaSize = word_length;

    // perform PAA conversion
    Timeseries PAA;
    try {
      PAA = TSUtils.paa(TSUtils.zNormalize(ts_dna), paaSize);
    }
    catch (CloneNotSupportedException e) {
      throw new TSException("Unable to clone: "); // + StackTrace.toString(e));
    }
    Sequence iSAX = new Sequence(dna_scrubbed.length());

    // transpose into numeric iSAX representation

    int[] arCuts = TSUtils.ts2Index(PAA, alphabet, base_cardinality);

    for (int x = 0; x < arCuts.length; x++) {
      iSAX.getSymbols().add(new Symbol(arCuts[x], base_cardinality));
    }

    return iSAX;

  }

  /**
   * 
   * Creates a raw Timeseries directly from DNA characters. Useful for when working with genome data
   * directly.
   * 
   * @param dna_segment
   * @return
   * @throws TSException
   */
  public static Timeseries CreateTimeseriesFromDNA(String dna_segment) throws TSException {

    Timeseries ts_dna = new Timeseries();

    String dna_scrubbed = dna_segment.toLowerCase().trim();

    for (int x = 0; x < dna_scrubbed.length(); x++) {

      if (dna_scrubbed.charAt(x) == 'a') {
        ts_dna.add(new TPoint(2, x));
      }
      if (dna_scrubbed.charAt(x) == 'g') {
        ts_dna.add(new TPoint(1, x));
      }
      if (dna_scrubbed.charAt(x) == 'c') {
        ts_dna.add(new TPoint(-1, x));
      }
      if (dna_scrubbed.charAt(x) == 't') {
        ts_dna.add(new TPoint(-2, x));
      }

    }

    return ts_dna;

  }

  public static String CreateDNAFromTimeseries(Timeseries ts_dna) throws TSException {

    // Timeseries ts_dna = new Timeseries();

    // String dna_scrubbed = dna_segment.toLowerCase().trim();

    StringBuffer buffer = new StringBuffer();

    for (int x = 0; x < ts_dna.size(); x++) {
      /*
       * if ( dna_scrubbed.charAt(x) == 'a' ) { ts_dna.add( new TPoint( 2, x ) ); } if (
       * dna_scrubbed.charAt(x) == 'g' ) { ts_dna.add( new TPoint( 1, x ) ); } if (
       * dna_scrubbed.charAt(x) == 'c' ) { ts_dna.add( new TPoint( -1, x ) ); } if (
       * dna_scrubbed.charAt(x) == 't' ) { ts_dna.add( new TPoint( -2, x ) ); }
       */

      if (ts_dna.elementAt(x).value() == 2) {
        buffer.append('a');
      }
      if (ts_dna.elementAt(x).value() == 1) {
        buffer.append('g');
      }
      if (ts_dna.elementAt(x).value() == -1) {
        buffer.append('c');
      }
      if (ts_dna.elementAt(x).value() == -2) {
        buffer.append('t');
      }

    }

    return buffer.toString();

  }

  /**
   * 
   * Pulls breakpoints for calculating SAX characters. Used to calculate MINDIST_PAA_SAX().
   * 
   * @param value
   * @param cuts
   * @param bp
   */
  public static void getSAXBreakpoints(double value, double[] cuts, SAXBreakpoints bp) {

    int count = 0;
    while ((count < cuts.length) && (cuts[count] <= value)) {

      bp.B_Li = cuts[count];
      if (cuts.length > count + 1) {
        bp.B_Ui = cuts[count + 1];
      }
      else {

        bp.B_Ui = cuts[count];
      }

      count++;
    }

  }

  /*
   * 
   * 
   * public static double MinDistPAAToiSAX(ushort[] saxVals, SaxOptions opts, double[] timeSeries) {
   * if (saxVals.Length <= 1 || saxVals.Length != Globals.SaxWordLength) throw new
   * ApplicationException("Invalid or Uneven length for ushort [] arrays.");
   * 
   * double minDist = 0.0; int wordLen = saxVals.Length; int? dupRatio = null; double remainder =
   * Math.IEEERemainder(timeSeries.Length, wordLen);
   * 
   * // if not divisible, expand timeseries sufficiently if (remainder != 0) { int lcm =
   * GetLCM(timeSeries, wordLen); dupRatio = lcm / timeSeries.Length; timeSeries =
   * DupArray(timeSeries, dupRatio.Value); #if DEBUG Assert.AreEqual(lcm, timeSeries.Length);
   * Assert.AreEqual(0, Math.IEEERemainder(timeSeries.Length, wordLen)); #endif }
   * 
   * double[] PAA = GetPAA(timeSeries, wordLen); Assert.AreEqual(PAA.Length, wordLen); int
   * segmentSize = timeSeries.Length / wordLen; ReadOnlyCollection<ushort> optsMask = opts.Mask;
   * 
   * for (int i = 0; i < wordLen; i++) { // PAA[offset] ushort card = ComputeCard(optsMask[i],
   * Globals.SaxBaseCard); // card for this value double tVal = PAA[i]; double ptDist = 0.0;
   * 
   * // #refactor, use +-inf if (saxVals[i] == 0) { double lrgr = breakpoints[card][saxVals[i]]; if
   * (tVal > lrgr) ptDist = Math.Pow((tVal - lrgr), 2); } // highest card(val) else if (saxVals[i]
   * == card - 1) { double smlr = breakpoints[card][saxVals[i] - 1]; if (tVal < smlr) ptDist =
   * Math.Pow((tVal - smlr), 2); } // check if pt falls on either side of breakpt region else {
   * double smlr = breakpoints[card][saxVals[i] - 1]; double lrgr = breakpoints[card][saxVals[i]];
   * 
   * if (tVal > lrgr) { ptDist = Math.Pow((tVal - lrgr), 2); } else if (tVal < smlr) { ptDist =
   * Math.Pow((tVal - smlr), 2); } } ptDist /= dupRatio ?? 1; ptDist *= segmentSize; minDist +=
   * ptDist; } minDist = Math.Sqrt(minDist); return minDist;
   */

  /**
   * The MINDIST_PAA_iSAX function is used in kNN Search and exact search to find a baseline
   * distance between a PAA representation and a SAX representation.
   * 
   * @param ts
   * @param paaSize length of the Timseries ts
   * @param iSAX
   * @return
   * @throws ISAXException
   */
  public static double MINDIST_PAA_iSAX(Timeseries ts, Sequence iSAX) throws SAXException {

    int paaSize = iSAX.getSymbols().size();

    Timeseries PAA = null;
    try {
      PAA = TSUtils.paa(TSUtils.zNormalize(ts), paaSize);
    }
    catch (TSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (CloneNotSupportedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // 1. make sure they are of equal lengths

    if (iSAX.getSymbols().size() != PAA.size()) {
      throw new SAXException("Sequences are not of equal length!");
    }

    // 2. make sure their original length is the same

    if (iSAX.getOrigLength() != ts.size()) {
      throw new SAXException("Timeseries original length are not of equal length!");
    }

    double front_coef = Math.sqrt(iSAX.getOrigLength() / paaSize); // n / w

    // System.out.println( "orig: " + iSAX.getOrigLength() + " / paaSize: " + paaSize );

    /*
     * 
     * public static char[] ts2String(Timeseries paa, Alphabet alphabet, int alphabetSize) throws
     * TSException { double[] cuts = alphabet.getCuts(alphabetSize); char[] res = new
     * char[paa.size()]; for (int i = 0; i < paa.size(); i++) { res[i] =
     * num2char(paa.elementAt(i).value(), cuts); } return res; }
     */
    Alphabet alphabet = new NormalAlphabet();
    double paa_value = 0;
    double[] cuts = null;

    double sum = 0;

    int sax_val = 0;

    for (int x = 0; x < paaSize; x++) {

      paa_value = PAA.elementAt(x).value();

      // look at the SAX breakpoints for which we pull the SAX value, which is turned into a
      // character

      // TSUtils.ts2Index(arg0, arg1, arg2)

      try {
        int[] arCuts = TSUtils.ts2Index(PAA, alphabet, iSAX.getSymbols().get(x).cardinality);
        sax_val = arCuts[x];
        /*
         * for ( int z = 0; z < arCuts.length; z++ ) { System.out.print( " " + arCuts[ z ] ); }
         * System.out.println( "[end] " );
         */
      }
      catch (TSException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }

      try {
        cuts = alphabet.getCuts(iSAX.getSymbols().get(x).cardinality);
      }
      catch (TSException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      // Double B_Li = new Double(0); // lower cut value
      // Double B_Ui = new Double(0); // upper cut value

      if (0 == sax_val) {

        System.out.println(" ------------- zero ----------- ");

      }
      else if (iSAX.getSymbols().get(x).cardinality - 1 == sax_val) {

        System.out.println(" ------------- top ----------- ");

      }
      else {

        SAXBreakpoints bp = new SAXBreakpoints();

        // this point here is a place we might need to review
        ISAXUtils.getSAXBreakpoints(paa_value, cuts, bp);

        if (bp.B_Li == bp.B_Ui) {
          System.out.println("same breakpoints");
        }

        if (bp.B_Li > paa_value) {

          // System.out.println( "******************* B_Li" );

          sum += Math.pow((bp.B_Li - paa_value), 2);

        }
        else if (bp.B_Ui < paa_value) {

          sum += Math.pow((bp.B_Ui - paa_value), 2);

        }
        else {
          // sum += 0; // do nothing
        }

      } // if

      // System.out.println( "PAA: " + paa_value + ", Li: " + bp.B_Li + ", Ui: " + bp.B_Ui );

      /*
       * System.out.print( "cuts: " ); for ( int c = 0; c < cuts.length; c++ ) {
       * 
       * System.out.print( ", " + cuts[ c ] );
       * 
       * } System.out.println( "" );
       */

      /*
       * System.out.println( "paa_val: " + paa_value );
       * 
       * System.out.println( "sum: " + sum );
       */

    } // for

    double bp_deltas = Math.sqrt(sum);

    // System.out.println( "MINDIST_PAA_iSAX: " + (bp_deltas * front_coef) );

    return front_coef * bp_deltas;
  }

}
