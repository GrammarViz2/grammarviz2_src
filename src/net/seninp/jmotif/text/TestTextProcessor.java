package net.seninp.jmotif.text;

import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the text utilities class.
 * 
 * @author psenin
 * 
 */
public class TestTextProcessor {

  private static final String[][] BAG1 = { { "the", "3" }, { "brown", "5" }, { "cow", "2" } };
  private static final String[][] BAG2 = { { "the", "3" }, { "green", "2" }, { "hill", "3" },
      { "cow", "2" }, { "grass", "4" } };
  private static final String[][] BAG3 = { { "the", "3" }, { "hill", "2" }, { "meadow", "4" },
      { "cow", "4" }, { "air", "2" } };

  private WordBag bag1;
  private WordBag bag2;
  private WordBag bag3;
  private HashMap<String, WordBag> bags;

  private TextProcessor tp;

  /**
   * Test set-up.
   */
  @Before
  public void setUp() {
    tp = new TextProcessor();
    bag1 = buildBag("bag1", BAG1);
    bag2 = buildBag("bag2", BAG2);
    bag3 = buildBag("bag3", BAG3);
    bags = new HashMap<String, WordBag>();
    bags.put(bag1.getLabel(), bag1);
    bags.put(bag2.getLabel(), bag2);
    bags.put(bag3.getLabel(), bag3);
  }

  /**
   * Test the term frequency method.
   */
  @Test
  public void testTF() {
    assertTrue(Double.valueOf(3.0D / 5D).doubleValue() == tp.normalizedTF(bag1, BAG1[0][0]));
    assertTrue(Double.valueOf(2.0D / 4D).doubleValue() == tp.normalizedTF(bag2, BAG2[1][0]));
    assertTrue(Double.valueOf(4.0D / 4D).doubleValue() == tp.normalizedTF(bag3, BAG3[3][0]));
  }

  /**
   * Test the document frequency method.
   */
  @Test
  public void testDF() {
    assertTrue(3 == tp.df(bags, "the"));
    assertTrue(1 == tp.df(bags, "meadow"));
  }

  /**
   * Test inverse document frequency method.
   */
  @Test
  public void testIDF() {
    assertTrue(Double.POSITIVE_INFINITY == tp.idf(bags, "non"));
    assertTrue(1.0D == tp.idf(bags, "the"));
    assertTrue(3.0D / 2.0D == tp.idf(bags, "hill"));
    assertTrue(3.0D / 1.0D == tp.idf(bags, "air"));
  }

  /**
   * Test tf-idf statistics.
   */
  @Test
  public void testTFIDF() {
    HashMap<String, HashMap<String, Double>> tfidf = tp.computeTFIDF(bags.values());
    assertTrue(0.0D == tfidf.get("bag1").get("the"));

    double tfHill2 = tp.logTF(bag2, "hill");
    double tfHill3 = tp.logTF(bag3, "hill");

    double idfHill = tp.idf(bags, "hill");

    double tfidfHill2 = tfHill2 * Math.log10(idfHill);

    double tfidfHill3 = tfHill3 * Math.log10(idfHill);

    assertTrue(tfidfHill2 == tfidf.get("bag2").get("hill"));

    assertTrue(tfidfHill3 == tfidf.get("bag3").get("hill"));
  }

  /**
   * private method for building test bag objects.
   * 
   * @param name The bag name.
   * @param data The test data.
   * @return The wordBag class.
   */
  private WordBag buildBag(String name, String[][] data) {
    WordBag res = new WordBag(name);
    for (String[] d : data) {
      res.addWord(d[0], Integer.valueOf(d[1]));
    }
    return res;
  }

}
