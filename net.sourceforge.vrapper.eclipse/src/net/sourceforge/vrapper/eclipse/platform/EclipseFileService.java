package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.platform.FileService;

import org.eclipse.core.commands.common.CommandException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
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
    
    /**
     * @param filename name of file to find
     * @param previous the previous match found (if any)
     * @param paths list of paths to search for filename
     * @return filename found within one of the paths
     */
    public String findFileInPath(String filename, String previous, String[] paths) {
    	IContainer dir;
    	for(String path : paths) {
    		dir = resolvePath(path);
    		String fullPath = findNextMatchWithPrefix(filename, previous, dir);
    		//findPath just returns filename if no match found
    		if( ! fullPath.equals(filename)) {
    			return fullPath;
    		}
    	}
    	return filename;
    }
    
    /**
     * Perform tab-completion on a path.
     * Given a partial file path, return the next possible match.
     * 
     * @param prefix A partial file path we need to match
     * @param previous The previous match we found, find the next one after it
     * @return Path to the next file (or directory) that has 'prefix' in its path
     */
    public String getFilePathMatch(String prefix, String previous) {
    	return findNextMatchWithPrefix(prefix, previous, getCurrentSelectedProject());
    }
    
    /**
     * Find the first file or directory whose path contains 'prefix' after
     * any previous value 'previous'.  Start in 'startDir' and recurse if
     * necessary.
     * @param prefix partial file path to search for
     * @param previous previous match found (if any)
     * @param startDir parent directory to start search in
     * @return next file/folder (after 'previous') which contains 'prefix', or 'prefix' if none found
     */
    private String findNextMatchWithPrefix(String prefix, String previous, IContainer startDir) {
    	boolean foundPrevious = previous == null;
    	
    	try {
			for(IResource resource : startDir.members()) {
				String path = resource.getName();
				if(resource.getType() == IResource.FOLDER) {
					path += '/';
				}
				
				//prefix is in this folder, go into it
				if(resource.getType() == IResource.FOLDER && prefix.startsWith(path)) {
					if(previous != null && previous.startsWith(path)) {
						previous = previous.substring(path.length());
					}
					return path + findNextMatchWithPrefix(prefix.substring(path.length()), previous, (IFolder)resource);
				}
				//keep looping until we hit the previous match
				else if( ! foundPrevious) {
					if(path.equals(previous)) {
						foundPrevious = true;
					}
				}
				else if(path.startsWith(prefix)) {
					return path;
				}
			}
			//if we never found the previous, try again
			//but this time, don't look for previous
			//(useful when 'previous' was in another path)
			if( ! foundPrevious) {
				return findNextMatchWithPrefix(prefix, null, startDir);
			}
		} catch (CoreException e) {
			return prefix;
		}
    	
    	//couldn't find a more-specific path
    	//the user needs to provide more information
    	return prefix;
    }
    
    /**
     * Open file with default Eclipse editor.  File may
     * be found under any of the directories within 'paths'.
     * 
     * @param filename to open
     * @param paths list of directories to search for filename
     * @return true if file opened successfully
     */
    public boolean findAndOpenFile(String filename, String paths[]) {
    	String fullPath = filename;
    	IContainer dir;
    	for(String path : paths) {
    		dir = resolvePath(path);
    		if(dir.findMember(filename) != null) {
    			fullPath = dir.getProjectRelativePath().toString() + '/' + filename;
    		}
    	}
    	return openFile(fullPath);
    }
    
    /**
     * Open file with default Eclipse editor.  Filename is relative
     * to project root.
     * 
     * @param filename project-local path to file
     * @return true if file opened successfully
     */
    public boolean openFile(String filename) {
    	if(filename == null || filename.length() == 0) {
    		return false;
    	}
    	
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
    
    private IContainer resolvePath(String path) {
    	IProject project = getCurrentSelectedProject();
    	IContainer dir;
    	if(path.equals("/")) { //project root
    		dir = project;
    	}
    	else if(path.equals(".") || path.equals("")) { //current file's directory
    		path = getCurrentFileDir();
    		dir = project.getFolder(path);
    	}
    	else { //normal directory
    		dir = project.getFolder(path);
    	}
    	return dir;
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
    
    private String getCurrentFileDir() {
    	IEditorPart  editorPart =
    			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
    	
    	if(editorPart  != null) {
    		IFileEditorInput input = (IFileEditorInput)editorPart.getEditorInput() ;
    		IFile file = input.getFile();
    		return file.getParent().getProjectRelativePath().toString();
    	}
    	return "";
    }

}
