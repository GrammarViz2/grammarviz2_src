package edu.hawaii.jmotif.sequitur.view;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/* 1.4 example used by DialogDemo.java. */
class ParametersDialog extends JDialog implements ActionListener, PropertyChangeListener {

  private static final long serialVersionUID = -8273240774350932580L;

  private static final String OK_BUTTON_TEXT = "Save";
  private static final String CANCEL_BUTTON_TEXT = "Cancel";

  private static ParametersPane optionPane;

  private int strategy;
  private int algorithm;
  private Double normalizationThreshold;

  /** Creates the reusable dialog. */
  public ParametersDialog(JFrame parentFrame, JPanel optionPanel) {

    super(parentFrame, true);
    if (parentFrame != null) {
      Dimension parentSize = parentFrame.getSize();
      Point p = parentFrame.getLocation();
      setLocation(p.x + parentSize.width / 4, p.y + parentSize.height / 4);
    }
    // setPreferredSize(new Dimension(400, 260));
    // setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    optionPane = (ParametersPane) optionPanel;

    MigLayout mainFrameLayout = new MigLayout("fill", "[grow,center]", "[grow]5[]");

    getContentPane().setLayout(mainFrameLayout);

    getContentPane().add(optionPane, "h 200:300:,w 400:500:,growx,growy,wrap");

    JPanel buttonPane = new JPanel();
    JButton okButton = new JButton(OK_BUTTON_TEXT);
    JButton cancelButton = new JButton(CANCEL_BUTTON_TEXT);
    buttonPane.add(okButton);
    buttonPane.add(cancelButton);
    okButton.addActionListener(this);
    cancelButton.addActionListener(this);

    getContentPane().add(buttonPane, "wrap");

    pack();
    setVisible(true);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // TODO Auto-generated method stub
    assert true;
  }

  /** This method handles events for the text field. */
  @Override
  public void actionPerformed(ActionEvent e) {
    if (OK_BUTTON_TEXT.equalsIgnoreCase(e.getActionCommand())) {
      // collect settings
      this.strategy = optionPane.getSelectedStrategyValue();
      this.algorithm = optionPane.getSelectedAlgorithmValue();
      this.normalizationThreshold = optionPane.getNormalizationThreshold();
    }
    else if (CANCEL_BUTTON_TEXT.equalsIgnoreCase(e.getActionCommand())) {
      assert true;
    }
    this.dispose();
  }

  /** This method clears the dialog and hides it. */
  public void clearAndHide() {
    setVisible(false);
  }

  public int getSelectedStrategyValue() {
    return this.strategy;
  }

  public int getSelectedAlgorithmValue() {
    return this.algorithm;
  }

  public double getNormalizationThresholdValue() {
    return Double.valueOf(this.normalizationThreshold);
  }
}