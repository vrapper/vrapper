package net.sourceforge.vrapper.plugin.window.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.ui.IWorkbenchPartSite;

// Eclipse 4 API version 0.10.1 bundled with Eclipse 4.2.1 is considered provisional.
@SuppressWarnings("restriction")
public class SwitchEditorCommand extends AbstractWindowCommand {
    private WindowDirection direction;
    public static final SwitchEditorCommand SWITCH_UP = new SwitchEditorCommand(WindowDirection.UP);
    public static final SwitchEditorCommand SWITCH_DOWN = new SwitchEditorCommand(WindowDirection.DOWN);
    public static final SwitchEditorCommand SWITCH_LEFT = new SwitchEditorCommand(WindowDirection.LEFT);
    public static final SwitchEditorCommand SWITCH_RIGHT = new SwitchEditorCommand(WindowDirection.RIGHT);

    private SwitchEditorCommand(WindowDirection direction) {
        this.direction = direction;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        IWorkbenchPartSite editorSite = getEditorSite();
        EPartService psvc = (EPartService) editorSite.getService(EPartService.class);
        MPartStack stack = findAdjacentStack(editorSite, direction);
        if (stack != null) {
            psvc.activate((MPart) stack.getSelectedElement(), true);
        }
    }

}
