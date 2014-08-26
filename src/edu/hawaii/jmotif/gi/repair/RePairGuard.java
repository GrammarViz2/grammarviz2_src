package edu.hawaii.jmotif.gi.repair;


public class RePairGuard extends Symbol {

  protected RePairRule rule;

  public RePairGuard(RePairRule rule) {
    super();
    this.rule = rule;
  }

  public String toString() {
    return this.rule.toString();
  }

  public boolean isGuard() {
    return true;
  }

  public int getLevel() {
    return rule.getLevel();
  }

}
