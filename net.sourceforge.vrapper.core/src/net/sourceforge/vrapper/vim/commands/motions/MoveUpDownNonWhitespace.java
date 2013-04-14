package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.MotionCommand;

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
    public Position destination(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        int linesToMove = count + defaultAmount;
        if (linesToMove > 0) {
            if (down) {
                MotionCommand.doIt(editorAdaptor, MoveDown.INSTANCE.withCount(linesToMove - 1));
            } else {
                MotionCommand.doIt(editorAdaptor, MoveUp.INSTANCE.withCount(linesToMove - 1));
            }
        }
        return LineStartMotion.NON_WHITESPACE.destination(editorAdaptor);
    }

    public BorderPolicy borderPolicy() {
        return BorderPolicy.LINE_WISE;
    }

    public boolean updateStickyColumn() {
        return true;
    }

}
