package net.sourceforge.vrapper.plugin.splitEditor.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.internal.WorkbenchPage;

// Eclipse 4 API version 0.10.1 bundled with Eclipse 4.2.1 is considered provisional.
@SuppressWarnings("restriction")
/**
 * Activates the most recently active editor in another split.
 */
public class SwitchOtherEditorCommand extends AbstractWindowCommand {
    public static final SwitchOtherEditorCommand INSTANCE = new SwitchOtherEditorCommand();

    @Override
    public void execute(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        final IWorkbenchPartSite editorSite = getEditorSite();
        final WorkbenchPage page = (WorkbenchPage) editorSite.getPage();
        final EPartService psvc = (EPartService) editorSite.getService(EPartService.class);
        MPart activePart = (MPart) editorSite.getService(MPart.class);
        MElementContainer<MUIElement> editorStack = activePart.getParent();

        IEditorReference[] sortedEditors = page.getSortedEditors();
        //
        // Find most recently activated editor in a different part stack.
        //
        for (IEditorReference otherEditor : sortedEditors) {
            IWorkbenchPart wbPart = otherEditor.getPart(true);
            if (wbPart != null) {
                MPart p = (MPart) wbPart.getSite().getService(MPart.class);
                if (p.getParent() != editorStack) {
                    psvc.activate(p, true);
                    break;
                }
            }
        }
    }

}
