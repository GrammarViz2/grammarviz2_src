package edu.hawaii.jmotif.isax.index;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import edu.hawaii.jmotif.isax.ISAXUtils;
import edu.hawaii.jmotif.isax.Sequence;
import edu.hawaii.jmotif.isax.Symbol;
import edu.hawaii.jmotif.timeseries.TPoint;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.timeseries.Timeseries;

public class TestiSAXIndex {

  @Test
  public void testBasicIndexInsert() {

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

  }

  @Test
  public void testISAXRootNodeConstruct() {

    Sequence A = new Sequence(16); // T, 4, 8
    A.getSymbols().add(new Symbol(1, 4));
    A.getSymbols().add(new Symbol(1, 4));
    A.getSymbols().add(new Symbol(1, 4));
    A.getSymbols().add(new Symbol(1, 4));

    IndexHashParams p = new IndexHashParams();
    p.base_card = 4;
    p.d = 1;
    p.isax_word_length = 3;

    p.arWildBits.clear();
    p.arWildBits.add(1);
    p.arWildBits.add(1);
    p.arWildBits.add(1);
    p.arWildBits.add(1);

    // InternalNode root_node = new InternalNode(A, p, NodeType.ROOT);

    // System.out.println("root > masked rep > " + root_node.getMaskedRepresentation());

  }

  @Test
  public void testISAXIndex_0() {

    // System.out.println("\ntestISAXIndex_0");

    iSAXIndex index = new iSAXIndex(4, 4, 8);

    Timeseries ts = new Timeseries();
    ts.add(new TPoint(-1.0, 0));
    ts.add(new TPoint(-0.5, 1));
    ts.add(new TPoint(-0.25, 2));
    ts.add(new TPoint(0.0, 3));
    ts.add(new TPoint(0.25, 4));
    ts.add(new TPoint(0.50, 5));
    ts.add(new TPoint(0.75, 6));
    ts.add(new TPoint(1.0, 7));

    index.InsertSequence(ts, "genome.txt", 104526);
    index.InsertSequence(ts, "genome.txt", 2304526);

    index.InsertSequence(ts, "genome.txt", 2304526);

    index.InsertSequence(ts, "genome.txt", 3304526);
    index.InsertSequence(ts, "genome.txt", 4304526);
    index.InsertSequence(ts, "genome.txt", 5304526);
    index.InsertSequence(ts, "genome.txt", 6304526);
    index.InsertSequence(ts, "genome.txt", 7304526);
    index.InsertSequence(ts, "genome.txt", 8304526);

    Timeseries ts2 = new Timeseries();
    ts2.add(new TPoint(-1.0, 0));
    ts2.add(new TPoint(-0.5, 1));
    ts2.add(new TPoint(-0.25, 2));
    ts2.add(new TPoint(0.0, 3));
    ts2.add(new TPoint(0.25, 4));
    ts2.add(new TPoint(0.50, 5));
    ts2.add(new TPoint(0.75, 6));
    ts2.add(new TPoint(0.0, 7));

    index.InsertSequence(ts2, "genome.txt", 8304526);

  }

  @Test
  public void testISAXIndex_1() {

    // System.out.println("\ntestISAXIndex_1");

    iSAXIndex index = new iSAXIndex(4, 4, 8);

    Timeseries ts = new Timeseries();

    ts.add(new TPoint(-1.0, 0));
    ts.add(new TPoint(-0.5, 1));
    ts.add(new TPoint(-0.25, 2));
    ts.add(new TPoint(0.0, 3));

    ts.add(new TPoint(0.25, 4));
    ts.add(new TPoint(0.50, 5));
    ts.add(new TPoint(0.75, 6));
    ts.add(new TPoint(1.0, 7));

    index.InsertSequence(ts, "genome.txt", 104526);

    Timeseries ts_2 = new Timeseries();

    ts_2.add(new TPoint(0.0, 0));
    ts_2.add(new TPoint(-0.5, 1));
    ts_2.add(new TPoint(-0.25, 2));
    ts_2.add(new TPoint(0.0, 3));

    ts_2.add(new TPoint(0.25, 4));
    ts_2.add(new TPoint(0.50, 5));
    ts_2.add(new TPoint(0.75, 6));
    ts_2.add(new TPoint(-1.0, 7));

    TimeseriesInstance tsi_B = new TimeseriesInstance(ts_2);

    index.InsertSequence(ts_2, "genome.txt", 104526);
    index.InsertSequence(ts_2, "genome.txt", 104527);
    index.InsertSequence(ts_2, "genome.txt", 104528);
    index.InsertSequence(ts_2, "genome.txt", 104529);
    index.InsertSequence(ts_2, "genome.txt", 104530);
    index.InsertSequence(ts_2, "genome.txt", 104531);

    TimeseriesInstance result = index.ApproxSearch(ts_2);

    if (result == null) {
      assertEquals("null check", true, false);
    }

    assertEquals("base Approx Search test", true, result.equals(tsi_B));

  }

