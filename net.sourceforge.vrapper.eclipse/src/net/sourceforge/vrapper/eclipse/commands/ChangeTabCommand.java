package net.sourceforge.vrapper.eclipse.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.AbstractCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.MultipleExecutionCommand;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.EditorAreaHelper;
import org.eclipse.ui.internal.EditorPane;
import org.eclipse.ui.internal.EditorStack;
import org.eclipse.ui.internal.WorkbenchPage;

/**
 * Emulating vim gt/gT tab cycling behavior.
 *
 * Not straightforward.
 *
 * The eclipse next/previous-editor commands cycle using "activation" order.
 * Also, next-editor has a bug. When called programmatically, instead of by
 * eclipse keymap, it leaves its editor stack widget open.
 *
 * Ctrl-PageUp/Down cycle through visual tab order, but that's an SWT keymap,
 * not an eclipse keymap. Thus no way to call it programmatically. Furthermore
 * it stops cycling when it hits either end of the tab list.
 *
 * Getting editors in "activation" order from eclipse API's is easy. But only
 * the presentation-layer knows the "visual" order of the editor tabs. So, to
 * cycle through visual order we have to use internal-only eclipse APIs.
 *
 */
public class ChangeTabCommand extends AbstractCommand {

    private final boolean previous;

    private ChangeTabCommand(boolean previous) {
        this.previous = previous;
    }

    public void execute(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow activeWorkbenchWindow = workbench
                .getActiveWorkbenchWindow();
        IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
        IEditorPart activeEditor = activePage.getActiveEditor();

        IEditorPart[] editors;

        /*
         * { // "activation" order IEditorReference[] editorReferences =
         * activePage.getEditorReferences(); editors = new
         * IEditorPart[editorReferences.length]; for ( int i = 0; i <
         * editorReferences.length; i++ ) { IEditorReference eRef =
         * editorReferences[i]; editors[i] = eRef.getEditor(true); i++; } }
         */

        {
            // "tab" (visual) order, uses internal APIs
            WorkbenchPage wbp = (WorkbenchPage) activePage;
            EditorAreaHelper eah = wbp.getEditorPresentation();
            // eah.displayEditorList();
            EditorStack activeWorkbook = eah.getActiveWorkbook();
            EditorPane[] editorPanes = activeWorkbook.getEditors();

            editors = new IEditorPart[editorPanes.length];

            for (int i = 0; i < editorPanes.length; i++) {
                EditorPane ePane = editorPanes[i];
                editors[i] = ePane.getEditorReference().getEditor(true);
            }
        }

        Integer activeEditorIndex = null;
        for (int i = 0; i < editors.length; i++) {
            IEditorPart editor = editors[i];
            if (editor == activeEditor) {
                activeEditorIndex = i;
                break;
            }
        }

        // now activate that editor.
        if (activeEditorIndex != null) {
            int nextEditorIndex;
            if (previous) {
                if (activeEditorIndex == 0) {
                    activeEditorIndex = editors.length;
                }
                nextEditorIndex = (activeEditorIndex - 1) % editors.length;
            } else {
                nextEditorIndex = (activeEditorIndex + 1) % editors.length;
            }
            IEditorPart nextEditor = editors[nextEditorIndex];
            activePage.activate(nextEditor);
        }

    }

    public Command repetition() {
        return this;
    }

    public Command withCount(int count) {
        return new MultipleExecutionCommand(count, this);
    }

    public static ChangeTabCommand NEXT_EDITOR = new ChangeTabCommand(false);
    public static ChangeTabCommand PREVIOUS_EDITOR = new ChangeTabCommand(true);

}
