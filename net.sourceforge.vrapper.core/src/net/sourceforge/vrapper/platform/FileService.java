package net.sourceforge.vrapper.platform;

import java.io.IOException;

public interface FileService {

	boolean openInGvim(String gvimpath, String gvimargs, int row, int col) throws IOException;
	
	boolean isEditable();

	boolean revertFile();
	
	boolean close(boolean force);
	
	boolean closeAll(boolean force);
	
	boolean closeOthers(boolean force);

	boolean save();
	
	boolean saveAll();
	
	boolean createFile(String filename);
	
	String findFileInPath(String filename, String previous, boolean reverse, String[] paths);
	
	String getFilePathMatch(String prefix, String previous, boolean reverse, String startDir);
	
	String getDirPathMatch(String prefix, String previous,  boolean reverse,String startDir);
	
	boolean openFile(String filename);
	
	boolean findAndOpenFile(String filename, String[] paths);
	
	/**
	 * Get the "absolute" path to the current file in the workspace, based on the workspace dir.
	 * <p>Example:<br/>
	 * <tt>/project/src/main/java/net/sf/vrapper/FileService.java</tt>
	 */
    String getCurrentFilePath();

    String getCurrentFileName();

    String getFileNameOfGlobalMark(String name);

    /**
     * Get the absolute path to the current file.
     */
    String getCurrentFileLocation() throws IOException;
}
