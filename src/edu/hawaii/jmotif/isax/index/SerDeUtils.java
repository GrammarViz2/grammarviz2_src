package edu.hawaii.jmotif.isax.index;

import edu.hawaii.jmotif.timeseries.Timeseries;

/**
 * 
 * A set of static methods used for SerDe related processes.
 * 
 * @author jpatterson
 * 
 */
public class SerDeUtils {

  public static final int byteArrayToInt(byte[] b, int iByteArrayOffset) {
    return (b[iByteArrayOffset + 0] << 24) + ((b[iByteArrayOffset + 1] & 0xFF) << 16)
        + ((b[iByteArrayOffset + 2] & 0xFF) << 8) + (b[iByteArrayOffset + 3] & 0xFF);
  }

  /**
   * Converts a four byte sequence into an integer primitive.
   * 
   * @param b The input byte array.
   * @return Returns an integer value.
   */
  public static final int byteArrayToInt(byte[] b) {
    return (b[0] << 24) + ((b[1] & 0xFF) << 16) + ((b[2] & 0xFF) << 8) + (b[3] & 0xFF);
  }

  /**
   * Converts an integer primitive to a sequence of four bytes.
   * 
   * @param value
   * @return Returns a byte array representing a 32 bit integer.
   */
  public static byte[] intToByteArray(int value) {
    byte[] b = new byte[4];
    for (int i = 0; i < 4; i++) {
      int offset = (b.length - 1 - i) * 8;
      b[i] = (byte) ((value >>> offset) & 0xFF);
    }
    return b;
  }

  public static void writeIntIntoByteArray(int value, byte[] b, int b_offset) {
    // byte[] b = new byte[4];
    for (int i = 0; i < 4; i++) {
      int offset = (3 - i) * 8;
      b[b_offset + i] = (byte) ((value >>> offset) & 0xFF);
    }
    // return b;
  }

  public static int calcBytesForTS(Timeseries ts) {

    return 4 + (ts.size() * 8);

  }

}
