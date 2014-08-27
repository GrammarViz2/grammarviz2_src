package edu.hawaii.jmotif.sax.trie;

import java.util.List;

/**
 * Implements the magic trie structure.
 * 
 * @author Pavel Senin.
 * 
 */
public class SAXTrieTree {

  /**
   * Constants for the alphabet. Where it starts and ends. English.
   */
  private static final Integer ALPHABET_CODE_START = 97;
  private static final Integer ALPHABET_CODE_END = 122;

  /** The root of the tree. */
  private TrieInnerNode root;

  /** The alphabet size used for the building - the maximal word length. */
  private Integer alphabetSize;

  /**
   * Constructor.
   * 
   * @param alphabetSize The alphabet size.
   * @throws TrieException If wrong parameters specified.
   */
  public SAXTrieTree(Integer alphabetSize) throws TrieException {
    if ((null != alphabetSize) && (alphabetSize > 0)
        && (alphabetSize < (ALPHABET_CODE_END - ALPHABET_CODE_START))) {
      this.alphabetSize = alphabetSize;
      root = new TrieInnerNode("root");
      buildTrie(root, alphabetSize, alphabetSize);
    }
    else {
      throw new TrieException("Unable to create trie structure for the alphabet size of "
          + alphabetSize);
    }
  }

  /**
   * Traverse the tree following the string and get occurrences list.
   * 
   * @param string The query string.
   * @return The occurrences array.
   * @throws TrieException If error occurs - wrong string length provided etc.
   */
  public List<Integer> getOccurrences(String string) throws TrieException {

    // sanity check
    //
    if (this.root.getDescendants().size() > 0 && string.length() > 0
        && this.alphabetSize == string.length() && !containsWrongSymbols(string)) {

      // if string length is 2 - just pull an answer
      //
      if (2 == string.length()) {
        return ((TrieLeafNode) ((TrieInnerNode) this.root.getDescendant(string.substring(0, 1)))
            .getDescendant(string.substring(1))).getOccurences();
      }

      // if length greater than 2 - call the method recursively
      //
      String keyPrefix = string.substring(0, 1);
      String suffix = string.substring(1);
      return getSubstringOccurrence(this.root.getDescendant(keyPrefix), suffix);

    }
    else {
      throw new TrieException("Unable to get occurences for the string \"" + string
          + "\" the tree depth (alphabet size) is " + this.alphabetSize);
    }
  }

  /**
   * Add the occurrence into the table.
   * 
   * @param str The string.
   * @param idx The occurrence index.
   * @return the full list of occurrences.
   * @throws TrieException if goes wrong.
   */
  public List<Integer> addOccurence(String str, int idx) throws TrieException {

    // sanity check
    //
    if ((null == str) || (str.length() != this.alphabetSize) || (containsWrongSymbols(str))) {
      throw new TrieException("Cannot populate occurrence of \"" + str + "\"into the trie of size "
          + this.alphabetSize);
    }

    // first wee need to see if the string is going to be accepted
    // so we do traverse till the last symbol of the string
    //
    String prefix = str.substring(0, 1);
    String rest = str.substring(1);

    // pick the first internal node corresponding to the first character
    // WE HAVE TO DO THIS SINCE ROOT NODE IS DIFFERENT A BIT
    //
    TrieAbstractNode cNode = this.root;

    // traverse the tree in the loop
    //
    while ((rest.length() > 0) && (cNode = ((TrieInnerNode) cNode).getDescendant(prefix)) != null) {
      // if string is larger than 1 symbol - go deeper
      prefix = rest.substring(0, 1);
      rest = rest.substring(1);
    }

    // check that traversal finished properly
    //
    if (null == cNode) {
      throw new TrieException("Internal error: having a null node where it shouldn't be.");
    }

    // here last character left - get the array of occurrences and add the new one
    //
    TrieLeafNode leaf = (TrieLeafNode) ((TrieInnerNode) cNode).getDescendant(prefix);
    leaf.addOccurrence(idx);
    return leaf.getOccurences();
  }

  /**
   * Build the actual trie.
   * 
   * @param root The root of the current tree.
   * @param alphabetSize The alphabet size.
   * @param depth2Go The depth of the tree left.
   */
  private void buildTrie(TrieAbstractNode root, Integer alphabetSize, Integer depth2Go) {
    // if depth allows
    //
    if (depth2Go > 1) {
      //
      // internal trie nodes
      // create nodes for the alphabet size and recursively call further
      for (int i = 0; i < alphabetSize; i++) {
        // this char is the one from aStart + a value between 0 and the alphabet size
        char curChar = (char) (i + ALPHABET_CODE_START);
        // instantiate and add the node to the descendants list
        TrieInnerNode node = new TrieInnerNode(String.valueOf(curChar));
        ((TrieInnerNode) root).addNext(node);
        // recursively call the build
        buildTrie(node, alphabetSize, depth2Go - 1);
      }
    }
    else {
      //
      // depth2Go == 1
      //
      // nodes after this one must be leafs
      for (int i = 0; i < alphabetSize; i++) {
        char curChar = (char) (i + ALPHABET_CODE_START);
        // so we put them at place
        TrieLeafNode node = new TrieLeafNode(String.valueOf(curChar));
        ((TrieInnerNode) root).addNext(node);
      }
    }
  }

  /**
   * Internal method used for the tree traversal.
   * 
   * @param root The current node, IT IS NOT ROOT NODE, we just call it root here due to context.
   * @param str The string to use for traversal from this node.
   * @return list of found occurrences.
   * @throws TrieException If error occurs.
   */
  private List<Integer> getSubstringOccurrence(TrieAbstractNode root, String str)
      throws TrieException {
    if (TrieNodeType.INNER.equals(root.getType()) && str.length() > 1) {
      // normal case - digging deeper
      String keyPrefix = str.substring(0, 1);
      String suffix = str.substring(1);
      return getSubstringOccurrence(((TrieInnerNode) root).getDescendant(keyPrefix), suffix);
    }
    else if (TrieNodeType.INNER.equals(root.getType()) && str.length() == 1) {
      // string length is 1
      return ((TrieLeafNode) ((TrieInnerNode) root).getDescendant(str)).getOccurences();
    }
    else {
      throw new TrieException("Ubnormal condition passed into the method. Unable to proceed.");
    }
  }

  /**
   * The string validator - check if string has only proper letters.
   * 
   * @param str The string to check.
   * @return True if string contains only acceptable (valid) symbols.
   */
  private boolean containsWrongSymbols(String str) {
    int maxSymbol = ALPHABET_CODE_START + alphabetSize;
    for (int i = 0; i < str.length(); i++) {
      if (((int) str.charAt(i)) >= maxSymbol) {
        return true;
      }
    }
    return false;
  }
}
