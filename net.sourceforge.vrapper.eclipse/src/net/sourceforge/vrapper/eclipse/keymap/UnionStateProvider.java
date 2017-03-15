package net.sourceforge.vrapper.eclipse.keymap;

import static net.sourceforge.vrapper.keymap.StateUtils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sourceforge.vrapper.keymap.KeyMapInfo;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.platform.PlatformSpecificStateProvider;
import net.sourceforge.vrapper.platform.PlatformSpecificVolatileStateProvider;
import net.sourceforge.vrapper.vim.TextObjectProvider;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.modes.commandline.EvaluatorMapping;

public class UnionStateProvider implements PlatformSpecificStateProvider {
    private String name;
    private AbstractEclipseSpecificStateProvider fixedUnionProvider;
    private Collection<PlatformSpecificVolatileStateProvider> volatileProviders;
    
    public UnionStateProvider(String name,
                              Iterable<AbstractEclipseSpecificStateProvider> fixedProviders,
                              Collection<PlatformSpecificVolatileStateProvider> volatileProviders) {
        this.name = name;
        this.volatileProviders = volatileProviders;

        fixedUnionProvider = new AbstractEclipseSpecificStateProvider() {};
        for (AbstractEclipseSpecificStateProvider base : fixedProviders) {
            updateStateMap(fixedUnionProvider.states, base.states);
            updateStateMap(fixedUnionProvider.keyMaps, base.keyMaps);
            fixedUnionProvider.commands.addAll(base.commands);
        }
    }
    
    @Override
    public void initializeProvider(TextObjectProvider textObjProvider) {
        // This is never called on UnionStateProvider
    }

    @Override
    public State<Command> getState(String modeName) {
        return fixedUnionProvider.getState(modeName);
    }

    @Override
    public State<KeyMapInfo> getKeyMaps(String name) {
        // No overhead if no volatile providers
        if (!volatileProviders.iterator().hasNext()) return fixedUnionProvider.getKeyMaps(name);
        
        HashMap<String, State<KeyMapInfo>> res = new HashMap<String, State<KeyMapInfo>>();
        // Volatile key maps first, as they can decide to override built-ins
        for (PlatformSpecificVolatileStateProvider provider : getPrioritizedVolatileProviders()) {
            updateStateMap(res, provider.getVolatileKeyMaps());
        }
        updateStateMap(res, fixedUnionProvider.keyMaps);

        return res.get(name);
    }

    @Override
    public EvaluatorMapping getCommands() {
        // No overhead if no volatile providers
        if (!volatileProviders.iterator().hasNext()) return fixedUnionProvider.getCommands();

        EvaluatorMapping res = new EvaluatorMapping();
        // Volatile commands first, as they can decide to override built-ins
        for (PlatformSpecificVolatileStateProvider provider : getPrioritizedVolatileProviders()) {
            res.addAll(provider.getVolatileCommands());
        }
        res.addAll(fixedUnionProvider.commands);

        return res;
    }

    private Iterable<PlatformSpecificVolatileStateProvider> getPrioritizedVolatileProviders() {
        // Volatile priorities can change at any time, so sorting them here every time
        ArrayList<PlatformSpecificVolatileStateProvider> prioritized = new ArrayList<PlatformSpecificVolatileStateProvider>(
            volatileProviders);
        Collections.sort(prioritized, new Comparator<PlatformSpecificVolatileStateProvider>() {
            @Override
            public int compare(PlatformSpecificVolatileStateProvider left, PlatformSpecificVolatileStateProvider right) {
                return -Integer.compare(left.getVolatilePriority(), right.getVolatilePriority());
            }
        });
        return prioritized;
    }

    @Override
    public String getName() {
        return name;
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
