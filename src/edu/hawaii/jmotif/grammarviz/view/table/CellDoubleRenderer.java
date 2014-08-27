package edu.hawaii.jmotif.grammarviz.view.table;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public class CellDoubleRenderer extends DefaultTableCellRenderer {

  /**
   * 
   */
  private static final long serialVersionUID = 3040778303718817255L;

  public CellDoubleRenderer() {
    super();
  }

  @Override
  public void setValue(Object aValue) {
    Object result = aValue;
    if ((aValue != null) && (aValue instanceof Number)) {
      Number numberValue = (Number) aValue;
      NumberFormat formatter = NumberFormat.getNumberInstance();
      DecimalFormat df = (DecimalFormat) formatter;
      df.applyPattern("#####0.00");
      result = df.format(numberValue.doubleValue());
    }
    super.setValue(result);
    super.setHorizontalAlignment(SwingConstants.RIGHT);
  }
}
