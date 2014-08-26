package edu.hawaii.jmotif.gi.repair;


public class ParallelRePairGuard extends Symbol {

  protected ParallelRePairRule rule;

  public ParallelRePairGuard(ParallelRePairRule r) {
    super();
    this.rule = r;
    r.setGuard(this);
  }

  public String toString() {
    return this.rule.toString();
  }

  public boolean isGuard() {
    return true;
  }

}
