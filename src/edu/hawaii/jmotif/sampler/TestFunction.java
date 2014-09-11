/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.hawaii.jmotif.sampler;
import java.util.Random;
import edu.hawaii.jmotif.text.SAXCollectionStrategy;

/**
 * Simple test function without analytical gradient or hessian
 *
 * @author ytoh
 */
public class TestFunction implements Function, FunctionGradient, FunctionHessian {

    private int xxx = 1;

    private boolean hasXxx = true;

    private String xxxIndex = "";

    private int value = 100;

    public void setXxxIndex(String xxxIndex) {
        this.xxxIndex = xxxIndex;
    }

    public void setHasXxx(boolean hasXxx) {
        this.hasXxx = hasXxx;
    }

    public void setXxx(int xxx) {
        this.xxx = xxx;
    }

    public boolean getHasXxx() {
        return hasXxx;
    }

    public boolean isHasXxx() {
        return hasXxx;
    }

    public String getXxxIndex() {
        return xxxIndex;
    }

    public int getXxx() {
        return xxx;
    }

    public double valueAt(Point point) {
        Random random = new Random();
        if(random.nextDouble() > 0.4) {
            return value -= random.nextDouble() * random.nextInt(5);
        } else {
            return value += random.nextDouble() * random.nextInt(4);
        }
        
    }

    public int getDimension() {
        return 1;
    }

    public double[] getMinimum() {
        return new double[] { 0.0 };
    }

    public double[] getMaximum() {
        return new double[] { 1.0 };
    }

    public Gradient gradientAt(Point point) {
        return Gradient.valueOf(new double[] {});
    }

    public Hessian hessianAt(Point point) {
        return Hessian.valueOf(new double[][] {});
    }

    @Override
    public SAXCollectionStrategy getSAXSamplingStrategy() {
      // TODO Auto-generated method stub
      return null;
    }
}
