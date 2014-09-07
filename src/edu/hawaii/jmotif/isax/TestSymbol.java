package edu.hawaii.jmotif.isax;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import edu.hawaii.jmotif.sax.SAXException;
import edu.hawaii.jmotif.sax.alphabet.NormalAlphabet;

/**
 * 
 * @author Josh Patterson
 * 
 * Testing the Symbol class.
 * 
 */
public class TestSymbol {

  /**
   * 
   * Test the iSAX bit representation
   * 
   */
  @Test
  public void testiSAXBitRep() {

    Symbol A = new Symbol(2, 4);

    String bits = A.getiSAXBitRepresentation(0);

    // System.out.println( "isax > bit rep > '" + bits + "' " );

    assertEquals("bit rep test", "10", bits);

    Symbol B = new Symbol(2, 8);
    assertEquals("bit rep test", "010", B.getiSAXBitRepresentation(0));

    Symbol C = new Symbol(4, 8);
    assertEquals("bit rep test", "100", C.getiSAXBitRepresentation(0));

    Symbol D = new Symbol(4, 16);
    // System.out.println( "wild: " + D.getiSAXBitRepresentation(0) );
    assertEquals("bit rep test", "0100", D.getiSAXBitRepresentation(0));

    Symbol E = new Symbol(4, 16);
    // System.out.println( "wild: " + E.getiSAXBitRepresentation(1) );
    assertEquals("bit rep test", "010*", E.getiSAXBitRepresentation(1));

    // Symbol F = new Symbol( 4, 16 );
    assertEquals("bit rep test", "01**", E.getiSAXBitRepresentation(2));

    assertEquals("bit rep test", "0***", E.getiSAXBitRepresentation(3));

    Symbol F = new Symbol(4, 32);
    // System.out.println( "wild: " + E.getiSAXBitRepresentation(1) );
    assertEquals("bit rep test", "0010*", F.getiSAXBitRepresentation(1));

  }

  /**
   * Test the symbol split mechanics used to split iSAX index space
   * 
   * 
   */
  @Test
  public void testSymbolSplit() {

    Symbol A = new Symbol(2, 4);

    Symbol a_0 = new Symbol();
    Symbol a_1 = new Symbol();

    Symbol A_low = new Symbol(4, 8);

    Symbol A_high = new Symbol(5, 8);

    A.promoteAndSplit(a_0, a_1);

    assertEquals("check low out card", a_0.cardinality, 8);
    assertEquals("check high out card", a_1.cardinality, 8);

    assertEquals("check low out SAX", a_0.saxCharacter, A_low.saxCharacter);
    assertEquals("check high out SAX", a_1.saxCharacter, A_high.saxCharacter);

    System.out.println("Symbol > " + a_1.saxCharacter);

  }

  /**
   * Test single symbol promotion mechanics to ensure the correct symbol cardinality is used.
   * 
   */
  @Test
  public void testPromoteSymbol() {

    System.out.println("testing promote symbol.");

    Symbol A_in = new Symbol(0, 2);

    Symbol B_in = new Symbol(1, 2);

    Symbol A_out = new Symbol(0, 0);
    Symbol B_out = new Symbol(0, 0);

    try {
      Symbol.PerformPromotion(A_in, B_in, A_out, B_out);
    }
    catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    assertEquals("testPromoteSymbol_assert_0", A_out.cardinality, 2);

    assertEquals("testPromoteSymbol_assert_1", B_out.cardinality, 2);

    assertEquals("testPromoteSymbol_assert_2", A_out.saxCharacter, 0);

    assertEquals("testPromoteSymbol_assert_3", B_out.saxCharacter, 1);

    System.out.println("testing promote symbol part 2.");

    A_in = new Symbol(6, 8);

    B_in = new Symbol(0, 2);

    A_out = new Symbol(0, 0);
    B_out = new Symbol(0, 0);

    try {
      Symbol.PerformPromotion(A_in, B_in, A_out, B_out);
    }
    catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    assertEquals("testPromoteSymbol_assert_4", A_out.cardinality, 8);

    assertEquals("testPromoteSymbol_assert_5", B_out.cardinality, 8);

    assertEquals("testPromoteSymbol_assert_6", A_out.saxCharacter, 6);

    assertEquals("testPromoteSymbol_assert_7", B_out.saxCharacter, 3);

    System.out.println("testing promote symbol part 3.");

    A_in = new Symbol(1, 2);

    B_in = new Symbol(3, 8);

    A_out = new Symbol(0, 0);
    B_out = new Symbol(0, 0);

    try {
      Symbol.PerformPromotion(A_in, B_in, A_out, B_out);
    }
    catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    assertEquals("testPromoteSymbol_assert_8", A_out.cardinality, 8);

    assertEquals("testPromoteSymbol_assert_9", B_out.cardinality, 8);

    assertEquals("testPromoteSymbol_assert_10", A_out.saxCharacter, 4);

    assertEquals("testPromoteSymbol_assert_11", B_out.saxCharacter, 3);

    System.out.println("testing promote symbol part 4.");

    A_in = new Symbol(1, 2);

    B_in = new Symbol(0, 8);

    A_out = new Symbol(0, 0);
    B_out = new Symbol(0, 0);

    try {
      Symbol.PerformPromotion(A_in, B_in, A_out, B_out);
    }
    catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    assertEquals("testPromoteSymbol_assert_12", A_out.cardinality, 8);

    assertEquals("testPromoteSymbol_assert_13", B_out.cardinality, 8);

    assertEquals("testPromoteSymbol_assert_14", A_out.saxCharacter, 4);

    assertEquals("testPromoteSymbol_assert_15", B_out.saxCharacter, 0);

  }

