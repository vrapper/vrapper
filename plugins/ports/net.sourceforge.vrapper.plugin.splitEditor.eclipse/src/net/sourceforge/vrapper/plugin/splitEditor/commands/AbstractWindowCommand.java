package net.sourceforge.vrapper.plugin.splitEditor.commands;

import java.io.ByteArrayInputStream;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountIgnoringNonRepeatableCommand;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.ui.model.application.ui.MGenericTile;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

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

    protected static void matchPositions(final StyledText original, final Point cursor, double relativeOfs, StyledText cloned) {
        //
        // When VIM splits it's window it adjusts the view to keep relative
        // cursor offset
        //
        final int topIndex = original.getTopIndex();
        final int cursorIndex = original.getLineAtOffset(cursor.x);
        final int bottomIndex = JFaceTextUtil.getBottomIndex(original);
        final int height = (bottomIndex - topIndex);
        final int newTopIndex = (int) Math.round(cursorIndex - height * relativeOfs);
        original.setTopIndex(newTopIndex);
        original.setSelection(cursor);
        cloned.setTopIndex(newTopIndex);
        cloned.setSelection(cursor);
    }

    private static Display getDisplay() {
        Display display = Display.getCurrent();
        // may be null if outside the UI thread
        if (display == null) {
            display = Display.getDefault();
        }
        return display;
    }

    protected static MPart cloneEditor() throws PartInitException {
        final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        final IEditorPart editor = page.getActiveEditor();
        final IEditorPart newEditor = page.openEditor(editor.getEditorInput(),
                editor.getSite().getId(), false, IWorkbenchPage.MATCH_NONE);
        //
        // Schedule position update onto the UI thread
        //
        final Object ctl = editor.getAdapter(Control.class);
        final Object newCtl = newEditor.getAdapter(Control.class);
        if (ctl instanceof StyledText && newCtl instanceof StyledText) {
            final StyledText styledText = (StyledText) ctl;
            final Point cursor = styledText.getSelection();
            if (cursor != null) {
                //
                // Calculate relative cursor offset from the top visible line
                //
                final int cursorLineIndex = styledText.getLineAtOffset(cursor.x);
                final int topIndex = styledText.getTopIndex();
                final int bottomIndex = JFaceTextUtil.getBottomIndex(styledText);
                final double relativeOfs = (double)(cursorLineIndex - topIndex) / (double) (bottomIndex - topIndex);
                getDisplay().asyncExec(new Runnable() { public void run() {
                    matchPositions(styledText, cursor, relativeOfs, (StyledText) newCtl);
                }});
            }
        }
        return (MPart) newEditor.getSite().getService(MPart.class);
    }

    protected static MPart openFileInEditor(String filename) throws CommandExecutionException {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IEditorInput input = page.getActiveEditor().getEditorInput();
        IFile currentFile = (IFile) input.getAdapter(IFile.class);
        IProject currentProject = currentFile.getProject();
        IPath filePath = new Path(filename);
        IFile file = currentProject.getFile(filePath);
        if (!file.exists()) {
            try {
                file.create(new ByteArrayInputStream(new byte[0]), false, null);
            } catch (CoreException e) {
                throw new CommandExecutionException("Error creating '" + filePath.toString() + "': " + e.getMessage());
            }
        }
        IEditorInput editorInput = new FileEditorInput(file);
        IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName());
        try {
            IEditorPart newEditor = page.openEditor(editorInput,
                    desc == null ? "org.eclipse.ui.DefaultTextEditor" : desc.getId(),
                    false, IWorkbenchPage.MATCH_NONE);
            return (MPart) newEditor.getSite().getService(MPart.class);
        } catch (PartInitException e) {
            throw new CommandExecutionException("Unable to open editor for '" + filePath.toString() + "'");
        }
    }

    
}
