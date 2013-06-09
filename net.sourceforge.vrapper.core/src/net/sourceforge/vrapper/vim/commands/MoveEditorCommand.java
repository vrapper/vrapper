package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.UserInterfaceService.SplitMode;
import net.sourceforge.vrapper.platform.UserInterfaceService.Where;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class MoveEditorCommand extends CountIgnoringNonRepeatableCommand {
    private Where direction;
    private SplitMode mode;
    public static final MoveEditorCommand MOVE_UP = new MoveEditorCommand(Where.UP, SplitMode.MOVE);
    public static final MoveEditorCommand MOVE_DOWN = new MoveEditorCommand(Where.DOWN, SplitMode.MOVE);
    public static final MoveEditorCommand MOVE_LEFT = new MoveEditorCommand(Where.LEFT, SplitMode.MOVE);
    public static final MoveEditorCommand MOVE_RIGHT = new MoveEditorCommand(Where.RIGHT, SplitMode.MOVE);
    public static final MoveEditorCommand CLONE_UP = new MoveEditorCommand(Where.UP, SplitMode.CLONE);
    public static final MoveEditorCommand CLONE_DOWN = new MoveEditorCommand(Where.DOWN, SplitMode.CLONE);
    public static final MoveEditorCommand CLONE_LEFT = new MoveEditorCommand(Where.LEFT, SplitMode.CLONE);
    public static final MoveEditorCommand CLONE_RIGHT = new MoveEditorCommand(Where.RIGHT, SplitMode.CLONE);

    private MoveEditorCommand(Where direction, SplitMode mode) {
        this.direction = direction;
        this.mode = mode;
    }

    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        editorAdaptor.getUserInterfaceService().moveEditor(direction, mode);
    }
}
