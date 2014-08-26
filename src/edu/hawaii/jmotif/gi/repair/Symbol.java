package edu.hawaii.jmotif.gi.repair;

import edu.hawaii.jmotif.sax.parallel.SaxRecord;


public class Symbol {

  private String string;

  private Integer stringPosition;

  public Symbol() {
    super();
    this.stringPosition = null;
  }

  public Symbol(SaxRecord r, Integer stringPosition) {
    super();
    this.string = String.valueOf(r.getPayload());
    this.stringPosition = stringPosition;
  }

  public Symbol(String token, int stringPositionCounter) {
    super();
    this.string = token;
    this.stringPosition = stringPositionCounter;
  }

  public boolean isGuard() {
    return false;
  }

  public int getStringPosition() {
    return this.stringPosition;
  }

  public void setStringPosition(int saxStringPosition) {
    this.stringPosition = saxStringPosition;
  }

  public String toString() {
    return this.string;
  }

  public int getLevel() {
    return 0;
  }

}
