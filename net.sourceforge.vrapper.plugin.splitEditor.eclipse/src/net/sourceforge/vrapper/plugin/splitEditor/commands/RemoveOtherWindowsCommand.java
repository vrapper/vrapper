package net.sourceforge.vrapper.plugin.splitEditor.commands;

import java.util.ArrayList;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;

// Eclipse 4 API version 0.10.1 bundled with Eclipse 4.2.1 is considered provisional.
@SuppressWarnings("restriction")
/**
 * Removes all splits except the current by moving/closing editors from other
 * splits.
 */
public class RemoveOtherWindowsCommand extends AbstractWindowCommand {
    private boolean join;
    public static final RemoveOtherWindowsCommand REMOVE_CLOSE = new RemoveOtherWindowsCommand(false);
    public static final RemoveOtherWindowsCommand REMOVE_JOIN = new RemoveOtherWindowsCommand(true);

    private RemoveOtherWindowsCommand(boolean join) {
        this.join = join;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        IWorkbenchPartSite site = getEditorSite();
        final EPartService psvc = (EPartService) site.getService(EPartService.class);
        EModelService svc = (EModelService) site.getService(EModelService.class);
        final MPart activePart = (MPart) site.getService(MPart.class);
        final MElementContainer<MUIElement> editorStack = activePart.getParent();
        final IWorkbenchPage page = site.getPage();
        IEditorReference[] editors = page.getEditorReferences();
        final ArrayList<IEditorInput> currentStackEditors = findStackEditors(editorStack, editors);
        ArrayList<IEditorReference> partsToClose = new ArrayList<IEditorReference>();
        final ArrayList<MPart> partsToJoin = new ArrayList<MPart>();
        for (IEditorReference editor : editors) {
            // Need the part to identify it's input to find file clones.
            IWorkbenchPart wbPart = editor.getPart(true);
            if (wbPart != null) {
                MPart p = (MPart) wbPart.getSite().getService(MPart.class);
                if (p.getParent() != editorStack) {
                    try {
                        IEditorInput input = editor.getEditorInput();
                        if (!join || currentStackEditors.contains(input)) {
                            partsToClose.add(editor);
                        } else {
                            partsToJoin.add(p);
                        }
                    } catch (PartInitException e) {
                        // Skip the troublesome part.
                    }
                }
            }
        }
        // Using page interface rather than model interface for closing editors
        // to get nice prompts for unsaved editor.
        page.closeEditors(partsToClose.toArray(new IEditorReference[0]), true);
        for (MPart p : partsToJoin)
        {
            editorStack.getChildren().add(p);
        }
        psvc.activate(activePart, true);
    }

    /**
     * Find all editor inputs (read files) for the specified editor stack.
     * @param editorStack containing editor stack.
     * @param editors list of all editors.
     * @return filtered editor lists.
     */
    private ArrayList<IEditorInput> findStackEditors(
            MElementContainer<MUIElement> editorStack,
            IEditorReference[] editors) {
        ArrayList<IEditorInput> stackEditors = new ArrayList<IEditorInput>();
        for (IEditorReference editor : editors)
        {
            IWorkbenchPart wbPart = editor.getPart(true);
            if (wbPart != null) {
                MPart p = (MPart) wbPart.getSite().getService(MPart.class);
                if (p.getParent() == editorStack) {
                    try {
                        // NOTE: duplicates within the stack are fine.
                        stackEditors.add(editor.getEditorInput());
                    } catch (PartInitException e) {
                        // Ignore.
                    }
                }
            }
        }
        return stackEditors;
    }
}
