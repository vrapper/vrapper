package net.sourceforge.vrapper.plugin.test.commands;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.keymap.KeyMapInfo;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.PlatformSpecificVolatileStateProvider;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.modes.commandline.Evaluator;
import net.sourceforge.vrapper.vim.modes.commandline.EvaluatorMapping;

public class MixedTestCommandsStateProvider extends AbstractEclipseSpecificStateProvider implements PlatformSpecificVolatileStateProvider {
    @Override
    public int getVolatilePriority() {
        return 20;
    }
    
    public MixedTestCommandsStateProvider() {
        name = "Mixed Test Commands State Provider";

        commands.add("test-fixed-1", new Evaluator() {
            @Override
            public Object evaluate(EditorAdaptor vim, Queue<String> command) throws CommandExecutionException {
                TestUtils.showVimMessage(vim, "Hi, I'm first test fixed command");
                return null;
            }
        });

        commands.add("test-fixed-2", new Evaluator() {
            @Override
            public Object evaluate(EditorAdaptor vim, Queue<String> command) throws CommandExecutionException {
                TestUtils.showVimMessage(vim, "Hi, I'm second test fixed command");
                return null;
            }
        });
    }
    
    @Override
    public EvaluatorMapping getVolatileCommands() {
        try {
            EvaluatorMapping res = new EvaluatorMapping();

            res.add("test-volatile-hardcoded", new Evaluator() {
                @Override
                public Object evaluate(EditorAdaptor vim, Queue<String> command) throws CommandExecutionException {
                    TestUtils.showVimMessage(vim, "Hi, I'm hardcoded test volatile command");
                    return null;
                }
            });

            Properties props = readPropsFromBundle("stuff/volatile-commands.properties");
            Set<Entry<Object, Object>> entrySet = props.entrySet();
            for (final Entry<Object, Object> entry : entrySet) {
                res.add("test-volatile-" + entry.getKey(), new Evaluator() {
                    @Override
                    public Object evaluate(EditorAdaptor vim, Queue<String> command) throws CommandExecutionException {
                        TestUtils.showVimMessage(vim, "" + entry.getValue());
                        return null;
                    }
                });
            }
            
            return res;
        } catch (Exception e) {
            VrapperLog.error(e.getMessage(), e);
            return new EvaluatorMapping();
        }
    }

    private Properties readPropsFromBundle(String path) throws IOException {
        Bundle bundle = Platform.getBundle("net.sourceforge.vrapper.plugin.test.commands");
        URL url = FileLocator.toFileURL(bundle.getEntry(path));
        InputStreamReader reader = new InputStreamReader(url.openStream(), "UTF-8");
        try {
            Properties props = new Properties();
            props.load(reader);
            return props;
        } finally {
            reader.close();
        }
    }

    @Override
    public Map<String, State<KeyMapInfo>> getVolatileKeyMaps() {
        return new HashMap<String, State<KeyMapInfo>>();
    }

    @Override
    public Map<String, State<Command>> getVolatileState(String modeName) {
        return new HashMap<String, State<Command>>();
    }

}
