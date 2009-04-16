package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;

public class CountedMotion implements Motion {

	private final int count;
	private final CountAwareMotion motion;

	public CountedMotion(int count, CountAwareMotion motion) {
		this.count = count;
		this.motion = motion;
	}

	@Override
	public Position destination(EditorAdaptor editorAdaptor) {
		return motion.destination(editorAdaptor, count);
	}

	@Override
	public boolean updateStickyColumn() {
		return motion.updateStickyColumn();
	}

	@Override
	public BorderPolicy borderPolicy() {
		return motion.borderPolicy();
	}

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public Motion withCount(int count) {
		return new CountedMotion(count, motion);
	}

}
