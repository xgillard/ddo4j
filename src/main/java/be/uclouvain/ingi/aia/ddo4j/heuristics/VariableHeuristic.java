package be.uclouvain.ingi.aia.ddo4j.heuristics;

import java.util.Iterator;

/** 
 * A variable heuristic is used to determine the next variable to branch on.
 * To help making its decision, the heuristic is given an access to the 
 * nodes from the layer that is about to be expanded.
 */
public interface VariableHeuristic<T> {
    /**
     * @return The next variable to branch on or null if no decision can be 
     *  made about any of the states in the next layer
     */
    Integer nextVariable(final Iterator<T> states);
}
