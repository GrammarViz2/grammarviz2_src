package net.seninp.tinker;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class SamplingSorter {

  private static final String prefix = "/media/Stock/tmp/ydata-labeled-time-series-anomalies-v1_0/A2Benchmark/";
  private static final String fileExtension = ".out";

  private static final Logger consoleLogger;
  private static final Level LOGGING_LEVEL = Level.INFO;

  // static block - we instantiate the logger
  //
  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(SamplingSorter.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  // the main runnable
  //
  public static void main(String[] args) throws Exception {

    File dir = new File(prefix);
    File[] filesList = dir.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith(fileExtension);
      }
    });

    List<String> samplerBatch = new ArrayList<String>();

    // this runs for each file
    //
    for (File file : filesList) {
      if (file.isFile()) {

        // get the file reader set up
        //
        consoleLogger.info("processing " + file.getName());
        CsvListReader reader = new CsvListReader(new FileReader(file),
            CsvPreference.STANDARD_PREFERENCE);
        final String[] header = reader.getHeader(true);
        consoleLogger.info(" file header: " + Arrays.toString(header));

        // setup data keepers
        //
        List<SamplerRecord> values = new ArrayList<SamplerRecord>();

        // setup the processor and read the data
        //
        List<String> record;
        while ((record = reader.read()) != null) {
          SamplerRecord value = new SamplerRecord(record);
          values.add(value);
        }
        reader.close();

        // filter the data
        //
        List<SamplerRecord> cleanValues = cleanValuesByCoverage(values, 0.99);

        // sort the data
        //
        Collections.sort(cleanValues, new Comparator<SamplerRecord>() {
          @Override
          public int compare(SamplerRecord o1, SamplerRecord o2) {
            return Double.valueOf(o1.reduction).compareTo(Double.valueOf(o2.reduction));
          }
        });

        consoleLogger.info(cleanValues.get(0).toString());

        // // print the top
        // //
        // sort the data
        // String samplerInputFilename = file.getName().concat(".column");
        // BufferedWriter bw = new BufferedWriter(new FileWriter(new File(prefix
        // + samplerInputFilename)));
        // for (Double v : values) {
        // bw.write(v + "\n");
        // }
        // bw.close();
        //
        // // set boundaries string
        // //
        // StringBuffer samplingBoundaries = new StringBuffer("10 ");
        // if (values.size() < 3000) {
        // samplingBoundaries.append(Integer.valueOf(values.size() / 10).toString());
        // }
        // else {
        // samplingBoundaries.append("300 ");
        // }
        // samplingBoundaries.append(" 10 2 20 1 2 10 1");
        //
        // // makeup the sampler command
        // //
        // StringBuffer samplerCommand = new StringBuffer(
        // "java -Xmx4G -cp \"jmotif-gi-0.3.1-SNAPSHOT-jar-with-dependencies.jar\"");
        // samplerCommand.append(" net.seninp.gi.rulepruner.RulePrunerPrinter ");
        //
        // samplerCommand.append(" -d ");
        // samplerCommand.append(samplerInputFilename);
        //
        // samplerCommand.append(" -b \"");
        // samplerCommand.append(samplingBoundaries.toString()).append("\"");
        //
        // samplerCommand.append(" -o ");
        // samplerCommand.append(samplerInputFilename).append(".out");
        //
        // samplerBatch.add(samplerCommand.toString());

      }
    }

    for (String line : samplerBatch) {
      System.out.println(line);
    }

  }

  // /**
  // * Sets up the processors.
  // *
  // * @return the cell processors
  // */
  // private static CellProcessor[] getProcessors() {
  //
  // final CellProcessor[] processors = new CellProcessor[] { new ParseDouble(), // timestamp
  // new ParseDouble(), // value
  // new ParseInt() // anomaly flag
  // };
  //
  // return processors;
  // }

  private static List<SamplerRecord> cleanValuesByCoverage(List<SamplerRecord> values,
      double threshold) {
    List<SamplerRecord> res = new ArrayList<SamplerRecord>();
    for (SamplerRecord v : values) {
      if (v.coverage >= threshold) {
        res.add(v);
      }
    }
    return res;
  }

}
