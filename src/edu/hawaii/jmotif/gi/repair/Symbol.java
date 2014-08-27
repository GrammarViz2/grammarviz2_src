package edu.hawaii.jmotif.gi.repair;

import java.util.Arrays;
import edu.hawaii.jmotif.sax.datastructures.SaxRecord;

/**
 * The symbol class.
 * 
 * @author psenin
 * 
 */
public class Symbol {

  /**
   * Payload.
   */
  private char[] string;

  /**
   * Position of the symbol in the string.
   */
  private Integer stringPosition;

  /**
   * Constructor.
   */
  public Symbol() {
    super();
    this.stringPosition = null;
  }

  /**
   * Constructor.
   * 
   * @param r the SAX record to use for the symbol construction.
   * @param stringPosition the position of the symbol in the string.
   */
  public Symbol(SaxRecord r, Integer stringPosition) {
    super();
    this.string = Arrays.copyOf(r.getPayload(), r.getPayload().length);
    this.stringPosition = stringPosition;
  }

  /**
   * Constructor.
   * 
   * @param token the payload.
   * @param stringPosition the position of the symbol in the string.
   */
  public Symbol(String token, int stringPosition) {
    super();
    this.string = token.toCharArray();
    this.stringPosition = stringPosition;
  }

  /**
   * This is overridden in Guard.
   * 
   * @return true if the symbol is the guard.
   */
  public boolean isGuard() {
    return false;
  }

  /**
   * The position getter.
   * 
   * @return The symbol position in the string.
   */
  public int getStringPosition() {
    return this.stringPosition;
  }

  /**
   * The position setter.
   * 
   * @param saxStringPosition the position to set.
   */
  public void setStringPosition(int saxStringPosition) {
    this.stringPosition = saxStringPosition;
  }

  public String toString() {
    return String.valueOf(this.string);
  }

  /**
   * This will be overridden in the non-Terminal symbol, i.e. guard.
   * 
   * @return The rule hierarchy level.
   */
  public int getLevel() {
    return 0;
  }

}
