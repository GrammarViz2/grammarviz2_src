package edu.hawaii.jmotif.sampler;

import java.util.ArrayList;
import java.util.TreeMap;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import edu.hawaii.jmotif.text.SAXCollectionStrategy;

public class PrintConsumer implements Consumer<ValuePointListTelemetryColored> {

  private static final String COMMA = ",";
  private static final Level LOGGING_LEVEL = Level.DEBUG;
  private static Logger consoleLogger;

  private String prefix;
  private int callNumber = -1;
  private ArrayList<TreeMap<String, Double>> points = new ArrayList<TreeMap<String, Double>>();

  // static block - we instantiate the logger
  //
  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(PrintConsumer.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  public PrintConsumer(SAXCollectionStrategy strategy) {
    super();
    prefix = "NOREDUCTION";
    if (strategy.equals(SAXCollectionStrategy.CLASSIC)) {
      prefix = "CLASSIC";
    }
    else if (strategy.equals(SAXCollectionStrategy.EXACT)) {
      prefix = "EXACT";
    }
  }

  @Override
  public void notifyOf(Producer<? extends ValuePointListTelemetryColored> producer) {

    callNumber++;
    TreeMap<String, Double> cMap = new TreeMap<String, Double>();
    points.add(cMap);

    ValuePointListTelemetryColored val = producer.getValue();
    int size = val.getValue().size();
    for (int i = 0; i < size; i++) {
      double error = val.getValue().get(i).getValue();
      double[] coordinates = val.getValue().get(i).getPoint().toArray();
      String coordStr = prefix + COMMA + coordinates[0] + COMMA + coordinates[1] + COMMA
          + coordinates[2];
      cMap.put(coordStr, error);
      if (callNumber > 0) {
        if (!(points.get(callNumber - 1).containsKey(coordStr))) {
          consoleLogger.info(callNumber + COMMA + coordStr + COMMA + error);
        }
      }
      else {
        consoleLogger.info(callNumber + COMMA + coordStr + COMMA + error);
      }

    }

  }
}
