package net.sourceforge.vrapper.keymap.vim;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SimpleTransition;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.Transition;
import net.sourceforge.vrapper.keymap.WrappingState;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.commands.Counted;

/**
 *  Keymap state that implements counting.
 * @author Krzysiek Goj
 *
 * @param <T> - counted thing (Command, TextObject, Motion) that is a subject to counting.
 */
public class CountingState<T extends Counted<T>> implements State<Function<T, T>> {

    @SuppressWarnings("rawtypes")
    static CountingState<?> INSTANCE = new CountingState(0);

    private final int count;

    private CountingState(int count) {
        this.count = count;
    }

    public Transition<Function<T, T>> press(KeyStroke key) {
        char character = key.getCharacter();
        // Vim uses CTRL-6 as an alternative to Ctrl-^
        if (key.withCtrlKey())
            return null;
        if (!Character.isDigit(character))
            return null;
        int lastDigit = Integer.decode(Character.toString(character));
        if (count == 0 && lastDigit == 0)
            return null;

        final int newCount = 10 * count + lastDigit;

        Function<T, T> applyCount = new Function<T, T>() {
            public T call(T arg) {
                return arg.withCount(newCount);
            }
        };

        return new SimpleTransition<Function<T, T>>(applyCount, new CountingState<T>(newCount));
    }

    public State<Function<T, T>> union(State<Function<T, T>> other) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public static<T extends Counted<T>> State<T> wrap(State<T> inner) {
        return new WrappingState<T>((CountingState<T>) INSTANCE, inner);
    }

}
