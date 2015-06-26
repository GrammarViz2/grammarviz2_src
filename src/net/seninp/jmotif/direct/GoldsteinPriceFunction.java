package net.seninp.jmotif.direct;

public class GoldsteinPriceFunction {

  public static double compute(double x1, double x2) {

    double z1 = x1 / 10;
    double z2 = x2 / 10;

    double p1 = (z1 + z2 + 1) * (z1 + z2 + 1);
    double p2 = (19 - 14 * z1 + 3 * z1 * z1 - 14 * z2 + 6 * z1 * z2 + 3 * z2 * z2);
    double p3 = (2 * z1 - 3 * z2) * (2 * z1 - 3 * z2);
    double p4 = (18 - 32 * z1 + 12 * z1 * z1 + 48 * z2 - 36 * z1 * z2 + 27 * z2 * z2);

    double res = (1 + p1 * p2) * (30 + p3 * p4);

    System.out.println(x1 + ", " + x2 + ", " + res);

    return res;
  }

  public static double valueAt(Point point) {
    return compute(point.toArray()[0], point.toArray()[1]);
  }

}
