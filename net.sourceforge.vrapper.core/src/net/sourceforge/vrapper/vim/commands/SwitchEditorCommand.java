package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.UserInterfaceService.Where;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class SwitchEditorCommand extends CountIgnoringNonRepeatableCommand {
    private Where direction;
    public static final SwitchEditorCommand SWITCH_UP = new SwitchEditorCommand(Where.UP);
    public static final SwitchEditorCommand SWITCH_DOWN = new SwitchEditorCommand(Where.DOWN);
    public static final SwitchEditorCommand SWITCH_LEFT = new SwitchEditorCommand(Where.LEFT);
    public static final SwitchEditorCommand SWITCH_RIGHT = new SwitchEditorCommand(Where.RIGHT);

    private SwitchEditorCommand(Where direction) {
        this.direction = direction;
    }

    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        editorAdaptor.getUserInterfaceService().switchEditor(direction);
    }
}
