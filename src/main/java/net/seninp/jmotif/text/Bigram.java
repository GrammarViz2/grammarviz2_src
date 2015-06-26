package net.seninp.jmotif.text;

public class Bigram implements Comparable<Bigram> {

  private String firstWord;
  private String secondWord;

  public Bigram(String str1, String str2) {
    this.firstWord = str1;
    this.secondWord = str2;
  }

  public Bigram() {
    super();
  }

  public String concat() {
    if (null == firstWord) {
      return null;
    }
    else if (null == secondWord) {
      return this.firstWord + "-";
    }
    String res = firstWord.substring(0);
    res += secondWord;
    return res;
  }

  public void setNext(String str) {
    if (null == this.firstWord) {
      this.firstWord = str.substring(0);
    }
    else if (null == secondWord) {
      this.secondWord = str.substring(0);
    }
    else {
      throw new IndexOutOfBoundsException("all ngram slots are full: 1) '" + this.firstWord
          + "', 2) '" + this.secondWord + "'");
    }
  }

  public boolean isComplete() {
    return !((null == this.firstWord) || (null == this.secondWord));
  }

  @Override
  public int compareTo(Bigram o) {
    // TODO Auto-generated method stub
    return 0;
  }

  public int hashCode() {
    return this.firstWord.hashCode() + this.secondWord.hashCode();
  }

  public boolean equals(Object o) {
    if (o == null)
      return false;
    if (o == this)
      return true;
    if (o.getClass() != getClass())
      return false;

    return this.concat().equals(((Bigram) o).concat());
  }

  public String toString() {
    return concat();
  }
}
