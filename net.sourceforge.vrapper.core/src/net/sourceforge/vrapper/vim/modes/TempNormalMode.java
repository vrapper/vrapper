package net.sourceforge.vrapper.vim.modes;

import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.ChangeToVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

/**
 * Temporary normal mode to execute a single command.
 * The mode is to be switched to from the insert mode via <C-O>.
 */
public class TempNormalMode extends NormalMode implements TemporaryMode {

    public static final String NAME = "temporary normal mode";
    public static final String DISPLAY_NAME = "NORMAL (insert)";

    public TempNormalMode(final EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    public void enterMode(final ModeSwitchHint... args) throws CommandExecutionException {
        super.enterMode(args);
        final Position pos = editorAdaptor.getCursorService().getMark(CursorService.LAST_CHANGE_END);
        editorAdaptor.setPosition(pos, StickyColumnPolicy.ON_CHANGE);
    }

    @Override
    protected void commandDone() {
        super.commandDone();
        editorAdaptor.changeModeSafely(InsertMode.NAME, InsertMode.RESUME_ON_MODE_ENTER);
    }

    public void leaveMode(ModeSwitchHint... hints) throws CommandExecutionException {
        //
        // There are two reasons for leaving this mode:
        // - completed command sequence;
        // - command itself caused a mode switch (':');
        // In the latter case we need to switch to insert mode first so the command
        // mode will do the switch back to it. isEnabled flag is used to avoid
        // infinite recursive calls to leaveMode.
        //
        boolean switchBackToInsert = isEnabled;
        super.leaveMode(hints);
        if (switchBackToInsert) {
            editorAdaptor.changeModeSafely(InsertMode.NAME, InsertMode.RESUME_ON_MODE_ENTER);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected State<Command> buildInitialState() {
        State<Command> switchTempModes = state(
                leafBind('V', (Command) new ChangeToVisualModeCommand(TempLinewiseVisualMode.NAME)),
                leafBind('v', (Command) new ChangeToVisualModeCommand(TempVisualMode.NAME))
                );
        return union(switchTempModes, super.buildInitialState());
    }
}
