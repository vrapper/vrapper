package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class ChangeCaretShapeCommand extends CountIgnoringNonRepeatableCommand {

    private static final ChangeCaretShapeCommand VERTICAL_BAR = new ChangeCaretShapeCommand(CaretType.VERTICAL_BAR);
    private static final ChangeCaretShapeCommand RECTANGULAR = new ChangeCaretShapeCommand(CaretType.RECTANGULAR);
    private static final ChangeCaretShapeCommand HALF_RECT = new ChangeCaretShapeCommand(CaretType.HALF_RECT);
    private static final ChangeCaretShapeCommand UNDERLINE = new ChangeCaretShapeCommand(CaretType.UNDERLINE);

    private final CaretType caretType;

    private ChangeCaretShapeCommand(CaretType caretType) {
        this.caretType = caretType;
    }

    public static ChangeCaretShapeCommand getInstance(CaretType type) {
        switch (type) {
        case VERTICAL_BAR:
            return VERTICAL_BAR;
        case RECTANGULAR:
            return RECTANGULAR;
        case HALF_RECT:
            return HALF_RECT;
        case UNDERLINE:
            return UNDERLINE;
        default:
            throw new IllegalArgumentException("unsupported caret type: " + type);
        }
    }

    public void execute(EditorAdaptor editorAdaptor) {
        editorAdaptor.getCursorService().setCaret(caretType);
    }

}
