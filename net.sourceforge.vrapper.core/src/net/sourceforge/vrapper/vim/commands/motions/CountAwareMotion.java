package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public abstract class CountAwareMotion implements Motion {

	public abstract Position destination(EditorAdaptor editorAdaptor, int noCountGiven);

	@Override
	public Position destination(EditorAdaptor editorAdaptor) {
		return destination(editorAdaptor, 1);
	}

	@Override
	public int getCount() {
		return 1;
	}

	@Override
	public Motion withCount(int count) {
		return new CountedMotion(count, this);
	}

}
