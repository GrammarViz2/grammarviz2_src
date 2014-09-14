package edu.hawaii.jmotif.grammarviz.view;

import java.awt.Component;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.NumberFormatter;
import net.miginfocom.swing.MigLayout;

/**
 * Implements the parameter panel for GrammarViz.
 * 
 * @author psenin
 * 
 */
public class ParametersPane extends JPanel {

  /** Serial id. */
  private static final long serialVersionUID = -1883929065855038047L;

  private static final String TITLE_FONT = "helvetica";

  //
  // The coverage count strategies
  //
  public static final String STRATEGY_COUNT = "count";
  public static final String STRATEGY_LEVEL = "level";
  public static final String STRATEGY_OCCURRENCE = "occurrence";
  public static final String STRATEGY_YIELD = "yield";
  public static final String STRATEGY_PRODUCT = "product";
  //
  // and their UI widgets
  //
  private static final int STRATEGY_BUTTONS_NUM = 5;
  private static final JRadioButton[] strategyRadioButtons = new JRadioButton[STRATEGY_BUTTONS_NUM];
  private static final ButtonGroup strategyRadioGroup = new ButtonGroup();
  //
  // The GI algorithm variables
  //
  public static final String GI_SEQUITUR = "Sequitur";
  public static final String GI_REPAIR = "Re-Pair";
  private static final int GI_BUTTONS_NUM = 2;
  private static final JRadioButton[] giRadioButtons = new JRadioButton[GI_BUTTONS_NUM];
  private static final ButtonGroup giRadioGroup = new ButtonGroup();
  //
  // Normalization threshold option
  //
  public static final String SAX_NORMALIZATION_THRESHOLD_LABEL = "Normalization threshold:";
  public static final JFormattedTextField normalizationThresholdField = new JFormattedTextField(
      new NumberFormatter(NumberFormat.getNumberInstance(Locale.US)));
  //
  // Output options section
  //
  public static final String OUTPUT_RULE_DENSITY_CURVE_LABEL = "Rule density curve filename:";
  public static final JTextField outputRuleCoverageFilename = new JTextField();
  //
  public static final String OUTPUT_GRAMMAR_LABEL = "Grammar filename:";
  public static final JTextField outputGrammarFileName = new JTextField();
  //
  public static final String OUTPUT_ANOMALY_LABEL = "Anomalies filename:";
  public static final JTextField outputAnomalyFileName = new JTextField();
  //
  public static final String OUTPUT_CHARTS_LABEL = "Charts folder:";
  public static final JTextField outputChartsFolderName = new JTextField();

  /**
   * Constructor.
   */
  public ParametersPane() {

    super(new MigLayout("fill", "[grow]", "[grow]"));

    JTabbedPane tabbedPane = new JTabbedPane();

    // the count strategy pane
    //
    tabbedPane.addTab("Coverage Strategy", null, buildStrategyPanel(),
        "Coverage Count Strategy selection");

    // the GI Implementation pane
    //
    tabbedPane.addTab("GI Implementation", null, buildGIImplementationPanel(),
        "GI Implementation selection");

    // the Output pane
    //
    tabbedPane.addTab("Output", null, buildOutputFilesPanel(), "Output configuration");

    // the Auxiliary parameters pane pane
    //
    tabbedPane.addTab("Options", null, buildOptionsPanel(), "Other GrammarViz options");

    this.add(tabbedPane, "grow");

  }

