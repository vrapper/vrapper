package net.sourceforge.vrapper.vim.modes.commandline;

import java.util.Queue;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.BufferDoException;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.LocalConfiguration;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

/**
 * Helper class for configurable (global) search highlighting.
 */
public class HighlightSearch {

    /** Highlighting happens for every active editor, clearing as well. */
    public static final String SEARCH_HL_SCOPE_GLOBAL = "global";
    /** Highlighting and clearing is always local to the current editor. */
    public static final String SEARCH_HL_SCOPE_LOCAL = "local";
    /** Only one editor can have active highlighting, clear is global. */
    public static final String SEARCH_HL_SCOPE_CLEAR = "clear";

    /** Clears highlighting in one or more editors depending on the 'globalregisters' setting. */
    public static final Evaluator CLEAR_HIGHLIGHT = new ClearHighlightingEvaluator();
    public static final Evaluator CLEAR_LOCAL_HIGHLIGHT = new ClearLocalHighlightingEvaluator();
    public static final Evaluator HIGHLIGHT = new HighlightingEvaluator();

    public static class HighlightingEvaluator implements Evaluator {
        @Override
        public Object evaluate(EditorAdaptor vim, Queue<String> command)
                throws CommandExecutionException {
            LocalConfiguration configuration = vim.getConfiguration();
            String hlScope  = configuration.get(Options.SEARCH_HL_SCOPE);
            if (hlScope.equals(SEARCH_HL_SCOPE_CLEAR)) {
                // Every other editor must be cleared of highlighting.
                clearHighlightExceptCurrent(vim, command);
            }
            // When using local registers, the search register is local. Act as if hlscope = "local"
            if (hlScope.equals(SEARCH_HL_SCOPE_GLOBAL)) {
                try {
                    vim.getBufferAndTabService().doInBuffers(false, command, new Evaluator() {
                        @Override
                        public Object evaluate(EditorAdaptor vim, Queue<String> command)
                                throws CommandExecutionException {
                            // Grab search for each editor - some might use local registers.
                            Search lastSearch = vim.getRegisterManager().getSearch();
                            vim.getSearchAndReplaceService().highlight(lastSearch);
                            return null;
                        }
                    });
                } catch (BufferDoException e) {
                    VrapperLog.error("search highlighting failed", e);
                    throw new CommandExecutionException("search highlighting failed: "
                            + e.toString());
                }
            } else {
                Search lastSearch = vim.getRegisterManager().getSearch();
                vim.getSearchAndReplaceService().highlight(lastSearch);
            }
            return null;
        }

        protected void clearHighlightExceptCurrent(EditorAdaptor vim,
                Queue<String> command) throws CommandExecutionException {
            final EditorAdaptor currentEditor = vim;
            try {
                vim.getBufferAndTabService().doInBuffers(false, command, new Evaluator() {
                    @Override
                    public Object evaluate(EditorAdaptor vim, Queue<String> command)
                            throws CommandExecutionException {
                        // Exclude current editor to prevent "highlight flashing" in "clear" setting
                        if ( ! vim.equals(currentEditor)) {
                            CLEAR_LOCAL_HIGHLIGHT.evaluate(vim, command);
                        }
                        return null;
                    }
                });
            } catch (BufferDoException e) {
                VrapperLog.error("nohlsearch failed", e);
                throw new CommandExecutionException("nohlsearch failed: "
                        + e.toString());
            }
        }
    }
    
    /** Clears highlighting for all editors if it has 'globalregisters' set. */
    public static class ClearHighlightingEvaluator implements Evaluator {
        @Override
        public Object evaluate(EditorAdaptor vim, Queue<String> command)
                throws CommandExecutionException {
            LocalConfiguration configuration = vim.getConfiguration();
            String hlScope  = configuration.get(Options.SEARCH_HL_SCOPE);
            if (hlScope.equalsIgnoreCase(SEARCH_HL_SCOPE_CLEAR)
                    || hlScope.equalsIgnoreCase(SEARCH_HL_SCOPE_GLOBAL)) {
                try {
                    vim.getBufferAndTabService().doInBuffers(false, command, CLEAR_LOCAL_HIGHLIGHT);
                } catch (BufferDoException e) {
                    VrapperLog.error("nohlsearch failed", e);
                    throw new CommandExecutionException("nohlsearch failed: "
                            + e.toString());
                }
            } else {
                CLEAR_LOCAL_HIGHLIGHT.evaluate(vim, command);
            }
            return null;
        }
    }

    /** Removes highlighting for a single editor if it has 'globalregisters' set. */
    protected static class ClearLocalHighlightingEvaluator implements Evaluator {
        @Override
        public Object evaluate(EditorAdaptor vim, Queue<String> command)
                throws CommandExecutionException {
            vim.getSearchAndReplaceService().removeHighlighting();
            return null;
        }
    }
}
