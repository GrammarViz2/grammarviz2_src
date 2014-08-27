package edu.hawaii.jmotif.gi.sequitur.test;

import edu.hawaii.jmotif.gi.sequitur.SAXRule;
import edu.hawaii.jmotif.gi.sequitur.SequiturFactory;
import edu.hawaii.jmotif.timeseries.TSException;

public class PaperTest2 {

  private static final String input = "a b c d b c a b c d b c";

  public static void main(String[] args) throws TSException {
    
    SAXRule r = SequiturFactory.runSequiturWithEditDistanceThreshold(input, null, null);

    System.out.println(r.getRules());
    
  }

}
