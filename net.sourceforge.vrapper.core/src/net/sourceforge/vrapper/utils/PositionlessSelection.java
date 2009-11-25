package net.sourceforge.vrapper.utils;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.LineWiseSelection;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.SimpleSelection;
import net.sourceforge.vrapper.vim.commands.TextObject;

public abstract class PositionlessSelection implements TextObject {

    protected int linesSpanned;

    public static PositionlessSelection getInstance(EditorAdaptor editorAdaptor) {
        Selection selection = editorAdaptor.getSelection();
        if (selection instanceof LineWiseSelection)
            return new LineWisePositionlessSelection(editorAdaptor, (LineWiseSelection)selection);
        else
            return new SimplePositionlessSelection(editorAdaptor, (SimpleSelection)selection);
    }

    public int getLinesSpanned() {
        return linesSpanned;
    }

	public int getCount() {
		return 1;
	}

	public TextObject withCount(int count) {
		return this;
	}
    

}
