package net.sourceforge.vrapper.plugin.surround.mode;

import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.platform.KeyMapProvider;
import net.sourceforge.vrapper.plugin.surround.state.AbstractDynamicDelimiterHolder;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.DelimitedText;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;
import net.sourceforge.vrapper.vim.modes.commandline.AbstractCommandLineMode;
import net.sourceforge.vrapper.vim.modes.commandline.AbstractCommandParser;

/**
 * This custom mode opens the mini-buffer to update a set of dynamic delimiters.
 * @author Bert Jacobs
 */
public class ReplaceDelimiterMode extends AbstractCommandLineMode {

    private DelimitedText toWrap;
    private AbstractDynamicDelimiterHolder replacement;
    
    public ReplaceDelimiterMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }
    
    public static void switchMode(EditorAdaptor vim, DelimitedText toWrap,
            AbstractDynamicDelimiterHolder dynamicDelimiter) throws CommandExecutionException {
        DelimiterHint delimiterHint = new DelimiterHint(toWrap, dynamicDelimiter);
        vim.changeMode(ReplaceDelimiterMode.class.getName(), delimiterHint);
    }
    
    static class DelimiterHint implements ModeSwitchHint {
        protected DelimitedText toWrap;
        protected AbstractDynamicDelimiterHolder replacement;

        public DelimiterHint(DelimitedText toWrap, AbstractDynamicDelimiterHolder replacement) {
            this.toWrap = toWrap;
            this.replacement = replacement;
        }
    }
    
    @Override
    protected String getPrompt() {
        return replacement.getTemplate(editorAdaptor, toWrap);
    }

    @Override
    public void enterMode(ModeSwitchHint... args) {
        if (args.length > 0 && args[0] instanceof DelimiterHint) {
            DelimiterHint hint = (DelimiterHint) args[0];
            toWrap = hint.toWrap;
            replacement = hint.replacement;
        }
        super.enterMode(args);
    }

    @Override
    public String getDisplayName() {
        return "SURROUND (" + replacement.getDelimiterDisplayName() + ")";
    }

    @Override
    public String getName() {
        return ReplaceDelimiterMode.class.getName();
    }

    @Override
    public KeyMap resolveKeyMap(KeyMapProvider provider) {
        return null;
    }

    @Override
    protected AbstractCommandParser createParser() {
        return new DelimiterParser(editorAdaptor, toWrap, replacement);
    }
}
