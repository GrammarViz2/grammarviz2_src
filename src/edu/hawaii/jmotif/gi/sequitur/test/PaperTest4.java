package edu.hawaii.jmotif.gi.sequitur.test;

import edu.hawaii.jmotif.gi.sequitur.SAXRule;
import edu.hawaii.jmotif.gi.sequitur.SequiturFactory;
import edu.hawaii.jmotif.timeseries.TSException;

public class PaperTest4 {

  private static final String input = "a b a b c a b c d a b c d e a b c d e f";

  public static void main(String[] args) throws TSException {
    
    SAXRule r = SequiturFactory.runSequiturWithEditDistanceThreshold(input, null, null);

    System.out.println(r.getRules());
    
  }

}
