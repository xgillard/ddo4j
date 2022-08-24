package be.uclouvain.ingi.aia.ddo4j.core;

import java.util.Iterator;

/**
 * This is the definition of the problem one tries to optimize. It basically
 * consists of a problem's formulation in terms of the labeled transition
 * system semantics of a dynamic programme.
 */
public interface Problem<T> {
    /** @return the number of variables in the problem */
    int nbVars();
    /** @return the intial state of the problem */
    T intialState();
    /** @return the problem's initial value */
    int initialValue();

    /**
     * @param state the state from which the tansitions should be applicable
     * @param var the variable whose domain in being queried
     * @return all values in the domain of `var` if a decision is made about the given variable 
     */
    Iterator<Integer> domain(final T state, final int var);

    /** 
     * Applies the problem transition function from one state to the next
     * going through a given decision. (Computes the next state)
     * 
     * @param state the state from which the transition originates
     * @param decision the decision which is applied to `state`. 
     */
    T transition(final T state, final Decision decision);
    /** 
     * Computes the impact on the objective value of making the given
     * decision in the specified state.
     * 
     * @param state the state from which the transition originates
     * @param decision the decision which is applied to `state`. 
     */
    int transitionCost(final T state, final Decision decision);
}
