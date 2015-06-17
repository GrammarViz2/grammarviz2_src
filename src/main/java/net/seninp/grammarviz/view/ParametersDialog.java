package net.seninp.grammarviz.view;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import edu.hawaii.jmotif.grammarviz.logic.UserSession;

/* 1.4 example used by DialogDemo.java. */
class ParametersDialog extends JDialog implements ActionListener {

  private static final long serialVersionUID = -8273240774350932580L;

  private static final String OK_BUTTON_TEXT = "Save";
  private static final String CANCEL_BUTTON_TEXT = "Cancel";

  private UserSession session;
  private ParametersPane optionPane;

  /** Creates the reusable dialog. */
  public ParametersDialog(JFrame parentFrame, JPanel optionPanel, UserSession session) {

    super(parentFrame, true);
    if (parentFrame != null) {
      Dimension parentSize = parentFrame.getSize();
      Point p = parentFrame.getLocation();
      setLocation(p.x + parentSize.width / 4, p.y + parentSize.height / 4);
    }

    this.session = session;

    this.optionPane = (ParametersPane) optionPanel;

    MigLayout mainFrameLayout = new MigLayout("fill", "[grow,center]", "[grow]5[]");

    getContentPane().setLayout(mainFrameLayout);

    getContentPane().add(this.optionPane, "h 200:300:,w 400:500:,growx,growy,wrap");

    JPanel buttonPane = new JPanel();
    JButton okButton = new JButton(OK_BUTTON_TEXT);
    JButton cancelButton = new JButton(CANCEL_BUTTON_TEXT);
    buttonPane.add(okButton);
    buttonPane.add(cancelButton);
    okButton.addActionListener(this);
    cancelButton.addActionListener(this);

    getContentPane().add(buttonPane, "wrap");

    pack();
  }

  //
  // Handles events for the text field.
  //
  @Override
  public void actionPerformed(ActionEvent e) {
    if (OK_BUTTON_TEXT.equalsIgnoreCase(e.getActionCommand())) {

      // collect settings
      this.session.setCountStrategy(this.optionPane.getSelectedStrategyValue());

      this.session.setGIAlgorithm(this.optionPane.getSelectedAlgorithmValue());

      this.session.setNormalizationThreshold(this.optionPane.getNormalizationThreshold());

      // the output file names
      this.session.setGrammarOutputFileName(this.optionPane.getGrammarOutputFileName());
      this.session.setRuleDensityOutputFileName(this.optionPane.getRuleCoverageFileName());
      this.session.setAnomaliesOutputFileName(this.optionPane.getAnomalyOutputFileName());
      this.session.setChartsSaveFolder(this.optionPane.getChartsFolderName());

    }
    else if (CANCEL_BUTTON_TEXT.equalsIgnoreCase(e.getActionCommand())) {
      assert true;
    }

    this.dispose();
  }

  /**
   * Clears the dialog and hides it.
   */
  public void clearAndHide() {
    setVisible(false);
  }
}