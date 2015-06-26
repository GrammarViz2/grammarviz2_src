package net.seninp.jmotif.direct;

public class ShubertFunction {

  public static double compute(double x1, double x2) {

    double p1 = 0.0;
    for (int j = 1; j < 5; j++) {
      p1 = p1 + j * Math.cos((j + 1) * x1 + j);
    }

    double p2 = 0.0;
    for (int j = 1; j < 5; j++) {
      p2 = p2 + j * Math.cos((j + 1) * x2 + j);
    }

    double res = p1 * p2;

    System.out.println(x1 + ", " + x2 + ", " + res);

    return res;
  }

  public static double valueAt(Point point) {
    return compute(point.toArray()[0], point.toArray()[1]);
  }

}
