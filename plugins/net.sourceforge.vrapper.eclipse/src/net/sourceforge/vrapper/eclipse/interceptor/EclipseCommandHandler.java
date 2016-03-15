package net.sourceforge.vrapper.eclipse.interceptor;

import java.util.HashSet;
import java.util.Set;

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
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;
import net.sourceforge.vrapper.vim.modes.AbstractVisualMode;
import net.sourceforge.vrapper.vim.modes.commandline.AbstractCommandLineMode;

public class EclipseCommandHandler {

    protected EditorAdaptor editorAdaptor;
    protected Selection lastSelection;
    protected boolean vrapperCommandActive;
    protected boolean recognizedCommandActive;
    protected Set<String> motions = new HashSet<String>();
    protected Set<String> textObjects = new HashSet<String>();

    public EclipseCommandHandler(EditorAdaptor editorAdaptor) {
        this.editorAdaptor = editorAdaptor;

        textObjects.add("org.eclipse.jdt.ui.edit.text.java.select.enclosing");
        textObjects.add("org.eclipse.jdt.ui.edit.text.java.select.last");
        textObjects.add("org.eclipse.jdt.ui.edit.text.java.select.next");
        textObjects.add("org.eclipse.jdt.ui.edit.text.java.select.previous");
        textObjects.add("org.eclipse.ui.edit.selectAll");
        textObjects.add("org.eclipse.ui.edit.text.moveLineUp");
        textObjects.add("org.eclipse.ui.edit.text.moveLineDown");
        textObjects.add("org.eclipse.ui.edit.text.select.wordPrevious");
        textObjects.add("org.eclipse.ui.edit.text.select.wordNext");

        motions.add("org.eclipse.ui.edit.text.goto.lineStart");
        motions.add("org.eclipse.ui.edit.text.goto.lineEnd");
        motions.add("org.eclipse.ui.edit.text.goto.wordPrevious");
        motions.add("org.eclipse.ui.edit.text.goto.wordNext");
        motions.add("org.eclipse.jdt.ui.edit.text.java.goto.matching.bracket");
    }

    public void beforeCommand(final String commandId) {
        // Always reset this
        lastSelection = null;
        if ( ! VrapperPlugin.isVrapperEnabled() || vrapperCommandActive) {
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
        // Workaround for 
        if (commandId != null && motions.contains(commandId)) {
            if (lastSelection == null) {
                // [TODO] Potentially compensate for Vim behaviour in normal mode: repeating an
                // Eclipse motion might be a no-op because Eclipse wants to move to the end of the
                // line. Normal mode will keep pulling the caret away from that line end. It's also
                // possible that this needs to be done *after* the command has run.
            } else {
                // Reset selection so that Eclipse motion doesn't get confused. Eclipse motions look
                // at the end of the selection which could be on the next line when the 'To'
                // position sits on the previous end-of-line character.
                editorAdaptor.setPosition(lastSelection.getTo(), StickyColumnPolicy.NEVER);
                recognizedCommandActive = true;
            }
        }
    }

    public void afterCommand(String commandId) {
        if ( ! VrapperPlugin.isVrapperEnabled() || vrapperCommandActive) {
            return;
        }
        if (commandId != null && textObjects.contains(commandId)) {
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
        } else if (commandId != null && motions.contains(commandId)) {
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
        recognizedCommandActive = false;
    }

    public void cleanup() {
    }

    public boolean isVrapperCommandActive() {
        return vrapperCommandActive || recognizedCommandActive;
    }

    public void setVrapperCommandActive(boolean eclipseCommandActive) {
        this.vrapperCommandActive = eclipseCommandActive;
    }
}