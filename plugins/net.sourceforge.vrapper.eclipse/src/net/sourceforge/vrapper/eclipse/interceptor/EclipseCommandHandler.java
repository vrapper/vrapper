package net.sourceforge.vrapper.eclipse.interceptor;

import net.sourceforge.vrapper.eclipse.activator.VrapperPlugin;
import net.sourceforge.vrapper.eclipse.commands.EclipseMotionPlugState;
import net.sourceforge.vrapper.eclipse.commands.EclipseTextObjectPlugState;
import net.sourceforge.vrapper.keymap.vim.PlugKeyStroke;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.SelectionService;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.SimpleSelection;
import net.sourceforge.vrapper.vim.modes.AbstractVisualMode;
import net.sourceforge.vrapper.vim.modes.commandline.AbstractCommandLineMode;

public class EclipseCommandHandler {

    protected EditorAdaptor editorAdaptor;
    protected Selection lastSelection;

    public EclipseCommandHandler(EditorAdaptor editorAdaptor) {
        this.editorAdaptor = editorAdaptor;
    }

    public void beforeCommand(final String commandId) {
        // Always reset this
        lastSelection = null;
        if ( ! VrapperPlugin.isVrapperEnabled()) {
            return;
        }
        // [TODO] Eclipse motions don't know about inclusive mode; it's unable to change the
        // selection to the left when Vrapper shows the cursor *on* a landing spot.
        // Chop off that last character if selection is left-to-right and command is a motion.
        TextRange selRange = editorAdaptor.getNativeSelection();
        if (selRange.getModelLength() > 0 || selRange == SelectionService.VRAPPER_SELECTION_ACTIVE) {
            if (selRange == SelectionService.VRAPPER_SELECTION_ACTIVE) {
                // This is the generic part: store Vrapper selection so that 'gv' works.
                editorAdaptor.rememberLastActiveSelection();
                VrapperLog.debug("Stored Vrapper selection");
            }
            // Store current selection so that postExecuteSuccess can update it.
            lastSelection = editorAdaptor.getSelection();
            VrapperLog.debug("Grabbed selection");
        }
    }

    public void afterCommand(String commandId) {
        if ( ! VrapperPlugin.isVrapperEnabled()) {
            return;
        }
        // [TODO] Record that command was executed when recording a macro.
        if ("org.eclipse.jdt.ui.edit.text.java.select.enclosing".equals(commandId)
                || "org.eclipse.jdt.ui.edit.text.java.select.last".equals(commandId)
                || "org.eclipse.jdt.ui.edit.text.java.select.next".equals(commandId)
                || "org.eclipse.jdt.ui.edit.text.java.select.previous".equals(commandId)
                || "org.eclipse.ui.edit.selectAll".equals(commandId)
                || "org.eclipse.ui.edit.text.moveLineUp".equals(commandId)
                || "org.eclipse.ui.edit.text.moveLineDown".equals(commandId)
                || "org.eclipse.ui.edit.text.select.wordPrevious".equals(commandId)
                || "org.eclipse.ui.edit.text.select.wordNext".equals(commandId)
            ) {
            // Only works for Eclipse text objects, Eclipse motions need some different logic
            TextRange nativeSelection = editorAdaptor.getNativeSelection();
            // Vrapper selection might be still active if command did not modify selection state
            if (nativeSelection.getModelLength() > 0
                    && nativeSelection != SelectionService.VRAPPER_SELECTION_ACTIVE
                    && ! (editorAdaptor.getCurrentMode() instanceof AbstractCommandLineMode)) {
                // Record action plug in current macro
                // [TODO] Don't do this when EclipseCommand is invoked from Vrapper
                editorAdaptor.getMacroRecorder().handleKey(new PlugKeyStroke(EclipseTextObjectPlugState.TEXTOBJPREFIX + commandId + ')'));
                if (lastSelection == null) {
                    lastSelection = new SimpleSelection(nativeSelection);
                }
                lastSelection = lastSelection.wrap(editorAdaptor, nativeSelection);
                editorAdaptor.setSelection(lastSelection);
                // Should not pose any problems if we are still in the same visual mode.
                editorAdaptor.changeModeSafely(lastSelection.getModeName(), AbstractVisualMode.KEEP_SELECTION_HINT);
            }
        } else if ("org.eclipse.ui.edit.text.goto.lineStart".equals(commandId)
                || "org.eclipse.ui.edit.text.goto.lineEnd".equals(commandId)
                || "org.eclipse.ui.edit.text.goto.wordPrevious".equals(commandId)
                || "org.eclipse.ui.edit.text.goto.wordNext".equals(commandId)
                || "org.eclipse.jdt.ui.edit.text.java.goto.matching.bracket".equals(commandId)
                ) {
            if (lastSelection != null) {
                if (editorAdaptor.getCurrentMode() instanceof AbstractCommandLineMode) {
                    // Restore selection to former state - Home / End will clear or mutilate sel
                    // [TODO] Check for inclusive / exclusive!
                    editorAdaptor.setSelection(lastSelection);
                } else {
                    // Record action plug in current macro
                    // [TODO] Don't do this when EclipseCommand is invoked from Vrapper
                    editorAdaptor.getMacroRecorder().handleKey(new PlugKeyStroke(EclipseMotionPlugState.MOTIONPREFIX + commandId + ')'));
                    // [TODO] Check for inclusive / exclusive!
                    lastSelection = lastSelection.reset(editorAdaptor, lastSelection.getFrom(), editorAdaptor.getPosition());
                    editorAdaptor.setSelection(lastSelection);
                    // Should not pose any problems if we are still in the same visual mode.
                    editorAdaptor.changeModeSafely(lastSelection.getModeName(), AbstractVisualMode.KEEP_SELECTION_HINT);
                }
            }
        }
    }

    public void cleanup() {
    }
}