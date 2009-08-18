package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class SwapSelectionSidesCommand extends CountIgnoringNonRepeatableCommand {

    public static final SwapSelectionSidesCommand INSTANCE = new SwapSelectionSidesCommand();

    private SwapSelectionSidesCommand() { /* NOP */ }

    public void execute(EditorAdaptor editorAdaptor) {
        Selection selection = editorAdaptor.getSelection();
        if (selection.getModelLength() == 1) {
            // do nothing
            return;
        }
        editorAdaptor.setPosition(selection.getEnd(), true);
        editorAdaptor.setSelection(new SimpleSelection(
                new StartEndTextRange(selection.getEnd(), selection.getStart())));
        CaretType type = selection.isReversed() ? CaretType.LEFT_SHIFTED_RECTANGULAR : CaretType.RECTANGULAR;
        editorAdaptor.getCursorService().setCaret(type);
    }

}
