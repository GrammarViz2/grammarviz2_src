package edu.hawaii.jmotif.isax.index;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import org.junit.Test;
import edu.hawaii.jmotif.isax.ISAXUtils;
import edu.hawaii.jmotif.isax.Sequence;
import edu.hawaii.jmotif.isax.Symbol;
import edu.hawaii.jmotif.timeseries.TPoint;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.timeseries.Timeseries;

/**
 * Test hash parameters.
 * 
 * @author Josh Patterson, psenin
 * 
 */
public class TestIndexHashParams {

  @Test
  public void testMaskSequence() {

    IndexHashParams p = new IndexHashParams();
    p.addWildBit(2);
    p.addWildBit(2);
    p.addWildBit(2);
    p.addWildBit(2);

    Timeseries ts = new Timeseries();
    ts.add(new TPoint(-1.0, 0));
    ts.add(new TPoint(-0.5, 1));
    ts.add(new TPoint(-0.25, 2));
    ts.add(new TPoint(0.0, 3));
    ts.add(new TPoint(0.25, 4));
    ts.add(new TPoint(0.50, 5));
    ts.add(new TPoint(0.75, 6));
    ts.add(new TPoint(1.0, 7));

    Sequence isax = null;

    try {
      isax = ISAXUtils.CreateiSAXSequence(ts, 4, 4);
    }
    catch (TSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.println("isax: " + isax.getBitStringRepresentation());

    String rep = p.createMaskedBitSequence(isax);

    System.out.println("rep : '" + rep + "' ");

    assertEquals("wildbits test 0", "0**, 0**, 0**, 0**, ", rep);

  }

  @Test
  public void testMaskSequence_1() {

    IndexHashParams p = new IndexHashParams();

    p.addWildBit(2);
    p.addWildBit(2);
    p.addWildBit(2);
    p.addWildBit(2);

    p.setWildBitCountAtIndex(0, 1);

    Timeseries ts = new Timeseries();
    ts.add(new TPoint(-1.0, 0));
    ts.add(new TPoint(-0.5, 1));
    ts.add(new TPoint(-0.25, 2));
    ts.add(new TPoint(0.0, 3));
    ts.add(new TPoint(0.25, 4));
    ts.add(new TPoint(0.50, 5));
    ts.add(new TPoint(0.75, 6));
    ts.add(new TPoint(1.0, 7));

    Sequence isax = null;

    try {
      isax = ISAXUtils.CreateiSAXSequence(ts, 4, 4);
    }
    catch (TSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.println("isax: " + isax.getBitStringRepresentation());

    String rep = p.createMaskedBitSequence(isax);

    System.out.println("rep : '" + rep + "' ");

    assertEquals("wildbits test 0", "00*, 0**, 0**, 0**, ", rep);

  }

  @Test
  public void testWildBitReduction_0() {

    System.out.println("\ntestWildBitReduction_0");

    IndexHashParams p = new IndexHashParams();

    p.addWildBit(2);
    p.addWildBit(2);
    p.addWildBit(2);
    p.addWildBit(2);

    System.out.println("wb: " + p.debugGetWildBits());

    p.reduceNextWildbit();
    p.reduceNextWildbit();
    p.reduceNextWildbit();
    p.reduceNextWildbit();
    p.reduceNextWildbit();

    System.out.println("wb: " + p.debugGetWildBits());

    Timeseries ts = new Timeseries();
    ts.add(new TPoint(-1.0, 0));
    ts.add(new TPoint(-0.5, 1));
    ts.add(new TPoint(-0.25, 2));
    ts.add(new TPoint(0.0, 3));
    ts.add(new TPoint(0.25, 4));
    ts.add(new TPoint(0.50, 5));
    ts.add(new TPoint(0.75, 6));
    ts.add(new TPoint(1.0, 7));

    Sequence isax = null;

    try {
      isax = ISAXUtils.CreateiSAXSequence(ts, 4, 4);
    }
    catch (TSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.println("wildbit red > isax: " + isax.getBitStringRepresentation());

    String rep = p.createMaskedBitSequence(isax);
    System.out.println("wildbit red > rep : '" + rep + "' ");

  }

  @Test
  public void testWildBitReduction_1() {

    System.out.println("\ntestWildBitReduction_1");

    IndexHashParams p = new IndexHashParams();

    p.addWildBit(2);
    p.addWildBit(2);
    p.addWildBit(2);
    p.addWildBit(2);

    System.out.println("wb: " + p.debugGetWildBits());

    p.reduceNextWildbit();
    p.reduceNextWildbit();
    p.reduceNextWildbit();
    p.reduceNextWildbit();

    p.reduceNextWildbit();
    p.reduceNextWildbit();
    p.reduceNextWildbit();
    p.reduceNextWildbit();

    // extra pass, should change nothing
    p.reduceNextWildbit();

    System.out.println("wb: " + p.debugGetWildBits());

    Timeseries ts = new Timeseries();
    ts.add(new TPoint(-1.0, 0));
    ts.add(new TPoint(-0.5, 1));
    ts.add(new TPoint(-0.25, 2));
    ts.add(new TPoint(0.0, 3));
    ts.add(new TPoint(0.25, 4));
    ts.add(new TPoint(0.50, 5));
    ts.add(new TPoint(0.75, 6));
    ts.add(new TPoint(1.0, 7));

    Sequence isax = null;

    try {
      isax = ISAXUtils.CreateiSAXSequence(ts, 4, 4);
    }
    catch (TSException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.println("wildbit red > isax: " + isax.getBitStringRepresentation());

    String rep = p.createMaskedBitSequence(isax);
    System.out.println("wildbit red > rep : '" + rep + "' ");

  }

  @Test
  public void testProgressiveCardPromotion_0() {

    System.out.println("\ntestProgressiveCardPromotion_0");

    IndexHashParams p = new IndexHashParams();
    p.base_card = 4;

  }

  @Test
  public void testChildCardGeneration_0() {

    System.out.println("\nChild Card Gen test 0");

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

    ArrayList<Integer> arCards = IndexHashParams.generateChildCardinality(A);

    System.out.println("" + arCards.toString());

    arCards = IndexHashParams.generateChildCardinality(arCards);
    System.out.println("" + arCards.toString());

    arCards = IndexHashParams.generateChildCardinality(arCards);
    System.out.println("" + arCards.toString());

    arCards = IndexHashParams.generateChildCardinality(arCards);
    System.out.println("" + arCards.toString());

    arCards = IndexHashParams.generateChildCardinality(arCards);
    System.out.println("" + arCards.toString());

  }

  @Test
  public void testSerDe() {

    IndexHashParams p = new IndexHashParams();

    p.base_card = 8;
    p.d = 1;
    p.dim_index = 3;
    p.isax_word_length = 16;
    // p.node_type = NodeType.INTERNAL;
    p.orig_ts_len = 128;
    p.threshold = 100;

    byte[] b = null;

    try {
      b = p.getBytes();
    }
    catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    assertEquals("serDeBytes", 24, b.length);

    IndexHashParams p_new = new IndexHashParams();

    p_new.deserialize(b, 0);

    assertEquals("serDe_new_th", p_new.threshold, 100);
    assertEquals("serDe_new_orig", p_new.orig_ts_len, 128);
    assertEquals("serDe_new_basecard", p_new.base_card, 8);
    assertEquals("serDe_new_d", p_new.d, 1);
    assertEquals("serDe_new_isaxwl", p_new.isax_word_length, 16);
    assertEquals("serDe_new_di", p_new.dim_index, 3);

  }

}
