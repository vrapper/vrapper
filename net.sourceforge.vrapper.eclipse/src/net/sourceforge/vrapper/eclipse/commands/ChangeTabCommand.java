package net.sourceforge.vrapper.eclipse.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;

/**
 * Emulating vim gt/gT tab cycling behavior.
 *
 * Not straightforward.
 */
public class ChangeTabCommand extends EclipseCommand {

    private static final String PREVIOUS_ACTION = "org.eclipse.ui.navigate.previousTab";
    private static final String NEXT_ACTION = "org.eclipse.ui.navigate.nextTab";
    private int count = NO_COUNT_GIVEN;
    
    public static ChangeTabCommand NEXT_EDITOR = new ChangeTabCommand(NEXT_ACTION);
    public static ChangeTabCommand PREVIOUS_EDITOR = new ChangeTabCommand(PREVIOUS_ACTION);

    private ChangeTabCommand(String action) {
    	super(action);
    }

    public Command withCount(int count) {
    	this.count = count;
        return this;
    }

    public Command repetition() {
        return null;
    }
    
    public void execute(EditorAdaptor editorAdaptor) {
    	if(count == NO_COUNT_GIVEN) {
    		count = 1;
    	}
    	//<n>gt goes to the <n>th tab, not gt <n> times
    	//if(count > 1) {
    	//	moveToTab(editorAdaptor, count - 1);
    	//}
    	for(int i=0; i < count; i++) {
    		doIt(1, getCommandName(), editorAdaptor);
    	}
    	
    	//reset the count since this instance is used as a static
    	//(we might call this command again in a new context)
    	count = NO_COUNT_GIVEN;
    }

    /*
    public void moveToTab(EditorAdaptor editorAdaptor, int index) throws CommandExecutionException {
        IWorkbench workbench = PlatformUI.getWorkbench();
        IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
        IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();

        IEditorPart[] editors;
        IEditorReference[] editorReferences = activePage.getEditorReferences();
        editors = new IEditorPart[editorReferences.length];
        for(int i=0; i < editorReferences.length; i++) {
        	IEditorReference eRef = editorReferences[i];
        	editors[i] = eRef.getEditor(true);
        }

        int nextEditorIndex = index % editors.length;
        IEditorPart nextEditor = editors[nextEditorIndex];
        activePage.activate(nextEditor);
    }
    */

}
