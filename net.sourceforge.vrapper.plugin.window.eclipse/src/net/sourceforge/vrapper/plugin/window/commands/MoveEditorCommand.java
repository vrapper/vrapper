package net.sourceforge.vrapper.plugin.window.commands;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.UserInterfaceService;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;

// Eclipse 4 API version 0.10.1 bundled with Eclipse 4.2.1 is considered provisional.
@SuppressWarnings("restriction")
public class MoveEditorCommand extends AbstractWindowCommand {
    private WindowDirection direction;
    private SplitMode mode;
    public static final Command MOVE_UP = new MoveEditorCommand(WindowDirection.UP, SplitMode.MOVE);
    public static final Command MOVE_DOWN = new MoveEditorCommand(WindowDirection.DOWN, SplitMode.MOVE);
    public static final Command MOVE_LEFT = new MoveEditorCommand(WindowDirection.LEFT, SplitMode.MOVE);
    public static final Command MOVE_RIGHT = new MoveEditorCommand(WindowDirection.RIGHT, SplitMode.MOVE);
    public static final Command CLONE_UP = new MoveEditorCommand(WindowDirection.UP, SplitMode.CLONE);
    public static final Command CLONE_DOWN = new MoveEditorCommand(WindowDirection.DOWN, SplitMode.CLONE);
    public static final Command CLONE_LEFT = new MoveEditorCommand(WindowDirection.LEFT, SplitMode.CLONE);
    public static final Command CLONE_RIGHT = new MoveEditorCommand(WindowDirection.RIGHT, SplitMode.CLONE);

    private MoveEditorCommand(WindowDirection direction, SplitMode mode) {
        this.direction = direction;
        this.mode = mode;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        UserInterfaceService interfaceService = editorAdaptor.getUserInterfaceService();
        IWorkbenchPartSite site = getEditorSite();
        EPartService psvc = (EPartService) site.getService(EPartService.class);
        MPartStack stack = findAdjacentStack(site, direction);
        EModelService svc = (EModelService) site.getService(EModelService.class);
        MPart p = (MPart) site.getService(MPart.class);
        MElementContainer<MUIElement> editorStack = p.getParent();

        if (stack == null) {
            interfaceService.setErrorMessage("Couldn't find a split to move into");
            return;
        }

        if (mode == SplitMode.CLONE) {
            try {
                MPart newPart = cloneEditor();
                stack.getChildren().add(newPart);
                // Temporary activate the cloned editor.
                psvc.activate(p);
                p = newPart;
            } catch (PartInitException e) {
                interfaceService.setErrorMessage("Unable to split editor");
                VrapperLog.error("Unable to split editor", e);
            }
        } else {
            editorStack.getChildren().remove(p);
            if (editorStack.getChildren().size() > 0) {
                // Deactivate this tab by activating preceding tab in the current stack.
                psvc.activate((MPart) editorStack.getSelectedElement());
            } else {
                // Temporary activate the split this tab is moving into
                psvc.activate((MPart) stack.getSelectedElement());
            }
            stack.getChildren().add(p);
        }

        // Activate the moved part.
        psvc.activate(p, true);
    }

}
