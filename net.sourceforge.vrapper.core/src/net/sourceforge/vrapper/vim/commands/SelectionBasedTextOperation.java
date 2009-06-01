package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;

public class SelectionBasedTextOperation extends AbstractCommand {

    private final TextOperation command;

    public SelectionBasedTextOperation(TextOperation command) {
        this.command = command;
    }

    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        TextObject selection = editorAdaptor.getSelection();
        command.execute(editorAdaptor, selection.getRegion(editorAdaptor, 1), selection.getContentType());
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
