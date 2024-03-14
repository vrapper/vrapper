package net.sourceforge.vrapper.utils;

public interface Position extends Comparable<Position> {
	int getModelOffset();
	int getViewOffset();
	// adds the given offset (model space) to this offset
	Position addModelOffset(int offset);
	// same for view space
	Position addViewOffset(int offset);

	/**
	 * @param offset the offset in view space
	 * @return a new Position instance for the given offset
	 */
    Position setViewOffset(int offset);

	/**
	 * @param offset the offset in model space
	 * @return a new Position instance for the given offset
	 */
    Position setModelOffset(int offset);
}
