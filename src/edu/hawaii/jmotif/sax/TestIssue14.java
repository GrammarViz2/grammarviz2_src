package edu.hawaii.jmotif.sax;

import static org.junit.Assert.*;
import java.util.Arrays;
import org.junit.Test;
import edu.hawaii.jmotif.sax.alphabet.NormalAlphabet;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.timeseries.TSUtils;

public class TestIssue14 {

  private static final double[] series1 = { -0.5, -1.0, -0.538, 1.0, -0.083 };
  private static final double[] series2 = { -0.2, -0.6, -1.0, 1, 0.2 };

  @Test
  public void test() {

    try {

      NormalAlphabet alphabet = new NormalAlphabet();

      char[] str1 = TSUtils.ts2String(series1, alphabet.getCuts(6));

      char[] str2 = TSUtils.ts2String(series2, alphabet.getCuts(6));

      System.out.print("series1: " + Arrays.toString(series1));
      System.out.println(", series2: " + Arrays.toString(series2));

      System.out.print("string1: " + String.valueOf(str1));
      System.out.println(", series2: " + String.valueOf(str2));

      System.out.println("Str distance: " + SAXFactory.strDistance(str1, str2));

      System.out.println("Str distance in numbers: " + SAXFactory.strDistance(str1, str2));
      for (int i = 0; i < str1.length; i++) {
        int tDist = Math.abs(Character.getNumericValue(str1[i])
            - Character.getNumericValue(str2[i]));
        System.out.println(str1[i] + " - " + str2[i] + " = " + tDist);
      }

      assertEquals("Testing the Str distance", 0, SAXFactory.strDistance(str1, str2));

    }
    catch (TSException e) {
      fail("Exception shall not be thrown!");
    }

  }

}
