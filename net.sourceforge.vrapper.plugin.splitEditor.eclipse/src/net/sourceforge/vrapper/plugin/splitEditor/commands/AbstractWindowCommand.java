package net.sourceforge.vrapper.plugin.splitEditor.commands;

import net.sourceforge.vrapper.vim.commands.CountIgnoringNonRepeatableCommand;

import org.eclipse.e4.ui.model.application.ui.MGenericTile;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

// Eclipse 4 API version 0.10.1 bundled with Eclipse 4.2.1 is considered provisional.
@SuppressWarnings("restriction")
public abstract class AbstractWindowCommand extends CountIgnoringNonRepeatableCommand {

    protected static IWorkbenchPartSite getEditorSite() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getActivePage().getActivePart().getSite();
    }

    @SuppressWarnings("unchecked")
    protected static MPartStack findAdjacentStack(IWorkbenchPartSite site, WindowDirection where) {
        EModelService svc = (EModelService) site.getService(EModelService.class);
        MPart p = (MPart) site.getService(MPart.class);
        MPartSashContainerElement editorStack = (MPartSashContainerElement) p.getParent();

        //
        // Find parent container with the desired layout.
        //
        MUIElement child = editorStack;
        MUIElement cont = editorStack.getParent();
        boolean isHorizontal = where == WindowDirection.LEFT || where == WindowDirection.RIGHT;
        while (cont instanceof MGenericTile<?>) {
            MGenericTile<MUIElement> curTile = (MGenericTile<MUIElement>) cont;
            if (curTile.isHorizontal() == isHorizontal && curTile.getChildren().size() > 1) {
                // Found right layout, calculate the index of the adjacent container
                int childIndex = curTile.getChildren().indexOf(child);
                if (where == WindowDirection.RIGHT || where == WindowDirection.DOWN) {
                    ++childIndex;
                } else {
                    --childIndex;
                }
                // Check if there is a neighbour on this level, otherwise -- go higher.
                if (childIndex >= 0 && childIndex < curTile.getChildren().size()) {
                    cont = curTile.getChildren().get(childIndex);
                    // Descend down to the selected part stack.
                    while (cont instanceof MGenericTile<?> || cont instanceof MPlaceholder) {
                        if (cont instanceof MPlaceholder) {
                            MPlaceholder mp = (MPlaceholder) cont;
                            cont = mp.getRef();
                        } else {
                            curTile = (MGenericTile<MUIElement>) cont;
                            cont = curTile.getSelectedElement();
                        }
                    }
                    if (cont instanceof MPartStack) {
                        return (MPartStack) cont;
                    } else {
                        // Can't find an adjacent part stack.
                        return null;
                    }
                }
            }
            child = cont;
            cont = cont.getParent();
            if (cont == null) {
                // Check if there is a placeholder for the element.
                MPlaceholder ph = svc.findPlaceholderFor(svc.getTopLevelWindowFor(child), child);
                if (ph != null) {
                    child = ph;
                    cont = child.getParent();
                }
            }
        }
        return null;
    }

    protected static MPart cloneEditor() throws PartInitException {
    
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IEditorPart editor = page.getActiveEditor();
        IEditorPart newEditor = page.openEditor(editor.getEditorInput(),
                editor.getSite().getId(), false, IWorkbenchPage.MATCH_NONE);
        MPart newPart = (MPart) newEditor.getSite().getService(MPart.class);
        return newPart;
    }

    
}
