package edu.hawaii.jmotif.isax;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import org.junit.Test;
import edu.hawaii.jmotif.distance.EuclideanDistance;
import edu.hawaii.jmotif.isax.index.IndexHashParams;
import edu.hawaii.jmotif.sax.SAXException;
import edu.hawaii.jmotif.timeseries.TPoint;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.timeseries.Timeseries;

public class TestISAXUtils {

  /**
   * Test the most basics of timeseries to iSAX decompositions
   */
  @Test
  public void testTStoISAXdecomp() {

    Timeseries ts = new Timeseries();
    ts.add(new TPoint(-1.0, 0));
    ts.add(new TPoint(-0.5, 1));
    ts.add(new TPoint(-0.25, 2));
    ts.add(new TPoint(0.0, 3));
    ts.add(new TPoint(0.25, 4));
    ts.add(new TPoint(0.50, 5));
    ts.add(new TPoint(0.75, 6));
    ts.add(new TPoint(1.0, 7));

    // setup the test timeseries, based on a known sequence (paper)

    Sequence isax = null;

    try {
      isax = ISAXUtils.CreateiSAXSequence(ts, 4, 4);
    }
    catch (TSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // now check to see that the symbos in the iSAX sequence match whats expected.

    System.out.println("isax > out > " + isax.getOrigLength());
    System.out.println("isax > word > " + isax.getSymbols().size());
    System.out.println("isax > name > " + isax.getIndexHash());

  }

  @Test
  public void testISAXSequenceSplitTest() {

    Timeseries ts = new Timeseries();
    ts.add(new TPoint(-1.0, 0));
    ts.add(new TPoint(-0.5, 1));
    ts.add(new TPoint(-0.25, 2));
    ts.add(new TPoint(0.0, 3));
    ts.add(new TPoint(0.25, 4));
    ts.add(new TPoint(0.50, 5));
    ts.add(new TPoint(0.75, 6));
    ts.add(new TPoint(1.0, 7));

    // setup the test timeseries, based on a known sequence (paper)

    Sequence A = new Sequence(16); // T, 4, 8
    A.getSymbols().add(new Symbol(3, 4));
    A.getSymbols().add(new Symbol(3, 4));
    A.getSymbols().add(new Symbol(1, 4));
    A.getSymbols().add(new Symbol(0, 8));

    Sequence seq_out = null;

    try {
      seq_out = ISAXUtils.CreateiSAXSequenceBasedOnCardinality(ts, A); // ( ts, 4, 4 );
    }
    catch (TSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    Sequence isax = null;

    try {
      isax = ISAXUtils.CreateiSAXSequence(ts, 4, 4);
    }
    catch (TSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.println("normally: " + isax.getBitStringRepresentation());

    System.out.println("out:      " + seq_out.getBitStringRepresentation());

    System.out.println("\nbased on: " + A.getBitStringRepresentation());

  }

  @Test
  public void testISAXSequenceSplitTest_1() {

    System.out.println("\nsplit test 2");

    Timeseries ts = new Timeseries();
    ts.add(new TPoint(1.0, 0));
    ts.add(new TPoint(-0.5, 1));
    ts.add(new TPoint(0.25, 2));
    ts.add(new TPoint(0.0, 3));
    ts.add(new TPoint(0.25, 4));
    ts.add(new TPoint(0.50, 5));
    ts.add(new TPoint(0.75, 6));
    ts.add(new TPoint(0.0, 7));

    // setup the test timeseries, based on a known sequence (paper)

    Sequence A = new Sequence(16); // T, 4, 8
    A.getSymbols().add(new Symbol(3, 4));
    A.getSymbols().add(new Symbol(3, 4));
    A.getSymbols().add(new Symbol(1, 4));
    A.getSymbols().add(new Symbol(0, 4));

    Sequence A2 = new Sequence(16); // T, 4, 8
    A2.getSymbols().add(new Symbol(3, 4));
    A2.getSymbols().add(new Symbol(3, 4));
    A2.getSymbols().add(new Symbol(1, 4));
    A2.getSymbols().add(new Symbol(0, 8));

    // A2.getSymbols().get(3).

    Sequence seq_out_0 = null;

    try {
      seq_out_0 = ISAXUtils.CreateiSAXSequenceBasedOnCardinality(ts, A); // ( ts, 4, 4 );
    }
    catch (TSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    Sequence seq_out_1 = null;

    try {
      seq_out_1 = ISAXUtils.CreateiSAXSequenceBasedOnCardinality(ts, A2); // ( ts, 4, 4 );
    }
    catch (TSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // System.out.println( "normally: " + isax.getStringRepresentation() );

    System.out.println("out:      " + seq_out_0.getBitStringRepresentation());
    System.out.println("out:      " + seq_out_1.getBitStringRepresentation());

    // System.out.println( "\nbased on: " + A.getStringRepresentation() );

  }

  @Test
  public void testISAXSequenceSplitTest_2() {

    System.out.println("\nChild Card Hash Test 0");

    Timeseries ts = new Timeseries();
    ts.add(new TPoint(1.0, 0));
    ts.add(new TPoint(-0.5, 1));
    ts.add(new TPoint(0.25, 2));
    ts.add(new TPoint(0.0, 3));
    ts.add(new TPoint(0.25, 4));
    ts.add(new TPoint(0.50, 5));
    ts.add(new TPoint(0.75, 6));
    ts.add(new TPoint(0.0, 7));

    // setup the test timeseries, based on a known sequence (paper)

    Sequence A = new Sequence(16); // T, 4, 8
    A.getSymbols().add(new Symbol(3, 4));
    A.getSymbols().add(new Symbol(3, 4));
    A.getSymbols().add(new Symbol(1, 4));
    A.getSymbols().add(new Symbol(0, 4));

    /*
     * Sequence A2 = new Sequence( 16 ); // T, 4, 8 A2.getSymbols().add(new Symbol( 3, 4 ) );
     * A2.getSymbols().add(new Symbol( 3, 4 ) ); A2.getSymbols().add(new Symbol( 1, 4 ) );
     * A2.getSymbols().add(new Symbol( 0, 8 ) );
     */
    // A2.getSymbols().get(3).

    Sequence seq_out_0 = null;

    ArrayList<Integer> arCards = A.getCardinalities();

    System.out.println("cards-start: " + arCards);

    arCards = IndexHashParams.generateChildCardinality(arCards);

    System.out.println("cards-gen: " + arCards);

    try {
      seq_out_0 = ISAXUtils.CreateiSAXSequenceBasedOnCardinality(ts, arCards); // ( ts, 4, 4 );
    }
    catch (TSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // System.out.println( "seq-hash: " + seq_out_0.getIndexHash() );
    System.out.println("out:      " + seq_out_0.getBitStringRepresentation());
    System.out.println("seq-hash: " + seq_out_0.getIndexHash());

    arCards = IndexHashParams.generateChildCardinality(arCards);

    System.out.println("cards-gen: " + arCards);

    try {
      seq_out_0 = ISAXUtils.CreateiSAXSequenceBasedOnCardinality(ts, arCards); // ( ts, 4, 4 );
    }
    catch (TSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.println("out:      " + seq_out_0.getBitStringRepresentation());
    System.out.println("seq-hash: " + seq_out_0.getIndexHash());

  }

  @Test
  public void testDNA_to_TS_0() {

    Timeseries ts_answer = new Timeseries();
    ts_answer.add(new TPoint(1.0, 0));
    ts_answer.add(new TPoint(1.0, 1));
    ts_answer.add(new TPoint(1.0, 2));
    ts_answer.add(new TPoint(1.0, 3));

    Timeseries ts = null;

    try {
      ts = ISAXUtils.CreateTimeseriesFromDNA("GGgg");
    }
    catch (TSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.println("\n\ndna_to_ts: " + ts);

    assertEquals("", true, ts_answer.equals(ts));

  }

  @Test
  public void testDNA_to_TS_1() {

    Timeseries ts_answer = new Timeseries();
    ts_answer.add(new TPoint(1.0, 0));
    ts_answer.add(new TPoint(2.0, 1));
    ts_answer.add(new TPoint(-2.0, 2));
    ts_answer.add(new TPoint(-1.0, 3));

    Timeseries ts = null;

    try {
      ts = ISAXUtils.CreateTimeseriesFromDNA("GaTc");
    }
    catch (TSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.println("\n\ndna_to_ts: " + ts);

    assertEquals("", true, ts_answer.equals(ts));

  }

  @Test
  public void testDNA_to_TS_to_DNA() {

    Timeseries ts_answer = new Timeseries();
    ts_answer.add(new TPoint(1.0, 0));
    ts_answer.add(new TPoint(2.0, 1));
    ts_answer.add(new TPoint(-2.0, 2));
    ts_answer.add(new TPoint(-1.0, 3));

    Timeseries ts = null;

    try {
      ts = ISAXUtils.CreateTimeseriesFromDNA("GaTc");
    }
    catch (TSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.println("\n\ndna_to_ts: " + ts);

    assertEquals("DNA to Timeseries", true, ts_answer.equals(ts));

    String dna = "";

    try {
      dna = ISAXUtils.CreateDNAFromTimeseries(ts);
    }
    catch (TSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.println("TS to DNA > " + dna);

    assertEquals("Timeseries back to DNA", true, dna.equals("gatc"));

  }

  @Test
  public void testDNA_to_iSAX_0() {

    Timeseries ts_answer = new Timeseries();
    ts_answer.add(new TPoint(1.0, 0));
    ts_answer.add(new TPoint(2.0, 1));
    ts_answer.add(new TPoint(-2.0, 2));
    ts_answer.add(new TPoint(-1.0, 3));

    Sequence A = null;

    try {
      A = ISAXUtils.CreateiSAXSequence(ts_answer, 4, 4);
    }
    catch (TSException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    Sequence dna = null;

    try {
      dna = ISAXUtils.CreateiSAXSequenceFromDNA("gatC", 4, 4);
    }
    catch (TSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.println("Dna: " + dna.getBitStringRepresentation() + " == "
        + A.getBitStringRepresentation());

    assertEquals("dna conversion test", dna.equals(A), true);

    System.out.println("DNA in iSAX: " + dna.getBitStringRepresentation());

  }

  /**
   * Something to note here;
   * 
   * --- if we always find our cut points from paa_val, theres no way for it to be out of the ranges
   * unless its at either end out of range.
   * 
   * -------- is that right?
   */
  @Test
  public void testMinDist_Components() {

    double paa_val = 1;
    double[] cuts = { -1.15, -0.67, -0.32, 0.0, 0.32, 0.67, 1.15 };
    SAXBreakpoints bp = new SAXBreakpoints();

    ISAXUtils.getSAXBreakpoints(paa_val, cuts, bp);

    assertEquals(bp.B_Li, 0.67d, 0.000001d);

    assertEquals(bp.B_Ui, 1.15d, 0.000001d);

    paa_val = -1.10d;
    bp = new SAXBreakpoints();

    ISAXUtils.getSAXBreakpoints(paa_val, cuts, bp);

    assertEquals(bp.B_Li, -1.15d, 0.000001d);

    assertEquals(bp.B_Ui, -0.67d, 0.000001d);

  }

  private void RunDistTest() {

    Timeseries ts = ISAXUtils.generateRandomTS(32); // 8
    Timeseries ts2 = ISAXUtils.generateRandomTS(32);

    double euc = 0;

    try {
      euc = EuclideanDistance.distance(ts.values(), ts2.values());
      // System.out.println( "\nEUCLID-DIST:      " + euc );
    }
    catch (TSException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    Sequence isax = null;
    Sequence isax2 = null;
    try {
      isax = ISAXUtils.CreateiSAXSequence(ts, 8, 8);

      isax2 = ISAXUtils.CreateiSAXSequence(ts2, 8, 8);
    }
    catch (TSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    double sax_dist = 0;
    try {
      sax_dist = isax.sax_distance(isax2);
    }
    catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    double mindist_paa = 0;

    try {
      mindist_paa = ISAXUtils.MINDIST_PAA_iSAX(ts, isax2);
    }
    catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    if (euc < mindist_paa) {

      System.out.println(">> ERR ");

    }

    if (mindist_paa > sax_dist) {

      System.out.println(">> ERR 2");
      System.out.println("mindist_paa: " + mindist_paa + " < sax_dist: " + sax_dist + "\n");

    }

  }

  @Test
  public void testCuts() {

    System.out.println("\n\ntestCuts\n");

    for (int x = 0; x < 20; x++) {

      RunDistTest();

    }

  }

}
