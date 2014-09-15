package edu.hawaii.jmotif.grammarviz.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Observable;
import javax.swing.JFileChooser;
import edu.hawaii.jmotif.grammarviz.logic.UserSession;
import edu.hawaii.jmotif.grammarviz.model.SequiturMessage;
import edu.hawaii.jmotif.grammarviz.model.SequiturModel;
import edu.hawaii.jmotif.sax.NumerosityReductionStrategy;

/**
 * Implements the Controler component for GrammarViz2 GUI MVC.
 * 
 * @author psenin
 * 
 */
public class SequiturController extends Observable implements ActionListener {

  private SequiturModel model;

  private UserSession session;

  public SequiturController(SequiturModel model) {
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
        String outputFileName = session.getGrammarOutputFileName();

        try {
          model.processData(algorithm, useSlidingWindow, numerosityReductionStrategy, windowSize,
              paaSize, alphabetSize, normalizationThreshold, outputFileName);
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
    notifyObservers(new SequiturMessage(SequiturMessage.STATUS_MESSAGE,
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

}
