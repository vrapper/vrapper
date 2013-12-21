package net.sourceforge.vrapper.eclipse.commands;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.vim.commands.AbstractMessagesCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

public class ListTabsCommand extends AbstractMessagesCommand {

    public static final Command INSTANCE = new ListTabsCommand();

    @Override
    protected String getMessages() throws CommandExecutionException {
        StringBuilder sb = new StringBuilder();

        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        
        IEditorReference[] editorReferences = page.getEditorReferences();
        IEditorPart current = page.getActiveEditor();
        for (int i = 0; i < editorReferences.length; i++) {
            sb.append("Tab page ").append(i + 1).append("\n");
            IEditorReference editorRef = editorReferences[i];
            IEditorInput editorInput = null;
            IEditorPart editor = editorRef.getEditor(false);
            // If editor == null, then it isn't the current editor
            if (editor != null && editor.equals(current)) {
                sb.append(">   ");
            } else {
                sb.append("    ");
            }
            try {
                editorInput = editorRef.getEditorInput();
                if (editorInput instanceof FileEditorInput) {
                    FileEditorInput fileInput = (FileEditorInput) editorInput;
                    sb.append(fileInput.getFile().getFullPath().toFile().getPath());
                } else if (editorInput != null) {
                    sb.append(editorInput.getName());
                }
            } catch (PartInitException e) {
                VrapperLog.error("Failed to list editor " + i, e);
                sb.append("<Editor not available anymore>");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

}
