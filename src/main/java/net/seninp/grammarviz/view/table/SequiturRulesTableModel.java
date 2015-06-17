package edu.hawaii.jmotif.grammarviz.view.table;

import edu.hawaii.jmotif.gi.GrammarRules;

/**
 * Table Data Model for the sequitur JTable
 * 
 * @author Manfred Lerner, seninp
 * 
 */
public class SequiturRulesTableModel extends SequiturRulesTableDataModel {

  /** Fancy serial. */
  private static final long serialVersionUID = -2952232752352963293L;

  /**
   * Constructor.
   */
  public SequiturRulesTableModel() {
    SequiturRulesTableColumns[] columns = SequiturRulesTableColumns.values();
    String[] schemaColumns = new String[columns.length];
    for (int i = 0; i < columns.length; i++) {
      schemaColumns[i] = columns[i].getColumnName();
    }
    setSchema(schemaColumns);
  }

  /**
   * Updates the table model with provided data.
   * 
   * @param grammarRules the data for table.
   */
  public void update(GrammarRules grammarRules) {
    int rowIndex = 0;
    rows.clear();
    if (!(null == grammarRules)) {
      for (rowIndex = 0; rowIndex < grammarRules.size(); rowIndex++) {
        Object[] item = new Object[getColumnCount() + 1];
        int nColumn = 0;
        item[nColumn++] = grammarRules.get(rowIndex).ruleNumber();
        item[nColumn++] = grammarRules.get(rowIndex).getRuleLevel();
        item[nColumn++] = grammarRules.get(rowIndex).getOccurrences().size();
        item[nColumn++] = grammarRules.get(rowIndex).getRuleString();
        item[nColumn++] = grammarRules.get(rowIndex).getExpandedRuleString();
        item[nColumn++] = grammarRules.get(rowIndex).getRuleUseFrequency();
        item[nColumn++] = grammarRules.get(rowIndex).getMeanLength();
        item[nColumn++] = grammarRules.get(rowIndex).minMaxLengthAsString();
        // item[nColumn++] = saxContainerList.get(rowIndex).getOccurenceIndexes();
        rows.add(item);
      }
    }

    fireTableDataChanged();
  }

  /*
   * Important for table column sorting (non-Javadoc)
   * 
   * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
   */
  public Class<?> getColumnClass(int columnIndex) {
    /*
     * for the RuleNumber and RuleFrequency column we use column class Integer.class so we can sort
     * it correctly in numerical order
     */
    if (columnIndex == SequiturRulesTableColumns.RULE_NUMBER.ordinal())
      return Integer.class;
    if (columnIndex == SequiturRulesTableColumns.RULE_LEVEL.ordinal())
      return Integer.class;
    if (columnIndex == SequiturRulesTableColumns.RULE_FREQUENCY.ordinal())
      return Integer.class;
    if (columnIndex == SequiturRulesTableColumns.SEQUITUR_RULE.ordinal())
      return String.class;
    if (columnIndex == SequiturRulesTableColumns.EXPANDED_SEQUITUR_RULE.ordinal())
      return String.class;
    if (columnIndex == SequiturRulesTableColumns.RULE_USE_FREQUENCY.ordinal())
      return Integer.class;
    if (columnIndex == SequiturRulesTableColumns.RULE_MEAN_LENGTH.ordinal())
      return Integer.class;
    if (columnIndex == SequiturRulesTableColumns.LENGTH.ordinal())
      return String.class;
    // if (columnIndex == SequiturTableColumns.RULE_INDEXES.ordinal())
    // return String.class;

    return String.class;
  }

}
