package net.seninp.grammarviz.view;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Comparator;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import net.seninp.grammarviz.logic.MotifChartData;
import net.seninp.grammarviz.model.GrammarVizMessage;
import net.seninp.grammarviz.view.table.SequiturRulesTableColumns;
import net.seninp.grammarviz.view.table.SequiturRulesTableModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTableHeader;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * 
 * handling the chart panel and sequitur rules table
 * 
 * @author Manfred Lerner, seninp
 * 
 */

public class SequiturRulesPanel extends JPanel implements ListSelectionListener,
    PropertyChangeListener {

  /** Fancy serial. */
  private static final long serialVersionUID = -2710973854572981568L;

  public static final String FIRING_PROPERTY = "selectedRow";

  private SequiturRulesTableModel sequiturTableModel = new SequiturRulesTableModel();

  private JXTable sequiturTable;

  private MotifChartData chartData;

  private JScrollPane sequiturRulesPane;

  private String selectedRule;

  private boolean acceptListEvents;

  // the logger business
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.DEBUG;
  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(SequiturRulesPanel.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  /*
   * 
   * Comparator for the sorting of the Expanded Sequitur Rules Easy logic: sort by the length of the
   * Expanded Sequitur Rules
   */
  private Comparator<String> expandedRuleComparator = new Comparator<String>() {
    public int compare(String s1, String s2) {
      return s1.length() - s2.length();
    }
  };

  /**
   * Constructor.
   */
  public SequiturRulesPanel() {
    super();
    this.sequiturTableModel = new SequiturRulesTableModel();
    this.sequiturTable = new JXTable() {

      private static final long serialVersionUID = 2L;

      @Override
      protected JTableHeader createDefaultTableHeader() {
        return new JXTableHeader(columnModel) {
          private static final long serialVersionUID = 1L;

          @Override
          public void updateUI() {
            super.updateUI();
            // need to do in updateUI to survive toggling of LAF
            if (getDefaultRenderer() instanceof JLabel) {
              ((JLabel) getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

            }
          }
        };
      }

    };

    this.sequiturTable.setModel(sequiturTableModel);
    this.sequiturTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.sequiturTable.setShowGrid(false);

    this.sequiturTable.getSelectionModel().addListSelectionListener(this);

    @SuppressWarnings("unused")
    org.jdesktop.swingx.renderer.DefaultTableRenderer renderer = (org.jdesktop.swingx.renderer.DefaultTableRenderer) sequiturTable
        .getDefaultRenderer(String.class);

    // Make some columns wider than the rest, so that the info fits in.
    TableColumnModel columnModel = sequiturTable.getColumnModel();
    columnModel.getColumn(SequiturRulesTableColumns.RULE_NUMBER.ordinal()).setPreferredWidth(30);
    columnModel.getColumn(SequiturRulesTableColumns.RULE_USE_FREQUENCY.ordinal())
        .setPreferredWidth(40);
    columnModel.getColumn(SequiturRulesTableColumns.SEQUITUR_RULE.ordinal()).setPreferredWidth(100);
    columnModel.getColumn(SequiturRulesTableColumns.EXPANDED_SEQUITUR_RULE.ordinal())
        .setPreferredWidth(150);
    columnModel.getColumn(SequiturRulesTableColumns.RULE_MEAN_LENGTH.ordinal()).setPreferredWidth(
        120);

    TableRowSorter<SequiturRulesTableModel> sorter = new TableRowSorter<SequiturRulesTableModel>(
        sequiturTableModel);
    sequiturTable.setRowSorter(sorter);
    sorter.setComparator(SequiturRulesTableColumns.EXPANDED_SEQUITUR_RULE.ordinal(),
        expandedRuleComparator);

    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
    rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
    this.sequiturTable.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);

    this.sequiturRulesPane = new JScrollPane(sequiturTable);
  }

  /**
   * Set the new data.
   * 
   * @param chartData the new data.
   */
  public void setChartData(MotifChartData chartData) {

    this.acceptListEvents = false;

    // save the data
    this.chartData = chartData;

    // update
    sequiturTableModel.update(this.chartData.getGrammarRules());

    // put new data on show
    resetPanel();

    this.acceptListEvents = true;
  }

  /**
   * create the panel with the sequitur rules table
   * 
   * @return sequitur panel
   */
  public void resetPanel() {
    // cleanup all the content
    this.removeAll();
    this.add(sequiturRulesPane);
    this.validate();
    this.repaint();
  }

  /**
   * @return sequitur table model
   */
  public SequiturRulesTableModel getSequiturTableModel() {
    return sequiturTableModel;
  }

  /**
   * @return sequitur table
   */
  public JTable getSequiturTable() {
    return sequiturTable;
  }

  @Override
  public void valueChanged(ListSelectionEvent arg) {

    if (!arg.getValueIsAdjusting() && this.acceptListEvents) {
      int col = sequiturTable.getSelectedColumn();
      int row = sequiturTable.getSelectedRow();
      consoleLogger.debug("Selected ROW: " + row + " - COL: " + col);
      String rule = String.valueOf(sequiturTable.getValueAt(row,
          SequiturRulesTableColumns.RULE_NUMBER.ordinal()));
      this.firePropertyChange(FIRING_PROPERTY, this.selectedRule, rule);
      this.selectedRule = rule;
    }
  }

  /**
   * Resets the selection and resorts the table by the Rules.
   */
  public void resetSelection() {
    // TODO: there is the bug. commented out.
    // sequiturTable.getSelectionModel().clearSelection();
    // sequiturTable.setSortOrder(0, SortOrder.ASCENDING);
  }

  public void propertyChange(PropertyChangeEvent event) {
    String prop = event.getPropertyName();

    if (prop.equalsIgnoreCase(GrammarVizMessage.MAIN_CHART_CLICKED_MESSAGE)) {
      String rule = (String) event.getNewValue();
      for (int row = 0; row <= sequiturTable.getRowCount() - 1; row++) {
        for (int col = 0; col <= sequiturTable.getColumnCount() - 1; col++) {
          if (rule.equals(chartData.convert2OriginalSAXAlphabet('1',
              sequiturTable.getValueAt(row, col).toString()))) {
            sequiturTable.scrollRectToVisible(sequiturTable.getCellRect(row, 0, true));
            sequiturTable.setRowSelectionInterval(row, row);
          }
        }
      }
    }
  }

  /**
   * Clears the panel.
   */
  public void clear() {
    this.acceptListEvents = false;
    this.removeAll();
    this.chartData = null;
    sequiturTableModel.update(null);
    this.validate();
    this.repaint();
    this.acceptListEvents = true;
  }

}
