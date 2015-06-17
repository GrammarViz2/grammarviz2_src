package edu.hawaii.jmotif.grammarviz.view.table;

import net.seninp.gi.GrammarRules;

/**
 * Table Data Model for the sequitur JTable
 * 
 * @author seninp
 * 
 */
public class PeriodicityTableModel extends SequiturRulesTableDataModel {

  /** Fancy serial. */
  private static final long serialVersionUID = -2952232752352693293L;

  /**
   * Constructor.
   */
  public PeriodicityTableModel() {
    PeriodicityTableColumns[] columns = PeriodicityTableColumns.values();
    String[] schemaColumns = new String[columns.length];
    for (int i = 0; i < columns.length; i++) {
      schemaColumns[i] = columns[i].getColumnName();
    }
    setSchema(schemaColumns);
  }

  public void update(GrammarRules grammarRules) {
    int rowIndex = 0;
    rows.clear();
    if (!(null == grammarRules)) {
      for (rowIndex = 0; rowIndex < grammarRules.size(); rowIndex++) {
        Object[] item = new Object[getColumnCount() + 1];
        int nColumn = 0;
        item[nColumn++] = grammarRules.get(rowIndex).ruleNumber();
        item[nColumn++] = grammarRules.get(rowIndex).getOccurrences().size();
        item[nColumn++] = grammarRules.get(rowIndex).getMeanLength();
        item[nColumn++] = grammarRules.get(rowIndex).getPeriod();
        item[nColumn++] = grammarRules.get(rowIndex).getPeriodError();
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
    if (columnIndex == PeriodicityTableColumns.RULE_NUMBER.ordinal())
      return Integer.class;
    if (columnIndex == PeriodicityTableColumns.RULE_FREQUENCY.ordinal())
      return Integer.class;
    if (columnIndex == PeriodicityTableColumns.LENGTH.ordinal())
      return Integer.class;
    if (columnIndex == PeriodicityTableColumns.PERIOD.ordinal())
      return Double.class;
    if (columnIndex == PeriodicityTableColumns.PERIOD_ERROR.ordinal())
      return Double.class;

    return String.class;
  }

}
