package net.sourceforge.vrapper.keymap;

import static net.sourceforge.vrapper.keymap.StateUtils.transitionUnion;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HashMapState<T> implements State<T> {

    protected Map<KeyStroke, Transition<T>> map;

    public HashMapState(Collection<KeyBinding<T>> bindings) {
        this.map = new HashMap<KeyStroke, Transition<T>>(bindings.size());
        for (KeyBinding<T> binding: bindings) {
            if (!map.containsKey(binding.getKeyPress())) {
                map.put(binding.getKeyPress(), binding.getTransition());
            }
        }
    }

    protected HashMapState() {
        this.map = new HashMap<KeyStroke, Transition<T>>();
    }

    private HashMapState(Map<KeyStroke, Transition<T>> map) {
        this.map = map;
    }

    public Transition<T> press(KeyStroke key) {
        return map.get(key);
    }

    public Collection<KeyStroke> supportedKeys() {
        return map.keySet();
    }

	public State<T> union(State<T> other) {
        HashMapState<T> result = new HashMapState<T>(new HashMap<KeyStroke, Transition<T>>(map));
        if (other instanceof EmptyState<?>) {
            return this;
        } else if (other instanceof HashMapState<?>) {
            HashMapState<T> otherHMS = (HashMapState<T>) other;
            result.map.putAll(map);
            result.map.putAll(otherHMS.map);
            Set<KeyStroke> commonKeys = new HashSet<KeyStroke>(map.keySet());
            commonKeys.retainAll(otherHMS.map.keySet());
            for (KeyStroke key: commonKeys)
                result.map.put(key, transitionUnion(press(key), other.press(key)));
            return result;
        }
        return new UnionState<T>(this, other);
    }

}
