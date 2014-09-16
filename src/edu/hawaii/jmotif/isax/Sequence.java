package edu.hawaii.jmotif.isax;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import edu.hawaii.jmotif.isax.index.SerDeUtils;
import edu.hawaii.jmotif.sax.SAXException;
import edu.hawaii.jmotif.sax.alphabet.NormalAlphabet;
import edu.hawaii.jmotif.timeseries.TSException;

/**
 * 
 * TODO: - what happens if the two iSAX words are of different lengths? - how does the original
 * jmotif handle this? - SAXFactory.saxMinDist
 * 
 * - how does the base dimensionality of the word factor in here?
 * 
 * Represents a sequence of iSAX "Symbols". With regular SAX, the whole word or sequence had a
 * cardinality associated with it. With iSAX each individual character has an associated
 * cardinality.
 * 
 * @author Josh Patterson
 */
public class Sequence implements Iterable<Symbol>, Cloneable {

  private int orig_length = 0;

  private Vector<Symbol> symbols = new Vector<Symbol>();

  /**
   * Constructor.
   * 
   * @param len The length.
   */
  public Sequence(int len) {
    this.orig_length = len;
  }

  /**
   * Provides access to internal storage.
   * 
   * @return The vector of sequence symbols.
   */
  public Vector<Symbol> getSymbols() {
    return this.symbols;
  }

  /**
   * Get the length.
   * 
   * @return The length.
   */
  public int getOrigLength() {
    return this.orig_length;
  }

  public void setOrigLength(int len) {
    this.orig_length = len;
  }

  /**
   * Clones the iSAX sequence
   */
  public Sequence clone() throws CloneNotSupportedException {

    Sequence new_Sequence = new Sequence(this.orig_length);

    for (int i = 0; i < this.symbols.size(); i++) {
      new_Sequence.symbols.add(this.symbols.get(i).clone());
    }

    return new_Sequence;

  }

