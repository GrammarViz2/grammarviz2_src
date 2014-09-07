package edu.hawaii.jmotif.isax;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;
import edu.hawaii.jmotif.sax.SAXException;
import edu.hawaii.jmotif.sax.SAXFactory;
import edu.hawaii.jmotif.sax.alphabet.Alphabet;
import edu.hawaii.jmotif.timeseries.TSException;
import edu.hawaii.jmotif.util.StackTrace;

/**
 * The iSAX data point implementation. In SAX we used a series of letters based on a cardinality;
 * Here we use a series of integers each with its own cardinality.
 * 
 * @author Josh Patterson
 * 
 */
public class Symbol implements Cloneable, Comparable<Symbol> {

  /** SAX character stored as integer representation **/
  public int saxCharacter;

  /** Cardinality of this SAX symbol **/
  public int cardinality;

  /** used for indexing **/
  private int wildcardbits = 0;

  /** do we want to print out some debug info? If you arent a developer, then leave this as false **/
  public boolean debug = false;

  private static Logger consoleLogger;
  private static final Level LOGGING_LEVEL = Level.INFO;

  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(SAXFactory.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  /**
   * Default Constructor.
   */
  public Symbol() {
    this.cardinality = 0;
    this.saxCharacter = 0;
    this.wildcardbits = 0;
  }

  /**
   * Constructor.
   * 
   * @param SAX The SAX symbol value.
   * @param Card The cardinality of the symbol.
   */
  public Symbol(int SAX, int Card) {
    this.cardinality = Card;
    this.saxCharacter = SAX;
    this.wildcardbits = 0;
  }

  /**
   * Constructor.
   * 
   * @param SAX The SAX symbol value.
   * @param Card The cardinality of the symbol.
   * @param wildcard_bits in indexing we have symbols which span bits
   */
  public Symbol(int SAX, int Card, int wildcard_bits) {
    this.cardinality = Card;
    this.saxCharacter = SAX;
    this.wildcardbits = wildcard_bits;
  }

  /**
   * Basic clone() implementation
   */
  public Symbol clone() throws CloneNotSupportedException {

    return new Symbol(this.saxCharacter, this.cardinality, this.wildcardbits);

  }

  /**
   * Creates a clone of another object with this current object as the instantiation.
   * 
   * @param source
   */
  public void clone(Symbol source) {

    this.cardinality = source.cardinality;
    this.saxCharacter = source.saxCharacter;
    this.wildcardbits = source.wildcardbits;

  }

  /**
   * A method to clear out values of a symbol
   */
  public void reset() {

    this.cardinality = 0;
    this.saxCharacter = 0;
    this.wildcardbits = 0;

  }

  /**
   * A debug method to pull the SAX distance value out of the SAX distance lookup table.
   * 
   * @param other The other symbol we're looking up the distance against.
   * @param alphabet The alphabet used for the lookup.
   * @return The Euclidean distance that was found in the SAX table.
   * @throws SAXException if error occurs.
   */
  public double sax_table_dist(Symbol other, Alphabet alphabet) throws SAXException {

    double[][] distanceMatrix = { { 0 }, { 0 } };

    try {
      // we have to pull a new distance table per symbol
      distanceMatrix = alphabet.getDistanceMatrix(other.cardinality);
    }
    catch (TSException e) {
      throw new SAXException("Exception thrown in isax symbol: " + StackTrace.toString(e));
    }

    return distanceMatrix[this.saxCharacter][other.saxCharacter];

  }

  /**
   * A method to promote the cardinality of a symbol based on another symbol which follows Shieh and
   * Keogh's paper on iSAX.
   * 
   * @param target the other symbol to promote against.
   * @return returns a new symbol based on the promotion of this Symbol
   * @throws ISAXException if the cardinality is not a positive power of two, or this symbol's
   * cardinality is greater than the target symbol's cardinality. Also throws this exception if the
   * SAXValue is out of range of the cardinality.
   */
  public Symbol promote(Symbol target) throws SAXException {

    return this.promote(target.saxCharacter, target.cardinality);

  }

  /**
   * A method to promote the cardinality of a symbol based on another symbol which follows Shieh and
   * Keogh's paper on iSAX.
   * 
   * @param SAXNumber value of the SAX Symbol
   * @param iCardinality cardinality of the SAX Symbol
   * @return returns a Symbol instance that has been generated from this instance against the target
   * params.
   * @throws SAXException if the cardinality is not a positive power of two, or this symbol's
   * cardinality is greater than the target symbol's cardinality. Also throws this exception if the
   * SAXValue is out of range of the cardinality.
   */
  public Symbol promote(int SAXNumber, int iCardinality) throws SAXException {

    Symbol s = new Symbol();

    // 1. is the new cardinality
    // a.) positive
    // b.) a power of 2
    // c.) is greater than this word's cardinality

    if (iCardinality < 1) {
      throw new SAXException("not a positive cardinality");
    }

    if (false == powerOfTwo(iCardinality)) {
      throw new SAXException("New cardinality not a power of two");
    }

    if (iCardinality < this.cardinality) {
      throw new SAXException("can't promote to a lesser cardinality");
    }

    if (SAXNumber >= iCardinality) {
      throw new SAXException("invalid cardinality!");
    }

    consoleLogger.log(LOGGING_LEVEL, "pre-target: " + getBits(SAXNumber));
    consoleLogger.log(LOGGING_LEVEL, "pre-local : " + getBits(this.saxCharacter));

    // 2. figure out how many bits to shift up
    int iNewBits = cardinalityBitDelta(this.cardinality, iCardinality);

    s.cardinality = iCardinality;

    consoleLogger.log(LOGGING_LEVEL, "bit diff: " + iNewBits);

    // lets compare the prefix of both
    int iTargetSAXNumber_Prefix = (SAXNumber >> iNewBits) << iNewBits;

    int iLocalSAXNumber_Prefix = (this.saxCharacter << iNewBits);

    consoleLogger.log(LOGGING_LEVEL, "target: " + getBits(iTargetSAXNumber_Prefix));
    consoleLogger.log(LOGGING_LEVEL, "local : " + getBits(iLocalSAXNumber_Prefix));

    // if the original bits match up (the non-projected ones), forming a "prefix", then
    // we simply copy over the bits from the target symbol

    if (iTargetSAXNumber_Prefix == iLocalSAXNumber_Prefix) {
      consoleLogger.log(LOGGING_LEVEL, "PREFIX are equal!");
      s.saxCharacter = SAXNumber;
    }
    else if (iTargetSAXNumber_Prefix > iLocalSAXNumber_Prefix) {
      consoleLogger.log(LOGGING_LEVEL, "PREFIX are LESS THAN!");

      // if this symbol's compared bits are lexographically SMALLER than the bits in the target
      // symbol, then we use all 1's to fill out the promoted symbol we'll return from the function

      s.saxCharacter = iLocalSAXNumber_Prefix;
      int iMask = 1;

      for (int x = 0; x < iNewBits; x++) {
        s.saxCharacter = s.saxCharacter ^ iMask;
        iMask = iMask << 1;
      }

    }
    else if (iTargetSAXNumber_Prefix < iLocalSAXNumber_Prefix) {

      consoleLogger.log(LOGGING_LEVEL, "PREFIX are GREATER THAN!");

      // if this symbol's compared bits are lexographically LARGER than the bits in the target
      // symbol,
      // then we use all 0's to fill out the promoted symbol we'll return from the function
      s.saxCharacter = iLocalSAXNumber_Prefix;
      // int iMask = 1;
      /*
       * for ( int x = 0; x < iNewBits; x++ ) {
       * 
       * this.SAXCharacter = this.SAXCharacter & iMask; iMask = iMask << 1;
       * 
       * }
       */
    }
    else {

      throw new SAXException("case not meant to happen?");

    }

    consoleLogger.log(LOGGING_LEVEL, "post-target: " + getBits(SAXNumber));
    consoleLogger.log(LOGGING_LEVEL, "post-local : " + getBits(this.saxCharacter));

    return s;
  }

  /**
   * 
   * Used in iSAX indexing;
   * 
   * We promote the cardinality by adding a bit to the right hand side. This splits the cardinality
   * space into two halves. The low side gets a 0 bit, the high side gets a 1 bit.
   * 
   * @param newLowSymbol
   * @param newHighSymbol
   */
  public void promoteAndSplit(Symbol newLowSymbol, Symbol newHighSymbol) {

    int iNewCardinality = this.cardinality << 1;

    newLowSymbol.cardinality = iNewCardinality;
    newHighSymbol.cardinality = iNewCardinality;

    newLowSymbol.saxCharacter = this.saxCharacter << 1;
    newHighSymbol.saxCharacter = (this.saxCharacter << 1) + 1;

  }

  public int getWildcardBitsCount() {

    return this.wildcardbits;

  }

  public String getiSAXBitRepresentation() {

    return this.getiSAXBitRepresentation(this.wildcardbits);

  }

  /**
   * Debug method to look at the bits of a SAX value.
   * 
   * @param value SAX value, or any integer, that we want to convert into a string of bits.
   * @return returns a string of the bit representation of the integer.
   */
  public String getiSAXBitRepresentation(int wildcard_bits) {
    int displayMask = 1 << 31; // (numberBitsInCardinality( this.Cardinality) - 1);
    StringBuffer buf = new StringBuffer(35);

    // int bits_for_card = numberBitsInCardinality( this.Cardinality - 1 );
    int bits_for_card = numberBitsInCardinality(this.cardinality);

    // System.out.println( "bits for card: " + this.Cardinality + ",  " + bits_for_card );

    int val = this.saxCharacter;

    for (int c = 1; c <= 32; c++) {

      if (32 - c < bits_for_card) {

        if (wildcard_bits >= 33 - c) {

          buf.append('*');

        }
        else {

          buf.append((val & displayMask) == 0 ? '0' : '1');

        }

      }
      val <<= 1;

      // if ( c % 8 == 0 )
      // buf.append( ' ' );
    }

    return buf.toString();
  }

  /**
   * Debug method to look at the bits of a SAX value.
   * 
   * @param value SAX value, or any integer, that we want to convert into a string of bits.
   * @return returns a string of the bit representation of the integer.
   */
  public static String getBits(int value) {
    int displayMask = 1 << 31;
    StringBuffer buf = new StringBuffer(35);

    for (int c = 1; c <= 32; c++) {
      buf.append((value & displayMask) == 0 ? '0' : '1');
      value <<= 1;

      if (c % 8 == 0)
        buf.append(' ');
    }

    return buf.toString();
  }

  /**
   * Counts how many zeros we have in the bit representation of an integer before we hit a 1.
   * 
   * @param x the integer to examine.
   * @return number of zeros until a 1 was encountered.
   */
  public static int countLeadZs(int x) {

    int displayMask = 1 << 31;
    // StringBuffer buf = new StringBuffer( 35 );
    int cnt = 0;

    for (int c = 1; c <= 32; c++) {
      if ((x & displayMask) == 0) {
        // '0'
        cnt++;
      }
      else {
        return cnt;
      }
      x <<= 1;

      // if ( c % 8 == 0 )
      // buf.append( ' ' );
    }
    return cnt;
  }

  /**
   * public static int countLeadingBitZeros( int x ) {
   * 
   * 
   * 
   * if ( x == 0) { return 32; }
   * 
   * int n = 1; if ( x <= 0x0000FFFF ) { n = n + 16; x = x << 16; }
   * 
   * if ( x <= 0x00FFFFFF ) { n = n + 8; x = x << 8; }
   * 
   * if ( x <= 0x0FFFFFFF ) { n = n + 4; x = x << 4; }
   * 
   * if ( x <= 0x3FFFFFFF ) { n = n + 2; x = x << 2; }
   * 
   * if ( x <= 0x7FFFFFFF ) { n = n + 1; }
   * 
   * if ((x >> 16) == 0) { n = n + 16; x = x << 16; }
   * 
   * if ((x >> 24) == 0) { n = n + 8; x = x << 8; }
   * 
   * if ((x >> 28) == 0) { n = n + 4; x = x << 4; }
   * 
   * if ((x >> 30) == 0) { n = n + 2; x = x << 2; }
   * 
   * 
   * return (n - (x >> 31) );
   * 
   * }
   */

  /**
   * Used to figure out how many bits we need to shift a SAX value for promotion to a higher
   * cardinality.
   */
  private static int cardinalityBitDelta(int c0, int c1) {

    int c0_bits = numberBitsInCardinality(c0);
    int c1_bits = numberBitsInCardinality(c1);

    return Math.abs(c1_bits - c0_bits);

  }

  /**
   * Calculates the minimum number bits a cardinality fits in.
   * 
   * @param card Cardinality to examine.
   * @return returns the number of bits.
   */
  public static int numberBitsInCardinality(int card) {

    return 32 - countLeadZs(card);

  }

  /**
   * Determines if an integer is a power of two.
   * 
   * @param x integer to look at.
   * @return return true if x is a power of two, false if not.
   */
  public static boolean powerOfTwo(int x) {

    if (0 == (x & (x - 1))) {

      // System.out.println( "int: " + x + ", is power of two!" );
      return true;
    }

    return false;
  }

  // since we dont alter the source symbols in a comparison, we use pairs of objects for in and out
  /**
   * Takes two symbols and promotes the symbol of lesser cardinality to the cardinality of the other
   * symbol, adjusting the value as well.
   * 
   */
  public static void PerformPromotion(Symbol a_in, Symbol b_in, Symbol a_out, Symbol b_out)
      throws SAXException {

    if (a_in.cardinality >= b_in.cardinality) {

      a_out.clone(a_in);
      b_out.clone(b_in.promote(a_in)); // ***** come back and change this memory usage mechanic

    }
    else {

      a_out.clone(a_in.promote(b_in)); // = w1_in.symbols.get(x).promote( w2_in.symbols.get(x) );
      b_out.clone(b_in); // w2_in.symbols.get(x);

    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object o) {

    if (o instanceof Symbol) {
      Symbol other = (Symbol) o;

      if (this.cardinality == other.cardinality) {

        if (this.saxCharacter == other.saxCharacter) {

          return true;

        }

      }
    }

    return false;
  }

  /**
   * Compares the TPoint object with other TPoint using timestamps first: i.e. by the timestamp
   * values, if they are equal, the TPoint values used.
   * 
   * @param o the TPoint to compare with.
   * 
   * @return the standard compareTo result.
   */
  @Override
  public int compareTo(Symbol o) {

    if (this.cardinality == o.cardinality && this.saxCharacter == o.saxCharacter) {
      return 0;
    }
    else {

      // do a card promotion

      Symbol a_out = new Symbol();
      Symbol b_out = new Symbol();

      try {
        Symbol.PerformPromotion(this, o, a_out, b_out);
      }
      catch (SAXException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      // now cardinalities are the same
      if (a_out.saxCharacter == b_out.saxCharacter) {
        return 0;
      }
      else if (a_out.saxCharacter > b_out.saxCharacter) {
        return 1;
      }
      else if (a_out.saxCharacter < b_out.saxCharacter) {
        return -1; // only other thing it can be
      }

    }

    return -1;
  }

}
