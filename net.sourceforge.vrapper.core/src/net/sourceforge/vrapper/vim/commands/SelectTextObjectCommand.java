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
            /**
             * TODO: ugly casting. The problem is, that if textObject already
             * returns a LineWiseSelection, then by creating new instance of
             * LineWiseSelection from the old one we extend the selected range
             * by one line. This is due implementation of LineWiseSelection,
             * whose getEnd() points to the line AFTER the actual selection. It
             * might be solved by refactoring TextRange with introduction of
             * linewise range (by line numbers)
             */
            if (region instanceof LineWiseSelection) {
                selection = (LineWiseSelection) region;
            }
            else {
                selection = new LineWiseSelection(editorAdaptor, region.getStart(), region.getEnd());
            }
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
