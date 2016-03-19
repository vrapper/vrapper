package net.sourceforge.vrapper.eclipse.interceptor;

import java.util.Set;

import net.sourceforge.vrapper.eclipse.activator.VrapperPlugin;
import net.sourceforge.vrapper.eclipse.commands.EclipseMotionPlugState;
import net.sourceforge.vrapper.eclipse.commands.EclipsePlugState;
import net.sourceforge.vrapper.eclipse.commands.EclipseTextObjectPlugState;
import net.sourceforge.vrapper.keymap.vim.PlugKeyStroke;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.SelectionService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.SimpleSelection;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;
import net.sourceforge.vrapper.vim.modes.AbstractVisualMode;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.commandline.AbstractCommandLineMode;

public class EclipseCommandHandler {

    protected final EditorAdaptor editorAdaptor;
    protected final Set<String> motions;
    /**
     * These motions should never leave the line.
     * Note that they should still be added to {@link #motions}.
     */
    protected final Set<String> noWrapMotions;
    protected final Set<String> textObjects;
    /**
     * These are the commands which are not useful as textobject or motion but which we still want
     * to see recorded in a macro.
     */
    protected final Set<String> commands;
    protected Selection lastSelection;
    protected Position lastPosition;
    protected boolean vrapperCommandActive;
    protected boolean recognizedCommandActive;

    public EclipseCommandHandler(EditorAdaptor editorAdaptor, EclipseCommandRegistry registry) {
        this.editorAdaptor = editorAdaptor;
        this.motions = registry.motions;
        this.noWrapMotions = registry.noWrapMotions;
        this.textObjects = registry.textObjects;
        this.commands = registry.commands;
    }

    public void beforeCommand(final String commandId) {
        // Always reset this
        lastSelection = null;
        recognizedCommandActive = false;
        if ( ! VrapperPlugin.isVrapperEnabled() || vrapperCommandActive) {
            return;
        }
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
        lastPosition = editorAdaptor.getPosition();
        // Workaround for motions
        if (commandId != null && motions.contains(commandId)) {
            if (lastSelection == null && editorAdaptor.getCurrentMode() instanceof NormalMode) {
                // Stops the SelectionHandler from running. This is a workaround for when
                // Eclipse motions move the caret to the end of the line, we don't want Normal mode
                // to move the caret back to be "on" the last character until we fix the situation
                // in the afterCommand(..) function.
                recognizedCommandActive = true;
            } else if (lastSelection != null) {
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
            // Record action plug in current macro
            editorAdaptor.getMacroRecorder().handleKey(
                    new PlugKeyStroke(EclipseTextObjectPlugState.TEXTOBJPREFIX + commandId + ')'));
            // Only works for Eclipse text objects, Eclipse motions need some different logic
            TextRange nativeSelection = editorAdaptor.getNativeSelection();
            // Vrapper selection might be still active if command did not modify selection state
            if (nativeSelection.getModelLength() > 0
                    && nativeSelection != SelectionService.VRAPPER_SELECTION_ACTIVE
                    && ! (editorAdaptor.getCurrentMode() instanceof AbstractCommandLineMode)) {
                if (lastSelection == null) {
                    lastSelection = new SimpleSelection(nativeSelection);
                }
                lastSelection = lastSelection.wrap(editorAdaptor, nativeSelection);
                editorAdaptor.setSelection(lastSelection);
                // Should not pose any problems if we are still in the same visual mode.
                editorAdaptor.changeModeSafely(lastSelection.getModeName(), AbstractVisualMode.KEEP_SELECTION_HINT);
            }
        } else if (commandId != null && motions.contains(commandId)) {
            // Record action plug in current macro
            editorAdaptor.getMacroRecorder().handleKey(
                    new PlugKeyStroke(EclipseMotionPlugState.MOTIONPREFIX + commandId + ')'));
            if (lastSelection == null && editorAdaptor.getCurrentMode() instanceof NormalMode) {
                TextContent modelContent = editorAdaptor.getModelContent();
                Position currentPosition = editorAdaptor.getPosition();
                int currentOffset = currentPosition.getModelOffset();
                // Check if caret offset is at line end, if so move the caret to the next line so
                // so that Eclipse motions don't become a no-op.
                LineInformation lineInfo = modelContent.getLineInformationOfOffset(currentOffset);
                int lineEnd = lineInfo.getEndOffset();

                // Shift cursor back to be "on" last character of line
                if (lineEnd == currentOffset && lineInfo.getLength() > 0
                        && noWrapMotions.contains(commandId)) {
                    Position fixedPos = editorAdaptor.getCursorService().shiftPositionForViewOffset(
                            currentPosition.getViewOffset(), -1, false);
                    editorAdaptor.setPosition(fixedPos, StickyColumnPolicy.ON_CHANGE);

                // Shift cursor to start of next line or simply back depending on direction
                } else if (lineEnd == currentOffset && lineInfo.getLength() > 0) {
                    int leftOrRightDelta = (lastPosition.getModelOffset() < currentOffset ? 1 : -1);
                    Position fixedPos = editorAdaptor.getCursorService().shiftPositionForViewOffset(
                            currentPosition.getViewOffset(), leftOrRightDelta, false);
                    editorAdaptor.setPosition(fixedPos, StickyColumnPolicy.ON_CHANGE);
                }
            } else if (lastSelection != null) {
                if (editorAdaptor.getCurrentMode() instanceof AbstractCommandLineMode) {
                    // Restore selection to former state - Home / End will clear or mutilate sel
                    editorAdaptor.setSelection(lastSelection);
                    // [TODO] Do something with Home and End keys
                } else {
                    // [TODO] Check for inclusive / exclusive!
                    lastSelection = lastSelection.reset(editorAdaptor, lastSelection.getFrom(), editorAdaptor.getPosition());
                    editorAdaptor.setSelection(lastSelection);
                    // Should not pose any problems if we are still in the same visual mode.
                    editorAdaptor.changeModeSafely(lastSelection.getModeName(), AbstractVisualMode.KEEP_SELECTION_HINT);
                }
            }
        } else if (commandId != null && commands.contains(commandId)) {
            // Record action plug in current macro
            editorAdaptor.getMacroRecorder().handleKey(
                    new PlugKeyStroke(EclipsePlugState.COMMANDPREFIX + commandId + ')'));
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