package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

public class SwapSelectionSidesCommand extends CountIgnoringNonRepeatableCommand {

    public static final SwapSelectionSidesCommand INSTANCE = new SwapSelectionSidesCommand();

    private SwapSelectionSidesCommand() { /* NOP */ }

    public void execute(EditorAdaptor editorAdaptor) {
        boolean isSelectionExclusive = editorAdaptor.getConfiguration().get(Options.SELECTION).equals("exclusive");
        Selection selection = editorAdaptor.getSelection();
        if (selection.getModelLength() == 1 && !isSelectionExclusive) {
            // do nothing
            return;
        }
        editorAdaptor.setPosition(selection.getEnd(), StickyColumnPolicy.ON_CHANGE);
        editorAdaptor.setSelection(new SimpleSelection(
                new StartEndTextRange(selection.getEnd(), selection.getStart())));
        if (!isSelectionExclusive) {
            CaretType type = selection.isReversed() ? CaretType.LEFT_SHIFTED_RECTANGULAR : CaretType.RECTANGULAR;
            editorAdaptor.getCursorService().setCaret(type);
        }
    }

}
