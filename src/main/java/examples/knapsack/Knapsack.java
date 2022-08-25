package examples.knapsack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import be.uclouvain.ingi.aia.ddo4j.core.Decision;
import be.uclouvain.ingi.aia.ddo4j.core.Problem;
import be.uclouvain.ingi.aia.ddo4j.core.Relaxation;
import be.uclouvain.ingi.aia.ddo4j.heuristics.StateRanking;
import be.uclouvain.ingi.aia.ddo4j.heuristics.VariableHeuristic;
import be.uclouvain.ingi.aia.ddo4j.heuristics.WidthHeuristic;
import be.uclouvain.ingi.aia.ddo4j.implem.frontier.NoDuplicateFrontier;
import be.uclouvain.ingi.aia.ddo4j.implem.frontier.SimpleFrontier;
import be.uclouvain.ingi.aia.ddo4j.implem.heuristics.DefaultVariableHeuristic;
import be.uclouvain.ingi.aia.ddo4j.implem.heuristics.FixedWidth;
import be.uclouvain.ingi.aia.ddo4j.implem.solver.ParallelSolver;

/**
 * This is the knapsack example. It shows how one could gather all the pieces
 * together so as to get a working solver for the knapsack problem using the
 * branch and bound with mdd algorithm.
 */
public final class Knapsack {
    /** 
     * This is the state in the knapsack.
     * 
     * Note 1: 
     * It is very important for a state to override both equals and hashcode 
     * as the library uses these methods to reconcile equal states
     * 
     * Note 2: 
     * This class is actually not useful in the context of the knapsack. It is
     * only present to show that a state can be any POJO provided that you use
     * a class that correctly overrides equals and hashcode.
     */
    private static class KPState {
        private int capacity;

        public KPState(final int capacity) {
            this.capacity = capacity;
        }

        @Override
        public int hashCode() {
            return capacity;
        }
        @Override
        public boolean equals(final Object o) {
            if (!(o instanceof KPState)) {
                return false;
            } else {
                KPState that = (KPState) o;
                return this.capacity == that.capacity;
            }
        }
    }
    
    /** 
     * This is an example of a fixed knapsack instance. 
     * The optimum value for this instance is 295 (http://artemisa.unicauca.edu.co/~johnyortega/instances_01_KP/)
     */
    private static class KPProblem implements Problem<KPState> {
        /** when you decide to not take the item in the sack */
        private static final int   NO = 0;
        /** when you decide to take the item in the sack */
        private static final int   YES = 1;
        /** domain with only the no value */
        private static final List<Integer> DOM_NO = Arrays.asList(NO);
        /** domain where you could chose to either pick the item in the sack or leave it out */
        private static final List<Integer> DOM_YES_NO = Arrays.asList(YES, NO);
        
        /** cost of takin each item */
        private final int[] cost  = new int[]{ 95,  4, 60, 32, 23, 72, 80, 62, 65, 46};
        /** benefit of takin each item */
        private final int[] value = new int[]{ 55, 10, 47,  5,  4, 50,  8, 61, 85, 87};
        /** capacity of the sack */
        private final int capacity= 269;

        @Override
        public int nbVars() {
            return cost.length;
        }

        @Override
        public KPState intialState() {
            return new KPState(capacity);
        }

        @Override
        public int initialValue() {
            return 0;
        }

        @Override
        public Iterator<Integer> domain(final KPState state, final int var) {
            if (state.capacity >= cost[var]) {
                return DOM_YES_NO.iterator();
            } else {
                return DOM_NO.iterator();
            }
        }

        @Override
        public KPState transition(final KPState state, final Decision decision) {
            return new KPState(state.capacity - (decision.val() * cost[decision.var()]));
        }

        @Override
        public int transitionCost(final KPState state, final Decision decision) {
            return decision.val() * value[decision.var()];
        }
    }

    /** This is an example of a knapsack relaxation (just take the max) */
    private static class KPRelax implements Relaxation<KPState> {
        private final KPProblem problem;

        public KPRelax(final KPProblem problem) {
            this.problem = problem;
        }

        @Override
        public KPState mergeStates(final Iterator<KPState> states) {
            int merged = Integer.MIN_VALUE;
            while (states.hasNext()) {
                KPState next = states.next();
                merged = Math.max(next.capacity, merged);
            }
            return new KPState(merged);
        }

        @Override
        public int relaxEdge(final KPState from, final KPState to, final KPState merged, final Decision d, final int cost) {
            return cost;
        }

        @Override
        public int fastUpperBound(final KPState state, final Set<Integer> variables) {
            int tot = 0;
            for (int i : variables) {
                tot += problem.value[i];
            }
            return tot;
        }
    }

    /** An ordering on the states to determine what is the most promising stuff */
    private static final class KPRanking implements StateRanking<KPState> {
        @Override
        public int compare(KPState o1, KPState o2) {
            return o1.capacity - o2.capacity;
        }
    }

    /** The exemple entry point */
    public static void main(final String[] args) {
        KPProblem problem               = new KPProblem();
        KPRelax relax                   = new KPRelax(problem);
        KPRanking ranking               = new KPRanking();
        VariableHeuristic<KPState> varh = new DefaultVariableHeuristic<>();
        WidthHeuristic<KPState> width   = new FixedWidth<>(2);
        ParallelSolver<KPState> solver  = new ParallelSolver<>(
            Runtime.getRuntime().availableProcessors(), 
            problem, 
            relax, 
            varh, 
            ranking, 
            width, 
            // In this case, the model guarantees uniqueness of the state
            // hence you are allowed to use the noduplicate frontier if 
            // you like. But for a first example, it is maybe better to
            // simply stick to the SimpleFrontier as shown below.
            // new NoDuplicateFrontier<>(ranking));
            new SimpleFrontier<>(ranking));
        
        solver.maximize();

        System.out.println("Explored = " + solver.explored());
        
        int bestValue = solver.bestValue().get();
        System.out.println("best value = " + bestValue);
        
        // Because I want to read the solution with the decision oredered by variable
        ArrayList<Decision> solution = new ArrayList<>();
        solution.addAll(solver.bestSolution().get());
        solution.sort((a, b) -> a.var() - b.var());
        for (Decision d : solution) {
            System.out.println("x_" + d.var() + " = " + d.val());
        }
    }
}
