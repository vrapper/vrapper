package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.platform.FileService;

import org.eclipse.core.commands.common.CommandException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.FileEditorInput;
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
    
    public boolean openFile(String filename) {
    	IProject project = getCurrentSelectedProject();
    	if(project == null) {
    		return false;
    	}
    	
    	IFile fileToBeOpened = project.getFile(filename);
    	if( ! fileToBeOpened.exists()) {
    		return false;
    	}
    	
    	IEditorInput editorInput = new FileEditorInput(fileToBeOpened);
    	IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(fileToBeOpened.getName());
    	try {
			page.openEditor(editorInput, desc.getId());
		} catch (PartInitException e) {
			return false;
		}
    	
    	return true;
    }
    
    private IProject getCurrentSelectedProject() {
    	IEditorPart  editorPart =
    			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
    	
    	if(editorPart  != null) {
    		IFileEditorInput input = (IFileEditorInput)editorPart.getEditorInput() ;
    		IFile file = input.getFile();
    		return file.getProject();
    	}
    	return null;
    }

}
