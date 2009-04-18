package net.sourceforge.vrapper.core.tests.utils;

import net.sourceforge.vrapper.utils.Position;

public class DumbPosition implements Position {

	private final int position;

	public DumbPosition(int position) {
		this.position = position;
	}

	public Position addModelOffset(int offset) {
		return new DumbPosition(position + offset);
	}

	public Position addViewOffset(int offset) {
		return new DumbPosition(position + offset);
	}

	public int getModelOffset() {
		return position;
	}

	public int getViewOffset() {
		return position;
	}

}
