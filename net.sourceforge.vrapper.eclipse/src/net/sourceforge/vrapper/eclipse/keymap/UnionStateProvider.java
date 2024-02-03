package net.sourceforge.vrapper.eclipse.keymap;

import static net.sourceforge.vrapper.keymap.StateUtils.union;

import java.util.Map;
import java.util.Map.Entry;

import net.sourceforge.vrapper.keymap.State;

public class UnionStateProvider extends AbstractEclipseSpecificStateProvider {
    
    public UnionStateProvider(String name, Iterable<AbstractEclipseSpecificStateProvider> bases) {
        this.name = name;
        for (AbstractEclipseSpecificStateProvider base: bases) {
            updateStateMap(states, base.states);
            updateStateMap(keyMaps, base.keyMaps);
            commands.addAll(base.commands);
        }
    }
    
    private static<T> void updateStateMap(Map<String, State<T>> old, Map<String, State<T>> update) {
        for (Entry<String, ? extends State<T>> entry: update.entrySet()) {
            if (!old.containsKey(entry.getKey()))
                old.put(entry.getKey(), entry.getValue());
            else
                old.put(entry.getKey(), union(old.get(entry.getKey()), entry.getValue()));
        }
    }

}
