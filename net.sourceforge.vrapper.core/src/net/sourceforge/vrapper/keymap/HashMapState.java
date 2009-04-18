package net.sourceforge.vrapper.keymap;

import static net.sourceforge.vrapper.keymap.StateUtils.transitionUnion;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

    public Iterable<KeyStroke> supportedKeys() {
        return map.keySet();
    }

    public State<T> union(State<T> other) {
        HashMapState<T> result = new HashMapState<T>(new HashMap<KeyStroke, Transition<T>>(map));
        for(KeyStroke key: other.supportedKeys()) {
            if (result.map.containsKey(key)) {
                result.map.put(key, transitionUnion(result.map.get(key), other.press(key)));
            } else {
                result.map.put(key, other.press(key));
            }
        }
        return result;
    }

}
