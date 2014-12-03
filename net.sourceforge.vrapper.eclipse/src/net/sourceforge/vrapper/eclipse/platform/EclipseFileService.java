package net.sourceforge.vrapper.eclipse.platform;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.FileService;

import org.eclipse.core.commands.common.CommandException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class EclipseFileService implements FileService {

    private final AbstractTextEditor editor;
    private String lastFindPath;
    private String lastFindPrevious;

    public EclipseFileService(AbstractTextEditor editor) {
        this.editor = editor;
    }
    
    /**
     * Open the current file in gvim to perform any operations Vrapper doesn't
     * support. The cursor will be in the exact same position in gvim as it was
     * in Vrapper.  As soon as you save and close gvim, the file will be reloaded.
     */
    public boolean openInGvim(String gvimpath, String gvimargs, int row, int col) throws IOException {
    	if(editor.isDirty()) {
    		editor.doSave(null);
    	}
    	IFile currentFile = getCurrentFile();
    	if (currentFile == null) {
    	    throw new IOException("Current editor did not have an associated IFile.");
    	}
        String filePath = currentFile.getRawLocation().toString();

        String[] defaultCmd = { gvimpath, "+" + row, "-c normal zv" + col + "|", "-c set nobackup", "-f", "-n", filePath };

        String[] customCmd = {};
        if(gvimargs.length() > 0) {
        	String gvimArgsExpanded = gvimargs.replace("{line}", ""+row).replace("{col}", ""+col).replace("{file}", filePath);
        	//keep quoted strings as a single argument
        	ArrayList<String> args = new ArrayList<String>();
        	args.add(gvimpath);
        	Matcher m = Pattern.compile("\"([^\"]*)\"|(\\S+)").matcher(gvimArgsExpanded);
        	while (m.find()) {
        		if (m.group(1) != null) {
        			//quoted strings
        			args.add(m.group(1));
        		} else {
        			//non-quoted strings
        			args.add(m.group(2));
        		}
        	}
        	customCmd = args.toArray(new String[args.size()]);
        }
        //variable must be final to be used in Thread
    	final String[] cmd = gvimargs.length() > 0 ? customCmd : defaultCmd;
    	new Thread() {
    		public void run() {
    			try {
    				Process p = Runtime.getRuntime().exec(cmd);
    				p.waitFor();
    				//tell eclipse to reload the file
    				runCommand(IWorkbenchCommandConstants.FILE_REFRESH);
    			} catch (Exception e) {
    				VrapperLog.error(e.getMessage());
    			}
    		}
    	}.start();
		return true;
    }

    public boolean isEditable() {
        return editor.isEditable();
    }
    
    @Override
    public boolean checkModifiable() {
        return editor.validateEditorInputState();
    }

    public boolean isReadOnly() {
        return editor.isEditorInputReadOnly();
    }

    private boolean runCommand(String commandId) {
    	IHandlerService handlerService = (IHandlerService) PlatformUI
                .getWorkbench().getService(IHandlerService.class);
        try {
			handlerService.executeCommand(commandId, null);
		} catch (CommandException e) {
			return false;
		}
    	return true;
    }
    
    public boolean refreshFile() {
        return runCommand(IWorkbenchCommandConstants.FILE_REFRESH);
    }
    
    public boolean revertFile() {
        return runCommand(IWorkbenchCommandConstants.FILE_REVERT);
    }

    public boolean close(boolean force) {
        if (force || !editor.isDirty()) {

        	final IWorkbenchPage workbenchPage = editor.getSite().getPage();

        	// 'editor' could be a page in a multi-part editor, and not all of them delegate the
        	// close operation to their parent editor.
        	// Get hold of the entire editor, and close that one instead.
            final IEditorPart topLevelEditor = workbenchPage.getActiveEditor();
            
            // Run async to avoid a NullPointerException in Vrapper (it has likely something to do
            //  with a disposed component).
            //  Async code will only run when Vrapper has processed all of its input.
        	editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    workbenchPage.closeEditor(topLevelEditor, false);
                }
            });
        	return true;
        }
        return false;
    }
    
    public boolean closeAll(boolean force) {
        boolean success = false;
    	IWorkbenchPage page = editor.getSite().getPage();
        if (force || page.getDirtyEditors().length == 0) {
            // Don't close the current editor until the others are successfully closed,
            //   otherwise the user might jump to an editor where Vrapper isn't active or supported.
            success = closeOthers(force);
            success = success && close(force);
        }
        return success;
    }
    
    public boolean closeOthers(boolean force) {
        IWorkbenchPage page = editor.getSite().getPage();
        if(page.getEditorReferences().length < 2) {
            return true;
        }
        
        boolean success = (force || page.getDirtyEditors().length == 0);

        // Vim behavior is to close all clean editors, yet keep the dirty ones if force is false.
        IEditorPart current = page.getActiveEditor();
        IEditorReference[] references = page.getEditorReferences();
        List<IEditorReference> toClose = new ArrayList<IEditorReference>();
        for (IEditorReference reference : references) {
            IEditorPart editor = reference.getEditor(false);
            // If editor == null, then it was never restored in this session and cannot be dirty.
            if (editor == null
                    || (! editor.equals(current) && (force || ! editor.isDirty()))) {
                toClose.add(reference);
            }
        }
        page.closeEditors(toClose.toArray(new IEditorReference[toClose.size()]), false);
        return success;
    }

    public boolean save() {
        if (editor.isDirty() && editor.isEditable()) {
        	//use the handler service so it sends all the right "save" events
        	//to any listeners (such as the AnyEdit plugin)
        	return runCommand(IWorkbenchCommandConstants.FILE_SAVE);
        }
        return false;
    }
    
    public boolean saveAll() {
    	return runCommand(IWorkbenchCommandConstants.FILE_SAVE_ALL);
    }
    
    /**
     * Create a new (empty) file at the location specified.
     * @param filename - full path of file to be created
     * @return true if success
     */
    public boolean createFile(String filename) {
    	IProject project = getCurrentSelectedProject();
    	IFile file = project.getFile(filename);
    	if(file.exists()) {
    		return false;
    	}
    	
    	try {
    		//I have to create a dummy instance of InputStream to create an empty file.
    		//If I send in 'null' as the InputStream, Eclipse flags the file as 'not local'
    		//which causes lots of issues.
			file.create(new InputStream() {
				@Override
				public int read() throws IOException {
					return -1;
				}
			}, false, null);
		} catch (CoreException e) {
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
    	//expand all '**' wildcards (if any)
    	List<String> dirs = expandPathNames(paths);
    	
    	if(reverse) {
    		Collections.reverse(dirs);
    	}
    	
    	//if we had a previous match, reset our starting point
    	//(start in the directory of 'previous')
    	if(lastFindPath != null && lastFindPrevious.equals(previous)) {
    		for(int i=0; i < dirs.size(); i++) {
    			//check index '0' each time since we're modifying the list
    			if(dirs.get(0).equals(lastFindPath)) {
    				//the item we want will start the iteration
    				break;
    			}
    			//move this item to the end of the list
    			dirs.add( dirs.remove(0) );
    		}
    	}
    	
    	IContainer dir;
    	for(String path : dirs) {
    		dir = resolvePath(path);
    		String fullPath = findNextMatchWithPrefix(filename, previous, reverse, dir);
    		//findPath returns filename if no match found
    		if( ! fullPath.equals(filename)) {
    			lastFindPath = path;
    			lastFindPrevious = fullPath;
    			return  fullPath;
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
					if(path.equals(previous) || resource.getProjectRelativePath().toString().equals(previous)) {
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
    	List<String> expandedPaths = expandPathNames(paths);
    	String fullPath = filename;
    	IContainer dir;
    	for(String path : expandedPaths) {
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

    public String getCurrentFileName() {
    	return "/" + getCurrentFile().getProjectRelativePath().toString();
    }

    public String getFileNameOfGlobalMark(String name) {
    	IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    	IMarker mark = null;
        try {
            final IMarker[] markers = root.findMarkers(IMarker.BOOKMARK, true, IResource.DEPTH_INFINITE);
            for (final IMarker m: markers) {
                if (m.getAttribute(IMarker.MESSAGE, "--").equals(name)) {
                    mark = m;
                    break;
                }
            }
        } catch (CoreException e) {
            // Ignore.
        }

        if(mark == null) {
        	return "";
        }
    	return "/" + mark.getResource().getProjectRelativePath().toString();
    }
    
    /**
     * Given a list of paths, replace any paths ending in '**' with
     * all subdirectories within that path.  Expanded directories are
     * inserted in place to preserve ordering of the path items.
     * I'm returning a List simply because I don't have a need to
     * convert it back to an array.
     */
    private List<String> expandPathNames(String[] paths) {
    	List<String> dirs = new ArrayList<String>();
    	for(String path : paths) {
    		if(path.endsWith("**")) {
    			List<String> expandedDirs = getWildcardDirectoryNames(path);
    			dirs.addAll(expandedDirs);
    		}
    		else {
    			dirs.add(path);
    		}
    	}
    	return dirs;
    }
    
    /**
     * The '**' wildcard should be expanded to represent all subdirectories
     * of its parent.  Take the 'path' variable (which should end in '**')
     * and expand out all its corresponding directories.
     * @param path - a path which ends with '**'
     * @return List of path Strings representing all subdirectories of the '**'
     */
    private List<String> getWildcardDirectoryNames(String path) {
    	IContainer start;
    	if(path.equals("**")) {
    		start = getCurrentSelectedProject();
    	}
    	else {
    		//everything up to the '**' (/foo/bar/**)
    		start = resolvePath( path.substring(0, path.indexOf('*')) );
    	}
    	
    	ArrayList<IResource> folders = new ArrayList<IResource>();
    	try {
    		folders.add(start);
    		IResource[] members = start.members();
    		getAllDirectories(folders, members);
    	} catch (CoreException e) {
    		//folder defined in path doesn't exist
    		return new ArrayList<String>();
    	} 
    	
    	ArrayList<String> dirs = new ArrayList<String>();
    	for(IResource folder : folders) {
    		//note that "root" will have a path of "" which is fixed below
    		dirs.add(folder.getProjectRelativePath().toString());
    	}
    	//alphabetical order so we iterate correctly
    	Collections.sort(dirs);
    	
    	if(path.equals("**") || path.equals("/**")) { //if 'root' is in this list
    		for(int i=0; i < dirs.size(); i++) {
    			if(dirs.get(i).equals("")) {
    				//treat root "" as "/" because "" is treated as current dir elsewhere in the algorithm
    				//also, force "/" to be the first item in the list (otherwise, '.' dirs get precedence)
    				dirs.remove(i);
    				dirs.add(0, "/");
    			}
    		}
    	}
    	
    	return dirs;
    }
    
    private void getAllDirectories(List<IResource> folders, IResource[] members) throws CoreException {
    	for(IResource member : members) {
    		if(member.getType() == IResource.FOLDER) {
    			folders.add(member);
    			getAllDirectories(folders, ((IContainer)member).members());
    		}
    	}
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
    	IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IEditorPart editorPart = activeWindow.getActivePage().getActiveEditor();

    	if (editorPart != null) {
    		IEditorInput input = editorPart.getEditorInput();
			// Can still be null at this point, let caller handle it.
    		return (IFile) input.getAdapter(IFile.class);
    	}
    	return null;
    }

    @Override
    public String getCurrentFileLocation() throws IOException {
        final IFile currentFile = getCurrentFile();
        if (currentFile == null) {
            throw new IOException("Current editor did not have an associated IFile.");
    	}
        return currentFile.getRawLocation().toString();
    }

}
