package edu.hawaii.jmotif.isax;

import static org.junit.Assert.assertEquals;
import java.text.DecimalFormat;
import org.junit.Before;
import org.junit.Test;
import edu.hawaii.jmotif.sax.SAXException;

/**
 * 
 * Testing the iSAX Sequence class to make sure values computed match values in the equations from
 * the iSAX paper.
 * 
 * @author Josh Patterson
 * 
 */
public class TestSequence {

  @Before
  public void setUp() {

  }

  /**
   * Test basic object cloning.
   */
  @Test
  public void testClone() {

    Sequence A = new Sequence(16);
    A.getSymbols().add(new Symbol(1, 1));
    A.getSymbols().add(new Symbol(1, 1));
    A.getSymbols().add(new Symbol(1, 1));
    A.getSymbols().add(new Symbol(1, 1));

    Sequence B = null;

    try {
      B = A.clone();
    }
    catch (CloneNotSupportedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    assertEquals("clone Sequence test", 4, B.getSymbols().size());

  }

  /**
   * 
   * Test the SAX distance table for Cardinality == 4 and dist( 00, 01 )
   * 
   */
  @Test
  public void testSAXDistanceTable_dist_00_01() {

    Symbol A_s1 = new Symbol();
    A_s1.cardinality = 4;
    A_s1.saxCharacter = 1;

    Sequence w1 = new Sequence(16);
    w1.getSymbols().add(A_s1);

    Symbol B_s1 = new Symbol();
    B_s1.cardinality = 4;
    B_s1.saxCharacter = 0;

    Sequence w2 = new Sequence(16);
    w2.getSymbols().add(B_s1);

    double localDist = 0;

    try {
      localDist = w1.sax_distance(w2);
    }
    catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    assertEquals("dist( 00, 01 )", (Double) 0.00D, (Double) localDist);

  }

  /**
   * 
   * Test the SAX distance table for Cardinality == 4 and dist( 00, 10 )
   * 
   */
  @Test
  public void testSAXDistanceTable_dist_00_10() {

    Symbol A_s1 = new Symbol();
    A_s1.cardinality = 4;
    A_s1.saxCharacter = 2;

    Sequence w1 = new Sequence(16);
    w1.getSymbols().add(A_s1);

    Symbol B_s1 = new Symbol();
    B_s1.cardinality = 4;
    B_s1.saxCharacter = 0;

    Sequence w2 = new Sequence(16);
    w2.getSymbols().add(B_s1);

    double localDist = 0;

    try {
      localDist = w1.sax_distance(w2);
    }
    catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.println(" TestSequence > dist( 00, 10 ) == " + localDist);

    assertEquals("dist( 00, 10 )", (Double) 0.67D, (Double) localDist);

  }

  /**
   * 
   * Test the SAX distance table for Cardinality == 4 and dist( 00, 01 )
   * 
   */
  /*
   * @Test public void testDistanceTable_dist_00_01() {
   * 
   * NormalAlphabet alphabet = new NormalAlphabet();
   * 
   * Symbol A_s1 = new Symbol(); A_s1.cardinality = 4; A_s1.saxCharacter = 1;
   * 
   * Symbol B_s1 = new Symbol(); B_s1.cardinality = 4; B_s1.saxCharacter = 0;
   * 
   * 
   * Symbol a = null; Symbol b = null;
   * 
   * 
   * if (A_s1.cardinality >= B_s1.cardinality ) { //iHighCard =
   * this.getSymbols().get(x).cardinality; a = A_s1; try { b = B_s1.promote( A_s1 ); } catch
   * (ISAXException e) { // TODO Auto-generated catch block e.printStackTrace(); }
   * 
   * } else { //iHighCard = other.getSymbols().get(x).cardinality;
   * 
   * try { a = A_s1.promote( B_s1 ); } catch (ISAXException e) { // TODO Auto-generated catch block
   * e.printStackTrace(); } b = B_s1;
   * 
   * }
   * 
   * // pass to MINDIST function to find pre-dist
   * 
   * double[][] distanceMatrix = { { 0 }, { 0 } };
   * 
   * try { distanceMatrix = alphabet.getDistanceMatrix(a.cardinality); } catch (TSException e) { //
   * TODO Auto-generated catch block e.printStackTrace(); }
   * 
   * 
   * double localDist = distanceMatrix[a.saxCharacter][b.saxCharacter];
   * 
   * assertEquals("dist( 00, 01 )", (Double) 0.0D, (Double) localDist );
   * 
   * System.out.println( "test > dist( 00, 01 ) = " + localDist ); }
   */

  /**
   * 
   * Test two Sequences to makes sure their MINDIST() value is correct
   * 
   */
  @Test
  public void testSingleSymbolCompare() {

    Sequence A = new Sequence(16);
    Symbol A_s1 = new Symbol();
    A_s1.cardinality = 4;
    A_s1.saxCharacter = 3;
    A.getSymbols().add(A_s1);

    Sequence B = new Sequence(16);
    Symbol B_s1 = new Symbol();
    B_s1.cardinality = 4;
    B_s1.saxCharacter = 0;
    B.getSymbols().add(B_s1);

    try {
      System.out.println("min-distance: " + A.MINDIST(B));
    }
    catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  /**
   * 
   * Take the two Sequences listed in the iSAX paper and see if the Cardinality promotion mechanics
   * work correctly for the less cardinality Sequence.
   * 
   */
  @Test
  public void testExampleSequencePromotion() {

    // now lets test a real Sequence

    // iSAX(T,4,8) = T8 = {110,110,011,000} = {68,68,38,08}
    // iSAX(S,4,2) = S2 = {0 ,0 ,1 ,1 } = {02,02,12,12}

    System.out.println("testing example full Sequence promotion");

    Sequence A = new Sequence(16); // T, 4, 8
    A.getSymbols().add(new Symbol(6, 8));
    A.getSymbols().add(new Symbol(6, 8));
    A.getSymbols().add(new Symbol(3, 8));
    A.getSymbols().add(new Symbol(0, 8));

    Sequence A_out = new Sequence(16);

    Sequence B = new Sequence(16);
    B.getSymbols().add(new Symbol(0, 2));
    B.getSymbols().add(new Symbol(0, 2));
    B.getSymbols().add(new Symbol(1, 2));
    B.getSymbols().add(new Symbol(1, 2));

    Sequence B_out = new Sequence(16);

    Sequence.PerformPromotion(A, B, A_out, B_out);

    // System.out.println( "len: " + B.getSymbols().size() );

    for (int x = 0; x < B_out.getSymbols().size(); x++) {

      assertEquals("paper example Sequence promotion test", B_out.getSymbols().get(x).cardinality,
          8);

    }

    // S8 = {011,011,100,100}

    assertEquals("paper example Sequence promotion test", B_out.getSymbols().get(0).saxCharacter, 3);
    assertEquals("paper example Sequence promotion test", B_out.getSymbols().get(1).saxCharacter, 3);
    assertEquals("paper example Sequence promotion test", B_out.getSymbols().get(2).saxCharacter, 4);
    assertEquals("paper example Sequence promotion test", B_out.getSymbols().get(3).saxCharacter, 4);

  }

  /**
   * Make sure the MINDIST calculation matches the values from the iSAX paper.
   */
  @Test
  public void testMINDIST_Calc() {
    /*
     * SAX(T,4,4) = T4 = {11,11,01,00}, and therefore SAX(S,4,4) = S4 = {00,01,11,11}.
     */

    System.out.println("testing MINDIST function");

    Sequence A = new Sequence(16); // T, 4, 8
    A.getSymbols().add(new Symbol(3, 4));
    A.getSymbols().add(new Symbol(3, 4));
    A.getSymbols().add(new Symbol(1, 4));
    A.getSymbols().add(new Symbol(0, 4));

    // Sequence A_out = new Sequence();

    Sequence B = new Sequence(16);
    B.getSymbols().add(new Symbol(0, 4));
    B.getSymbols().add(new Symbol(1, 4));
    B.getSymbols().add(new Symbol(3, 4));
    B.getSymbols().add(new Symbol(3, 4));

    double val = 0;
    try {
      val = A.MINDIST(B);
    }
    catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.println("dist: " + val);

    double actual = 4.237D;

    DecimalFormat twoDForm = new DecimalFormat("#.###");

    // assertEquals( "paper example MINDIST test",(Double)Double.valueOf(twoDForm.format( val )),
    // (Double)actual );

    assertEquals("paper example MINDIST test", Double.valueOf(twoDForm.format(val)),
        (Double) actual);

  }

  /**
   * 
   * Test label naming mechanics for a Sequence
   * 
   * 
   */
  @Test
  public void testSequenceNamingMechanics() {

    Sequence A = new Sequence(16); // T, 4, 8
    A.getSymbols().add(new Symbol(3, 4));
    A.getSymbols().add(new Symbol(3, 4));
    A.getSymbols().add(new Symbol(1, 4));
    A.getSymbols().add(new Symbol(0, 4));

    assertEquals("Testing Sequence Name", A.getIndexHash(), "3.4_3.4_1.4_0.4_");

  }

  @Test
  public void testComparison() {

    Sequence A = new Sequence(16);
    A.getSymbols().add(new Symbol(0, 4));
    A.getSymbols().add(new Symbol(0, 4));

    Sequence B = new Sequence(16);
    B.getSymbols().add(new Symbol(0, 4));
    B.getSymbols().add(new Symbol(0, 4));

    Sequence C = new Sequence(16);
    C.getSymbols().add(new Symbol(0, 4));
    C.getSymbols().add(new Symbol(1, 4));

    Sequence D = new Sequence(16);
    D.getSymbols().add(new Symbol(0, 8));
    D.getSymbols().add(new Symbol(0, 4));

    Sequence E = new Sequence(16);
    E.getSymbols().add(new Symbol(0, 4));
    E.getSymbols().add(new Symbol(0, 4));
    E.getSymbols().add(new Symbol(0, 4));

    assertEquals("base comparison", true, A.equals(B));

    assertEquals("ne comparison", false, A.equals(C));

    assertEquals("ne diff-card comparison", false, A.equals(D));

    assertEquals("ne diff-len comparison", false, A.equals(E));

  }

  @Test
  public void testSerDe() {

    Sequence A = new Sequence(16); // T, 4, 8
    A.getSymbols().add(new Symbol(1, 4));
    A.getSymbols().add(new Symbol(2, 8));
    A.getSymbols().add(new Symbol(3, 16));
    A.getSymbols().add(new Symbol(4, 32));

    byte[] ser = A.getBytes();

    System.out.println("SerDe size: " + ser.length);

    assertEquals("Serialize size", 40, ser.length);

    Sequence B = new Sequence(0);
    B.deserialize(ser);

    assertEquals("orig_len", 16, B.getOrigLength());

    assertEquals("B_len", 4, B.getSymbols().size());

  }

  @Test
  public void testIndexHashMechanics() {

    Sequence A = new Sequence(16); // T, 4, 8
    A.getSymbols().add(new Symbol(1, 4));
    A.getSymbols().add(new Symbol(2, 8));
    A.getSymbols().add(new Symbol(3, 16));
    A.getSymbols().add(new Symbol(4, 32));

    String hash = A.getIndexHash();

    System.out.println("hash: " + hash);

    Sequence B = new Sequence(0);
    B.parseFromIndexHash(hash);

    // System.out.println( B.getIndexHash());

    assertEquals("index hash test", A.getIndexHash(), B.getIndexHash());

  }

  @Test
  public void testSequenceBitMechanics() {

    Symbol A_s1 = new Symbol();
    A_s1.cardinality = 4; // 2 bits
    A_s1.saxCharacter = 1;

    Sequence w1 = new Sequence(16);
    w1.getSymbols().add(A_s1);

    System.out.println("card bits > " + Symbol.numberBitsInCardinality(A_s1.cardinality));

    assertEquals("does this match bits sizes", 2, Symbol.numberBitsInCardinality(A_s1.cardinality));

    Symbol B_s1 = new Symbol();
    B_s1.cardinality = 16; // 2 bits
    A_s1.saxCharacter = 1;

    // Sequence w1 = new Sequence( 16 );
    // w1.getSymbols().add( A_s1 );

    System.out.println("card bits > " + Symbol.numberBitsInCardinality(B_s1.cardinality));

    assertEquals("does this match bits sizes", 4, Symbol.numberBitsInCardinality(B_s1.cardinality));

    Symbol B_wild_0 = new Symbol(2, 16, 1);

    System.out.println("wildcard rep > " + B_wild_0.getiSAXBitRepresentation());

    assertEquals("wildcard sax bit rep test", "001*", B_wild_0.getiSAXBitRepresentation());

    Symbol B_wild_1 = new Symbol(2, 16, 2);
    assertEquals("wildcard sax bit rep test", "00**", B_wild_1.getiSAXBitRepresentation());

    Symbol B_wild_2 = new Symbol(2, 16, 3);
    System.out.println("wildcard rep > " + B_wild_2.getiSAXBitRepresentation());

    assertEquals("wildcard sax bit rep test", "0***", B_wild_2.getiSAXBitRepresentation());

  }

  @Test
  public void testSequenceMembership_0() {

    Symbol A_s1 = new Symbol(2, 8, 1);

    Sequence w1 = new Sequence(16);
    w1.getSymbols().add(A_s1);

    // System.out.println( "card bits > " + A_s1.getiSAXBitRepresentation() );

    Symbol A_s2 = new Symbol(2, 8, 1);

    // Sequence w2 = new Sequence( 16 );
    // w2.getSymbols().add( A_s2 );
    Sequence nk1 = new Sequence(16);
    nk1.getSymbols().add(A_s2);

    System.out.println("string rep > " + nk1.getBitStringRepresentation());

    assertEquals("Contains Sequence", nk1.ContainsSequence(w1), true);

    // assertEquals( "does this match bits sizes", 2, Symbol.numberBitsInCardinality(
    // A_s1.cardinality ) );

  }

  @Test
  public void testSequenceMembership_1() {

    Symbol A_s1 = new Symbol(2, 8);
    Symbol A_s2 = new Symbol(2, 8);
    Symbol A_s3 = new Symbol(2, 8);

    Sequence w1 = new Sequence(16);
    w1.getSymbols().add(A_s1);
    w1.getSymbols().add(A_s2);
    w1.getSymbols().add(A_s3);

    System.out.println("testSequenceMembership_1 > w1  > " + w1.getBitStringRepresentation());

    Symbol A_nk1 = new Symbol(2, 8, 1);
    Symbol A_nk2 = new Symbol(2, 8, 1);
    Symbol A_nk3 = new Symbol(2, 8, 1);

    Sequence nk1 = new Sequence(16);
    nk1.getSymbols().add(A_nk1);
    nk1.getSymbols().add(A_nk2);
    nk1.getSymbols().add(A_nk3);

    System.out.println("testSequenceMembership_1 > nk1 > " + nk1.getBitStringRepresentation());

    assertEquals("Contains Sequence", nk1.ContainsSequence(w1), true);

    // assertEquals( "does this match bits sizes", 2, Symbol.numberBitsInCardinality(
    // A_s1.cardinality ) );

  }

  @Test
  public void testSequenceMembership_2() {

    Symbol A_s1 = new Symbol(3, 8);
    Symbol A_s2 = new Symbol(2, 8);
    Symbol A_s3 = new Symbol(3, 8);

    Sequence w1 = new Sequence(16);
    w1.getSymbols().add(A_s1);
    w1.getSymbols().add(A_s2);
    w1.getSymbols().add(A_s3);

    System.out.println("testSequenceMembership_1 > w1  > " + w1.getBitStringRepresentation());

    Symbol A_nk1 = new Symbol(2, 8, 1);
    Symbol A_nk2 = new Symbol(2, 8, 1);
    Symbol A_nk3 = new Symbol(2, 8, 1);

    Sequence nk1 = new Sequence(16);
    nk1.getSymbols().add(A_nk1);
    nk1.getSymbols().add(A_nk2);
    nk1.getSymbols().add(A_nk3);

    System.out.println("testSequenceMembership_1 > nk1 > " + nk1.getBitStringRepresentation());

    assertEquals("Contains Sequence", nk1.ContainsSequence(w1), true);

    // assertEquals( "does this match bits sizes", 2, Symbol.numberBitsInCardinality(
    // A_s1.cardinality ) );

  }

  @Test
  public void testSequenceMembership_3() {

    Symbol A_s1 = new Symbol(4, 8);
    Symbol A_s2 = new Symbol(2, 8);
    Symbol A_s3 = new Symbol(3, 8);

    Sequence w1 = new Sequence(16);
    w1.getSymbols().add(A_s1);
    w1.getSymbols().add(A_s2);
    w1.getSymbols().add(A_s3);

    System.out.println("testSequenceMembership_1 > w1  > " + w1.getBitStringRepresentation());

    Symbol A_nk1 = new Symbol(2, 8, 1);
    Symbol A_nk2 = new Symbol(2, 8, 1);
    Symbol A_nk3 = new Symbol(2, 8, 1);

    Sequence nk1 = new Sequence(16);
    nk1.getSymbols().add(A_nk1);
    nk1.getSymbols().add(A_nk2);
    nk1.getSymbols().add(A_nk3);

    System.out.println("testSequenceMembership_1 > nk1 > " + nk1.getBitStringRepresentation());

    assertEquals("Contains Sequence", nk1.ContainsSequence(w1), false);

    // assertEquals( "does this match bits sizes", 2, Symbol.numberBitsInCardinality(
    // A_s1.cardinality ) );

  }

  @Test
  public void testSequenceMembership_4() {

    Symbol A_s1 = new Symbol(4, 8);
    Symbol A_s2 = new Symbol(2, 8);
    Symbol A_s3 = new Symbol(3, 8);

    Sequence w1 = new Sequence(16);
    w1.getSymbols().add(A_s1);
    w1.getSymbols().add(A_s2);
    w1.getSymbols().add(A_s3);

    System.out.println("\ntestSequenceMembership_4 > w1  > " + w1.getBitStringRepresentation());

    Symbol A_nk1 = new Symbol(2, 8, 2);
    Symbol A_nk2 = new Symbol(2, 8, 3);
    Symbol A_nk3 = new Symbol(2, 8, 3);

    Sequence nk1 = new Sequence(16);
    nk1.getSymbols().add(A_nk1);
    nk1.getSymbols().add(A_nk2);
    nk1.getSymbols().add(A_nk3);

    System.out.println("testSequenceMembership_4 > nk1 > " + nk1.getBitStringRepresentation());

    assertEquals("Contains Sequence", nk1.ContainsSequence(w1), false);

    // in this case we'd split the SAX space
    // for sequence: 100, 010, 011,
    // ideally from 0**, ***, ***,
    // to
    // 1**, ***, ***,

    // 0, 0, 0
    // 0, 0, 1
    // 0, 1, 1
    // 1, 1, 1
    // 1, 1, 0
    // 1, 0, 0
    // 1, 0, 1
    // 0, 1, 0

    // assertEquals( "does this match bits sizes", 2, Symbol.numberBitsInCardinality(
    // A_s1.cardinality ) );

  }

}