  /**
   * This is a simple sanity check to make sure our base SAX distance table works the way we think
   * it does. Values used are from the iSAX paper.
   */
  @Test
  public void testSAXDistanceTable() {

    NormalAlphabet alphabet = new NormalAlphabet();

    Symbol a = new Symbol(0, 4);
    Symbol b = new Symbol(1, 4);

    double val = 0;
    try {
      val = a.sax_table_dist(b, alphabet);
    }
    catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // System.out.println( "val: " + val );

    assertEquals("testSAXDistanceTable", (Double) val, (Double) 0.0D);

    Symbol a2 = new Symbol(0, 4);
    Symbol b2 = new Symbol(2, 4);

    double val2 = 0;
    try {
      val2 = a2.sax_table_dist(b2, alphabet);
    }
    catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    assertEquals("testSAXDistanceTable2", (Double) val2, (Double) 0.67D);

    /*
     * dist(t1,s1) = dist(11,00) = 1.34 dist(t2,s2) = dist(11,01) = 0.67 dist(t3,s3) = dist(01,11) =
     * 0.67 dist(t4,s4) = dist(00,11) = 1.34
     */

    Symbol a3 = new Symbol(3, 4);
    Symbol b3 = new Symbol(0, 4);

    double val3 = 0;
    try {
      val3 = a3.sax_table_dist(b3, alphabet);
    }
    catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    assertEquals("testSAXDistanceTable3", (Double) val3, (Double) 1.34D);

    Symbol a4 = new Symbol(3, 4);
    Symbol b4 = new Symbol(1, 4);

    double val4 = 0;
    try {
      val4 = a4.sax_table_dist(b4, alphabet);
    }
    catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    assertEquals("testSAXDistanceTable4", (Double) val4, (Double) 0.67D);

    Symbol a5 = new Symbol(1, 4);
    Symbol b5 = new Symbol(3, 4);

    double val5 = 0;
    try {
      val5 = a5.sax_table_dist(b5, alphabet);
    }
    catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    assertEquals("testSAXDistanceTable5", (Double) val5, (Double) 0.67D);

    Symbol a6 = new Symbol(0, 4);
    Symbol b6 = new Symbol(3, 4);

    double val6 = 0;
    try {
      val6 = a6.sax_table_dist(b6, alphabet);
    }
    catch (SAXException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    assertEquals("testSAXDistanceTable6", (Double) val6, (Double) 1.34D);

  }

  @Test
  public void testComparison() {

    Symbol A = new Symbol(0, 4);

    Symbol B = new Symbol(0, 4);

    int val = A.compareTo(B);

    assertEquals("base compareTo() test", 0, val);

    Symbol A2 = new Symbol(1, 4);

    Symbol B2 = new Symbol(2, 4);

    val = A2.compareTo(B2);
    int val2 = B2.compareTo(A2);

    assertEquals("negative compareTo() test", -1, val);

    assertEquals("positive compareTo() test", 1, val2);

    Symbol A3 = new Symbol(1, 4);

    Symbol B3 = new Symbol(2, 8);

    val = A3.compareTo(B3);

    assertEquals("promotion compareTo() test", 0, val);

  }

}
