package edu.hawaii.jmotif.grammarviz;

import edu.hawaii.jmotif.grammarviz.controller.SequiturController;
import edu.hawaii.jmotif.grammarviz.model.SequiturModel;
import edu.hawaii.jmotif.grammarviz.view.SequiturView;

/**
 * Main runnable of Sequitur GUI.
 * 
 * @author psenin
 * 
 */
public class GrammarVizGUI {

  /** The model instance. */
  private static SequiturModel model;

  /** The controller instance. */
  private static SequiturController controller;

  /** The view instance. */
  private static SequiturView view;

  /**
   * Runnable GIU.
   * 
   * @param args None used.
   */
  public static void main(String[] args) {

    /** On the stage. */
    System.out.println("Starting GrammarViz 2.0 ...");

    // this is the Apple fix
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "SAXSequitur");

    // model...
    model = new SequiturModel();

    // controller...
    controller = new SequiturController(model);

    // view...
    view = new SequiturView(controller);

    // make sure these two met...
    model.addObserver(view);
    controller.addObserver(view);
    
    // live!!!
    view.showGUI();

  }

}
