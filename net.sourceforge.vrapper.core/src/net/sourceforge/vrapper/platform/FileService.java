package net.sourceforge.vrapper.platform;

public interface FileService {

	boolean isEditable();

	boolean close(boolean force);

	boolean save();
	
	boolean saveAll();
	
	String findFileInPath(String filename, String previous, String[] paths);
	
	String getFilePathMatch(String prefix, String previous, String startDir);
	
	String getDirPathMatch(String prefix, String previous, String startDir);
	
	boolean openFile(String filename);
	
	boolean findAndOpenFile(String filename, String[] paths);
	
    String getCurrentFilePath();
}
