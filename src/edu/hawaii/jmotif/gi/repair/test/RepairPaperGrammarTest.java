package edu.hawaii.jmotif.gi.repair.test;

import edu.hawaii.jmotif.gi.repair.RePairFactory;
import edu.hawaii.jmotif.gi.repair.RePairRule;
import edu.hawaii.jmotif.timeseries.TSException;

/**
 * Provides a paper example test implementation.
 * 
 * @author psenin
 * 
 */
public class RepairPaperGrammarTest {

  // private static final String input = "a b a b c a b c d a b c d a a a b a b a c";
  private static final String input = "abc abc cba XXX abc abc cba";

  // private static final String input = "a b a b c a b c d a b";

  public static void main(String[] args) throws TSException {

    @SuppressWarnings("unused")
    RePairRule r = RePairFactory.buildGrammar(input);

    System.out.println(RePairRule.toGrammarRules());

  }

}
