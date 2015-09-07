package net.seninp.grammarviz.view;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;
import net.seninp.grammarviz.session.UserSession;

/**
 * Implements the parameter panel for GrammarViz.
 * 
 * @author psenin
 * 
 */
public class GrammarvizGuesserPane extends JPanel {

  private static final long serialVersionUID = -941188995659753923L;

  // The labels
  //
  private static final JLabel SAMPLING_INTERVAL_LABEL = new JLabel("Window bounds:");
  private static final JLabel WINDOW_BOUND_LABEL = new JLabel("Window bounds:");
  private static final JLabel PAA_BOUND_LABEL = new JLabel("PAA bounds:");
  private static final JLabel ALPHABET_BOUND_LABEL = new JLabel("Alphabet bounds:");

  // and their UI widgets
  //
  private static final JTextField intervalStartField = new JTextField(10);
  private static final JTextField intervalEndField = new JTextField(10);

  private static final JTextField windowMinField = new JTextField(10);
  private static final JTextField windowMaxField = new JTextField(10);
  private static final JTextField windowIncrement = new JTextField(10);

  private static final JTextField paaMinField = new JTextField(10);
  private static final JTextField paaMaxField = new JTextField(10);
  private static final JTextField paaIncrement = new JTextField(10);

  private static final JTextField alphabetMinField = new JTextField(10);
  private static final JTextField alphabetMaxField = new JTextField(10);
  private static final JTextField alphabetIncrement = new JTextField(10);

  /**
   * Constructor.
   * 
   * @param userSession
   */
  public GrammarvizGuesserPane(UserSession userSession) {

    super(new MigLayout("fill", "[][grow][grow][grow]", "[grow]"));

    this.add(SAMPLING_INTERVAL_LABEL);
    this.add(new JLabel());
    this.add(intervalStartField);
    this.add(intervalEndField, "wrap");

    this.add(WINDOW_BOUND_LABEL);
    this.add(windowMinField);
    this.add(windowMaxField);
    this.add(windowIncrement, "wrap");

    this.add(PAA_BOUND_LABEL);
    this.add(paaMinField);
    this.add(paaMaxField);
    this.add(paaIncrement, "wrap");

    this.add(ALPHABET_BOUND_LABEL);
    this.add(alphabetMinField);
    this.add(alphabetMaxField);
    this.add(alphabetIncrement, "wrap");

    setValues(userSession);

  }

  private void setValues(UserSession userSession) {

    intervalStartField.setText(Integer.valueOf(userSession.samplingStart).toString());
    intervalEndField.setText(Integer.valueOf(userSession.samplingEnd).toString());

    windowMinField.setText(Integer.valueOf(userSession.boundaries[0]).toString());
    windowMaxField.setText(Integer.valueOf(userSession.boundaries[1]).toString());
    windowIncrement.setText(Integer.valueOf(userSession.boundaries[2]).toString());

    paaMinField.setText(Integer.valueOf(userSession.boundaries[3]).toString());
    paaMaxField.setText(Integer.valueOf(userSession.boundaries[4]).toString());
    paaIncrement.setText(Integer.valueOf(userSession.boundaries[5]).toString());

    alphabetMinField.setText(Integer.valueOf(userSession.boundaries[6]).toString());
    alphabetMaxField.setText(Integer.valueOf(userSession.boundaries[7]).toString());
    alphabetIncrement.setText(Integer.valueOf(userSession.boundaries[8]).toString());

  }

}
