package net.sourceforge.vrapper.vim.modes.commandline;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.key;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.platform.CommandLineUI;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.PasteRegisterCommand;
import net.sourceforge.vrapper.vim.modes.AbstractMode;
import net.sourceforge.vrapper.vim.modes.ExecuteCommandHint.OnLeave;
import net.sourceforge.vrapper.vim.modes.InsertMode;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;

/**
 * When you're in InsertMode, you can hit Ctrl+R and it will ask for the name of
 * a register. As soon as you enter a register name, it will paste the contents
 * of that register and keep you in InsertMode.
 * 
 * This class exists just for the "ask for the name of a register" part. The
 * command line will appear with '"' and the user enters a single character. As
 * soon as that character is entered, we dump the contents of that register
 * without waiting for the user to hit 'enter' or anything else.
 * 
 * Entering the mode with an instance of ExecuteCommandHint.{@link OnLeave} will make sure
 * that this hint gets passed back to Insert (needed for block edit mode).
 */
public class PasteRegisterMode extends AbstractMode {

    public static final String DISPLAY_NAME = "PASTE REGISTER";
    public static final String NAME = "paste register";

    protected static final KeyStroke KEY_RETURN = key(SpecialKey.RETURN);
    protected static final KeyStroke KEY_ESCAPE = key(SpecialKey.ESC);
    protected static final KeyStroke KEY_BACKSP = key(SpecialKey.BACKSPACE);

    protected CommandLineUI commandLine;
    protected OnLeave onLeaveCommandHint;

    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    public String getName() {
        return NAME;
    }

    public PasteRegisterMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    public String resolveKeyMap(KeyStroke stroke) {
        return null;
    }

    @Override
    public void enterMode(ModeSwitchHint... hints)
            throws CommandExecutionException {
        super.enterMode(hints);
        onLeaveCommandHint = VimUtils.findModeHint(OnLeave.class, hints);
        commandLine = editorAdaptor.getCommandLine();
        commandLine.setPrompt("\"");
        commandLine.open();
    }

    @Override
    public void leaveMode(ModeSwitchHint... hints)
            throws CommandExecutionException {
        super.leaveMode(hints);
        commandLine.close();
    }

    @Override
    public boolean handleKey(KeyStroke e) {
        if (e.equals(KEY_RETURN) || e.equals(KEY_ESCAPE)
                || e.equals(KEY_BACKSP)) {
            changeBackToInsertMode();
            return true;
        }

        Command command = new PasteRegisterCommand(Character.toString(e.getCharacter()));
        try {
            command.execute(editorAdaptor);
        } catch (CommandExecutionException err) {
            editorAdaptor.getUserInterfaceService().setErrorMessage(err.getMessage());
        }
        changeBackToInsertMode();
        return true;
    }

    /** Return to insert mode, continuing the previous insert operation. */
    protected void changeBackToInsertMode() {
        if (onLeaveCommandHint != null) {
            editorAdaptor.changeModeSafely(InsertMode.NAME,
                    InsertMode.RESUME_ON_MODE_ENTER, onLeaveCommandHint);
        } else {
            editorAdaptor.changeModeSafely(InsertMode.NAME, InsertMode.RESUME_ON_MODE_ENTER);
        }
    }
}
