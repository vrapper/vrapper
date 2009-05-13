package net.sourceforge.vrapper.platform;

import net.sourceforge.vrapper.utils.Position;

public interface ViewportService {
	// TODO: scrolling, etc.
	void setRepaint(boolean b);

	void exposeModelPosition(Position position);
}
