package net.seninp.grammarviz;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

/**
 * Regression tests for {@link GrammarVizAnomaly#toBoundaries(String)} grid parsing:
 * it must parse exactly nine whitespace-separated integers and fail with a clear
 * message on malformed input rather than an opaque {@code NumberFormatException} /
 * {@code ArrayIndexOutOfBoundsException}.
 */
public class TestToBoundaries {

  @Test
  public void parsesNineIntegers() {
    assertArrayEquals(new int[] {10, 20, 30, 40, 50, 60, 70, 80, 90},
        GrammarVizAnomaly.toBoundaries("10 20 30 40 50 60 70 80 90"));
  }

  @Test
  public void collapsesExtraWhitespace() {
    assertArrayEquals(new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9},
        GrammarVizAnomaly.toBoundaries("  1   2 3\t4 5 6 7 8 9 "));
  }

  @Test(expected = IllegalArgumentException.class)
  public void rejectsTooFew() {
    GrammarVizAnomaly.toBoundaries("1 2 3");
  }

  @Test(expected = IllegalArgumentException.class)
  public void rejectsTooMany() {
    GrammarVizAnomaly.toBoundaries("1 2 3 4 5 6 7 8 9 10");
  }

  @Test(expected = IllegalArgumentException.class)
  public void rejectsNonInteger() {
    GrammarVizAnomaly.toBoundaries("1 2 3 4 5 6 7 8 x");
  }

  @Test(expected = IllegalArgumentException.class)
  public void rejectsNull() {
    GrammarVizAnomaly.toBoundaries(null);
  }
}
