package net.seninp.jmotif.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Provides a simple solution to the common problem of obtaining a String containing the stack trace
 * produced by an exception.
 * <p>
 * Call StackTrace.toString(e) to get the string corresponding to the Exception e.
 * 
 * @author Philip Johnson, Takuya Yamashita
 */
public final class StackTrace {

  /** Disable public constructor. */
  private StackTrace() {
    // do nothing
  }

  /**
   * Converts the Throwable.getStackTrace to a String representation for logging.
   * 
   * @param throwable The Throwable exception.
   * @return A String containing the StackTrace.
   */
  public static String toString(Throwable throwable) {
    StringWriter stringWriter = new StringWriter();
    throwable.printStackTrace(new PrintWriter(stringWriter));
    return stringWriter.toString();
  }
}