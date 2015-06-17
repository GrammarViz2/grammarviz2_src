package net.seninp.grammarviz.view;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Observable;
import java.util.Observer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import net.miginfocom.swing.MigLayout;
import net.seninp.grammarviz.controller.SequiturController;
import net.seninp.grammarviz.logic.MotifChartData;
import net.seninp.grammarviz.model.SequiturMessage;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * View component of Sequitur MVC GUI.
 * 
 * @author psenin
 * 
 */
public class SequiturView implements Observer, ActionListener {

  private static final String APPLICATION_VERSION = "GrammarViz 2.0: visualizing time series grammars";

  // static block - we instantiate the logger
  //
  // logging stuff
  //
  private static Logger consoleLogger;
  private static Level LOGGING_LEVEL = Level.INFO;
  static {
    consoleLogger = (Logger) LoggerFactory.getLogger(SequiturView.class);
    consoleLogger.setLevel(LOGGING_LEVEL);
  }

  // relevant string constants go here
  //
  private static final String COMMA = ",";
  private static final String CR = "\n";
  private static final String TITLE_FONT = "helvetica";

  // String is the king - constants for actions
  //
  /** Select data file action key. */
  private static final String SELECT_FILE = "select_file";
  /** Load data action key. */
  private static final String LOAD_DATA = "load_data";
  /** Process data action key. */
  private static final String PROCESS_DATA = "process_data";
  /** Reduce overlaps data action key. */
  protected static final String CLUSTER_RULES = "cluster_rules";
  /** Find periodicity action key. */
  protected static final String FIND_PERIODICITY = "find_periodicity";
  /** Reduce overlaps data action key. */
  protected static final String DISPLAY_CHART = "display_sequitur_chart";
  /** Display density data action key. */
  protected static final String DISPLAY_DENSITY_DATA = "display_density";
  /** Display density data action key. */
  protected static final String DISPLAY_LENGTH_HISTOGRAM = "display_length_histogram";
  /** Display density data action key. */
  protected static final String DISPLAY_ANOMALIES_DATA = "display_anomalies_data";
  /** Save chart action key. */
  protected static final String SAVE_CHART = "save_chart";

  /** Chunking/Sliding switch action key. */
  protected static final String USE_SLIDING_WINDOW_ACTION_KEY = "sliding_window_key";

  /** The action command for Options dialog. */
  private static final String OPTIONS_MENU_ITEM = "menu_item_options";

  /** The action command for About dialog. */
  private static final String ABOUT_MENU_ITEM = "menu_item_about";

  /** Frame for the GUI. */
  private static final JFrame frame = new JFrame(APPLICATION_VERSION);

  /** The main menu bar. */
  private static final JMenuBar menuBar = new JMenuBar();

  /** Global controller handler - controller is supplier of action handlers. */
  private SequiturController controller;

  // data source related variables
  //
  private JPanel dataSourcePane;
  private JTextField dataFilePathField;
  private JButton selectFileButton;
  private JTextField dataRowsLimitTextField;

  // SAX parameters related fields
  //
  private JPanel saxParametersPane;
  private JCheckBox useSlidingWindowCheckBox;
  private JLabel windowSizeLabel;
  private JTextField SAXwindowSizeField;
  private JLabel paaSizeLabel;
  private JTextField SAXpaaSizeField;
  private JTextField SAXalphabetSizeField;

  private JPanel numerosityReductionPane;
  private ButtonGroup numerosityButtonsGroup = new ButtonGroup();
  private JRadioButton numerosityReductionOFFButton = new JRadioButton("OFF");
  private JRadioButton numerosityReductionExactButton = new JRadioButton("Exact");
  private JRadioButton numerosityReductionMINDISTButton = new JRadioButton("MINDIST");

  private NumerosityReductionStrategy selectedNumerosityReductionStrategy = NumerosityReductionStrategy.EXACT;

  // The process action pane
  //
  private JPanel processPane;

  // data chart place
  //
  private SequiturChartPanel dataChartPane;

  // sequitur rule list
  //
  private JTabbedPane tabbedRulesPane;
  private SequiturRulesPanel sequiturRulesPane;
  private PackedRulesPanel packedRulesPane;
  private RulesPeriodicityPanel rulesPeriodicityPane;
  private AnomaliesPanel anomaliesPane;

  // small rule show panel
  //
  private SequiturRuleChartPanel ruleChartPane;

