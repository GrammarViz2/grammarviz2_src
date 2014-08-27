package edu.hawaii.jmotif.sequitur.logic;


public class SequiturDiscordRecord {

  public int getStart() {
    return start;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public int getEnd() {
    return end;
  }

  public void setEnd(int end) {
    this.end = end;
  }

  private int start;
  private int end;

  public SequiturDiscordRecord(int start, int end) {
    this.start = start;
    this.end = end;
  }

}
