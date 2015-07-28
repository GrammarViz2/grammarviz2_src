package net.seninp.tinker;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;
import org.supercsv.io.CsvListReader;
import org.supercsv.prefs.CsvPreference;

public class ParamSampler {

  private static final String prefix = "/media/Stock/tmp/ydata-labeled-time-series-anomalies-v1_0/A1Benchmark/";
  private static final String fileExtension = ".csv";

  // the main runnable
  //
  public static void main(String[] args) throws Exception {

    File dir = new File(prefix);
    File[] filesList = dir.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith(fileExtension);
      }
    });

    for (File file : filesList) {
      if (file.isFile()) {

        System.out.println(file.getName());

        CsvListReader reader = new CsvListReader(new FileReader(file),
            CsvPreference.STANDARD_PREFERENCE);

        String[] header = reader.getHeader(true);
        List<String> content = reader.read();
        System.out.println(Arrays.toString(header) + " read " + content.size() + " lines");

        reader.close();
      }
    }

  }

}
