package edu.hawaii.jmotif.experimentation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.hawaii.jmotif.util.StackTrace;

public class ExperimentUtils {

  final static Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  private static final Level LOGGING_LEVEL = Level.INFO;
  private static Logger consoleLogger;

  // static block - we instantiate the logger
  //
  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(MovieMaker.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  /**
   * This reads the data
   * 
   * @param fname The filename.
   * @return
   */
  public static double[] loadData(String fname) {

    consoleLogger.info("reading from " + fname);

    long lineCounter = 0;
    double ts[] = new double[1];

    Path path = Paths.get(fname);

    ArrayList<Double> data = new ArrayList<Double>();

    try {

      BufferedReader reader = Files.newBufferedReader(path, DEFAULT_CHARSET);

      String line = null;
      while ((line = reader.readLine()) != null) {
        String[] lineSplit = line.trim().split("\\s+");
        for (int i = 0; i < lineSplit.length; i++) {
          double value = new BigDecimal(lineSplit[i]).doubleValue();
          data.add(value);
        }
        lineCounter++;
      }
      reader.close();
    }
    catch (Exception e) {
      System.err.println(StackTrace.toString(e));
    }
    finally {
      assert true;
    }

    if (!(data.isEmpty())) {
      ts = new double[data.size()];
      for (int i = 0; i < data.size(); i++) {
        ts[i] = data.get(i);
      }
    }

    consoleLogger.info("loaded " + data.size() + " points from " + lineCounter + " lines in "
        + fname);
    return ts;

  }

  public static void saveColumn(int[] density, String filename) throws IOException {
    BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filename)));
    for (int n : density) {
      bw.write(Integer.valueOf(n).toString() + "\n");
    }
    bw.close();
  }
}
