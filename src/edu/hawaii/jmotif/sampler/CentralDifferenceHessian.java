/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.hawaii.jmotif.sampler;

import edu.hawaii.jmotif.direct.Point;


/**
 *
 * @author ytoh
 */
public class CentralDifferenceHessian implements NumericalHessian {

    public Hessian hessianAt(ObjectiveFunction function, Point point) {
        int dimension       = function.getDimension();
        double[][] hessian  = new double[dimension][dimension];
        double[] h          = new double[dimension];
        double[] fplus      = new double[dimension];
        double[] fminus     = new double[dimension];
        double tolerance    = Math.pow(MachineAccuracy.EPSILON, 0.25);//TODO revise maybe 1/3
        double[] p          = point.toArray();
        double valueAtPoint = function.valueAt(point);

        double xh, oldx, oldy, fxx, tH;
        for (int i = 0; i < dimension; i++) {
            h[i] = tolerance * (Math.abs(p[i]) + MachineAccuracy.EPSILON);
            xh = p[i] + h[i];
            h[i] = xh - p[i];
          
            oldx = p[i];

            p[i] = oldx + h[i];
            fplus[i] = function.valueAt(Point.at(p));
            p[i] = oldx - h[i];
            fminus[i] = function.valueAt(Point.at(p));
            p[i] = oldx;
        }

        for (int i = 0; i < dimension; i++) {
            hessian[i][i] = h[i] * h[i];
            for (int j = 0; j < dimension; j++) {
                tH = h[i] * h[j];
                hessian[i][j] = tH;
                hessian[j][i] = tH;
            }
        }

        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                if (i == j) {
                    hessian[i][j] = (fplus[i] + fminus[j] - 2 * valueAtPoint) / hessian[i][j];
                } else {
                    oldx = p[i];
                    oldy = p[j];
                    p[i] = oldx + h[i];
                    p[j] = oldy - h[j];
                    fxx = function.valueAt(Point.at(p));
                    p[i] = oldx;
                    p[j] = oldy;
                    hessian[i][j] = (fplus[i] + fminus[j] - valueAtPoint - fxx) / hessian[i][j];
                }
            }
        }

        // H=(H+H')/2
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                tH = (hessian[i][j] + hessian[j][i]) / 2.0;
                hessian[i][j] = tH;
                hessian[j][i] = tH;
            }
        }

        return Hessian.valueOf(hessian);
    }
}
