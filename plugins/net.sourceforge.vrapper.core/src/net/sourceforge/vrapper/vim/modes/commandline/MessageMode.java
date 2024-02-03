package net.sourceforge.vrapper.vim.modes.commandline;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.key;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.platform.CommandLineUI;
import net.sourceforge.vrapper.platform.CommandLineUI.CommandLineMode;
import net.sourceforge.vrapper.platform.UserInterfaceService;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.AbstractMessagesCommand;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.modes.AbstractMode;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;
import net.sourceforge.vrapper.vim.modes.NormalMode;

/**
 * Display several message lines in the command line.
 * 
 * Short messages can be set using {@link UserInterfaceService#setInfoMessage(String)}, but output
 * of an external command or a listing of tabs won't fit that format.
 * 
 * This mode will open the command line, execute an {@link AbstractMessagesCommand} and let it put
 * its output to the screen. If the output won't fit, a --More-- prompt is shown to let the user
 * know that more follows.
 */
public class MessageMode extends AbstractMode {

    public static final String NAME = "message or more mode";
    private CommandLineUI commandLine;

    public static final ModeSwitchHint CLIP_LINES_HINT = new ModeSwitchHint() { };

    protected static final KeyStroke KEY_SPACE = key(' ');
    protected static final KeyStroke KEY_RETURN = key(SpecialKey.RETURN);
    protected static final KeyStroke KEY_ESCAPE = key(SpecialKey.ESC);
    protected static final KeyStroke KEY_Q = key('q');
    
    public static class MessagesHint implements ModeSwitchHint {
        private String messages;

        public MessagesHint(String messages) {
            this.messages = messages;
        }
        
        public String getMessages() {
            return messages;
        }
    }

    public MessageMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return "MESSAGE";
    }

    @Override
    public String resolveKeyMap(KeyStroke stroke) {
        return null;
    }

    @Override
    public void enterMode(ModeSwitchHint... hints)
            throws CommandExecutionException {
        super.enterMode(hints);
        String messages = null;
        boolean clipLines = false;
        for (ModeSwitchHint hint : hints) {
            if (hint instanceof MessagesHint) {
                messages = ((MessagesHint) hint).getMessages();
            }
            if (hint == CLIP_LINES_HINT) {
                clipLines = true;
            }
        }
        if (messages == null) {
            throw new CommandExecutionException("No messages hint set!");
        }

        commandLine = editorAdaptor.getCommandLine();
        commandLine.setPrompt("");
        commandLine.resetContents(messages);
        if (clipLines) {
            commandLine.setMode(CommandLineMode.MESSAGE_CLIPPED);
        } else {
            commandLine.setMode(CommandLineMode.MESSAGE);
        }
        commandLine.open();
        
        updateMorePrompt();
    }

    @Override
    public void leaveMode(ModeSwitchHint... hints)
            throws CommandExecutionException {
        super.leaveMode(hints);
        commandLine.close();
    }

    @Override
    public boolean handleKey(KeyStroke s) {
        if (s.equals(KEY_ESCAPE) || s.equals(KEY_Q) ||
                (s.equals(KEY_RETURN) && commandLine.isLastLineShown())) {
            editorAdaptor.getUserInterfaceService().setInfoMessage("");
            editorAdaptor.changeModeSafely(NormalMode.NAME);
            return true;
        }

        if (s.equals(KEY_RETURN)) {
            commandLine.scrollDown(false);
        } else if (s.equals(KEY_SPACE)) {
            commandLine.scrollDown(true);
        }
        updateMorePrompt();
        return true;
    }

    public void updateMorePrompt() {
        if (commandLine.isLastLineShown()) {
            editorAdaptor.getUserInterfaceService().setInfoMessage("Press RETURN or q to close.");
        } else {
            editorAdaptor.getUserInterfaceService().setInfoMessage("More -- press RETURN or space"
                    + " to scroll, q to quit");
        }
    }
}
