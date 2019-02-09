package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

/**
 * Move up or down lines with the cursor on the first non-whitespace character.
 * ('+' and '-' movement operations)
 */
public class MoveUpDownNonWhitespace extends CountAwareMotion {

    public static final MoveUpDownNonWhitespace MOVE_DOWN_LESS_ONE = new MoveUpDownNonWhitespace(true, 0);
    public static final MoveUpDownNonWhitespace MOVE_DOWN = new MoveUpDownNonWhitespace(true, 1);
    public static final MoveUpDownNonWhitespace MOVE_UP = new MoveUpDownNonWhitespace(false, 1);
    private boolean down = false;
    private int defaultAmount = 1;

    private MoveUpDownNonWhitespace(boolean down, int defaultAmount) {
        this.down = down;
        this.defaultAmount = defaultAmount;
    }

    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count, Position fromPosition) throws CommandExecutionException {
        int linesToMove = count + defaultAmount;
        Position destinationInLine = fromPosition;
        if (linesToMove > 0) {
            Motion upDownMotion = down ? MoveDown.INSTANCE : MoveUp.INSTANCE;
            destinationInLine = upDownMotion.withCount(linesToMove - 1).destination(editorAdaptor, fromPosition);
        }
        return LineStartMotion.NON_WHITESPACE.destination(editorAdaptor, destinationInLine);
    }

    public BorderPolicy borderPolicy() {
        return BorderPolicy.LINE_WISE;
    }

    public StickyColumnPolicy stickyColumnPolicy() {
        return StickyColumnPolicy.RESET_EOL;
    }

}