  /**
   * Builds the other options panel.
   * 
   * @return
   */
  private Component buildOptionsPanel() {
    // the resulting panel
    //
    JPanel res = new JPanel(
        new MigLayout("insets 0 0 0 0", "[fill,grow]", "[5:5:5][fill,grow 100]"));

    // the "spacer"
    res.add(new JLabel(), "wrap");

    // Create the GI radio components.
    //
    JPanel optionsPanel = new JPanel();

    optionsPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(BevelBorder.LOWERED), "SAX Options", TitledBorder.LEFT,
        TitledBorder.CENTER, new Font(TITLE_FONT, Font.PLAIN, 10)));

    MigLayout optionsPaneLayout = new MigLayout("insets 0 0 0 0", "[]10[grow,fill]", "[]");

    optionsPanel.setLayout(optionsPaneLayout);

    optionsPanel.add(new JLabel(SAX_NORMALIZATION_THRESHOLD_LABEL));

    optionsPanel.add(normalizationThresholdField, "wrap");

    res.add(optionsPanel, "pad 0 0 0 0");

    return res;
  }

  private Component buildStrategyPanel() {

    // the resulting panel
    //
    JPanel res = new JPanel(
        new MigLayout("insets 0 0 0 0", "[fill,grow]", "[5:5:5][fill,grow 100]"));

    // the "spacer" JPanel strategyPanel = new JPanel();
    res.add(new JLabel(), "wrap");

    // the radio buttons panel
    JPanel strategyPanel = new JPanel();

    strategyPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(BevelBorder.LOWERED), "Set the rule coverage strategy",
        TitledBorder.LEFT, TitledBorder.CENTER, new Font(TITLE_FONT, Font.PLAIN, 10)));

    MigLayout buttonsPaneLayout = new MigLayout("insets 0 0 0 0", "[]", "[][][][]");

    strategyPanel.setLayout(buttonsPaneLayout);

    // Create the STRATEGY radio components.
    //
    strategyRadioButtons[0] = new JRadioButton("rule count (i.e. coverage classic)");
    strategyRadioButtons[0].setActionCommand(STRATEGY_COUNT);

    strategyRadioButtons[1] = new JRadioButton("rule level increment");
    strategyRadioButtons[1].setActionCommand(STRATEGY_LEVEL);

    strategyRadioButtons[2] = new JRadioButton("rule occurrence increment");
    strategyRadioButtons[2].setActionCommand(STRATEGY_OCCURRENCE);

    strategyRadioButtons[3] = new JRadioButton(
        "rule yield (number of words in expanded form) increment");
    strategyRadioButtons[3].setActionCommand(STRATEGY_YIELD);

    strategyRadioButtons[4] = new JRadioButton("product of level and occurrence increment");
    strategyRadioButtons[4].setActionCommand(STRATEGY_PRODUCT);

    for (int i = 0; i < STRATEGY_BUTTONS_NUM; i++) {
      strategyRadioGroup.add(strategyRadioButtons[i]);
    }
    strategyRadioButtons[0].setSelected(true);

    int numChoices = strategyRadioButtons.length;

    for (int i = 0; i < numChoices; i++) {
      strategyPanel.add(strategyRadioButtons[i], "wrap");
    }

    res.add(strategyPanel, "pad 0 0 0 0");
    return res;
  }

  private Component buildGIImplementationPanel() {
    // the resulting panel
    //
    JPanel res = new JPanel(
        new MigLayout("insets 0 0 0 0", "[fill,grow]", "[5:5:5][fill,grow 100]"));

    // the "spacer"
    res.add(new JLabel(), "wrap");

    // Create the GI radio components.
    //
    JPanel giOptionPanel = new JPanel();

    giOptionPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(BevelBorder.LOWERED), "Set the GI implementation",
        TitledBorder.LEFT, TitledBorder.CENTER, new Font(TITLE_FONT, Font.PLAIN, 10)));

    MigLayout buttonsPaneLayout = new MigLayout("insets 0 0 0 0", "[]", "[][]");

    giOptionPanel.setLayout(buttonsPaneLayout);

    giRadioButtons[0] = new JRadioButton("Sequitur");
    giRadioButtons[0].setActionCommand(GI_SEQUITUR);

    giRadioButtons[1] = new JRadioButton("Re-Pair");
    giRadioButtons[1].setActionCommand(GI_REPAIR);

    for (int i = 0; i < GI_BUTTONS_NUM; i++) {
      giRadioGroup.add(giRadioButtons[i]);
    }
    giRadioButtons[0].setSelected(true);

    int numChoices = giRadioButtons.length;

    for (int i = 0; i < numChoices; i++) {
      giOptionPanel.add(giRadioButtons[i], "wrap");
    }

    res.add(giOptionPanel, "pad 0 0 0 0");

    return res;
  }

  private Component buildOutputFilesPanel() {
    // the resulting panel
    //
    JPanel res = new JPanel(
        new MigLayout("insets 0 0 0 0", "[fill,grow]", "[5:5:5][fill,grow 100]"));

    // the "spacer"
    res.add(new JLabel(), "wrap");

    // Create the GI radio components.
    //
    JPanel outputConfigurationPanel = new JPanel();

    outputConfigurationPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(BevelBorder.LOWERED), "Configure the output filenames",
        TitledBorder.LEFT, TitledBorder.CENTER, new Font(TITLE_FONT, Font.PLAIN, 10)));

    MigLayout filenamesLayout = new MigLayout("insets 0 0 0 0", "[][grow,fill]", "10[]10[]10[]");

    outputConfigurationPanel.setLayout(filenamesLayout);

    outputConfigurationPanel.add(new JLabel(OUTPUT_RULE_DENSITY_CURVE_LABEL));
    outputConfigurationPanel.add(outputRuleCoverageFilename, "wrap");

    outputConfigurationPanel.add(new JLabel(OUTPUT_GRAMMAR_LABEL));
    outputConfigurationPanel.add(outputGrammarFileName, "wrap");

    outputConfigurationPanel.add(new JLabel(OUTPUT_ANOMALY_LABEL));
    outputConfigurationPanel.add(outputAnomalyFileName, "wrap");

    outputConfigurationPanel.add(new JLabel(OUTPUT_CHARTS_LABEL));
    outputConfigurationPanel.add(outputChartsFolderName, "wrap");

    res.add(outputConfigurationPanel, "pad 0 0 0 0");

    return res;
  }

  public void setValues(int strategyValue, int algorithmValue, double threshold) {
    strategyRadioButtons[strategyValue].setSelected(true);
    giRadioButtons[algorithmValue].setSelected(true);
    normalizationThresholdField.setText(Double.toString(threshold));
  }

  public int getSelectedStrategyValue() {
    if (STRATEGY_COUNT.equalsIgnoreCase(strategyRadioGroup.getSelection().getActionCommand())) {
      return 0;
    }
    else if (STRATEGY_LEVEL.equalsIgnoreCase(strategyRadioGroup.getSelection().getActionCommand())) {
      return 1;
    }
    else if (STRATEGY_OCCURRENCE.equalsIgnoreCase(strategyRadioGroup.getSelection()
        .getActionCommand())) {
      return 2;
    }
    else if (STRATEGY_YIELD.equalsIgnoreCase(strategyRadioGroup.getSelection().getActionCommand())) {
      return 3;
    }
    else if (STRATEGY_PRODUCT
        .equalsIgnoreCase(strategyRadioGroup.getSelection().getActionCommand())) {
      return 4;
    }
    return 0;
  }

  public int getSelectedAlgorithmValue() {
    if (GI_SEQUITUR.equalsIgnoreCase(giRadioGroup.getSelection().getActionCommand())) {
      return 0;
    }
    else if (GI_REPAIR.equalsIgnoreCase(giRadioGroup.getSelection().getActionCommand())) {
      return 1;
    }
    return 0;
  }

  /**
   * Normalization threshold getter.
   * 
   * @return The normalization threshold value.
   */
  public Double getNormalizationThreshold() {
    return Double.valueOf(normalizationThresholdField.getText());
  }

  /**
   * Get the rule coverage output filename.
   * 
   * @return
   */
  public String getRuleCoverageFileName() {
    return outputRuleCoverageFilename.getText();
  }

  /**
   * Get the rule coverage output filename.
   * 
   * @return
   */
  public String getGrammarOutputFileName() {
    return outputGrammarFileName.getText();
  }

  /**
   * Get the rule coverage output filename.
   * 
   * @return
   */
  public String getChartsFolderName() {
    return outputChartsFolderName.getText();
  }

  /**
   * Get the rule coverage output filename.
   * 
   * @return
   */
  public String getAnomalyOutputFileName() {
    return outputAnomalyFileName.getText();
  }
}
