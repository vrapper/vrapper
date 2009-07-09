package net.sourceforge.vrapper.vim.commands;

import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.editText;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.register.RegisterContent;

public class ChangeToLastEditOperation extends SimpleTextOperation {

    @Override
    public void execute(EditorAdaptor editorAdapter, TextRange range, ContentType contentType) throws CommandExecutionException {
        editorAdapter.getHistory().beginCompoundChange();
        DeleteOperation.doIt(editorAdapter, range, contentType);
        if (contentType == ContentType.LINES) {
            editText("smartEnterInverse").execute(editorAdapter); // FIXME: use Vrapper's code
        }
        doIt(editorAdapter);
        editorAdapter.getHistory().endCompoundChange();
    }

    public TextOperation repetition() {
        return this;
    }

    private static void doIt(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        Position position = editorAdaptor.getCursorService().getPosition();
        RegisterContent registerContent = editorAdaptor.getRegisterManager().getLastEditRegister().getContent();
        if (registerContent != null) {
            String text = registerContent.getText();
            editorAdaptor.getModelContent().replace(position.getModelOffset(), 0, text);
            int textLength = Math.max(0, text.length() - 1);
            // cursor position may've changed since beginning of this method,
            // e.g. when we do it during renaming refactoring
            Position newPosition = editorAdaptor.getCursorService().getPosition().addModelOffset(textLength);
            editorAdaptor.getCursorService().setPosition(newPosition, true);
        } else {
            VrapperLog.error("no last edit register");
        }
    }
}
