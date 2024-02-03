package net.sourceforge.vrapper.testutil;

import net.sourceforge.vrapper.utils.AbstractPosition;
import net.sourceforge.vrapper.utils.Position;

public class DumbPosition extends AbstractPosition {

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

    public Position setModelOffset(int offset) {
        return new DumbPosition(offset);
    }

    public Position setViewOffset(int offset) {
        return new DumbPosition(offset);
    }
    
    @Override
    public String toString() {
        return String.format("DumbPosition(%d)", position);
    }
}
