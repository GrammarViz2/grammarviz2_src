package edu.hawaii.jmotif.saxvsm.cbf;

import cc.mallet.util.Randoms;

/**
 * CBF Domain data generator.
 * 
 * @author psenin
 * 
 */
public class CBFGenerator {

  private static Randoms randoms;

  static {
    randoms = new Randoms();
  }

  public static synchronized int getA() {
    return (int) Math.round(randoms.nextUniform(0d, 16d) + 16.0d);
  }

  public static synchronized int getB(int a) {
    return (int) Math.round(randoms.nextUniform(32d, 96d) + (double) a);
  }

  public static synchronized double eks(int t, int a, int b) {
    if (a <= t && t <= b) {
      return 1.0d;
    }
    else {
      return 0.0d;
    }
  }

  public static synchronized double[] cylinder(int[] t) {
    int a = getA();
    int b = getB(a);
    double[] res = new double[t.length];
    for (int i = 0; i < t.length; i++) {
      res[i] = (6.0d + randoms.nextGaussian()) * eks(t[i], a, b) + randoms.nextGaussian();
    }
    return res;
  }

  public static synchronized double[] bell(int[] t) {
    int a = getA();
    int b = getB(a);
    double[] res = new double[t.length];
    for (int i = 0; i < t.length; i++) {
      res[i] = (6.0d + randoms.nextGaussian()) * eks(t[i], a, b)
          * ((double) (t[i] - a) / (double) ((b - a))) + randoms.nextGaussian();
    }
    return res;
  }

  public static synchronized double[] funnel(int[] t) {
    int a = getA();
    int b = getB(a);
    double[] res = new double[t.length];
    for (int i = 0; i < t.length; i++) {
      res[i] = (6.0d + randoms.nextGaussian()) * eks(t[i], a, b)
          * ((double) (b - t[i]) / (double) (b - a)) + randoms.nextGaussian();
    }
    return res;
  }

  public static double[] twoPeriods(int seriesLength, int i) {
    // TODO Auto-generated method stub
    return null;
  }

}
