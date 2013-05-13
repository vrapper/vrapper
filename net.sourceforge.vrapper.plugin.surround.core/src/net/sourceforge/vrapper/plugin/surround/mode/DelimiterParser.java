package net.sourceforge.vrapper.plugin.surround.mode;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.plugin.surround.commands.ChangeDelimiterCommand;
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

    private DelimitedText toWrap;
    private AbstractDynamicDelimiterHolder replacement;

    public DelimiterParser(EditorAdaptor vim, DelimitedText toWrap,
            AbstractDynamicDelimiterHolder replacement) {
        super(vim);
        this.toWrap = toWrap;
        this.replacement = replacement;
    }

    @Override
    public Command parseAndExecute(String first, String command) {
        DelimiterHolder newDelimiters;
        try {
            newDelimiters = replacement.update(editor, toWrap, first + command);
            return new ChangeDelimiterCommand(toWrap, newDelimiters);
        } catch (CommandExecutionException e) {
            VrapperLog.error("Failed to update dynamic delimiters!", e);
            return new ChangeModeCommand(NormalMode.NAME);
        }
    }
}
