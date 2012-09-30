package net.sourceforge.vrapper.platform;

public interface FileService {

	boolean isEditable();

	boolean close(boolean force);

	boolean save();
	
	boolean saveAll();
	
	boolean openFile(String filename);
}
