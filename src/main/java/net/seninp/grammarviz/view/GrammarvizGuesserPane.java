package net.seninp.grammarviz.view;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.NumberFormatter;
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
  // NOTE: labels and widgets are instance (NOT static) fields -- a new pane is
  // constructed on every "Guess parameters" invocation, and a Swing component can
  // only belong to one container, so shared statics would alias across instances.
  private final JLabel samplingIntervalLabel = new JLabel("Sampling interval range:");
  private final JLabel minimalCoverLabel = new JLabel("Minimal rule cover threshold:");
  private final JLabel windowBoundLabel = new JLabel("Window range and step:");
  private final JLabel paaBoundLabel = new JLabel("PAA range and step:");
  private final JLabel alphabetBoundLabel = new JLabel("Alphabet range and step:");

  // and their UI widgets
  //
  private final JFormattedTextField intervalStartField = new JFormattedTextField(
      integerNumberFormatter());
  private final JFormattedTextField intervalEndField = new JFormattedTextField(
      integerNumberFormatter());

  private final JFormattedTextField minimalCoverField = new JFormattedTextField(
      new DecimalFormat("0.00"));

  private final JFormattedTextField windowMinField = new JFormattedTextField(
      integerNumberFormatter());
  private final JFormattedTextField windowMaxField = new JFormattedTextField(
      integerNumberFormatter());
  private final JFormattedTextField windowIncrement = new JFormattedTextField(
      integerNumberFormatter());

  private final JFormattedTextField paaMinField = new JFormattedTextField(integerNumberFormatter());
  private final JFormattedTextField paaMaxField = new JFormattedTextField(integerNumberFormatter());
  private final JFormattedTextField paaIncrement = new JFormattedTextField(integerNumberFormatter());

  private final JFormattedTextField alphabetMinField = new JFormattedTextField(
      integerNumberFormatter());
  private final JFormattedTextField alphabetMaxField = new JFormattedTextField(
      integerNumberFormatter());
  private final JFormattedTextField alphabetIncrement = new JFormattedTextField(
      integerNumberFormatter());

  /**
   * Constructor.
   * 
   * @param userSession the user session
   */
  public GrammarvizGuesserPane(UserSession userSession) {

    super(new MigLayout("", "[][fill,grow][fill,grow][fill,grow]", ""));

    this.add(samplingIntervalLabel, "span 2");
    this.add(intervalStartField);
    this.add(intervalEndField, "wrap");

    this.add(minimalCoverLabel, "span 2");
    this.add(minimalCoverField, "wrap");

    this.add(new JLabel("MIN"), "skip 1");
    this.add(new JLabel("MAX"));
    this.add(new JLabel("STEP"), "wrap");

    this.add(windowBoundLabel);
    this.add(windowMinField);
    this.add(windowMaxField);
    this.add(windowIncrement, "wrap");

    this.add(paaBoundLabel);
    this.add(paaMinField);
    this.add(paaMaxField);
    this.add(paaIncrement, "wrap");

    this.add(alphabetBoundLabel);
    this.add(alphabetMinField);
    this.add(alphabetMaxField);
    this.add(alphabetIncrement, "wrap");

    setValues(userSession);

  }

  private void setValues(UserSession userSession) {

    intervalStartField.setText(Integer.valueOf(userSession.samplingStart).toString());
    intervalEndField.setText(Integer.valueOf(userSession.samplingEnd).toString());

    minimalCoverField.setText(Double.valueOf(userSession.minimalCoverThreshold).toString());

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

  /**
   * Commits and validates the edited field values back into the session. This is the
   * read-back counterpart to {@link #setValues(UserSession)} -- without it the dialog's
   * edits are silently discarded.
   *
   * @param userSession the session to write the validated values into.
   * @return {@code true} if all fields parsed and the ranges are sane (values written);
   *         {@code false} if any field is unparseable or a range is invalid (session left
   *         untouched so the caller can keep the dialog open).
   */
  public boolean saveValues(UserSession userSession) {

    // commit each field through its formatter; bad text throws ParseException
    //
    try {
      for (JFormattedTextField f : new JFormattedTextField[] { intervalStartField,
          intervalEndField, minimalCoverField, windowMinField, windowMaxField, windowIncrement,
          paaMinField, paaMaxField, paaIncrement, alphabetMinField, alphabetMaxField,
          alphabetIncrement }) {
        f.commitEdit();
      }
    }
    catch (ParseException e) {
      return false;
    }

    int iStart = intValue(intervalStartField);
    int iEnd = intValue(intervalEndField);
    double cover = doubleValue(minimalCoverField);
    int wMin = intValue(windowMinField);
    int wMax = intValue(windowMaxField);
    int wInc = intValue(windowIncrement);
    int pMin = intValue(paaMinField);
    int pMax = intValue(paaMaxField);
    int pInc = intValue(paaIncrement);
    int aMin = intValue(alphabetMinField);
    int aMax = intValue(alphabetMaxField);
    int aInc = intValue(alphabetIncrement);

    // range sanity -- also keeps the sampler grid non-degenerate (guards the empty-grid case)
    //
    if (iStart >= iEnd || wMin > wMax || pMin > pMax || aMin > aMax || wInc <= 0 || pInc <= 0
        || aInc <= 0 || cover < 0.0 || cover > 1.0) {
      return false;
    }
    // alphabet is bounded by NormalAlphabet's supported range [2, 20]
    if (aMin < 2 || aMax > 20) {
      return false;
    }

    userSession.samplingStart = iStart;
    userSession.samplingEnd = iEnd;
    userSession.minimalCoverThreshold = cover;
    userSession.boundaries = new int[] { wMin, wMax, wInc, pMin, pMax, pInc, aMin, aMax, aInc };

    return true;
  }

  private static int intValue(JFormattedTextField field) {
    return ((Number) field.getValue()).intValue();
  }

  private static double doubleValue(JFormattedTextField field) {
    return ((Number) field.getValue()).doubleValue();
  }

  /**
   * Provides a convenient integer formatter.
   * 
   * @return a formatter instance.
   */
  private static NumberFormatter integerNumberFormatter() {
    NumberFormat format = NumberFormat.getInstance();
    NumberFormatter formatter = new NumberFormatter(format);
    formatter.setValueClass(Integer.class);
    formatter.setMinimum(0);
    formatter.setMaximum(Integer.MAX_VALUE);
    // If you want the value to be committed on each keystroke instead of focus lost
    formatter.setCommitsOnValidEdit(true);
    return formatter;
  }

}
