package be.uclouvain.ingi.aia.ddo4j.implem.frontier;

import java.util.Comparator;
import java.util.PriorityQueue;

import be.uclouvain.ingi.aia.ddo4j.core.Frontier;
import be.uclouvain.ingi.aia.ddo4j.core.SubProblem;
import be.uclouvain.ingi.aia.ddo4j.heuristics.StateRanking;

/**
 * The simple frontier is a plain priority queue of subproblems which are
 * pushed and popped fron by the solver.
 */
public final class SimpleFrontier<T> implements Frontier<T> {
    /** The underlying priority sub problem priority queue */
    private final PriorityQueue<SubProblem<T>> heap; 

    /** 
     * Creates a new instance
     * 
     * @param ranking an ordering to tell which of the subproblem is the most promising 
     * and should be explored first.
     */
    public SimpleFrontier(final StateRanking<T> ranking) {
        heap = new PriorityQueue<>(new SubProblemComparator<>(ranking).reversed());
    }


    @Override
    public void push(final SubProblem<T> sub) {
        heap.add(sub);
    }

    @Override
    public SubProblem<T> pop() {
        return heap.poll();
    }

    @Override
    public void clear() {
        heap.clear();
    }

    @Override
    public int size() {
        return heap.size();
    }

    /** This utility class implements a decorator pattern to sort ubProblems by their ub then state */
    private static final class SubProblemComparator<T> implements Comparator<SubProblem<T>>{
        /** This is the decorated ranking */
        private final StateRanking<T> delegate;
        
        /** 
         * Creates a new instance
         * @param delegate the decorated ranking
         */
        public SubProblemComparator(final StateRanking<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public int compare(SubProblem<T> o1, SubProblem<T> o2) {
            int cmp = o1.getUpperBound() - o2.getUpperBound();
            if (cmp == 0) {
                return delegate.compare(o1.getState(), o2.getState());
            } else {
                return cmp;
            }
        }        
    }
}
