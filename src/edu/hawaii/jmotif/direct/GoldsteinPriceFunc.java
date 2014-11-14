package edu.hawaii.jmotif.direct;

public class GoldsteinPriceFunc {

  public static double compute(double x1, double x2) {

    double p1 = (x1 + x2 + 1) * (x1 + x2 + 1);
    double p2 = (19 - 14 * x1 + 3 * x1 * x1 - 14 * x2 + 6 * x1 * x2 + 3 * x2 * x2);
    double p3 = (2 * x1 - 3 * x2) * (2 * x1 - 3 * x2);
    double p4 = (18 - 32 * x1 + 12 * x1 * x1 + 48 * x2 - 36 * x1 * x2 + 27 * x2 * x2);

    double res = (1 + p1 * p2) * (30 + p3 * p4);

    System.out.println(x1 + ", " + x2 + ", " + res);

    return res;
  }

  public static double valueAt(Point point) {
    return compute(point.toArray()[0], point.toArray()[1]);
  }

}
