package edu.hawaii.jmotif.gi.repair;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;
import edu.hawaii.jmotif.gi.GrammarRuleRecord;
import edu.hawaii.jmotif.gi.GrammarRules;
import edu.hawaii.jmotif.logic.RuleInterval;
import edu.hawaii.jmotif.sax.datastructures.SAXRecords;

public class RePairRule {

  /** This is static - the global rule enumerator counter. */
  protected static AtomicInteger numRules = new AtomicInteger(1);

  protected static Hashtable<Integer, RePairRule> theRules = new Hashtable<Integer, RePairRule>();

  protected static String r0String;

  private static String r0ExpandedString;

  private int ruleNumber;

  protected Symbol first;

  protected Symbol second;

  protected ArrayList<Integer> positions;

  protected ArrayList<RuleInterval> ruleIntervals;

  private String expandedRuleString;

  private int level;

  public RePairRule() {
    // assign a next number to this rule and increment the global counter
    this.ruleNumber = numRules.intValue();
    numRules.incrementAndGet();

    theRules.put(this.ruleNumber, this);

    this.positions = new ArrayList<Integer>();
    this.ruleIntervals = new ArrayList<RuleInterval>();
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
    if (00 == this.ruleNumber) {
      return r0String;
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

  public static void expandRules() {

    // iterate over all SAX containers
    for (int currentPositionIndex = 1; currentPositionIndex < theRules.size(); currentPositionIndex++) {

      RePairRule rr = theRules.get(currentPositionIndex);
      String resultString = rr.toRuleString();

      int currentSearchStart = resultString.indexOf("R");
      while (currentSearchStart >= 0) {

        int spaceIdx = resultString.indexOf(" ", currentSearchStart);

        String ruleName = resultString.substring(currentSearchStart, spaceIdx + 1);
        Integer ruleId = Integer.valueOf(ruleName.substring(1, ruleName.length() - 1));

        RePairRule rule = theRules.get(ruleId);
        if (rule != null) {
          if (rule.expandedRuleString.charAt(rule.expandedRuleString.length() - 1) == ' ') {
            resultString = resultString.replaceAll(ruleName, rule.expandedRuleString);
          }
          else {
            resultString = resultString.replaceAll(ruleName, rule.expandedRuleString + " ");
          }
        }

        currentSearchStart = resultString.indexOf("R", spaceIdx);
      }

      rr.setExpandedRule(resultString.trim());

    }

    // and the r0, String is immutable in Java
    //
    String resultString = r0String;

    int currentSearchStart = resultString.indexOf("R");
    while (currentSearchStart >= 0) {
      int spaceIdx = resultString.indexOf(" ", currentSearchStart);
      String ruleName = resultString.substring(currentSearchStart, spaceIdx + 1);
      Integer ruleId = Integer.valueOf(ruleName.substring(1, ruleName.length() - 1));
      RePairRule rule = theRules.get(ruleId);
      if (rule != null) {
        if (rule.expandedRuleString.charAt(rule.expandedRuleString.length() - 1) == ' ') {
          resultString = resultString.replaceAll(ruleName, rule.expandedRuleString);
        }
        else {
          resultString = resultString.replaceAll(ruleName, rule.expandedRuleString + " ");
        }
      }
      currentSearchStart = resultString.indexOf("R", spaceIdx);
    }
    r0ExpandedString = resultString;

  }

  public static String recoverString() {
    return r0ExpandedString;
  }

  private void setExpandedRule(String trim) {
    this.expandedRuleString = trim;
  }

  public static void buildIntervals(SAXRecords records, double[] originalTimeSeries,
      int slidingWindowSize) {
    records.buildIndex();
    for (int currentPositionIndex = 1; currentPositionIndex < theRules.size(); currentPositionIndex++) {
      RePairRule rr = theRules.get(currentPositionIndex);
      // System.out.println("R" + rr.ruleNumber + ", " + rr.toRuleString() + ", "
      // + rr.expandedRuleString);
      String[] split = rr.expandedRuleString.split(" ");
      for (int pos : rr.getPositions()) {
        Integer p2 = records.mapStringIndexToTSPosition(pos + split.length - 1);
        if (null == p2) {
          rr.ruleIntervals.add(new RuleInterval(records.mapStringIndexToTSPosition(pos),
              originalTimeSeries.length));
        }
        else {
          rr.ruleIntervals.add(new RuleInterval(records.mapStringIndexToTSPosition(pos), records
              .mapStringIndexToTSPosition(pos + split.length - 1) + slidingWindowSize));
        }
      }
    }
  }

  public ArrayList<RuleInterval> getRuleIntervals() {
    return this.ruleIntervals;
  }

  public static void setRuleString(String stringToDisplay) {
    r0String = stringToDisplay;
  }

  public static GrammarRules toGrammarRulesData() {

    GrammarRules res = new GrammarRules();

    GrammarRuleRecord r0 = new GrammarRuleRecord();
    r0.setRuleNumber(0);
    r0.setRuleString(theRules.get(0).toRuleString());
    r0.setExpandedRuleString(theRules.get(0).expandedRuleString);
    r0.setOccurrences(new int[1]);
    res.addRule(r0);

    for (RePairRule rule : theRules.values()) {

      GrammarRuleRecord rec = new GrammarRuleRecord();

      rec.setRuleNumber(rule.ruleNumber);
      rec.setRuleString(rule.toRuleString());
      rec.setExpandedRuleString(rule.expandedRuleString);
      rec.setRuleYield(countSpaces(rule.expandedRuleString));
      rec.setOccurrences(rule.getPositions());
      rec.setRuleIntervals(rule.getRuleIntervals());
      rec.setRuleLevel(rule.getLevel());
      rec.setMinMaxLength(rule.getLengths());
      rec.setMeanLength(mean(rule.getRuleIntervals()));

      res.addRule(rec);
    }

    return res;
  }

  public void assignLevel() {
    int lvl = Integer.MAX_VALUE;
    lvl = Math.min(first.getLevel() + 1, lvl);
    lvl = Math.min(second.getLevel() + 1, lvl);
    this.level = lvl;
  }

  public int getLevel() {
    return this.level;
  }

  private int[] getLengths() {
    if (this.ruleIntervals.isEmpty()) {
      return new int[1];
    }
    int[] res = new int[this.ruleIntervals.size()];
    int count = 0;
    for (RuleInterval ri : this.ruleIntervals) {
      res[count] = ri.getEndPos() - ri.getStartPos();
      count++;
    }
    return res;
  }

  private static int countSpaces(String str) {
    if (null == str) {
      return -1;
    }
    int counter = 0;
    for (int i = 0; i < str.length(); i++) {
      if (str.charAt(i) == ' ') {
        counter++;
      }
    }
    return counter;
  }

  private static int mean(ArrayList<RuleInterval> arrayList) {
    if (null == arrayList || arrayList.isEmpty()) {
      return 0;
    }
    int res = 0;
    int count = 0;
    for (RuleInterval ri : arrayList) {
      res = res + (ri.getEndPos() - ri.getStartPos());
      count++;
    }
    return res / count;
  }

  /**
   * Get all the rules as the map.
   * 
   * @return all the rules.
   */
  public Hashtable<Integer, RePairRule> getRules() {
    return theRules;
  }

  public static String toGrammarRules() {
    StringBuffer sb = new StringBuffer();
    System.out.println("R0 -> " + r0String);
    for (int i = 1; i < theRules.size(); i++) {
      RePairRule r = theRules.get(i);
      sb.append("R").append(r.ruleNumber).append(" -> ").append(r.toRuleString()).append(" : ")
          .append(r.expandedRuleString).append(", ").append(r.positions).append("\n");
    }
    return sb.toString();
  }
}
