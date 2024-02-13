package net.sourceforge.vrapper.keymap;

import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.utils.IdentityFunction;

/** State that handles prefixes that modify items in the wrapped state.
 * 
 * @author Krzysiek Goj
 */
public class WrappingState<T> implements State<T> {
    
    private final State<Function<T, T>> functions;
    private final State<T> wrapped;
    private final Function<T,T> currentFunction;

    public WrappingState(State<Function<T, T>> functions, State<T> wrapped) {
        this.functions = functions;
        this.wrapped = wrapped;
        this.currentFunction = IdentityFunction.getInstance();
    }

    public WrappingState(Function<T, T> currentFunction,
            State<Function<T, T>> transformer,
            State<T> transformed) {
        this.functions = transformer;
        this.wrapped = transformed;
        this.currentFunction = currentFunction;
    }

    public Transition<T> press(KeyStroke key) {
        Transition<Function<T, T>> fnTrans = functions.press(key);
        if (fnTrans == null) {
            Transition<T> wrTrans = wrapped.press(key);
            if (wrTrans == null)
                return null;
            return new ConvertingTransition<T, T>(currentFunction, wrTrans.getValue(), wrTrans.getNextState());
        }
        Function<T, T> nextFn = fnTrans.getValue();
        State<Function<T, T>> nextFnState = fnTrans.getNextState();
        if (nextFnState == null)
            nextFnState = EmptyState.getInstance();
        State<T> nextState = new WrappingState<T>(nextFn, nextFnState, wrapped);
        return new SimpleTransition<T>(nextState);
    }

    public State<T> union(State<T> other) {
        if (other instanceof EmptyState<?>)
            return this;
        if (other instanceof WrappingState<?>) {
            WrappingState<T> otherWS = (WrappingState<T>) other;
            if (functions == otherWS.functions && currentFunction == otherWS.currentFunction)
                return new WrappingState<T>(currentFunction, functions, StateUtils.union(wrapped, otherWS.wrapped));
        }
        return new UnionState<T>(this, other);
    }

}
