package net.sourceforge.vrapper.eclipse.commands;

import java.util.Arrays;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.AbstractCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * Emulating vim gt/gT tab cycling behavior.
 *
 * Not straightforward.
 */
public class ChangeTabCommand extends AbstractCommand {

    // Use an unlikely value
    private final static int LAST_EDITOR_INDEX = Integer.MIN_VALUE + 7;
    
    public static ChangeTabCommand NEXT_EDITOR = new ChangeTabCommand(true);
    public static ChangeTabCommand PREVIOUS_EDITOR = new ChangeTabCommand(false);
    
    public static ChangeTabCommand FIRST_EDITOR = new ChangeTabCommand(1);
    public static ChangeTabCommand LAST_EDITOR = new ChangeTabCommand(LAST_EDITOR_INDEX);
    
    private int count = NO_COUNT_GIVEN;
    private boolean tabToRight;

    private ChangeTabCommand(boolean toRight) {
        this.tabToRight = toRight;
    }

    private ChangeTabCommand(int count) {
        this.count = count;
    }
    
    public Command withCount(int count) {
        if (tabToRight) {
            // gt accepts a count to jump to absolute tab number
            return new ChangeTabCommand(count);
        } else {
            // gT is a different case: the count works relatively
            return new ChangeTabCommand(-count);
        }
    }

    public Command repetition() {
        return null;
    }

    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        
        IEditorReference[] editorReferences = page.getEditorReferences();
        
        int targetIndex;
        
        if (count == LAST_EDITOR_INDEX) {
            targetIndex = editorReferences.length - 1;
        } else if (count == NO_COUNT_GIVEN || count < 0) {
            IEditorReference activeEditorRef = null;
            IEditorPart activeEditor = page.getActiveEditor();
            IEditorReference[] activeEditorDuplicates = page.findEditors(activeEditor.getEditorInput(),
                    activeEditor.getSite().getId(),
                    IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
            for (int i = 0; i < activeEditorDuplicates.length; i++) {
                IEditorPart editor = activeEditorDuplicates[i].getEditor(true);
                if (editor.equals(activeEditor)) {
                    activeEditorRef = activeEditorDuplicates[i];
                }
            }
            int activeEditorIndex = Arrays.asList(editorReferences).indexOf(activeEditorRef);
            
            targetIndex = activeEditorIndex;
            
            if (activeEditorIndex == -1) {
                throw new CommandExecutionException("Current editor not found");
            }
            if (count < 0) {
                targetIndex += count;
            } else if (tabToRight) {
                targetIndex++;
            } else {
                targetIndex--;
            }
            // Bound editor number between -length and length
            targetIndex %= editorReferences.length;
            // Wrap around for negative indices
            if (targetIndex < 0) {
                targetIndex += editorReferences.length;
            }
        } else {
            if (count > editorReferences.length) {
                editorAdaptor.getUserInterfaceService().setInfoMessage(
                        "Can't switch to tab number '" + count
                        + "', only " + editorReferences.length + " tabs open.");
                targetIndex = -1;
            }
           targetIndex = count - 1; 
        }
        
        if (targetIndex >= 0) {
            page.activate(editorReferences[targetIndex].getPart(true));
        }
    }
}
