package net.seninp.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.seninp.gi.logic.RuleInterval;

/**
 * Implements few things for IO.
 * 
 * @author psenin
 *
 */
public class SAXFileIOHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(SAXFileIOHelper.class);

  /**
   * Delete a file.
   * 
   * @param path the path to the file.
   * @param fileName the filename.
   */
  public static void deleteFile(String path, String fileName) {

    String fullPath = path + File.separator + fileName;

    try {
      File file = new File(fullPath);
      if (file.exists()) {
        file.delete();
      }

    }
    catch (Exception e) {
      LOGGER.error("error while deleting file {}", fullPath, e);
    }
  }

  /**
   * Saves a time series.
   * 
   * @param path the file path.
   * @param fileName the file name.
   * @param positionFileName to be specified.
   * @param data to be specified.
   * @param subMotifs to be specified.
   */
  public static void writeFileXYSeries(String path, String fileName, String positionFileName,
      XYSeriesCollection data, ArrayList<RuleInterval> subMotifs) {
    StringBuffer s = new StringBuffer();
    StringBuffer relatedPositionS = new StringBuffer();
    Boolean isTrunk = false;

    if (isTrunk) {
      fileName = "(t)" + fileName;
      positionFileName = "(t)" + positionFileName;
    }

    String fullPath = path + fileName;
    String dirPath = path;

    String positionFullPath = path + positionFileName;

    try {
      deleteFile(path, fileName);
      deleteFile(path, positionFileName);

      File file = new File(fullPath);
      File filePosition = new File(positionFullPath);

      File dirFile = new File(dirPath);
      if (!(dirFile.isDirectory())) {
        dirFile.mkdirs();
      }

      if (!(file.exists())) {
        file.createNewFile();
      }
      if (!(filePosition.exists())) {
        filePosition.createNewFile();
      }

      // --- chunk the motifs to the same length with the minimal length
      // of one motif------------
      if (isTrunk) {
        int minLength = 10000;
        for (int series = 0; series < data.getSeriesCount(); series++) {
          XYSeries dataset = data.getSeries(series);
          minLength = minLength > dataset.getItemCount() ? dataset.getItemCount() : minLength;
        }

        for (int series = 0; series < data.getSeriesCount(); series++) {
          XYSeries dataset = data.getSeries(series);
          for (int i = 0; i < minLength; i++) {
            s.append(dataset.getDataItem(i).getYValue() + ",");
          }
          s.append("\n");
        }
        for (RuleInterval pos : subMotifs) {
          relatedPositionS.append(pos.getStart() + ", " + pos.getEnd() + "\n");
        }
      }
      // --- End of
      // chunking---------------------------------------------------------------------

      // Get the String with different length to write.
      else {
        for (int series = 0; series < data.getSeriesCount(); series++) {
          XYSeries dataset = data.getSeries(series);
          for (int i = 0; i < dataset.getItemCount(); i++) {
            s.append(dataset.getDataItem(i).getYValue() + ",");
          }
          s.append("\n");
        }
        for (RuleInterval pos : subMotifs) {
          relatedPositionS.append(pos.getStart() + ", " + pos.getEnd() + "\n");
        }
      }

      try (BufferedWriter output = new BufferedWriter(new FileWriter(file))) {
        output.write(s.toString());
      }
      LOGGER.info("written to file: {}", file.getAbsolutePath());

      try (BufferedWriter outputPosition = new BufferedWriter(new FileWriter(filePosition))) {
        outputPosition.write(relatedPositionS.toString());
      }
      LOGGER.info("written to file: {}", filePosition.getAbsolutePath());
    }
    catch (Exception e) {
      LOGGER.error("error while writing XY series to {}", fullPath, e);
    }

  }

  /**
   * Saves the String's content into the file.
   * 
   * @param path the path to the file.
   * @param fileName the filename.
   * @param content the content.
   */
  public static void writeFile(String path, String fileName, String content) {

    String s = new String();

    String fullPath = path + File.separator + fileName;
    String dirPath = path;

    try {
      File file = new File(fullPath);
      File dirFile = new File(dirPath);
      if (!(dirFile.isDirectory())) {
        dirFile.mkdirs();
      }

      if (!(file.exists())) {
        file.createNewFile();
      }

      int count = 0;
      String s1 = "";
      try (BufferedReader input = new BufferedReader(new FileReader(file))) {
        while ((s = input.readLine()) != null) {
          s1 += s + "\n";
          count++;
        }
      }

      if (count <= 32) {
        s1 += content;
      }
      try (BufferedWriter output = new BufferedWriter(new FileWriter(file))) {
        output.write(s1);
      }
      LOGGER.info("written to file: {}", file.getAbsolutePath());
    }
    catch (Exception e) {
      LOGGER.error("error while writing to file {}", fullPath, e);
    }
  }
}
