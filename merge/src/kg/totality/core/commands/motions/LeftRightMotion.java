package kg.totality.core.commands.motions;

import kg.totality.core.commands.BorderPolicy;

public abstract class LeftRightMotion extends AbstractModelSideMotion {

	@Override
	public BorderPolicy borderPolicy() {
		return BorderPolicy.EXCLUSIVE;
	}

	@Override
	public boolean updateStickyColumn() {
		return true;
	}

}
