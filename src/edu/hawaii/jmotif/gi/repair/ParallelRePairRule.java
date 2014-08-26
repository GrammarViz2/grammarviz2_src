package edu.hawaii.jmotif.gi.repair;

import java.util.ArrayList;

public class ParallelRePairRule {

  /** This is static - the global rule enumerator counter. */
  protected ParallelGrammarKeeper grammarHandler;

  protected int ruleNumber;

  protected Symbol first;

  protected Symbol second;

  protected ArrayList<Integer> positions;

  protected String expandedRuleString;
  //
  // private int level;

  protected ParallelRePairGuard guard;

  public ParallelRePairRule(ParallelGrammarKeeper grammarHandler) {

    this.grammarHandler = grammarHandler;

    this.ruleNumber = grammarHandler.numRules.intValue();
    this.grammarHandler.numRules.incrementAndGet();

    this.grammarHandler.theRules.put(this.ruleNumber, this);

    this.positions = new ArrayList<Integer>();
  }

  public void setFirst(Symbol symbol) {
    this.first = symbol;
  }

  public void setSecond(Symbol symbol) {
    this.second = symbol;
  }

  public int getId() {
    return this.ruleNumber;
  }

  public String toString() {
    return "R" + this.ruleNumber;
  }

  public String toRuleString() {
    if (0 == this.ruleNumber) {
      return grammarHandler.r0String;
    }
    return this.first.toString() + " " + this.second.toString() + " ";
  }

  public void addPosition(int currentIndex) {
    this.positions.add(currentIndex);
  }

  public int[] getPositions() {
    int[] res = new int[this.positions.size()];
    for (int i = 0; i < this.positions.size(); i++) {
      res[i] = this.positions.get(i);
    }
    return res;
  }

  public void setExpandedRule(String trim) {
    this.expandedRuleString = trim;
  }

  // public void assignLevel() {
  // int lvl = Integer.MAX_VALUE;
  // lvl = Math.min(first.getLevel() + 1, lvl);
  // lvl = Math.min(second.getLevel() + 1, lvl);
  // this.level = lvl;
  // }
  //
  // public int getLevel() {
  // return this.level;
  // }

  public String getRuleName() {
    return "R" + this.ruleNumber;
  }

  public void setGuard(ParallelRePairGuard parallelRePairGuard) {
    this.guard = parallelRePairGuard;
  }

}
