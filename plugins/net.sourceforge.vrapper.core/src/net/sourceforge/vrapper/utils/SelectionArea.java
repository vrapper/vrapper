package net.sourceforge.vrapper.utils;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection;
import net.sourceforge.vrapper.vim.commands.LineWiseSelection;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.SimpleSelection;
import net.sourceforge.vrapper.vim.commands.TextObject;

/**
 * Abstract class holding information about the area of the last selection.
 * Implementations will calculate a new TextRange based on the current offset and the amount of
 * lines or characters spanned in the last selection.
 */
public abstract class SelectionArea implements TextObject {

    protected int linesSpanned;

    public static SelectionArea getInstance(final EditorAdaptor editorAdaptor, final Selection selection) {
        if (selection instanceof LineWiseSelection)
            return new LineWiseSelectionArea(editorAdaptor, (LineWiseSelection)selection);
        else if (selection instanceof BlockWiseSelection)
            return new BlockWiseSelectionArea(editorAdaptor, (BlockWiseSelection) selection);
        else
            return new SimpleSelectionArea(editorAdaptor, (SimpleSelection)selection);
    }

    public int getLinesSpanned() {
        return linesSpanned;
    }

	@Override
    public int getCount() {
		return 1;
	}

	@Override
    public TextObject withCount(final int count) {
		return this;
	}

    @Override
    public TextObject repetition() {
        return this;
    }
}
