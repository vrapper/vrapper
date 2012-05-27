package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.platform.FileService;

import org.eclipse.core.commands.common.CommandException;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class EclipseFileService implements FileService {

    private final AbstractTextEditor editor;

    public EclipseFileService(AbstractTextEditor editor) {
        this.editor = editor;
    }

    public boolean isEditable() {
        return editor.isEditable();
    }

    public boolean close(boolean force) {
        if (force || !editor.isDirty()) {
            editor.close(false);
            return true;
        }
        return false;
    }

    public boolean save() {
        if (editor.isDirty() && editor.isEditable()) {
        	//use the handler service so it sends all the right "save" events
        	//to any listeners (such as the AnyEdit plugin)
        	IHandlerService handlerService = (IHandlerService) PlatformUI
                    .getWorkbench().getService(IHandlerService.class);
            try {
                handlerService.executeCommand(IWorkbenchCommandConstants.FILE_SAVE, null);
            } catch (CommandException e) {
                return false;
            } 
        	
            return true;
        }
        return false;
    }
    
    public boolean saveAll() {
    	IHandlerService handlerService = (IHandlerService) PlatformUI
    			.getWorkbench().getService(IHandlerService.class);
    	try {
    		handlerService.executeCommand(IWorkbenchCommandConstants.FILE_SAVE_ALL, null);
    	} catch (CommandException e) {
    		return false;
    	} 

    	return true;
    }

}
