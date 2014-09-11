package edu.hawaii.jmotif.isax.index;

import java.util.ArrayList;
import edu.hawaii.jmotif.isax.Sequence;

/**
 * 
 * This data structure is used for creating hash keys at each level of the iSAX index.
 * 
 * for node splitting, for now we'll just expand the next least dim
 * 
 * @author Josh Patterson
 * 
 */
public class IndexHashParams {

  public int base_card = 4; // these params are pulled out of thin air
  public int isax_word_length = 4;
  public int d = 1; // iterative double rate per level, has to be less than isax_word_length
  public int orig_ts_len = 0;
  public int dim_index = 0; // the index of the dimension that we are expanding

  public int threshold = 4;

  public boolean bDebug = false;

  // not sure if we want to use this
  public ArrayList<Integer> arWildBits = new ArrayList<Integer>(); // there would be 4 of these in
  // this case, with each value being 2 at the root

  public ArrayList<Integer> arCards = new ArrayList<Integer>();

  public IndexHashParams() {
  }

  public IndexHashParams(ArrayList<Integer> arCards) {

    for (int x = 0; x < this.arCards.size(); x++) {

      this.addCardEntry(this.arCards.get(x));

    }

  }

  public static ArrayList<Integer> generateChildCardinality(Sequence node_sax_key) {

    return generateChildCardinality(node_sax_key.getCardinalities());

  }

  public static ArrayList<Integer> generateChildCardinality(ArrayList<Integer> arSequenceCards) {

    ArrayList<Integer> arCards = new ArrayList<Integer>();

    if (arSequenceCards.size() < 1) {
      System.out.println("no symbols");
      return arCards;
    }

    int iLSF = arSequenceCards.get(0); // node_sax_key.symbols.get(0).Cardinality + 1;
    int index = 0;

    for (int x = 0; x < arSequenceCards.size(); x++) {

      if (iLSF > arSequenceCards.get(x)) {

        iLSF = arSequenceCards.get(x);
        index = x;

      }

      arCards.add(arSequenceCards.get(x));

    }

    arCards.set(index, arCards.get(index) << 1);

    return arCards;

  }

  public void addCardEntry(int card) {

    this.arCards.add(card);

  }

  public void addWildBit(int wildbit) {

    this.arWildBits.add(wildbit);

  }

  public void setWildBitCountAtIndex(int index, int wildbits) {

    this.arWildBits.set(index, wildbits);

  }

  public String debugGetWildBits() {

    String bits = "";

    for (int x = 0; x < this.arWildBits.size(); x++) {

      bits += this.arWildBits.get(x) + ", ";

    }

    return bits;
  }

  public boolean reduceNextWildbit() {

    if (this.arWildBits.size() < 1) {
      System.out.println("no bits");
      return false;
    }

    int iLSF = this.arWildBits.get(0) - 1;
    int index = 0;
    boolean bFound = false;

    for (int x = 0; x < this.arWildBits.size(); x++) {

      if (iLSF < this.arWildBits.get(x)) {
        iLSF = this.arWildBits.get(x);
        index = x;
        bFound = true;
      }

    }

    if (!bFound) {
      System.out.println("none found");
      return false;
    }

    if (this.arWildBits.get(index) > 0) {
      this.arWildBits.set(index, this.arWildBits.get(index) - 1);
    }

    return true;

  }

  public String createMaskedBitSequence(Sequence isax) {

    String rep = "";

    for (int x = 0; x < isax.getSymbols().size(); x++) {

      String ts_symbol_rep = isax.getSymbols().get(x)
          .getiSAXBitRepresentation(this.arWildBits.get(x));

      rep += ts_symbol_rep + ", ";
    }

    return rep;

  }

  public byte[] getBytes() throws Exception {

    byte[] rep = new byte[6 * 4];

    SerDeUtils.writeIntIntoByteArray(this.base_card, rep, 0);

    SerDeUtils.writeIntIntoByteArray(this.d, rep, 4);

    SerDeUtils.writeIntIntoByteArray(this.dim_index, rep, 8);

    SerDeUtils.writeIntIntoByteArray(this.isax_word_length, rep, 12);

    SerDeUtils.writeIntIntoByteArray(this.orig_ts_len, rep, 16);

    SerDeUtils.writeIntIntoByteArray(this.threshold, rep, 20);

    return rep;

  }

  public void deserialize(byte[] b, int offset) {

    this.base_card = SerDeUtils.byteArrayToInt(b, 0 + offset);
    this.d = SerDeUtils.byteArrayToInt(b, 4 + offset);
    this.dim_index = SerDeUtils.byteArrayToInt(b, 8 + offset);
    this.isax_word_length = SerDeUtils.byteArrayToInt(b, 12 + offset);
    this.orig_ts_len = SerDeUtils.byteArrayToInt(b, 16 + offset);

    this.threshold = SerDeUtils.byteArrayToInt(b, 20 + offset);

  }

}
