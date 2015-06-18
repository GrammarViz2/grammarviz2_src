package net.seninp.grammarviz.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Observable;
import javax.swing.JFileChooser;
import net.seninp.grammarviz.logic.UserSession;
import net.seninp.grammarviz.model.GrammarVizMessage;
import net.seninp.grammarviz.model.GrammarVizModel;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;

/**
 * Implements the Controler component for GrammarViz2 GUI MVC.
 * 
 * @author psenin
 * 
 */
public class GrammarVizController extends Observable implements ActionListener {

  private GrammarVizModel model;

  private UserSession session;

  public GrammarVizController(GrammarVizModel model) {
    super();
    this.model = model;
    this.session = new UserSession();
  }

  /**
   * Implements a listener for the "Browse" button at GUI; opens FileChooser and so on.
   * 
   * @return the action listener.
   */
  public ActionListener getBrowseFilesListener() {

    ActionListener selectDataActionListener = new ActionListener() {

      public void actionPerformed(ActionEvent e) {

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Data File");

        String filename = model.getDataFileName();
        if (!((null == filename) || filename.isEmpty())) {
          fileChooser.setSelectedFile(new File(filename));
        }

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
          File file = fileChooser.getSelectedFile();

          // here it calls to model -informing about the selected file.
          //
          model.setDataSource(file.getAbsolutePath());
        }
      }

    };
    return selectDataActionListener;
  }

  public ActionListener getLoadFileListener() {
    ActionListener loadDataActionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        model.loadData(e.getActionCommand());
      }
    };
    return loadDataActionListener;
  }

  /**
   * This provide Process action listener. Gets all the parameters from the session component
   * 
   * @return
   */
  public ActionListener getProcessDataListener() {
    ActionListener loadDataActionListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {

        int algorithm = session.getGiAlgorithm();
        boolean useSlidingWindow = session.isUseSlidingWindow();
        NumerosityReductionStrategy numerosityReductionStrategy = session
            .getNumerosityReductionStrategy();
        int windowSize = session.getSaxWindow();
        int paaSize = session.getSaxPAA();
        int alphabetSize = session.getSaxAlphabet();
        double normalizationThreshold = session.getNormalizationThreshold();
        String grammarOutputFileName = session.getGrammarOutputFileName();
        log("controller: running inference with settings alg: " + algorithm + ", sliding window: "
            + useSlidingWindow + ", num.reduction:" + numerosityReductionStrategy.toString()
            + ", SAX window: " + windowSize + ", SAX paa: " + paaSize + ", SAX alphabet: "
            + alphabetSize + ", norm.threshold: " + normalizationThreshold + ", grammar filename: "
            + grammarOutputFileName);

        try {
          model.processData(algorithm, useSlidingWindow, numerosityReductionStrategy, windowSize,
              paaSize, alphabetSize, normalizationThreshold, grammarOutputFileName);
        }
        catch (IOException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }

      }
    };
    return loadDataActionListener;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    this.setChanged();
    notifyObservers(new GrammarVizMessage(GrammarVizMessage.STATUS_MESSAGE,
        "controller: Unknown action performed " + e.getActionCommand()));
  }

  /**
   * Gets the current session.
   * 
   * @return
   */
  public UserSession getSession() {
    return this.session;
  }

  /**
   * Performs logging messages distribution.
   * 
   * @param message the message to log.
   */
  private void log(String message) {
    this.setChanged();
    notifyObservers(new GrammarVizMessage(GrammarVizMessage.STATUS_MESSAGE, "controller: " + message));
  }
}
