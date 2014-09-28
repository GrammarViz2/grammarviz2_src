/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.hawaii.jmotif.sampler;
import java.util.Random;
import edu.hawaii.jmotif.direct.Point;

/**
 * Simple method demonstrating automatic objective function method call 
 * statistics calculation.
 *
 * @author ytoh
 */
public class TestMethod implements OptimizationMethod<ValueTelemetry> {
    private Random r = new Random();
    private ObjectiveFunction function;

    //sensible default
    private int valueAtThreshold = 3;

    private double testDouble = 0.0;

    private boolean use = false;

    private String optionString = "c";

    private boolean stop = false;
    private double value;

    private int x;

    public double getTestDouble() {
        return testDouble;
    }

    public void setTestDouble(double testDouble) {
        this.testDouble = testDouble;
    }

    public String getOptionString() {
        return optionString;
    }

    public void setOptionString(String optionString) {
        this.optionString = optionString;
    }

    public boolean isUse() {
        return use;
    }

    public void setUse(boolean use) {
        this.use = use;
    }

    public int getX() {
        return x;
    }

    public void setValueAtThreshold(int valueAtThreshold) {
        this.valueAtThreshold = valueAtThreshold;
    }

    public int getValueAtThreshold() {
        return valueAtThreshold;
    }

    public void init(ObjectiveFunction function) {
        // initialize
        this.function = function;
    }

    
    public void optimize() throws OptimizationException {
        try {
            if(r.nextInt(10) > valueAtThreshold) {
                value = function.valueAt(null);
                stop = value < 0;
//                consumer.notifyOf(this);
            }
            if(r.nextInt(10) > 7) {
                function.gradientAt(null);
            }
            if(r.nextInt(10) > 5) {
                function.hessianAt(null);
            }
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            throw new OptimizationException("error during optimization. cause: " + ex.getMessage());
        }
    }
    
    public Solution finish() {
        return new SingleSolution(Point.at(new double[] {3}), -2);
    }

    
    public StopCondition[] getStopConditions() {
        return new StopCondition[] { new StopCondition() {

            public boolean isConditionMet() {
                return stop;
            }
        }};
    }

    private Consumer<? super ValueTelemetry> consumer;

    public void addConsumer(Consumer<? super ValueTelemetry> consumer) {
        this.consumer = consumer;
    }

    public ValueTelemetry getValue() {
        return new ValueTelemetry(value);
    }
}

