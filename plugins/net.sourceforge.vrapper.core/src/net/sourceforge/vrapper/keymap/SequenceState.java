package net.sourceforge.vrapper.keymap;

import net.sourceforge.vrapper.utils.Function;

/** Abstract state that implements traversing first State<T2>, then State<T3>
 * and returning value of type T1 based on intermediate results returned by those states.
 * 
 * @author Krzysiek Goj
 */
public abstract class SequenceState<T1, T2, T3> implements State<T1> {

    protected final State<T2> first;
    protected final State<T3> second;

    public SequenceState(State<T2> first, State<T3> second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Returns a <tt>Function</tt> based on an object of type T2 which will then wrap an object
     * of type T3 to a type T1.
     */
    protected abstract Function<T1, T3> getConverter(T2 intermediate);
    /**
     * Creates a new intermediate state when State of type T2 takes multiple key presses.
     */
    protected abstract SequenceState<T1, T2, T3> rewrap(State<T2> first);

    public Transition<T1> press(KeyStroke key) {
        Transition<T2> transition = first.press(key);
        if (transition == null)
            return null;
        State<T1> nextState;
        
        State<T2> newFirst = transition.getNextState();
        if (newFirst != null)
            nextState = rewrap(newFirst);
        else {
            T2 wrappedResult = transition.getValue();
            if (wrappedResult == null)
                return null;
            nextState = new Continuation(wrappedResult);
        }
        return new SimpleTransition<T1>(nextState);
    }
    
    private class Continuation extends ConvertingState<T1, T3> {
        public Continuation(T2 intermediate) {
            super(getConverter(intermediate), second);
        }
    }

    public State<T1> union(State<T1> other) {
        return new UnionState<T1>(this, other);
    }

}