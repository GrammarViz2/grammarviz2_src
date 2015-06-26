package net.seninp.jmotif.direct;

import java.util.Comparator;
import java.util.Map.Entry;

public class TfIdfEntryComparator implements Comparator<Entry<String, Double>> {

  @Override
  public int compare(Entry<String, Double> arg0, Entry<String, Double> arg1) {
    return -arg0.getValue().compareTo(arg1.getValue());
  }

}