  // workflow pane - buttons
  //
  private JPanel workflowManagementPane;
  private JButton dataLoadButton;
  private JButton processButton;
  private JButton clusterRulesButton;
  private JButton findPeriodicityButton;
  private JButton displayChartButton;
  private JButton displayRulesDensityButton;
  private JButton displayRulesLenHistogramButton;
  private JButton displayAnomaliesButton;
  private JButton saveChartButton;

  // The log panel configuration section
  //
  private SimpleDateFormat logDateFormat = new SimpleDateFormat("HH:mm:ss' '");

  // we keep the data pointer here
  private MotifChartData chartData;

  private boolean isTimeSeriesLoaded = false;

  // logging area
  //
  private static final JTextArea logTextArea = new JTextArea();
  private static final JScrollPane logTextPane = new JScrollPane(logTextArea,
      ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

  /**
   * Constructor.
   * 
   * @param controller The controller used for the application flow control.
   */
  public SequiturView(SequiturController controller) {
    this.controller = controller;
  }

  /**
   * Shows the GUI.
   */
  public void showGUI() {
    // Schedule a job for the event-dispatching thread:
    // creating and showing this application's GUI.
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        try {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
          // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
          // UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        }
        catch (ClassNotFoundException e) {
          System.err.println("ClassNotFoundException: " + e.getMessage());
        }
        catch (InstantiationException e) {
          System.err.println("InstantiationException: " + e.getMessage());
        }
        catch (IllegalAccessException e) {
          System.err.println("IllegalAccessException: " + e.getMessage());
        }
        catch (UnsupportedLookAndFeelException e) {
          System.err.println("UnsupportedLookAndFeelException: " + e.getMessage());
        }
        catch (Exception e) {
          System.err.print(StackTrace.toString(e));
        }

        configureGUI();
      }
    });
  }

  /**
   * Initialize the dialog
   */
  private void configureGUI() {

    // set look and fill
    JFrame.setDefaultLookAndFeelDecorated(true);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // build main UI components
    //
    buildMenuBar();

    buildDataSourcePane();
    buildSAXParamsPane();

    buildChartPane();

    buildSequiturPane();

    buildWorkflowPane();

    buildLogPane();

    // put listeners in place for the Sequitur rule panel
    //
    sequiturRulesPane.addPropertyChangeListener(dataChartPane);
    sequiturRulesPane.addPropertyChangeListener(ruleChartPane);
    dataChartPane.addPropertyChangeListener(SequiturMessage.MAIN_CHART_CLICKED_MESSAGE,
        sequiturRulesPane);

    // put listeners in place for the Clustered/Packed rule panel
    //
    packedRulesPane.addPropertyChangeListener(dataChartPane);
    // packedRulesPane.addPropertyChangeListener(ruleChartPane);
    // dataChartPane.addPropertyChangeListener(SequiturMessage.MAIN_CHART_CLICKED_MESSAGE,
    // packedRulesPane);

    // put listeners in place for the Periodicity rule panel
    //
    rulesPeriodicityPane.addPropertyChangeListener(dataChartPane);
    rulesPeriodicityPane.addPropertyChangeListener(ruleChartPane);
    dataChartPane.addPropertyChangeListener(SequiturMessage.MAIN_CHART_CLICKED_MESSAGE,
        rulesPeriodicityPane);

    // put listeners in place for the Anomalies rule panel
    //
    anomaliesPane.addPropertyChangeListener(dataChartPane);
    anomaliesPane.addPropertyChangeListener(ruleChartPane);

    // set the main panel layout
    MigLayout mainFrameLayout = new MigLayout("", "[fill,grow,center]",
        "[][][fill,grow 50][fill,grow 50][][]");
    frame.getContentPane().setLayout(mainFrameLayout);

    // set the menu bar
    frame.setJMenuBar(menuBar);

    // place panels
    frame.getContentPane().add(dataSourcePane, "wrap");

    frame.getContentPane().add(saxParametersPane, "grow, split");
    frame.getContentPane().add(numerosityReductionPane, "split");
    frame.getContentPane().add(processPane, "wrap");

    frame.getContentPane().add(dataChartPane, "wrap");

    frame.getContentPane().add(tabbedRulesPane, "w 70%, split");
    frame.getContentPane().add(ruleChartPane, "w 30%, wrap");

    frame.getContentPane().add(workflowManagementPane, "wrap");

    frame.getContentPane().add(logTextPane, "h 80:100:100,wrap");

    // Show frame
    frame.pack();
    frame.setSize(new Dimension(1000, 840));
    frame.setVisible(true);
  }

  /**
   * Build the application menu bar.
   */
  private void buildMenuBar() {

    // Build the File menu.
    //
    //
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);
    fileMenu.getAccessibleContext().setAccessibleDescription("The file menu");
    // Open file item
    JMenuItem openFileItem = new JMenuItem("Select", KeyEvent.VK_O);
    openFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
    openFileItem.getAccessibleContext().setAccessibleDescription("Open a data file");
    openFileItem.setActionCommand(SELECT_FILE);
    openFileItem.addActionListener(this);
    fileMenu.add(openFileItem);
    // add a separator
    fileMenu.addSeparator();
    // an exit item
    JMenuItem exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
    exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
    exitItem.getAccessibleContext().setAccessibleDescription("Exit from here");
    exitItem.addActionListener(this);
    fileMenu.add(exitItem);

    // Build the Options menu.
    //
    //
    JMenu settingsMenu = new JMenu("Settings");
    settingsMenu.setMnemonic(KeyEvent.VK_S);
    settingsMenu.getAccessibleContext().setAccessibleDescription("Settings menu");
    // an exit item
    JMenuItem optionsItem = new JMenuItem("GrammarViz options", KeyEvent.VK_P);
    optionsItem.setActionCommand(OPTIONS_MENU_ITEM);
    optionsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
    optionsItem.getAccessibleContext().setAccessibleDescription("Options");
    optionsItem.addActionListener(this);
    settingsMenu.add(optionsItem);

    // Build the About menu.
    JMenu helpMenu = new JMenu("Help");
    helpMenu.setMnemonic(KeyEvent.VK_F1);
    helpMenu.getAccessibleContext().setAccessibleDescription("Help & About");

    // a help item
    JMenuItem helpItem = new JMenuItem("Help", KeyEvent.VK_H);
    helpItem.getAccessibleContext().setAccessibleDescription("Get some help here.");
    exitItem.addActionListener(controller);
    helpMenu.add(helpItem);

    // an about item
    JMenuItem aboutItem = new JMenuItem("About", KeyEvent.VK_A);
    aboutItem.getAccessibleContext().setAccessibleDescription("About the app.");
    aboutItem.setActionCommand(ABOUT_MENU_ITEM);
    aboutItem.addActionListener(this);
    helpMenu.add(aboutItem);

    // make sure that controller is connected with Exit item
    //
    exitItem.addActionListener(controller);

    menuBar.add(fileMenu);
    menuBar.add(settingsMenu);
    menuBar.add(helpMenu);
  }

  private void buildDataSourcePane() {

    dataSourcePane = new JPanel();

    // Layout, insets: T, L, B, R.
    dataSourcePane.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(BevelBorder.LOWERED), "Data source", TitledBorder.LEFT,
        TitledBorder.CENTER, new Font(TITLE_FONT, Font.PLAIN, 10)));
    MigLayout dataSourcePaneLayout = new MigLayout("insets 0 2 2 2",
        "[][fill,grow 80][]10[][fill, grow 20][]", "[]");
    dataSourcePane.setLayout(dataSourcePaneLayout);

    // file label
    //
    JLabel fileNameLabel = new JLabel("Data file: ");

    // field
    dataFilePathField = new JTextField("");
    fileNameLabel.setLabelFor(dataFilePathField);

    // the Browse button
    selectFileButton = new JButton("Browse...");
    selectFileButton.setMnemonic('B');

    dataSourcePane.add(fileNameLabel, "");
    dataSourcePane.add(dataFilePathField, "");
    dataSourcePane.add(selectFileButton, "");

    // add the action listener
    //
    selectFileButton.addActionListener(controller.getBrowseFilesListener());
    // dataFilePathField.getDocument().addDocumentListener(controller.getDataFileNameListener());

    // data rows interval section
    //
    JLabel lblCountRows = new JLabel("Row limit (0=all):");
    dataRowsLimitTextField = new JTextField("0");
    dataSourcePane.add(lblCountRows, "");
    dataSourcePane.add(dataRowsLimitTextField, "");

    // the load button
    //
    dataLoadButton = new JButton("Load data");
    dataLoadButton.setMnemonic('L');
    // add the action listener
    dataLoadButton.setActionCommand(LOAD_DATA);
    dataLoadButton.addActionListener(this);
    dataSourcePane.add(dataLoadButton, "");

  }

  private void buildSAXParamsPane() {

    saxParametersPane = new JPanel();
    saxParametersPane.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(BevelBorder.LOWERED), "SAX parameteres",
        TitledBorder.LEFT, TitledBorder.CENTER, new Font(TITLE_FONT, Font.PLAIN, 10)));

    // insets: T, L, B, R.
    MigLayout saxPaneLayout = new MigLayout("insets 3 2 2 2",
        "[][]10[][fill,grow]10[][fill,grow]10[][fill,grow]", "[]");
    saxParametersPane.setLayout(saxPaneLayout);

    // the sliding window parameter
    JLabel slideWindowLabel = new JLabel("Slide the window");
    useSlidingWindowCheckBox = new JCheckBox();
    useSlidingWindowCheckBox.setSelected(this.controller.getSession().isUseSlidingWindow());
    useSlidingWindowCheckBox.setActionCommand(USE_SLIDING_WINDOW_ACTION_KEY);
    useSlidingWindowCheckBox.addActionListener(this);

    windowSizeLabel = new JLabel("Window size:");
    SAXwindowSizeField = new JTextField(String.valueOf(this.controller.getSession().getSaxWindow()));

    paaSizeLabel = new JLabel("PAA size:");
    SAXpaaSizeField = new JTextField(String.valueOf(this.controller.getSession().getSaxPAA()));

    JLabel alphabetSizeLabel = new JLabel("Alphabet size:");
    SAXalphabetSizeField = new JTextField(String.valueOf(this.controller.getSession()
        .getSaxAlphabet()));

    saxParametersPane.add(slideWindowLabel);
    saxParametersPane.add(useSlidingWindowCheckBox);

    saxParametersPane.add(windowSizeLabel);
    saxParametersPane.add(SAXwindowSizeField);

    saxParametersPane.add(paaSizeLabel);
    saxParametersPane.add(SAXpaaSizeField);

    saxParametersPane.add(alphabetSizeLabel);
    saxParametersPane.add(SAXalphabetSizeField);

    // numerosity reduction pane
    //
    numerosityReductionPane = new JPanel();
    numerosityReductionPane.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(BevelBorder.LOWERED), "Numerosity reduction",
        TitledBorder.LEFT, TitledBorder.CENTER, new Font(TITLE_FONT, Font.PLAIN, 10)));

    // insets: T, L, B, R.
    MigLayout numerosityPaneLayout = new MigLayout("insets 3 2 6 2", "[]5[]5[]", "[]");
    numerosityReductionPane.setLayout(numerosityPaneLayout);

    numerosityReductionOFFButton.setActionCommand(NumerosityReductionStrategy.NONE.toString());
    numerosityButtonsGroup.add(numerosityReductionOFFButton);
    numerosityReductionOFFButton.addActionListener(this);
    numerosityReductionPane.add(numerosityReductionOFFButton);

    numerosityReductionExactButton.setActionCommand(NumerosityReductionStrategy.EXACT.toString());
    numerosityButtonsGroup.add(numerosityReductionExactButton);
    numerosityReductionExactButton.addActionListener(this);
    numerosityReductionPane.add(numerosityReductionExactButton);

    numerosityReductionMINDISTButton.setActionCommand(NumerosityReductionStrategy.MINDIST
        .toString());
    numerosityButtonsGroup.add(numerosityReductionMINDISTButton);
    numerosityReductionMINDISTButton.addActionListener(this);
    numerosityReductionPane.add(numerosityReductionMINDISTButton);

    this.controller.getSession().setNumerosityReductionStrategy(NumerosityReductionStrategy.EXACT);
    numerosityReductionExactButton.setSelected(true);

    // PROCESS button
    //
    processButton = new JButton("Process data");
    processButton.setMnemonic('P');
    processButton.setActionCommand(PROCESS_DATA);
    processButton.addActionListener(this);

    processPane = new JPanel();
    processPane.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(BevelBorder.LOWERED), "Hit to run GI", TitledBorder.LEFT,
        TitledBorder.CENTER, new Font(TITLE_FONT, Font.PLAIN, 10)));
    // insets: T, L, B, R.
    MigLayout processPaneLayout = new MigLayout("insets 3 2 4 2", "5[]5", "[]");
    processPane.setLayout(processPaneLayout);
    processPane.add(processButton, "");

  }

  private void buildChartPane() {
    // MotifChartPanel _chart = new MotifChartPanel(null);
    dataChartPane = new SequiturChartPanel();
    dataChartPane.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(BevelBorder.LOWERED), "Data display", TitledBorder.LEFT,
        TitledBorder.CENTER, new Font(TITLE_FONT, Font.PLAIN, 10)));
    MigLayout chartPaneLayout = new MigLayout("insets 0 0 0 0", "[fill,grow]", "[fill,grow]");
    dataChartPane.setLayout(chartPaneLayout);
  }

  /**
   * Builds all objects and widgets related to Sequitur tables.
   */
  private void buildSequiturPane() {

    // first the tabbed pane which holds other panels
    //
    tabbedRulesPane = new JTabbedPane();

    // now add the raw Sequitur rules panel
    //
    sequiturRulesPane = new SequiturRulesPanel();
    MigLayout sequiturPaneLayout = new MigLayout(",insets 0 0 0 0", "[fill,grow]", "[fill,grow]");
    sequiturRulesPane.setLayout(sequiturPaneLayout);

    tabbedRulesPane.addTab("Grammar rules", null, sequiturRulesPane, "Shows grammar rules");
    // tabbedRulesPane.addTab("Sequitur", sequiturRulesPane);
    // tabbedRulesPane.setIgnoreRepaint(false);

    // now add the prototype of reduced rules panel
    //
    packedRulesPane = new PackedRulesPanel();
    MigLayout packedRulesPaneLayout = new MigLayout(",insets 0 0 0 0", "[fill,grow]", "[fill,grow]");
    packedRulesPane.setLayout(packedRulesPaneLayout);
    tabbedRulesPane.addTab("Regularized rules", null, packedRulesPane,
        "Shows reduced by overlapping criterion rules subset");

    // now add the rules periodicity panel
    //
    rulesPeriodicityPane = new RulesPeriodicityPanel();
    MigLayout rulesPeriodicityPaneLayout = new MigLayout(",insets 0 0 0 0", "[fill,grow]",
        "[fill,grow]");
    rulesPeriodicityPane.setLayout(rulesPeriodicityPaneLayout);
    tabbedRulesPane.addTab("Rules periodicity", null, rulesPeriodicityPane,
        "Shows rules periodicity");

    // now add the anomalies panel
    //
    anomaliesPane = new AnomaliesPanel();
    MigLayout anomaliesPaneLayout = new MigLayout(",insets 0 0 0 0", "[fill,grow]", "[fill,grow]");
    anomaliesPane.setLayout(anomaliesPaneLayout);
    tabbedRulesPane.addTab("GrammarViz anomalies", null, anomaliesPane,
        "Shows anomalous subsequences");

    // now format the tabbed pane
    //
    tabbedRulesPane.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(BevelBorder.LOWERED),
        "Grammar rules (search in list by clicking into list and pressing CTRL-F)",
        TitledBorder.LEFT, TitledBorder.CENTER, new Font(TITLE_FONT, Font.PLAIN, 10)));
    // MigLayout tabbedPaneLayout = new MigLayout(",insets 0 0 0 2", "[fill,grow]", "[fill,grow]");
    // tabbedRulesPane.setLayout(tabbedPaneLayout);

    // the rule chart panel
    //
    ruleChartPane = new SequiturRuleChartPanel();
    ruleChartPane.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(BevelBorder.LOWERED), "Rule subsequences, normalized",
        TitledBorder.LEFT, TitledBorder.CENTER, new Font(TITLE_FONT, Font.PLAIN, 10)));
    MigLayout ruleChartPaneLayout = new MigLayout(",insets 0 2 0 0", "[fill,grow]", "[fill,grow]");
    ruleChartPane.setLayout(ruleChartPaneLayout);

  }

  private void buildWorkflowPane() {

    workflowManagementPane = new JPanel();
    workflowManagementPane.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(BevelBorder.LOWERED),
        "Workflow management: load > process > display", TitledBorder.LEFT, TitledBorder.CENTER,
        new Font(TITLE_FONT, Font.PLAIN, 10)));
    MigLayout workflowPaneLayout = new MigLayout(",insets 2 2 2 2", "[fill,grow]", "[fill,grow]");
    workflowManagementPane.setLayout(workflowPaneLayout);

    clusterRulesButton = new JButton("Prune rules");
    clusterRulesButton.setMnemonic('C');
    clusterRulesButton.setActionCommand(CLUSTER_RULES);
    clusterRulesButton.addActionListener(this);

    displayChartButton = new JButton("Grammar rules");
    displayChartButton.setMnemonic('R');
    displayChartButton.setActionCommand(DISPLAY_CHART);
    displayChartButton.addActionListener(this);

    displayRulesDensityButton = new JButton("Rules density");
    displayRulesDensityButton.setMnemonic('D');
    displayRulesDensityButton.setActionCommand(DISPLAY_DENSITY_DATA);
    displayRulesDensityButton.addActionListener(this);

    displayRulesLenHistogramButton = new JButton("Rule length histogram");
    displayRulesLenHistogramButton.setMnemonic('H');
    displayRulesLenHistogramButton.setActionCommand(DISPLAY_LENGTH_HISTOGRAM);
    displayRulesLenHistogramButton.addActionListener(this);

    displayAnomaliesButton = new JButton("Find anomalies");
    displayAnomaliesButton.setMnemonic('A');
    displayAnomaliesButton.setActionCommand(DISPLAY_ANOMALIES_DATA);
    displayAnomaliesButton.addActionListener(this);

    saveChartButton = new JButton("Save Chart");
    saveChartButton.setMnemonic('S');
    saveChartButton.setActionCommand(SAVE_CHART);
    saveChartButton.addActionListener(this);

    // workflowManagementPane.add(processButton);
    workflowManagementPane.add(displayChartButton);
    workflowManagementPane.add(displayRulesLenHistogramButton);
    workflowManagementPane.add(clusterRulesButton);
    workflowManagementPane.add(displayRulesDensityButton);
    workflowManagementPane.add(displayAnomaliesButton);
    workflowManagementPane.add(saveChartButton);

  }

  /**
   * Build the logging panel.
   */
  private void buildLogPane() {
    // logging panel
    logTextArea.setFont(new Font("MonoSpaced", Font.PLAIN, 10));
    logTextArea.setEditable(false);
    logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
    logTextPane.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
    logTextPane.setAutoscrolls(true);
    log(Level.INFO, "running GrammarViz 2.0 demo");
  }

  /**
   * Shut downs the application.
   */
  private void shutdown() {
    Runtime.getRuntime().exit(0);
  }

  /**
   * Logs message.
   * 
   * @param level The logging level to use.
   * @param message The log message.
   */
  protected void log(Level level, String message) {
    message = message.replaceAll("\n", "");
    String dateStr = logDateFormat.format(System.currentTimeMillis());
    if (message.startsWith("model") || message.startsWith("controller")) {
      logTextArea.append(dateStr + message + CR);
    }
    else {
      logTextArea.append(dateStr + "view: " + message + CR);
    }
    logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
    consoleLogger.info(dateStr + message);
  }

  @Override
  public void update(Observable o, Object arg) {
    if (arg instanceof SequiturMessage) {

      final SequiturMessage message = (SequiturMessage) arg;

      // new FileName
      //
      if (SequiturMessage.DATA_FNAME.equalsIgnoreCase(message.getType())) {
        Runnable doHighlight = new Runnable() {
          @Override
          public void run() {
            dataFilePathField.setText((String) message.getPayload());
            dataFilePathField.repaint();
          }
        };
        SwingUtilities.invokeLater(doHighlight);
      }

      // new log message
      //
      else if (SequiturMessage.STATUS_MESSAGE.equalsIgnoreCase(message.getType())) {
        log(Level.ALL, (String) message.getPayload());
      }

      // chart object
      //
      else if (SequiturMessage.CHART_MESSAGE.equalsIgnoreCase(message.getType())) {

        MotifChartData chartData = (MotifChartData) message.getPayload();
        // TODO: this is ridiculous below here
        //
        this.chartData = (MotifChartData) message.getPayload();
        // setting the chart first
        //
        dataChartPane.setChartData(chartData, this.controller.getSession());

        // and the rules pane second
        //
        sequiturRulesPane.setChartData(chartData);

        // and the "snapshots panel"
        //
        ruleChartPane.setChartData(chartData);

        // and the rules periodicity panel
        //
        rulesPeriodicityPane.setChartData(chartData);

        // and the anomalies panel
        //
        anomaliesPane.setChartData(chartData);

        // dataChartPane.getChart().setNotify(true);
        frame.validate();
        frame.repaint();
      }

      else if (SequiturMessage.TIME_SERIES_MESSAGE.equalsIgnoreCase(message.getType())) {

        // setting the chart first
        //
        dataChartPane.showTimeSeries((double[]) message.getPayload());

        sequiturRulesPane.clear();
        ruleChartPane.clear();
        rulesPeriodicityPane.clear();
        anomaliesPane.clear();

        // dataChartPane.getChart().setNotify(true);
        // frame.validate();
        frame.repaint();

        this.isTimeSeriesLoaded = true;
        this.chartData = null;
      }
    }

  }

  @Override
  public void actionPerformed(ActionEvent arg) {

    // get the action command code
    //
    String command = arg.getActionCommand();

    // treating options
    //
    if (OPTIONS_MENU_ITEM.equalsIgnoreCase(command)) {
      log(Level.INFO, "options menu action performed");

      ParametersPane parametersPanel = new ParametersPane(this.controller.getSession());

      ParametersDialog parametersDialog = new ParametersDialog(frame, parametersPanel,
          this.controller.getSession());

      parametersDialog.setVisible(true);
    }

    // showing up the about dialog
    //
    if (ABOUT_MENU_ITEM.equalsIgnoreCase(command)) {
      log(Level.INFO, "about menu action performed");
      AboutGrammarVizDialog dlg = new AboutGrammarVizDialog(frame);
      dlg.clearAndHide();
    }

    if (SELECT_FILE.equalsIgnoreCase(command)) {
      log(Level.INFO, "select file action performed");
      controller.getBrowseFilesListener().actionPerformed(null);
    }

    if (LOAD_DATA.equalsIgnoreCase(command)) {
      log(Level.INFO, "load data action performed");
      this.isTimeSeriesLoaded = false;
      if (this.dataFilePathField.getText().isEmpty()) {
        raiseValidationError("The file is not yet selected.");
      }
      else {
        String loadLimit = this.dataRowsLimitTextField.getText();
        this.controller.getLoadFileListener().actionPerformed(new ActionEvent(this, 1, loadLimit));
      }
    }

    else if (PROCESS_DATA.equalsIgnoreCase(command)) {
      log(Level.INFO, "process data action performed");
      if (this.isTimeSeriesLoaded) {
        // check the values for window/paa/alphabet, etc.
        this.controller.getSession().setSaxWindow(
            Integer.valueOf(this.SAXwindowSizeField.getText()));
        this.controller.getSession().setSaxPAA(Integer.valueOf(this.SAXpaaSizeField.getText()));
        this.controller.getSession().setSaxAlphabet(
            Integer.valueOf(this.SAXalphabetSizeField.getText()));
        this.controller.getProcessDataListener().actionPerformed(new ActionEvent(this, 2, null));
      }
      else {
        raiseValidationError("The timeseries is not loaded yet.");
      }
    }

    else if (DISPLAY_CHART.equalsIgnoreCase(command)) {
      log(Level.INFO, "display chart action performed");
      if (null == this.chartData) {
        raiseValidationError("No chart data recieved yet.");
      }
      else {
        dataChartPane.resetChartPanel();
        sequiturRulesPane.resetSelection();
        ruleChartPane.resetChartPanel();
      }
    }

    else if (DISPLAY_DENSITY_DATA.equalsIgnoreCase(command)) {
      log(Level.INFO, "display density plot action performed");
      if (null == this.chartData) {
        raiseValidationError("No chart data recieved yet.");
      }
      else {
        ruleChartPane.resetChartPanel();
        this.dataChartPane.actionPerformed(new ActionEvent(this, 0, DISPLAY_DENSITY_DATA));
      }
    }

    else if (DISPLAY_LENGTH_HISTOGRAM.equalsIgnoreCase(command)) {
      log(Level.INFO, "display rule length histogram action performed");
      if (null == this.chartData) {
        raiseValidationError("No chart data recieved yet.");
      }
      else {
        ruleChartPane.resetChartPanel();
        this.dataChartPane.actionPerformed(new ActionEvent(this, 1, DISPLAY_LENGTH_HISTOGRAM));
      }
    }

    else if (DISPLAY_ANOMALIES_DATA.equalsIgnoreCase(command)) {
      log(Level.INFO, "find/display anomalies action performed");
      if (null == this.chartData) {
        raiseValidationError("No chart data recieved yet.");
      }
      else {

        log(Level.INFO, "going to run anomalies search, this takes time, please wait... ");

        try {
          this.chartData.addObserver(this);

          this.chartData.findAnomalies();
          this.anomaliesPane.updateAnomalies();
          this.anomaliesPane.resetPanel();

          this.chartData.deleteObserver(this);
        }
        catch (TSException e) {
          String errorTrace = StackTrace.toString(e);
          log(Level.ALL, errorTrace);
        }
      }
    }

    else if (SAVE_CHART.equalsIgnoreCase(command)) {
      log(Level.INFO, "save chart action performed");
      if (null == this.chartData) {
        raiseValidationError("No chart data recieved yet.");
      }
      else {
        this.dataChartPane.actionPerformed(new ActionEvent(this, 2, SAVE_CHART));
      }
    }

    else if (FIND_PERIODICITY.equalsIgnoreCase(command)) {
      log(Level.INFO, "find periodicity action performed");
      this.dataChartPane.actionPerformed(new ActionEvent(this, 3, FIND_PERIODICITY));
    }

    else if (CLUSTER_RULES.equalsIgnoreCase(command)) {
      log(Level.INFO, "cluster/prune rules action performed");
      if (null == this.chartData) {
        raiseValidationError("No chart data recieved yet.");
      }
      else {

        // fix the parameters
        JTextField lengthThreshold = new JTextField("0.1");
        JTextField overlapThreshold = new JTextField("0.5");

        // build a parameters panel
        JPanel parameterPanel = new JPanel();
        parameterPanel.add(new JLabel("threshold for length:"));
        parameterPanel.add(lengthThreshold);
        parameterPanel.add(Box.createHorizontalStrut(15)); // a spacer
        parameterPanel.add(new JLabel("threshold for overlap:"));
        parameterPanel.add(overlapThreshold);

        // wait for the user
        int result = JOptionPane.showConfirmDialog(null, parameterPanel, "Please Enter Parameter",
            JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {

          double thresholdLength = Double.parseDouble(lengthThreshold.getText());
          double thresholdCommon = Double.parseDouble(overlapThreshold.getText());

          dataChartPane.resetChartPanel();
          packedRulesPane.resetSelection();
          ruleChartPane.resetChartPanel();

          MotifChartData chartData = this.dataChartPane.getChartData();
          chartData.performRemoveOverlapping(thresholdLength, thresholdCommon);

          packedRulesPane.setChartData(chartData);
        }

      }
    }

    else if (USE_SLIDING_WINDOW_ACTION_KEY.equalsIgnoreCase(command)) {
      log(Level.INFO, "sliding window toggled");
      if (this.useSlidingWindowCheckBox.isSelected()) {
        this.controller.getSession().setUseSlidingWindow(true);
        this.windowSizeLabel.setText("Window size:");
        this.windowSizeLabel.setEnabled(true);
        this.windowSizeLabel.setVisible(true);
        this.SAXwindowSizeField
            .setText(String.valueOf(this.controller.getSession().getSaxWindow()));
        this.SAXwindowSizeField.setEnabled(true);
        this.SAXwindowSizeField.setVisible(true);
        this.paaSizeLabel.setText("PAA size:");
      }
      else {
        this.controller.getSession().setUseSlidingWindow(false);
        this.windowSizeLabel.setText("");
        this.windowSizeLabel.setEnabled(false);
        this.windowSizeLabel.setVisible(false);
        this.SAXwindowSizeField.setEnabled(false);
        this.SAXwindowSizeField.setVisible(false);
        this.paaSizeLabel.setText("Segments number:");
      }
    }

    else if (NumerosityReductionStrategy.NONE.toString().equalsIgnoreCase(command)
        || NumerosityReductionStrategy.EXACT.toString().equalsIgnoreCase(command)
        || NumerosityReductionStrategy.MINDIST.toString().equalsIgnoreCase(command)) {
      log(Level.INFO, "numerosity reduction option toggled");
      this.controller.getSession().setNumerosityReductionStrategy(
          NumerosityReductionStrategy.fromString(command));
    }

    else if ("Exit".equalsIgnoreCase(command)) {
      log(Level.INFO, "Exit selected, shutting down, bye! ");
      shutdown();
    }
  }

  private void raiseValidationError(String message) {
    JOptionPane.showMessageDialog(frame, message, "Validation error", JOptionPane.ERROR_MESSAGE);
  }

}
