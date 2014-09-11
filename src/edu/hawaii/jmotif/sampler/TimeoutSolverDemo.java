/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.hawaii.jmotif.sampler;

/**
 *
 * @author ytoh
 */
public class TimeoutSolverDemo {
    public static void main(String[] args) throws OptimizationException {
        // solver allowing maximum 50 interations or 500ms computation duration
        Solver solver = SolverFactory.getNewInstance(50, /*5000*/ 500);

        try{
            // method optimization is 1 second long
            // after 1 second the custom stop condition is triggered
            // with the current solver timeout duration of 500ms the optimization
            // will be stopped prematurely on an instance of TimeoutStopCondition
            // an error message is shown to indicate this
            // if you set the maximum computation duration to more then 1s the
            // computation finishes on an instrance of a custom stop condition
            // defined in SimpleStopConditionMethod
            solver.init(new TestFunction(), new SimpleStopConditionMethod());

            solver.solve();

            // result gathering
            OptimizationResults r = solver.getResults();

            System.out.println(r.getSolution());

            for(StopCondition condition : r.getMetConditions()) {
                System.out.println("stopped on condition: " + condition.getClass());
            }

            Statistics stats = r.getStatistics();
            System.out.println("# of Value evaluations:    " + stats.getValueAt());
            System.out.println("# of Gradient evaluations: " + stats.getGradientAt());
            System.out.println("# of Hessian evaluations:  " + stats.getHessianAt());
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
