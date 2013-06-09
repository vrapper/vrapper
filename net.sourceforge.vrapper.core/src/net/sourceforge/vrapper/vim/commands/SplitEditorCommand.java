package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.UserInterfaceService.SplitDirection;
import net.sourceforge.vrapper.platform.UserInterfaceService.SplitMode;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class SplitEditorCommand extends CountIgnoringNonRepeatableCommand {
    private final SplitDirection direction;
    private final SplitMode mode;
    public static final SplitEditorCommand VSPLIT = new SplitEditorCommand(SplitDirection.VERTICALLY, SplitMode.CLONE);
    public static final SplitEditorCommand HSPLIT = new SplitEditorCommand(SplitDirection.HORIZONTALLY, SplitMode.CLONE);
    public static final SplitEditorCommand VSPLIT_MOVE = new SplitEditorCommand(SplitDirection.VERTICALLY, SplitMode.MOVE);
    public static final SplitEditorCommand HSPLIT_MOVE = new SplitEditorCommand(SplitDirection.HORIZONTALLY, SplitMode.MOVE);

    private SplitEditorCommand(SplitDirection dir, SplitMode mode) {
        this.direction = dir;
        this.mode = mode;
    }

    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        editorAdaptor.getUserInterfaceService().splitEditor(direction, mode);
    }

}