  @Test
  public void testISAXIndex_2() {

    // System.out.println("\ntestISAXIndex_2");

    iSAXIndex index = new iSAXIndex(4, 4, 8);

    Timeseries ts = new Timeseries();

    ts.add(new TPoint(-1.0, 0));
    ts.add(new TPoint(-0.5, 1));
    ts.add(new TPoint(-0.25, 2));
    ts.add(new TPoint(0.0, 3));

    ts.add(new TPoint(0.25, 4));
    ts.add(new TPoint(0.50, 5));
    ts.add(new TPoint(0.75, 6));

    // the one we change
    ts.add(new TPoint(1.0, 7));

    // System.out.println("------- Inserting -----------");
    for (int x = 0; x < 5; x++) {

      // ts.elementAt(3).setValue(10 - x);
      ts.elementAt(7).setValue(x);

      // System.out.println("ts: " + ts);

      // TimeseriesInstance tsi_A = new TimeseriesInstance( ts );

      index.InsertSequence(ts, "genome.txt", 104526 + (x * 10));

    }

    // System.out.println("------- Inserting -----------");

    Timeseries ts_2 = new Timeseries();

    ts_2.add(new TPoint(-1.0, 0));
    ts_2.add(new TPoint(-0.5, 1));
    ts_2.add(new TPoint(-0.25, 2));
    ts_2.add(new TPoint(0.0, 3));

    ts_2.add(new TPoint(0.25, 4));
    ts_2.add(new TPoint(0.50, 5));
    ts_2.add(new TPoint(0.75, 6));
    ts_2.add(new TPoint(2.0, 7));

    TimeseriesInstance tsi_B = new TimeseriesInstance(ts_2);

    // index.InsertSequence(ts_2, "genome.txt", 104526 );

    TimeseriesInstance result = index.ApproxSearch(ts_2);

    if (result == null) {
      // System.out.println("Approx Search > no result found!");
      assertEquals("null check", true, false);
    }

    assertEquals("2nd base Approx Search test", true, result.equals(tsi_B));

  }

