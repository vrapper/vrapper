package net.sourceforge.vrapper.eclipse.platform;

import java.util.Arrays;
import java.util.Collections;

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
    
    public boolean closeAll(boolean force) {
    	IWorkbenchPage page = editor.getSite().getPage();
        if (force || page.getDirtyEditors().length == 0) {
        	page.closeAllEditors(false);
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
     * @param reverse search forwards for next match or backwards
     * @param paths list of paths to search for filename
     * @return filename found within one of the paths
     */
    public String findFileInPath(String filename, String previous, boolean reverse, String[] paths) {
    	IContainer dir;
    	String[] toSearch = paths;
    	if(reverse) {
    		Collections.reverse(Arrays.asList(toSearch));
    	}
    	for(String path : toSearch) {
    		dir = resolvePath(path);
    		String fullPath = findNextMatchWithPrefix(filename, previous, reverse, dir);
    		//findPath just returns filename if no match found
    		if( ! fullPath.equals(filename)) {
    			return fullPath;
    		}
    	}
    	return filename;
    }
    
    /**
     * Similar to getFilePathMatch, except only return directories.
     */
	public String getDirPathMatch(String prefix, String previous, boolean reverse, String startDir) {
		String file;
		while(true) {
			file = getFilePathMatch(prefix, previous, reverse, startDir);
			if(file.equals(prefix) || file.endsWith("/")) {
				//no match or we found a dir
				//either way, exit
				return file;
			}
			//else, matched a file, try again
			previous = file;
		}
	}
    
    /**
     * Perform tab-completion on a path.
     * Given a partial file path, return the next possible match.
     * 
     * @param prefix A partial file path we need to match
     * @param previous The previous match we found, find the next one after it
     * @param reverse search forwards for next match or backwards
     * @param startDir name of directory to start search in
     * @return Path to the next file (or directory) that has 'prefix' in its path
     */
    public String getFilePathMatch(String prefix, String previous, boolean reverse, String startDir) {
    	IProject project = getCurrentSelectedProject();
    	boolean absolutePath = false;
    	IContainer start;
    	if(prefix.startsWith("/")) {
    		absolutePath = true;
    		start = project;
    		//chop off leading '/' so 'startsWith' comparisons will work
    		prefix = prefix.substring(1);
    		//if previous is defined, it's probably an absolute path too
    		if(previous != null && previous.startsWith("/")) {
    			previous = previous.substring(1);
    		}
    	}
    	else if("".equals(startDir) || "/".equals(startDir)) {
    		start = project;
    	}
    	else if(".".equals(startDir)) {
    		start = getCurrentFileDir();
    	}
    	else {
    		start = project.getFolder(startDir);
    	}
    	
    	String unresolvedPathToPrefix = "";
    	if(prefix.contains("../")) {
    		//unresolved path still needs to be displayed to user
    		//even though that isn't the path we use to search
    		//(e.g., '../foo/../foo/bar' not resolved 'foo/bar')
    		unresolvedPathToPrefix = prefix.substring(0, prefix.lastIndexOf('/') + 1);
    		
    		//after last '/' is the actual prefix we want to match (it may be "")
    		prefix = prefix.substring(prefix.lastIndexOf('/') + 1);
    		
    		//previous is probably in the same unresolved dir as prefix
    		if(previous != null && previous.startsWith(unresolvedPathToPrefix) ) {
    			previous = previous.substring(unresolvedPathToPrefix.length());
    		}
    		
    		//resolvePath doesn't know current working directory
    		//must use absolute path
    		if(absolutePath) {
    			start = resolvePath("/"+unresolvedPathToPrefix);
    		}
    		else {
    			start = resolvePath(startDir + "/" + unresolvedPathToPrefix);
    		}
    	}
    	
    	String nextMatch;
    	if("..".equals(prefix) || prefix.endsWith("/..")) {
    		if(previous != null) { //if hitting tab on '../', return '..'
    			//This is a horrible special case that tells the caller that
    			//there was only one match and therefore it's a directory and
    			//therefore you should go into that directory... horrible.
    			//See rant in FilePathTabCompletion.java
    			nextMatch = prefix;
    		}
    		else { //if hitting tab on '..', return '../'
    			nextMatch = prefix + "/";
    		}
    	}
    	else {
    		nextMatch = findNextMatchWithPrefix(prefix, previous, reverse, start);
    	}
    	
    	//reapply mess of unresolved '../'
    	if(unresolvedPathToPrefix.length() > 0) {
    		nextMatch = unresolvedPathToPrefix + nextMatch;
    	}
    	//reapply leading '/'
    	if(absolutePath) {
    		nextMatch = "/" + nextMatch;
    	}
    	return nextMatch;
    }
    
    /**
     * Find the first file or directory whose path contains 'prefix' after
     * any previous value 'previous'.  Start in 'startDir' and recurse if
     * necessary.
     * @param prefix partial file path to search for
     * @param previous previous match found (if any)
     * @param reverse search forwards for next match or backwards
     * @param startDir parent directory to start search in
     * @return next file/folder (after 'previous') which contains 'prefix', or 'prefix' if none found
     */
    private String findNextMatchWithPrefix(String prefix, String previous, boolean reverse, IContainer startDir) {
    	boolean foundPrevious = previous == null;
    	
    	try {
    		IResource[] toSearch = startDir.members();
    		if(reverse) {
    			Collections.reverse(Arrays.asList(toSearch));
    		}
			for(IResource resource : toSearch) {
				String path = resource.getName();
				if(resource.getType() == IResource.FOLDER) {
					path += '/';
				}
				
				//prefix is in this folder, go into it
				if(resource.getType() == IResource.FOLDER && prefix.startsWith(path)) {
					if(previous != null && previous.startsWith(path)) {
						previous = previous.substring(path.length());
					}
					return path + findNextMatchWithPrefix(prefix.substring(path.length()), previous, reverse, (IFolder)resource);
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
				return findNextMatchWithPrefix(prefix, null, reverse, startDir);
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
     * @param filename file to open
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
    			break;
    		}
    	}
    	return openFile(fullPath);
    }
    
    /**
     * Open file with default Eclipse editor.
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
    	
    	//need to resolve current file's directory
    	if(filename.startsWith("./")) {
    		String file = filename.substring(1);
    		String dir = resolvePath(".").getProjectRelativePath().toString();
    		filename = dir + file;
    	}
    	
    	IFile fileToBeOpened = project.getFile(filename);
    	if( ! fileToBeOpened.exists()) {
    		return false;
    	}
    	
    	IEditorInput editorInput = new FileEditorInput(fileToBeOpened);
    	IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(fileToBeOpened.getName());
    	try {
    		if(desc == null) {
    			//file doesn't have file extension, Eclipse can't guess its type
    			page.openEditor(editorInput, "org.eclipse.ui.DefaultTextEditor");
    		}
    		else {
    			page.openEditor(editorInput, desc.getId());
    		}
		} catch (PartInitException e) {
			return false;
		}
    	
    	return true;
    }
    
    public String getCurrentFilePath() {
    	return "/" + getCurrentFileDir().getProjectRelativePath().toString();
    }
    
    private IContainer resolvePath(String path) {
    	IProject project = getCurrentSelectedProject();
    	IContainer dir;
    	if(path.equals("/")) { //project root
    		dir = project;
    	}
    	else if(path.equals(".") || path.equals("")) { //current file's directory
    		dir = getCurrentFileDir();
    	}
    	else if(path.contains("../")) { //path that moves into parent directories
    		if(path.startsWith("/")) {
    			//absolute path, start at project root
    			dir = project;
    		}
    		else {
    			//we don't know current working directory,
    			//try current file dir instead
    			dir = getCurrentFileDir();
    		}
    		
    		String[] pieces = path.split("/");
    		for(String piece : pieces) {
    			if("".equals(piece)) {
    				//go to next piece (leading '/' or intermediate '//')
    				continue;
    			}
    			
    			if("..".equals(piece)) {
    				//if we're not at project root, move up to parent dir
    				if(! dir.getProjectRelativePath().toString().equals("")) {
    					dir = dir.getParent();
    				}
    			}
    			else {
    				//move down a dir
    				dir = project.getFolder( dir.getProjectRelativePath().toString() + "/" + piece );
    			}
    		}
    	}
    	else { //normal directory
    		dir = project.getFolder(path);
    	}
    	return dir;
    }
    
    private IProject getCurrentSelectedProject() {
    	IFile file = getCurrentFile();
    	if(file != null) {
    		return file.getProject();
    	}
    	return null;
    }
    
    private IContainer getCurrentFileDir() {
    	IFile file = getCurrentFile();
    	if(file != null) {
    		IProject project = file.getProject();
    		String dir = file.getParent().getProjectRelativePath().toString();
    		return "".equals(dir) ? project : project.getFolder(dir);
    	}
    	return null;
    }
    
    private IFile getCurrentFile() {
    	IEditorPart  editorPart =
    			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
    	
    	if(editorPart  != null) {
    		IFileEditorInput input = (IFileEditorInput)editorPart.getEditorInput() ;
    		IFile file = input.getFile();
    		return file;
    	}
    	return null;
    }

}
