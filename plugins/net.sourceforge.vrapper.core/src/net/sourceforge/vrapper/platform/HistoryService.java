package net.sourceforge.vrapper.platform;

public interface HistoryService {
	void undo();
	void redo();
	boolean undoable();
	boolean redoable();
	void beginCompoundChange();
	void endCompoundChange();
	void lock();
	void unlock();
	void lock(String name);
	void unlock(String name);
}
