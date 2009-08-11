package net.sourceforge.vrapper.platform;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.ViewPortInformation;

/**
 * Provides control over the underlying editor view.
 * All line numbers are in view space.
 *
 * @author Matthias Radig
 */
public interface ViewportService {
	// TODO: scrolling, etc.
    /**
     * Enables and disables automatic repaint.
     */
	void setRepaint(boolean b);

	/**
	 * Locks the repaint status.
	 */
	void lockRepaint(Object lock);

	/**
	 * Unlocks the repaint status.
	 */
	void unlockRepaint(Object lock);

	/**
	 * Exposes the given model offset if it is in a folded area.
	 *
	 * @param position the position to expose
	 */
	void exposeModelPosition(Position position);

	/**
	 * @return information about the currently visible text range
	 */
	ViewPortInformation getViewPortInformation();

	/**
	 * Sets the first visible line.
	 *
	 * @param line the new line to set
	 */
	void setTopLine(int line);
}
