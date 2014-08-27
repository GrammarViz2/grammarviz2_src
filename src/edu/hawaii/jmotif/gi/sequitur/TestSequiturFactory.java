package edu.hawaii.jmotif.gi.sequitur;

import edu.hawaii.jmotif.timeseries.TSException;

/**
 * This is used for visual testing of resulting grammars.
 * 
 * @author psenin
 * 
 */
public class TestSequiturFactory {

  private static final String testString1 = "b b e b e e b e b e b b e b e e";
  private static final String testString2 = "a b c d b c";
  private static final String testString3 = "a b c d b c a b c d b c";

  private static final String testDeepestHierarchy = "a b a b c a b c d a b c d e a b c d e f";
  private static final String maximumProcessing = "y z x y z w x y z v w x y";

  private static final String utilityConstraint = "a b c d b c a b c d";
  
  private static final String testRose = "a rose is a rose is a rose";

  public static void main(String[] args) throws TSException {

    SAXRule rule = SequiturFactory.runSequiturWithEditDistanceThreshold(testRose, null, null);

    System.out.println(rule.getRules());

  }

}