  /**
   * Clone another iSAX sequence, source, and uses this object as the storage.
   * 
   * @param source the target source of the iSAX representation.
   */
  public void clone(Sequence source) {

    this.orig_length = source.getOrigLength();
    this.symbols = new Vector<Symbol>();

    for (int i = 0; i < source.symbols.size(); i++) {
      try {
        this.symbols.add(source.symbols.get(i).clone());
      }
      catch (CloneNotSupportedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

  }

  /**
   * Calculates SAX euclidean distance between two iSAX sequences.
   * 
   * @param other the other iSAX sequence to compare against.
   * @return returns the distance as a double.
   * @throws ISAXException
   */
  public double sax_distance(Sequence other) throws SAXException {

    NormalAlphabet alphabet = new NormalAlphabet();
    double sqd_dist = 0;

    for (int x = 0; x < this.symbols.size(); x++) {

      Symbol a = new Symbol(0, 0);
      Symbol b = new Symbol(0, 0);

      try {
        Symbol.PerformPromotion(this.symbols.get(x), other.symbols.get(x), a, b);
      }
      catch (SAXException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      double[][] distanceMatrix = { { 0 }, { 0 } };

      try {
        // we have to pull a new distance table per symbol
        distanceMatrix = alphabet.getDistanceMatrix(a.cardinality);
      }
      catch (TSException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      double localDist = distanceMatrix[a.saxCharacter][b.saxCharacter];

      // System.out.println("Sequence > sax_distance() == " + localDist);

      sqd_dist = localDist;

    }

    return sqd_dist;

  }

  /**
   * Calculates the iSAX distance function MINDIST based on the equations in the iSAX paper.
   * 
   * @param other The other sequence to compare against.
   * @return returns the distance as a double.
   * @throws SAXException
   */
  public double MINDIST(Sequence other) throws SAXException {

    NormalAlphabet alphabet = new NormalAlphabet();

    double sqd_dist = 0;

    for (int x = 0; x < this.symbols.size(); x++) {

      Symbol a = new Symbol(0, 0);
      Symbol b = new Symbol(0, 0);

      try {
        Symbol.PerformPromotion(this.symbols.get(x), other.symbols.get(x), a, b);
      }
      catch (SAXException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      double[][] distanceMatrix = { { 0 }, { 0 } };

      try {
        // we have to pull a new distance table per symbol
        distanceMatrix = alphabet.getDistanceMatrix(a.cardinality);
      }
      catch (TSException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      double localDist = distanceMatrix[a.saxCharacter][b.saxCharacter];
      sqd_dist += (localDist * localDist);

    }

    // get the sq root of the sum of the squares
    double dist_sqrt = Math.sqrt(sqd_dist);

    // calculate the coefficient
    int original_ts_length = this.orig_length;
    int derived_sax_length = this.symbols.size(); // length of sax Sequence

    double coef = Math.sqrt(original_ts_length / derived_sax_length);

    // now complete the min-dist calculation
    return dist_sqrt * coef;

  }

  /**
   * Takes 2 iSAX sequences and promotes each symbol to the higher cardinality.
   * 
   * @param w1_in An input iSAX sequence.
   * @param w2_in The other input iSAX sequence.
   * @param w1_out Storage for the output based on w1_in
   * @param w2_out Storage for the output based on w2_in
   */
  public static void PerformPromotion(Sequence w1_in, Sequence w2_in, Sequence w1_out,
      Sequence w2_out) {

    for (int x = 0; x < w1_in.symbols.size(); x++) {

      Symbol a = new Symbol(0, 0);
      Symbol b = new Symbol(0, 0);

      try {
        Symbol.PerformPromotion(w1_in.symbols.get(x), w2_in.symbols.get(x), a, b);
      }
      catch (SAXException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      w1_out.symbols.add(a);
      w2_out.symbols.add(b);

    }

  }

  /**
   * Extracts the cardinalities of each symbol
   * 
   * @return ArrayList of cardinalities
   */
  public ArrayList<Integer> getCardinalities() {

    ArrayList<Integer> arCards = new ArrayList<Integer>();

    for (int x = 0; x < this.symbols.size(); x++) {

      arCards.add(this.symbols.get(x).cardinality);

    }

    return arCards;

  }

  /**
   * Scans through the symbols in the sequence and returns the highest cardinality in the sequence.
   * 
   * @return Integer representing high cardinality in the sequence.
   */
  public int getHighestCardinality() {

    int iHighCard = 0;

    for (int x = 0; x < this.symbols.size(); x++) {

      if (this.symbols.get(x).cardinality > iHighCard) {
        iHighCard = this.symbols.get(x).cardinality;
      }

    }

    return iHighCard;

  }

  /**
   * Generates a label for the iSAX sequence. To be used in the iSAX indexing mechanics.
   * 
   * @return String representation of the sequence.
   */
  public String getIndexHash() {

    String strName = "";

    for (int x = 0; x < this.symbols.size(); x++) {

      strName += "" + this.symbols.get(x).saxCharacter + "." + this.symbols.get(x).cardinality
          + "_";

    }

    return strName;
  }

  public void parseFromIndexHash(String hash) {

    String[] parts = hash.split("_");

    this.symbols.clear();

    for (int x = 0; x < parts.length; x++) {

      String[] nums = parts[x].split("\\.");

      int sax = Integer.parseInt(nums[0]);
      int card = Integer.parseInt(nums[1]);

      this.symbols.add(new Symbol(sax, card));

    }

  }

  public String getBitStringRepresentation() {

    String rep = "";

    for (int x = 0; x < this.symbols.size(); x++) {

      String node_symbol_rep = this.symbols.get(x).getiSAXBitRepresentation(0);
      rep += node_symbol_rep + ", ";
    }

    return rep;

  }

  @Override
  public Iterator<Symbol> iterator() {
    return this.symbols.iterator();
  }

  @Override
  public boolean equals(Object o) {

    if (o instanceof Sequence) {
      Sequence other = (Sequence) o;

      if (other.getBitStringRepresentation().equals(this.getBitStringRepresentation())) {
        return true;
      }

    }

    return false;
  }

  public byte[] getBytes() {

    byte[] rep = new byte[(this.symbols.size() * 4 * 2) + 8];

    // write orig_len into byte array

    SerDeUtils.writeIntIntoByteArray(this.orig_length, rep, 0);

    SerDeUtils.writeIntIntoByteArray(this.symbols.size(), rep, 4);

    for (int x = 0; x < this.symbols.size(); x++) {

      SerDeUtils.writeIntIntoByteArray(this.symbols.get(x).saxCharacter, rep, (x * 8) + 8);
      SerDeUtils.writeIntIntoByteArray(this.symbols.get(x).cardinality, rep, (x * 8) + 12);

    }

    return rep;

  }

  public void deserialize(byte[] b) {

    this.orig_length = SerDeUtils.byteArrayToInt(b, 0);

    int passes = SerDeUtils.byteArrayToInt(b, 4);

    for (int x = 0; x < passes; x++) {

      this.symbols.add(new Symbol(SerDeUtils.byteArrayToInt(b, (x * 8) + 4), SerDeUtils
          .byteArrayToInt(b, (x * 8) + 8)));

      // byteArrayToInt( b, )

    }

  }

  /**
   * Confirm whether this sequence with wildcard bits "contains" another sequence
   * 
   * @param ts
   * @return
   */
  public boolean ContainsSequence(Sequence ts) {

    // scan through the Symbols, looking to make sure each one falls within the symbols+wildcard
    // bits
    // in this node's region

    for (int x = 0; x < this.symbols.size(); x++) {

      // get the wildcard bit rep from the node, and then get the same rep from the ts
      String node_symbol_rep = this.symbols.get(x).getiSAXBitRepresentation();
      String ts_symbol_rep = ts.symbols.get(x).getiSAXBitRepresentation(
          this.symbols.get(x).getWildcardBitsCount());

      if (false == node_symbol_rep.equals(ts_symbol_rep)) {
        return false;
      }

    }

    return true;
  }

}
