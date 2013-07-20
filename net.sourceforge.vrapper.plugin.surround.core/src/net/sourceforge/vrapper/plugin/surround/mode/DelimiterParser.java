package net.sourceforge.vrapper.plugin.surround.mode;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.plugin.surround.commands.DelimiterChangedListener;
import net.sourceforge.vrapper.plugin.surround.state.AbstractDynamicDelimiterHolder;
import net.sourceforge.vrapper.plugin.surround.state.DelimiterHolder;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.ChangeModeCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.DelimitedText;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.commandline.AbstractCommandParser;

public class DelimiterParser extends AbstractCommandParser {

    private Command leaveCommand;
    private DelimiterChangedListener listener;
    private DelimitedText toWrap;
    private AbstractDynamicDelimiterHolder replacement;

    public DelimiterParser(EditorAdaptor vim, Command leaveCommand,
            DelimiterChangedListener listener, DelimitedText toWrap,
            AbstractDynamicDelimiterHolder replacement) {
        super(vim);
        this.toWrap = toWrap;
        this.replacement = replacement;
        this.leaveCommand = leaveCommand;
        this.listener = listener;
    }

    @Override
    public Command parseAndExecute(String first, String command) {
        try {
            DelimiterHolder updatedDelim = replacement.update(editor, toWrap, first + command);
            listener.delimiterChanged(replacement, updatedDelim);
            return leaveCommand;
        } catch (CommandExecutionException e) {
            VrapperLog.error("Failed to update dynamic delimiters!", e);
            return new ChangeModeCommand(NormalMode.NAME);
        }
    }
}
