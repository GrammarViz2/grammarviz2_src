package net.seninp.grammarviz.cli;

import java.util.ArrayList;
import net.seninp.gi.GrammarRuleRecord;

public class GrammarStats {

  ArrayList<Integer> ruleLength = new ArrayList<Integer>();
  Integer minLength = Integer.MAX_VALUE;
  Integer maxLength = Integer.MIN_VALUE;

  ArrayList<Integer> ruleUse = new ArrayList<Integer>();
  Integer minUse = Integer.MAX_VALUE;
  Integer maxUse = Integer.MIN_VALUE;

  ArrayList<Integer> ruleFrequency = new ArrayList<Integer>();
  Integer minFrequency = Integer.MAX_VALUE;
  Integer maxFrequency = Integer.MIN_VALUE;

  public void process(GrammarRuleRecord ruleRecord) {

    if (0 == ruleRecord.getRuleNumber()) {
      return;
    }

    if (this.maxLength < ruleRecord.getMeanLength()) {
      this.maxLength = ruleRecord.getMeanLength();
    }

    if (this.minLength > ruleRecord.getMeanLength()) {
      this.minLength = ruleRecord.getMeanLength();
    }

    this.ruleLength.add(ruleRecord.getMeanLength());
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("GrammarStats [ruleLength=").append(ruleLength).append(", minLength=")
        .append(minLength).append(", maxLength=").append(maxLength).append("]");
    return builder.toString();
  }

}
