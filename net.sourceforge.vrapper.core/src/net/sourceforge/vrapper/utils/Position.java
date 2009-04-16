package net.sourceforge.vrapper.utils;

public interface Position {
	int getModelOffset();
	int getViewOffset();
	// adds the given offset (model space) to this offset
	Position addModelOffset(int offset);
	// same for view space
	Position addViewOffset(int offset);
}
