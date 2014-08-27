package edu.hawaii.jmotif.sequitur.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Observable;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import edu.hawaii.jmotif.sax.NumerosityReductionStrategy;
import edu.hawaii.jmotif.sequitur.model.SequiturMessage;
import edu.hawaii.jmotif.sequitur.model.SequiturModel;

public class SequiturController extends Observable implements ActionListener {

  private SequiturModel model;

  public SequiturController(SequiturModel model) {
    super();
    this.model = model;
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

  /**
   * This listener is not functional.
   * 
   * @return
   */
  public DocumentListener getDataFileNameListener() {

    DocumentListener filenamedocumentListener = new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        handleDocumentEvent(e);
      }

      public void removeUpdate(DocumentEvent e) {
        handleDocumentEvent(e);
      }

      public void insertUpdate(DocumentEvent e) {
        handleDocumentEvent(e);
      }

      private void handleDocumentEvent(DocumentEvent e) {
        @SuppressWarnings("unused")
        String filename = null;
        try {
          filename = e.getDocument().getText(0, e.getDocument().getLength());
        }
        catch (BadLocationException ble) {

        }
        // model.setDataSource(filename);
      }
    };
    return filenamedocumentListener;
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
        String[] split = e.getActionCommand().split(",");
        int algorithm = Integer.parseInt(split[0]);
        boolean useSlidingWindow = Boolean.parseBoolean(split[1]);
        NumerosityReductionStrategy numerosityReductionStrategy = NumerosityReductionStrategy
            .fromString(split[2]);
        int windowSize = Integer.parseInt(split[3]);
        int paaSize = Integer.parseInt(split[4]);
        int alphabetSize = Integer.parseInt(split[5]);
        double normalizationThreshold = Double.parseDouble(split[6]);

        try {
          model.processData(algorithm, useSlidingWindow, numerosityReductionStrategy, windowSize,
              paaSize, alphabetSize, normalizationThreshold);
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
}
