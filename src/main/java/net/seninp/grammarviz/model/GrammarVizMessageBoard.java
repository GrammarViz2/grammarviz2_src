package net.seninp.grammarviz.model;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A small thread-safe publish/subscribe helper that GrammarViz MVC components use to broadcast
 * {@link GrammarVizMessage}s. It replaces the deprecated {@link java.util.Observable} inheritance
 * with composition, so message sources are free to extend other classes and no longer depend on the
 * error-prone {@code setChanged()} / {@code notifyObservers()} protocol.
 *
 * @author psenin
 *
 */
public class GrammarVizMessageBoard {

  /** Registered listeners; copy-on-write so firing never races with (un)subscription. */
  private final CopyOnWriteArrayList<GrammarVizListener> listeners = new CopyOnWriteArrayList<>();

  /**
   * Registers a listener (no-op if it is {@code null} or already registered).
   *
   * @param listener the listener to add.
   */
  public void addListener(GrammarVizListener listener) {
    if (listener != null) {
      this.listeners.addIfAbsent(listener);
    }
  }

  /**
   * Unregisters a listener.
   *
   * @param listener the listener to remove.
   */
  public void removeListener(GrammarVizListener listener) {
    this.listeners.remove(listener);
  }

  /**
   * Broadcasts a message to all registered listeners.
   *
   * @param message the message to broadcast.
   */
  public void fire(GrammarVizMessage message) {
    if (message == null) {
      return;
    }
    for (GrammarVizListener listener : this.listeners) {
      listener.grammarVizMessageReceived(message);
    }
  }

}
