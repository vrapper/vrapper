package newpackage.glue;

public interface HistoryService {
	void undo();
	void redo();
	boolean undoable();
	boolean redoable();
	void beginCompoundChange();
	void endCompoundChange();
	void lock();
	void unlock();
}
