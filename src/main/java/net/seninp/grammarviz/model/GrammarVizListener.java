package net.seninp.grammarviz.model;

/**
 * Receives {@link GrammarVizMessage} events broadcast by GrammarViz MVC components. This is the
 * type-safe replacement for the deprecated {@link java.util.Observer} that GrammarViz used to rely
 * on.
 *
 * @author psenin
 *
 */
public interface GrammarVizListener {

  /**
   * Invoked when a GrammarViz component broadcasts a message.
   *
   * @param message the broadcast message (never {@code null}).
   */
  void grammarVizMessageReceived(GrammarVizMessage message);

}
