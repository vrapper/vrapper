package net.sourceforge.vrapper.eclipse.commands;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountIgnoringNonRepeatableCommand;

/**
 *:[range]up[date][!] [++opt] [>>] [file]
 * Like ":write", but only write when the buffer has been
 *  modified.  {not in Vi} 
 *  NOTE: Only the basic update is implemented. I'm not sure how useful any of the 
 *        options are. (Does anyone really just save a range?)
 * @author Brian Detweiler
 */
public class UpdateCommand extends CountIgnoringNonRepeatableCommand {

    public static final UpdateCommand INSTANCE = new UpdateCommand();

    private UpdateCommand() { /* NOP */ }

    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        
        IWorkbenchPage page =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IEditorPart editorPart = page.getActiveEditor();
        if (editorPart.isDirty())
            editorAdaptor.getFileService().save();
    }
}
