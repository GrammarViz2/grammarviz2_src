package net.seninp.jmotif.util;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Tests the Stack Trace class.
 * 
 * @author Philip Johnson
 */
public class TestStackTrace {

  /**
   * Tests the Stack Tracing. Generates an exception, makes the Stack Trace, and checks to see if it
   * seems OK.
   */
  @Test
  public void testStackTrace() {
    String trace;
    try {
      throw new Exception("Test Exception");
    }
    catch (Exception e) {
      trace = StackTrace.toString(e);
    }
    assertTrue("Check trace", trace.startsWith("java.lang.Exception"));
  }

}