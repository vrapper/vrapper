package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class SelectionBasedTextOperation extends AbstractCommand {

    private final TextOperation command;

    public SelectionBasedTextOperation(TextOperation command) {
        this.command = command;
    }

    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        command.execute(editorAdaptor, editorAdaptor.getSelection(), ContentType.TEXT);
    }

    public Command repetition() {
        TextOperation wrappedRepetition = command.repetition();
        if (wrappedRepetition != null) {
            return new SelectionBasedTextOperation(wrappedRepetition);
        }
        return null;
    }

    public Command withCount(int count) {
        return this; // ignore count
    }

}
