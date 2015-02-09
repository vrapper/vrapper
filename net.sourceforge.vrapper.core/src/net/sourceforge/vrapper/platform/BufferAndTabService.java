package net.sourceforge.vrapper.platform;

import java.util.List;
import java.util.Queue;

import net.sourceforge.vrapper.vim.modes.commandline.Evaluator;

public interface BufferAndTabService {

    /** Returns information about the buffer which was shown before switching to the current buffer.
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
    
    /**
     * Executes some code in each editor in the current window. If the code raises an exception
     * the execution will stop and the editor which caused the error will be activated (if
     * possible). The BufferDoException will point to the EditorAdapter in which the exception
     * occurred.
     * @param initialize whether to initialize editors from last session. This might be a
     *  time-consuming operation as some of these editors might not have Vrapper or their plugin
     *  loaded. Only pass <code>true</code> if the user explicitly asked for all editors!
     */
    public abstract List<Object> doInBuffers(boolean initialize, Queue<String> command,
            Evaluator code) throws BufferDoException;

    /** Get information about the currently active tab. */
    public abstract Tab getActiveTab();

    /** Get all tabs, sorted by display order. */
    public abstract List<Tab> getTabs();

    /** Switch to another tab. */
    public abstract void switchTab(Tab tab);

}
