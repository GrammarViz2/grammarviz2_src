package edu.hawaii.jmotif.gi.sequitur.test;

import edu.hawaii.jmotif.gi.sequitur.SAXRule;
import edu.hawaii.jmotif.gi.sequitur.SequiturFactory;
import edu.hawaii.jmotif.timeseries.TSException;

public class PaperTest6 {

  // private static final String input = "a b a b c a b c d a b c d a a a b a b a c";
  private static final String input = "a rose is a rose is a rose";

  // private static final String input = "a b a b c a b c d a b";

  public static void main(String[] args) throws TSException {

    SAXRule r = SequiturFactory.runSequiturWithEditDistanceThreshold(input, null, null);

    System.out.println(r.getRules());

    // System.out.println(r.getSAXRules());

  }

}
