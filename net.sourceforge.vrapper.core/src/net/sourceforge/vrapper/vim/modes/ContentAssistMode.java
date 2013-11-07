package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.keymap.EmptyState;
import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.Transition;
import net.sourceforge.vrapper.platform.KeyMapProvider;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

/**
 * This mode handles the content-assist popup in Eclipse. All operations are
 * Eclipse commands so they are defined in the EcipseSpecificStateProvider.
 */
public class ContentAssistMode extends AbstractMode {

    public static final String KEYMAP_NAME = "Content Assist Mode Keymap";
    public static final String NAME = "content assist";
    public static final String DISPLAY_NAME = "CONTENT-ASSIST";

    protected State<Command> currentState = buildState();

    public ContentAssistMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }
    
    @Override
    public boolean handleKey(KeyStroke keyStroke) {
        final Transition<Command> transition = currentState.press(keyStroke);
        if (transition != null && transition.getValue() != null) {
            try {
                transition.getValue().execute(editorAdaptor);
                return true;
            } catch (final CommandExecutionException e) {
                editorAdaptor.getUserInterfaceService().setErrorMessage(e.getMessage());
            }
        }
        return false;
    }
    
    protected State<Command> buildState() {
        State<Command> platformSpecificState = editorAdaptor.getPlatformSpecificStateProvider().getState(NAME);
        if(platformSpecificState == null) {
            platformSpecificState = EmptyState.getInstance();
        }
        return platformSpecificState;
    }

    @Override
    public KeyMap resolveKeyMap(KeyMapProvider provider) {
        return provider.getKeyMap(KEYMAP_NAME);
    }
}
