package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.Counted;

public interface Motion extends Counted<Motion> {
	Position destination(EditorAdaptor editorAdaptor) throws CommandExecutionException;
	BorderPolicy borderPolicy();
	boolean updateStickyColumn();
}
