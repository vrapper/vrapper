package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
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
        editorAdaptor.changeMode(MessageMode.NAME, new MessageMode.MessagesHint(messages));
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

    protected abstract String getMessages(EditorAdaptor editorAdaptor) throws CommandExecutionException; 

}
