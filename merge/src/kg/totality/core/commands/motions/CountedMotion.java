package kg.totality.core.commands.motions;

import kg.totality.core.EditorAdaptor;
import kg.totality.core.commands.BorderPolicy;
import newpackage.position.Position;

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
