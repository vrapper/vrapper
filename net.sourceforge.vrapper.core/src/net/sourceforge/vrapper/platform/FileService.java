package net.sourceforge.vrapper.platform;

public interface FileService {

	boolean isEditable();

	boolean close(boolean force);

	boolean save();
	
	boolean saveAll();
	
	String findFileInPath(String filename, String previous, String[] paths, boolean getFullPath);
	
	String getFilePathMatch(String prefix, String previous);
	
	boolean openFile(String filename);
	
	boolean findAndOpenFile(String filename, String[] paths);
}
