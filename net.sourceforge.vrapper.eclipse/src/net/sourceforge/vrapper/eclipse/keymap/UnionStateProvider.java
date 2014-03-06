package net.sourceforge.vrapper.eclipse.keymap;

import static net.sourceforge.vrapper.keymap.StateUtils.union;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sourceforge.vrapper.keymap.EmptyState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.modes.NormalMode;

public class UnionStateProvider extends AbstractEclipseSpecificStateProvider {
    
    @SuppressWarnings("unchecked")
    public UnionStateProvider(String name, Iterable<AbstractEclipseSpecificStateProvider> bases) {
        this.name = name;
        this.states = new HashMap<String, State<Command>>();
        this.keyMaps = new HashMap<String, State<String>>();
        //
        // Build combined text objects first so they can be used in the custom bindings.
        //
        textObjects = EmptyState.getInstance();
        for (AbstractEclipseSpecificStateProvider base: bases) {
            textObjects = union(textObjects, base.getTextObjects());
        }
        // NOTE: Combining with base text object after all plugin provided
        //       text objects in case they are overridden.
        textObjects = union(textObjects, NormalMode.baseTextObjects());
        for (AbstractEclipseSpecificStateProvider base: bases) {
            updateStateMap(states, base.getStates(textObjects));
            updateStateMap(keyMaps, base.getKeyMaps());
            commands.addAll(base.commands);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static<T> void updateStateMap(Map<String, State<T>> old, Map<String, State<T>> update) {
        for (Entry<String, ? extends State<T>> entry: update.entrySet()) {
            if (!old.containsKey(entry.getKey()))
                old.put(entry.getKey(), entry.getValue());
            else
                old.put(entry.getKey(), union(old.get(entry.getKey()), entry.getValue()));
        }
    }

}
