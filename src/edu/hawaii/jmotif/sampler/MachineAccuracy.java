package edu.hawaii.jmotif.sampler;

/**
 * User: drchaj1
 * Date: 23.8.2008
 * Time: 17:38:15
 */

/**
 * This class contains constants which determine the machine accuracy.
 * TODO references to literature
 */
public final class MachineAccuracy {
    /**
     * Machine epsilon. The difference between 1 and the smallest
     * exactly representable number greater than one.
     */
    public static final double EPSILON; //2.220446049250313E-16

    /**
     * Square root of machine epsilon.
     */
    public static final double SQRT_EPSILON; //1.4901161193847656E-8

    /**
     * Square root of square root machine epsilon.
     */
    public static final double SQRT_SQRT_EPSILON; //...

    static {
        double eps = 0.5;
        while (1 + eps > 1) {
            eps /= 2.0;
        }
        eps *= 2.0;

        //EISPACK
//        double a = 4.0 / 3.0;
//        double b, c;
//        do {
//            b = a - 1.0;
//            c = b + b + b;
//            eps = Math.abs(c - 1.0);
//
//        } while (eps == 0.0);

        EPSILON = eps;
        SQRT_EPSILON = Math.sqrt(EPSILON);
        SQRT_SQRT_EPSILON = Math.sqrt(SQRT_EPSILON);
    }
}
