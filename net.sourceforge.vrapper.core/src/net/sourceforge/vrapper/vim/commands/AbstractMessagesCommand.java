package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;
import net.sourceforge.vrapper.vim.modes.commandline.MessageMode;

/**
 * Base class for commands which want to output several lines to the command line.
 * 
 * @see MessageMode
 */
public abstract class AbstractMessagesCommand extends AbstractCommand {

    @Override
    public void execute(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        String messages = getMessages(editorAdaptor);
        ModeSwitchHint[] hints;
        if (isClipped()) {
            hints = new ModeSwitchHint[] {new MessageMode.MessagesHint(messages),
                    MessageMode.CLIP_LINES_HINT};
        } else {
            hints = new ModeSwitchHint[] { new MessageMode.MessagesHint(messages) };
        }
        editorAdaptor.changeMode(MessageMode.NAME, hints);
    }

    @Override
    public Command repetition() {
        // Can't be repeated.
        return null;
    }

    @Override
    public Command withCount(int count) {
        return this;
    }
    
    /**
     * Override and return true to clip messages when displaying messages.
     * Normally word-wrapping is enabled by default.
     */
    public boolean isClipped() {
        return false;
    }

    protected abstract String getMessages(EditorAdaptor editorAdaptor) throws CommandExecutionException; 

}
