package be.uclouvain.ingi.aia.ddo4j.implem.frontier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import be.uclouvain.ingi.aia.ddo4j.core.Frontier;
import be.uclouvain.ingi.aia.ddo4j.core.SubProblem;
import be.uclouvain.ingi.aia.ddo4j.heuristics.StateRanking;

/**
 * The NoDuplicateFrontier is implemented as a custom adaptative binary heap 
 * with uniqueness constraint. While it does offer the opportunity to solve many 
 * problems significantly faster than using the SimpleFrontier, one should refrain 
 * from always prefering the more advanced NoDuplicateFrontier as it imposes 
 * additional conditions on the model. Indeed for two 
 * this implementation to be usable, the model must guaranteed that subproblems
 * having one same root state are equivalent (might not always be the case).
 */
public final class NoDuplicateFrontier<T> implements Frontier<T> {
    /** The comparator that is used to order the subproblems from the most to least promising */
    private final Comparator<SubProblem<T>> compare;
    /** A mapping that associates some state to an entry in the heap */
    private final HashMap<T, SubProblemEntry<T>> states;
    /** The binary heap which actually orders the subproblems */
    private final ArrayList<SubProblemEntry<T>> heap;

    /** 
     * Creates a new instance
     * 
     * @param ranking an ordering to tell which of the subproblem is the most promising 
     * and should be explored first.
     */
    public NoDuplicateFrontier(final StateRanking<T> ranking) {
        compare= new SubProblemComparator<T>(ranking);
        states = new HashMap<>();
        heap   = new ArrayList<>();
    }

    /**
     * Pushes one node onto the heap while ensuring that only one copy of the
     * node (identified by its state) is kept in the heap.
     *
     * # Note:
     * In the event where the heap already contains a copy `x` of a node having
     * the same state as the `node` being pushed. The priority of the node
     * left in the heap might be affected. If `node` node is "better" (greater
     * UB and or longer longest path), the priority of the node will be
     * increased. As always, in the event where the newly pushed node has a
     * longer longest path than the pre-existing node, that one will be kept.
     */
    @Override
    public void push(final SubProblem<T> sub) {
        SubProblemEntry<T> entry = states.get(sub.getState());

        if (entry == null) {
            entry = new SubProblemEntry<>(sub, heap.size());
            states.put(sub.getState(), entry);
            heap.add(entry);
            bubbleUp(entry);
        } else {
            if (compare.compare(entry.prob, sub) > 0) {
                entry.prob = sub;
                bubbleUp(entry);
            }
        }
    }

    /**
     * Pops the best node out of the heap. Here, the best is defined as the
     * node having the best upper bound, with the longest `lp_len`.
     */
    @Override
    public SubProblem<T> pop() {
        if (isEmpty()) {
            return null;
        }

        SubProblemEntry<T> head = heap.set(0, heap.get(heap.size()-1));
        heap.remove(heap.size()-1);
        states.remove(head.prob.getState());
        
        if (!heap.isEmpty()) {
            SubProblemEntry<T> fix = heap.get(0);
            fix.position = 0;
            bubbleDown(fix);
        }

        return head.prob;
    }

    @Override
    public void clear() {
        states.clear();
        heap.clear();
    }

    @Override
    public int size() {
        return states.size();
    }
    /// Internal method to bubble a node up and restore the heap invariant.
    private void bubbleUp(final SubProblemEntry<T> entry) {
        SubProblemEntry<T> me  = entry;
        SubProblemEntry<T> par = heap.get(parent(me.position));


        while (!isRoot(me.position) && compare.compare(me.prob, par.prob) > 0) {
            int m_pos = me.position;
            int p_pos = par.position;
            
            me.position = p_pos;
            par.position= m_pos;
            
            heap.set(me.position, me);
            heap.set(par.position, par);

            par = heap.get(parent(me.position));
        }
    }

    /** Internal method to sink a node down so as to restor the heap invariant. */
    private void bubbleDown(final SubProblemEntry<T> entry) {
        SubProblemEntry<T> me  = entry;
        SubProblemEntry<T> kid = heap.get(maxChildOf(me.position));

        while (kid.position > 0 && compare.compare(me.prob, kid.prob) < 0) {
            int m_pos = me.position;
            int k_pos = kid.position;
            
            me.position = k_pos;
            kid.position= m_pos;
            
            heap.set(me.position, me);
            heap.set(kid.position, kid);

            kid = heap.get(maxChildOf(me.position));
        }
    }

    /**
     * Internal helper method that returns the position of the node which is
     * the parent of the node at `pos` in the heap.
     */
    private int parent(final int pos) {
        if (isRoot(pos)) {
            return pos;
        } else if (isLeft(pos)) {
            return (pos / 2);
        } else {
            return (pos / 2 - 1);
        }
    }
    /**
     * Internal helper method that returns the position of the child of the
     * node at position `pos` which is considered to be the maximum of the
     * children of that node.
     *
     * # Warning
     * When the node at `pos` is a leaf, this method returns **0** for the
     * position of the child. This value 0 acts as a marker to tell that no
     * child is to be found.
     */
    private int maxChildOf(int pos) {
        final int size = size();
        final int left = leftChild(pos);
        final int right = rightChild(pos);

        if (left >= size) {
            return 0;
        }
        if (right >= size) {
            return left;
        }

        int cmp = compare.compare(heap.get(left).prob, heap.get(right).prob);
        if (cmp > 0) {
            return left;
        } else {
            return right;
        }
    }
    /**
     * Internal helper method to return the position of the left child of
     * the node at the given `pos`.
     */
    private int leftChild(int pos) {
        return (pos * 2 + 1);
    }
    /**
     * Internal helper method to return the position of the right child of
     * the node at the given `pos`.
     */
    private int rightChild(int pos) {
        return (pos * 2 + 2);
    }
    /**
     * Internal helper method which returns true iff the node at `pos` is the
     * root of the binary heap (position is zero).
     */
    private boolean isRoot(int pos) {
        return (pos == 0);
    }
    /**
     * Internal helper method which returns true iff the node at `pos` is the
     * left child of its parent.
     */
    private boolean isLeft(int pos) {
        return (pos % 2 == 1);
    }
    ///**
    // * Internal helper method which returns true iff the node at `pos` is the
    // * right child of its parent.
    // */
    //private boolean isRight(int pos) {
    //    return (pos % 2 == 0);
    //}

    /** A subproblem entry in the heap */
    private static final class SubProblemEntry<T> {
        /** The subproblem which is represented */
        private SubProblem<T> prob;
        /** The position of this subproblem in the binary heap */
        private int position;

        public SubProblemEntry(final SubProblem<T> prob, final int position) {
            this.prob     = prob;
            this.position = position;
        }
    }

    /** This utility class implements a decorator pattern to sort SubProblems by their ub then state */
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