  @Test
  public void testDNAIndexPass_0() {

    String human_dna = "GTCAATGGCCAGGATATTAGAACAGTACTCTGTGAACCCTATTTATGGTGGCACCCCTTAGACTAAGATAACACAGGGAGCAAGAGGTTGACAGGAAAGCCAGGGGAGCAGGGAAGCCTCCTGTAAAGAGAGAAGTGCTAAGTCTCCTTTCTAAGGCACATGATGGAT";
    human_dna += "TCAAGGGAAAGCCACATTTGACTAAAGCCCAAGGGATTGTTGCTTCTAATCCGATTTCTTGGCAGAAGATATTACAAACTAAGAGTCAGATTAATATGTGGGTGCCAAAATAAATAAACAAATAATTGAATAATCCCTGGAGGTTTAAGTGAGGAGAAACTCCTCCAC";
    human_dna += "AGCTTGCTACCGAGGCAGAACCGGTTGAAACTGAAATGCATCCGCCGCCAGAGGATCTGTAAAAGAGAGGTTGTTACGAAACTGGCAACTGCCAACCAAAGTCCACCAATGGACAAGCAAAAAAGAGCACTCATCTCATGCTCCCAAGGATCAACCTTCCCAGAGTTT";
    human_dna += "TCACTTAAGTGGCCACCAAGCCAGTTGTCAATCCAGGGCTTTGGACTGAAATCTAGGGCTTCATCCGCTACCTCAGAGTGTCTTCTATTTCTTCCAGCCAGTGACAAATACAACAAACATCTGAGATGTTTTAGCTATAAATCCTTTACAATTGTTATTTATGTCTTA";
    human_dna += "ACTTTTGTTATACCTGGAAAAGTAGGGGAAACAATAAGAACATACTGTCTTGGCCAAGCATCCAAGGTTAAATGAGTTATGGAAATTCATTTGGGAGCCAAGACATTGCACGTGGTTATTTATTAGTCACCCAAGCATGTATTTTGCATGTCCATCAGTTGTTCTTGG";
    human_dna += "CCAAAAGAGCAGAATCAATGAGCCGCTGCAGATGCAGACATAGCAGCCCCTTGCAGGGACAAGTCTGCAAGATGAGCATTGAAGAGGATGCACAAGCCCGGTAGCCCGGGAAATGGCAGGCACTTACAAGAGCCCAGGTTGTTGCCATGTTTGTTTTTGCAACTTGTC";
    human_dna += "TATTTAAAGAGATTTG";

    String chimp_dna = "GGCAATGGCCAGGATATTAGAACAGTACTCTGTGAACCCTATTTATGGTAGCACCCCTTAGACTAAGATAACACAGGGAGCAAGAGGTTGACAGGAAAGCCAGGGGAGCAGGGAAGCCTCCTGTAAAGAGAGAAGTGCTAAGTCTCCTTTCTAAGGCACATGATGGAT";
    chimp_dna += "TCAAGGGAAAGTCACATTTGACTAAAGCCCAAGGGATTGTTGCTTCTAATCCGATTCTTGGCAGAAGATATTGCAAACTAAGAGTCAGATTAATATGTGGGTGCCAAAATAAATAAACAAATAATTGAATAATCCCTGGAGGTTTAAGTGAGGAGAAACTCCTCCACA";
    chimp_dna += "GCTTGCTACCGAGGCAGAACCGGTTGAAACTGAAATGCACCCGCTGCCAGAGGATCTGTAAAAGGGAGGTTGTTACCGAACTGGCAACTGCCAACCAAAGTCTACCAATGGACAAGCAAAAAAGAGCACTCATCTCATGCTCCCAAGGATCAACCTTCCCAGAATTTT";
    chimp_dna += "CACTTAAGTGGCCACCAAGCCAGTTGTCAATCCAGGGCTTTGGACTGAAATCTAGGGCTTCATCCACTACCTCAGAGTGTCTTCCATTTCTTCCAGCCAGTGACAAATACAACAAACATCTGAGATGTTTTAGCTATAAATCCTTTACAATTGTTATTTATGTCTTAA";
    chimp_dna += "CTTTTGTTATACCTGGAAAAGTAGGGGAAACAATAAGAACATACTGTCTTGGCCAAGCATCCAAGGTTAAATGAGTTATGGGAATTCATTTGGGAGCCAAGACATTGCGCGTGGTTATTTATTAGTCACCCAAGCATGTATTTTGCATGTCCATCAGTTGTTCTTGGC";
    chimp_dna += "CAAAAGAACAGAATCAATGAGCCGCTGCAGATGCAGACATAGCAGCCCCTTGCAGGAACAAGTCTGCAAGATGAGCATTGAAGAGGATGCACAAGCCCGGTAGCCCGGGAAATGGCAGGCACTTACAAGAGCCCAGGTTGTTGCCATGTTTGTTTTTGCAACTTGTCT";
    chimp_dna += "ATTTAAACAGATTTGA";

    int window_len = 16;

    // System.out.println("\n\n------------------------\n\nDNA Index Test: sample size: "
    // + human_dna.length());

    iSAXIndex index = new iSAXIndex(4, 4, window_len);
    index.InsertDNASample(human_dna, window_len, "human");
    index.InsertDNASample(chimp_dna, window_len, "chimp");

  }

  @Test
  public void testRandomInsert() {

    // System.out.println("\ntestISAXIndex_RandomInsert_0");

    iSAXIndex index = new iSAXIndex(4, 4, 8);

    Timeseries search_ts = null;

    for (int x = 0; x < 10000; x++) {
      Timeseries ts_insert = ISAXUtils.generateRandomTS(8);
      if (x == 5000) {
        search_ts = ts_insert;
      }
      index.InsertSequence(ts_insert, "ts.txt", 1000 + x * 20);
    }

    // System.out.println(" ----- done -------");

    // System.out.println("Searching for: " + search_ts);

    // index.InsertSequence(ts_2, "genome.txt", 104526 );
    long start = System.currentTimeMillis();

    TimeseriesInstance result = index.ApproxSearch(search_ts);

    long diff = System.currentTimeMillis() - start;

    if (result == null) {
      System.out.println("Approx Search > no result found!");
      // assertEquals( "null check", true, false );
    }
    else {
      System.out.println("Found ts in " + diff + " ms");
    }

  }

}
