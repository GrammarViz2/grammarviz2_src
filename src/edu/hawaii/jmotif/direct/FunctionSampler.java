package edu.hawaii.jmotif.direct;

public class FunctionSampler {

  private static final double X1_START = -2.0;
  private static final double X1_END = 2.0;

  private static final double X2_START = -2.0;
  private static final double X2_END = 2.0;

  private static final double step = 0.1;

  public static void main(String[] args) {

    double x1 = X1_START;
    double x2 = X2_START;

    while ((x1 < X1_END) && (x2 < X2_END)) {

      double value = GoldsteinPriceFunc.compute(x1, x2);

      System.out.println(x1 + ", " + x2 + ", " + value);

      // increment variable values
      //
      x1 = x1 + step;
      if (x1 > X1_END && x2 < X2_END) {
        x1 = X1_START;
        x2 = x2 + step;
      }
    }

  }
}
