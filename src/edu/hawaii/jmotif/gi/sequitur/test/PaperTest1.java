package edu.hawaii.jmotif.gi.sequitur.test;

import edu.hawaii.jmotif.gi.sequitur.SAXRule;
import edu.hawaii.jmotif.gi.sequitur.SequiturFactory;
import edu.hawaii.jmotif.timeseries.TSException;

public class PaperTest1 {

  // private static final String input = "aac abc abb bca acd aac abc";

  // private static String input = "a b c a b c a b c";
  private static String input = "a b a b c a b c";

  public static void main(String[] args) throws TSException {

    SAXRule r = SequiturFactory.runSequitur(input);

    System.out.println(r.getRules());

  }

}
