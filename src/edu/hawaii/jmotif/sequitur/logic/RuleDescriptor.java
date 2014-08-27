package edu.hawaii.jmotif.sequitur.logic;

public class RuleDescriptor {

  private int ruleIndex;
  private String ruleName;
  private String ruleString;
  private Integer ruleLength;
  private int ruleFrequency;

  public RuleDescriptor(int ruleIndex, String ruleName, String ruleString, Integer length,
      int ruleFrequency) {
    this.ruleIndex = ruleIndex;
    this.ruleName = ruleName;
    this.ruleString = ruleString;
    this.ruleLength = length;
    this.ruleFrequency = ruleFrequency;
  }

  /**
   * @return the ruleIndex
   */
  public int getRuleIndex() {
    return ruleIndex;
  }

  /**
   * @param ruleIndex the ruleIndex to set
   */
  public void setRuleIndex(int ruleIndex) {
    this.ruleIndex = ruleIndex;
  }

  /**
   * @return the ruleName
   */
  public String getRuleName() {
    return ruleName;
  }

  /**
   * @param ruleName the ruleName to set
   */
  public void setRuleName(String ruleName) {
    this.ruleName = ruleName;
  }

  /**
   * @return the ruleString
   */
  public String getRuleString() {
    return ruleString;
  }

  /**
   * @param ruleString the ruleString to set
   */
  public void setRuleString(String ruleString) {
    this.ruleString = ruleString;
  }

  /**
   * @return the ruleLength
   */
  public Integer getRuleLength() {
    return ruleLength;
  }

  /**
   * @param ruleLength the ruleLength to set
   */
  public void setRuleLength(Integer ruleLength) {
    this.ruleLength = ruleLength;
  }

  /**
   * @return the ruleFrequency
   */
  public int getRuleFrequency() {
    return ruleFrequency;
  }

  /**
   * @param ruleFrequency the ruleFrequency to set
   */
  public void setRuleFrequency(int ruleFrequency) {
    this.ruleFrequency = ruleFrequency;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ruleFrequency;
    result = prime * result + ruleIndex;
    result = prime * result + ((ruleLength == null) ? 0 : ruleLength.hashCode());
    result = prime * result + ((ruleName == null) ? 0 : ruleName.hashCode());
    result = prime * result + ((ruleString == null) ? 0 : ruleString.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof RuleDescriptor))
      return false;
    RuleDescriptor other = (RuleDescriptor) obj;
    if (ruleFrequency != other.ruleFrequency)
      return false;
    if (ruleIndex != other.ruleIndex)
      return false;
    if (ruleLength == null) {
      if (other.ruleLength != null)
        return false;
    }
    else if (!ruleLength.equals(other.ruleLength))
      return false;
    if (ruleName == null) {
      if (other.ruleName != null)
        return false;
    }
    else if (!ruleName.equals(other.ruleName))
      return false;
    if (ruleString == null) {
      if (other.ruleString != null)
        return false;
    }
    else if (!ruleString.equals(other.ruleString))
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "RuleDescriptor [ruleIndex=" + ruleIndex + ", ruleName=" + ruleName + ", ruleString="
        + ruleString + ", ruleLength=" + ruleLength + ", ruleFrequency=" + ruleFrequency + "]";
  }

}
