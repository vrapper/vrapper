package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.InitialContentsHint;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;
import net.sourceforge.vrapper.vim.modes.commandline.CommandLineMode;

/**
 * Switches to command line mode and optionally prefills it with an Ex line range expression if a
 * count was given.
 */
public class ChangeToCommandLineCommand extends CountAwareCommand {
    public static final Command INSTANCE = new ChangeToCommandLineCommand();

    @Override
    public void execute(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {

        ModeSwitchHint[] hints;
        if (count > NO_COUNT_GIVEN) {
            hints = new ModeSwitchHint[1];
            if (count == 1) {
                hints[0] = new InitialContentsHint(".");
            } else {
                hints[0] = new InitialContentsHint(".,.+" + (count - 1));
            }
        } else {
            hints = new ModeSwitchHint[0];
        }
        editorAdaptor.changeMode(CommandLineMode.NAME, hints);
    }

    @Override
    public CountAwareCommand repetition() {
        return null;
    }
}
