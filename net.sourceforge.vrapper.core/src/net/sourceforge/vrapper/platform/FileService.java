package net.sourceforge.vrapper.platform;

public interface FileService {

	boolean openInGvim(String gvimpath, int modelOffset);
	
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
	
    String getCurrentFilePath();
}
