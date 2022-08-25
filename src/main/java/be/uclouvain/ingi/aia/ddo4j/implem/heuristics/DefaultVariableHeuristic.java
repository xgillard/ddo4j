package be.uclouvain.ingi.aia.ddo4j.implem.heuristics;

import java.util.Iterator;
import java.util.Set;

import be.uclouvain.ingi.aia.ddo4j.heuristics.VariableHeuristic;

/** 
 * This class implements a default variable ordering. It offers no guarantee as to what 
 * variable is going to be selected next.
 */
public final class DefaultVariableHeuristic<T> implements VariableHeuristic<T> {

    @Override
    public Integer nextVariable(final Set<Integer> variables, final Iterator<T> states) {
        return variables.iterator().next();
    }
    
}
