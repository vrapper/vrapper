package net.sourceforge.vrapper.utils;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection;
import net.sourceforge.vrapper.vim.commands.LineWiseSelection;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.SimpleSelection;
import net.sourceforge.vrapper.vim.commands.TextObject;

public abstract class PositionlessSelection implements TextObject {

    protected int linesSpanned;

    public static PositionlessSelection getInstance(final EditorAdaptor editorAdaptor) {
        final Selection selection = editorAdaptor.getSelection();
        if (selection instanceof LineWiseSelection)
            return new LineWisePositionlessSelection(editorAdaptor, (LineWiseSelection)selection);
        else if (selection instanceof BlockWiseSelection)
            return new BlockWisePositionlessSelection(editorAdaptor, (BlockWiseSelection) selection);
        else
            return new SimplePositionlessSelection(editorAdaptor, (SimpleSelection)selection);
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
    

}
