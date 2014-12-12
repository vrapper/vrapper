package net.sourceforge.vrapper.platform;

import java.util.List;

public interface BufferAndTabService {

    /** Returns information about the buffer which was shown before switchin to the current buffer.
     * The current buffer might be returned if the user didn't switch to another editor since
     * Eclipse was opened.
     */
    public abstract Buffer getPreviousBuffer();

    /** Return information about the current active buffer. */
    public abstract Buffer getActiveBuffer();

    /** Switch to another buffer. In Vrapper's case, we switch to another tab as well because that's
     * the mismatch between how Vim and Eclipse work.
     */
    public abstract void switchBuffer(Buffer buffer);

    /** Returns a list of the currently opened buffers, sorted by id. */
    public abstract List<Buffer> getBuffers();

    /** Get information about the currently active tab. */
    public abstract Tab getActiveTab();

    /** Get all tabs, sorted by display order. */
    public abstract List<Tab> getTabs();

    /** Switch to another tab. */
    public abstract void switchTab(Tab tab);

}