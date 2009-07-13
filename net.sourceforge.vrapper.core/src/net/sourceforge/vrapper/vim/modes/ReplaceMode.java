package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.platform.SimpleConfiguration.NewLine;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountIgnoringNonRepeatableCommand;
import net.sourceforge.vrapper.vim.register.Register;

/**
 * Replace mode for overwriting existing text.
 *
 * @author Krzysiek Goj
 * @author Matthias Radig
 *
 * TODO: implement counts
 */
public class ReplaceMode extends InsertMode {

    public static final String NAME = "replace mode";

    public ReplaceMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void enterMode(Object... args) {
        super.enterMode(args);
        editorAdaptor.getEditorSettings().setReplaceMode(true);
        editorAdaptor.getCursorService().setCaret(CaretType.UNDERLINE);
    }

    @Override
    public void leaveMode() {
        editorAdaptor.getEditorSettings().setReplaceMode(false);
        super.leaveMode();
    }

    @Override
    protected Command createRepetition(Register lastEditRegister, String text) {
        return new RepeatReplaceCommand();
    }

    private static class RepeatReplaceCommand extends CountIgnoringNonRepeatableCommand {

        public void execute(EditorAdaptor editorAdaptor)
                throws CommandExecutionException {
            editorAdaptor.getHistory().beginCompoundChange();
            editorAdaptor.getHistory().lock();
            TextContent modelContent = editorAdaptor.getModelContent();
            String editorNewline = editorAdaptor.getConfiguration().getNewLine();
            String text = editorAdaptor.getRegisterManager().getLastEditRegister().getContent().getText();
            int pos = editorAdaptor.getPosition().getModelOffset();
            int start = 0;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (VimUtils.isNewLine(String.valueOf(c))) {
                    String replace = text.substring(start, i);
                    String nl = text.substring(i);
                    NewLine newline = NewLine.parse(nl);
                    replace(modelContent, pos, replace);
                    modelContent.replace(pos+replace.length(), 0, editorNewline);
                    i += newline.nl.length()-1;
                    pos += replace.length()+editorNewline.length();
                    start += replace.length()+newline.nl.length();
                }
            }
            String replace = text.substring(start);
            replace(modelContent, pos, replace);
        }

        private void replace(TextContent modelContent, int pos, String replace) {
            int length = replace.length();
            String toReplace = modelContent.getText(pos, length);
            for (int i = 0; i < length; i++) {
                String c = String.valueOf(toReplace.charAt(i));
                if (VimUtils.isNewLine(c)) {
                    length = i;
                }
            }
            modelContent.replace(pos, length, replace);
        }
    }

}
