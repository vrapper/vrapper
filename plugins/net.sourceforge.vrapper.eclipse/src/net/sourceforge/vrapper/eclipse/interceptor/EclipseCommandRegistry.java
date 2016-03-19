package net.sourceforge.vrapper.eclipse.interceptor;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import net.sourceforge.vrapper.log.VrapperLog;

/**
 * This singleton holds information about which Eclipse commands are interesting to listen for.
 * Items can be pushed through special commandline instructions or through a Vrapper-specific
 * extension point.
 */
public class EclipseCommandRegistry {
    public static final EclipseCommandRegistry INSTANCE = new EclipseCommandRegistry();

    protected EclipseCommandRegistry() {}

    protected Set<String> motions = new CopyOnWriteArraySet<String>();
    /**
     * These motions should never leave the line.
     * Note that they should still be added to {@link #motions}.
     */
    protected Set<String> noWrapMotions = new CopyOnWriteArraySet<String>();
    protected Set<String> textObjects = new CopyOnWriteArraySet<String>();
    protected Set<String> commands = new CopyOnWriteArraySet<String>();

    public void addEclipseTextObject(String commandId) {
        textObjects.add(commandId);
    }

    public void addEclipseMotion(String commandId, boolean restrictedToCurrentLine) {
        motions.add(commandId);
        if (restrictedToCurrentLine) {
            noWrapMotions.add(commandId);
        }
    }

    public void addEclipseCommand(String commandId) {
        commands.add(commandId);
    }

    public void loadExtensionDeclarations() {
        final IExtensionRegistry registry = Platform.getExtensionRegistry();
        final IConfigurationElement[] elements = registry.getConfigurationElementsFor(
                "net.sourceforge.vrapper.eclipse.commandregistry");

        for (final IConfigurationElement element : elements) {
            try {
                if ("textobject".equals(element.getName())) {
                    String command = element.getAttribute("command-id");
                    if (command != null && command.trim().length() > 0) {
                        addEclipseTextObject(command);
                    }
                } else if ("motion".equals(element.getName())) {
                    String command = element.getAttribute("command-id");
                    String restrictedToLineVal = element.getAttribute("restricted-to-line");
                    boolean restrictedToLine = Boolean.parseBoolean(restrictedToLineVal);
                    if (command != null && command.trim().length() > 0) {
                        addEclipseMotion(command, restrictedToLine);
                    }
                } else if ("command".equals(element.getName())) {
                    String command = element.getAttribute("command-id");
                    if (command != null && command.trim().length() > 0) {
                        addEclipseCommand(command);
                    }
                }
            } catch (Exception e) {
                VrapperLog.error("Failed to initialize eclipse command registry " + element, e);
            }
        }
    }
}
