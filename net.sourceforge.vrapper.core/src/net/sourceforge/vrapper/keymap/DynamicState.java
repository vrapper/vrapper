package net.sourceforge.vrapper.keymap;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link State} implementation derived from {@link HashMapState} but explicitly resisting any
 * union attempts to make sure that changes can be shared through an existing reference.
 */
public class DynamicState<T> implements State<T> {

    protected Map<KeyStroke, Transition<T>> map;

    public DynamicState(Collection<KeyBinding<T>> bindings) {
        this.map = new HashMap<KeyStroke, Transition<T>>(bindings.size());
        for (KeyBinding<T> binding: bindings) {
            if (!map.containsKey(binding.getKeyPress())) {
                map.put(binding.getKeyPress(), binding.getTransition());
            }
        }
    }

    public DynamicState(KeyBinding<T>...bindings) {
        this(Arrays.asList(bindings));
    }

    @Override
    public Transition<T> press(KeyStroke key) {
        return map.get(key);
    }

    @Override
    public State<T> union(State<T> other) {
        return new UnionState<T>(this, other);
    }

    public void addBinding(KeyBinding<T> leafBind) {
        map.put(leafBind.getKeyPress(), leafBind.getTransition());
    }
}
