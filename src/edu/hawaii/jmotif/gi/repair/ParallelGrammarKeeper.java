package edu.hawaii.jmotif.gi.repair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This implements a handler for the Re-Pair grammar built in parallel. This data structure is
 * responsible for enumerating rules and for tracking changes in the R0 of the grammar.
 * 
 * @author psenin
 * 
 */
public class ParallelGrammarKeeper {

  private static final char SPACE = ' ';
  private static final char THE_R = 'R';

  // rule 0 gets a separate treatment, so we start from 1
  //
  protected AtomicInteger numRules = new AtomicInteger(1);

  // the rules table
  protected Hashtable<Integer, ParallelRePairRule> theRules = new Hashtable<Integer, ParallelRePairRule>();

  // the grammar id
  private long id;

  // R0 strings
  //
  protected String r0String;
  protected String r0ExpandedString;

  // keeps a working string of this grammar
  //
  protected ArrayList<Symbol> workString;

  /**
   * Constructor.
   * 
   * @param id The handler id.
   */
  public ParallelGrammarKeeper(long id) {
    super();
    this.id = id;
  }

  /**
   * This adds an existing rule to this grammar. Useful in merging.
   * 
   * @param r The rule. It is not yet clear how to treat rules, be careful. This will not set the
   * rule number, but it will increment the internal rule counter.
   */
  public void addExistingRule(ParallelRePairRule r) {
    r.grammarHandler = this;
    if (this.theRules.containsKey(r.ruleNumber)) {
      // we do override an existing rule
      theRules.put(r.ruleNumber, r);
    }
    else {
      // plus 1 because the rule 0 has a special treatment
      theRules.put(r.ruleNumber, r);
      numRules.set(theRules.size() + 1);
    }
  }

  /**
   * Expands rules.
   */
  public void expandRules() {
    // iterate over all SAX containers
    ArrayList<Integer> keys = new ArrayList<Integer>(theRules.keySet());
    Collections.sort(keys);
    for (Integer key : keys) {
      ParallelRePairRule rr = theRules.get(key);
      String resultString = rr.toRuleString();

      int currentSearchStart = resultString.indexOf(THE_R);
      while (currentSearchStart >= 0) {
        int spaceIdx = resultString.indexOf(" ", currentSearchStart);
        // if (spaceIdx < 0) {
        // System.out.println("gotcha!");
        // }
        String ruleName = resultString.substring(currentSearchStart, spaceIdx + 1);
        Integer ruleId = Integer.valueOf(ruleName.substring(1, ruleName.length() - 1));

        ParallelRePairRule rule = theRules.get(ruleId);
        if (rule != null) {
          if (rule.expandedRuleString.charAt(rule.expandedRuleString.length() - 1) == ' ') {
            resultString = resultString.replaceAll(ruleName, rule.expandedRuleString);
          }
          else {
            resultString = resultString.replaceAll(ruleName, rule.expandedRuleString + SPACE);
          }
        }

        currentSearchStart = resultString.indexOf(THE_R, spaceIdx);
      }

      rr.setExpandedRule(resultString.trim());

    }
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getId() {
    return this.id;
  }

  public void setR0String(String string) {
    this.r0String = string;
  }

  public void setR0ExpandedString(String string) {
    this.r0ExpandedString = string;
  }

  public void expandR0() {
    // string is immutable it will get copied
    String finalString = this.r0String;
    int currentSearchStart = finalString.indexOf(THE_R);
    while (currentSearchStart >= 0) {

      int spaceIdx = finalString.indexOf(" ", currentSearchStart + 1);

      String ruleName = finalString.substring(currentSearchStart, spaceIdx + 1);
      Integer ruleId = Integer.valueOf(ruleName.substring(1, ruleName.length() - 1));

      ParallelRePairRule rr = theRules.get(ruleId);
      if (null == rr.expandedRuleString) {
        finalString = finalString.replaceAll(ruleName, theRules.get(ruleId).toRuleString());
      }
      else {
        finalString = finalString.replaceAll(ruleName, theRules.get(ruleId).expandedRuleString
            + SPACE);
      }

      currentSearchStart = finalString.indexOf(THE_R);
    }
    this.r0ExpandedString = finalString;
  }

  public void setWorkString(ArrayList<Symbol> string) {
    this.workString = string;
  }

  public String toGrammarRules() {
    StringBuffer sb = new StringBuffer();
    System.out.println("R0 -> " + r0String);
    for (int i = 1; i < theRules.size(); i++) {
      ParallelRePairRule r = theRules.get(i);
      sb.append("R").append(r.ruleNumber).append(" -> ").append(r.toRuleString()).append(" : ")
          .append(r.expandedRuleString).append(", ").append(r.positions).append("\n");
    }
    return sb.toString();
  }

}
