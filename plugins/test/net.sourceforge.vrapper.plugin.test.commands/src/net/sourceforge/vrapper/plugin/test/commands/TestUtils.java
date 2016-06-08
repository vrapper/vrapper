package net.sourceforge.vrapper.plugin.test.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;
import net.sourceforge.vrapper.vim.modes.commandline.MessageMode;

public class TestUtils {

    public static void showVimMessage(EditorAdaptor vim, String msg) throws CommandExecutionException {
        ModeSwitchHint[] hints = new ModeSwitchHint[] {new MessageMode.MessagesHint(msg)};
        vim.changeMode(MessageMode.NAME, hints);
    }

}
