package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;
import net.sourceforge.vrapper.vim.modes.AbstractVisualMode;
import net.sourceforge.vrapper.vim.modes.LinewiseVisualMode;
import net.sourceforge.vrapper.vim.modes.TempLinewiseVisualMode;
import net.sourceforge.vrapper.vim.modes.TempVisualMode;
import net.sourceforge.vrapper.vim.modes.TemporaryMode;
import net.sourceforge.vrapper.vim.modes.VisualMode;

/**
 * Changes to one of the visual modes by fixing the selection and resetting the cursor. An optional
 * command can be given which will be executed unconditionally.
 */
public class ChangeToVisualModeCommand extends CountIgnoringNonRepeatableCommand {

    protected final String mode;
    protected final Command command;

    public ChangeToVisualModeCommand(String mode) {
        this(mode, null);
    }

    public ChangeToVisualModeCommand(String mode, final Command command) {
        this.mode = mode;
        this.command = command;
        if (mode == null) {
            throw new IllegalArgumentException("Mode must not be null!");
        }
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        String targetMode = mode;
        // Fix for temporary modes.
        if (editorAdaptor.getCurrentMode() instanceof TemporaryMode) {
            if (mode.equalsIgnoreCase(VisualMode.NAME)) {
                targetMode = TempVisualMode.NAME;
            } else if (mode.equalsIgnoreCase(LinewiseVisualMode.NAME)) {
                targetMode = TempLinewiseVisualMode.NAME;
            }
        }

        editorAdaptor.changeMode(targetMode, AbstractVisualMode.FIX_SELECTION_HINT);
        // Fixes caret. This might not always happen with the call above if the mode is not enabled
        // at the right time.
        ((AbstractVisualMode)editorAdaptor.getCurrentMode()).placeCursor(StickyColumnPolicy.NEVER);

        // Execute this directly:
        // changeMode and its execute hint is a no-op if we are already in the given mode.
        if (command != null) {
            command.execute(editorAdaptor);
        }
    }
}
