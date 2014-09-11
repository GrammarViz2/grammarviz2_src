package edu.hawaii.jmotif.sampler;

import javax.swing.JPanel;

public class Printer implements TelemetryVisualization<ValuePointListTelemetryColored> {

  @Override
  public void notifyOf(Producer<? extends Iteration<ValuePointListTelemetryColored>> producer) {
    // TODO Auto-generated method stub

  }

  @Override
  public void attachTo(JPanel panel) {
    // TODO Auto-generated method stub

  }

  @Override
  public Class<ValuePointListTelemetryColored> getAcceptableType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void init(Function function) {
    // TODO Auto-generated method stub

  }

}
