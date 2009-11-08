package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.AbstractVisualMode;
import net.sourceforge.vrapper.vim.modes.LinewiseVisualMode;
import net.sourceforge.vrapper.vim.modes.VisualMode;

public class SelectTextObjectCommand extends CountAwareCommand {

    private final TextObject textObject;

    public SelectTextObjectCommand(TextObject textObject) {
        this.textObject = textObject;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        TextRange region = textObject.getRegion(editorAdaptor, count);
        Selection selection;
        String newMode;
        switch (textObject.getContentType(editorAdaptor.getConfiguration())) {
        case TEXT_RECTANGLE: throw new UnsupportedOperationException("rectangular selection");
        case LINES:
            selection = new LineWiseSelection(editorAdaptor, region.getStart(), region.getEnd());
            newMode = LinewiseVisualMode.NAME;
            break;
        case TEXT:
            selection = new SimpleSelection(region);
            newMode = VisualMode.NAME;
            break;
        default: throw new CommandExecutionException("WTF");
        }
        editorAdaptor.setSelection(selection);
        editorAdaptor.changeMode(newMode, AbstractVisualMode.FIX_SELECTION_HINT);
    }

    @Override
    public CountAwareCommand repetition() {
        return null;
    }

}
