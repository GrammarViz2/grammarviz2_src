package edu.hawaii.jmotif.gi.sequitur.test;

import edu.hawaii.jmotif.gi.sequitur.SAXRule;
import edu.hawaii.jmotif.gi.sequitur.SequiturFactory;
import edu.hawaii.jmotif.timeseries.TSException;

public class PaperTest5 {

  private static final String input = "a a a a a b a b a c a c a d a d";

  public static void main(String[] args) throws TSException {

    SAXRule r = SequiturFactory.runSequiturWithEditDistanceThreshold(input, null, null);

    System.out.println(r.getRules());

  }

}
