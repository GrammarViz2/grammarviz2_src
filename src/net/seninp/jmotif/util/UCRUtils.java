package net.seninp.jmotif.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This implements variety utils for UCR-formatted data.
 * 
 * @author psenin
 * 
 */
public class UCRUtils {

  /**
   * Reads bunch of series from file. First column treats as a class label. Rest as a real-valued
   * series.
   * 
   * @param fileName
   * @return
   * @throws IOException
   */
  public synchronized static Map<String, List<double[]>> readUCRData(String fileName)
      throws IOException {

    Map<String, List<double[]>> res = new HashMap<String, List<double[]>>();

    BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
    String line = "";
    while ((line = br.readLine()) != null) {
      if (line.trim().length() == 0) {
        continue;
      }
      String[] split = line.trim().split("[,\\s]+|\\s+");

      String label = split[0];
      Double num = parseValue(label);
      String seriesType = label;
      if (!(Double.isNaN(num))) {
        seriesType = String.valueOf(num.intValue());
      }
      double[] series = new double[split.length - 1];
      for (int i = 1; i < split.length; i++) {
        series[i - 1] = Double.valueOf(split[i].trim()).doubleValue();
      }

      if (!res.containsKey(seriesType)) {
        res.put(seriesType, new ArrayList<double[]>());
      }

      res.get(seriesType).add(series);
    }

    br.close();
    return res;

  }

  private static Double parseValue(String string) {
    Double res = Double.NaN;
    try {
      Double r = Double.valueOf(string);
      res = r;
    }
    catch (NumberFormatException e) {
      assert true;
    }
    return res;
  }

}
