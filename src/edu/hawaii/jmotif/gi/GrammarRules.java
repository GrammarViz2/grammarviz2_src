package edu.hawaii.jmotif.gi;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

public class GrammarRules implements Iterable<GrammarRuleRecord> {

  private SortedMap<Integer, GrammarRuleRecord> rules;

  public GrammarRules() {
    super();
    this.rules = new TreeMap<Integer, GrammarRuleRecord>();
  }

  public void addRule(GrammarRuleRecord arrRule) {
    int key = arrRule.getRuleNumber();
    this.rules.put(key, arrRule);
  }

  public GrammarRuleRecord getRuleRecord(Integer ruleIdx) {
    return this.rules.get(ruleIdx);
  }

  @Override
  public Iterator<GrammarRuleRecord> iterator() {
    return rules.values().iterator();
  }

  public GrammarRuleRecord get(Integer ruleIndex) {
    return rules.get(ruleIndex);
  }

  public int size() {
    return this.rules.size();
  }

}
