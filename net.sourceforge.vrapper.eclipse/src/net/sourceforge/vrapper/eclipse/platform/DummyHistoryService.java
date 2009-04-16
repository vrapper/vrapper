package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.platform.HistoryService;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IUndoManager;

/**
 * Dummy implementation to be used if no delegate is present
 *
 * @author Matthias Radig
 */
public class DummyHistoryService implements IUndoManager, HistoryService {

    @Override
    public void beginCompoundChange() {
        // TODO Auto-generated method stub

    }

    @Override
    public void connect(ITextViewer arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void disconnect() {
        // TODO Auto-generated method stub

    }

    @Override
    public void endCompoundChange() {
        // TODO Auto-generated method stub

    }

    @Override
    public void redo() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean redoable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }

    @Override
    public void setMaximalUndoLevel(int arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void undo() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean undoable() {
        // TODO Auto-generated method stub
        return false;
    }

	@Override
	public void lock() {
		// TODO Auto-generated method stub
	}

	@Override
	public void unlock() {
		// TODO Auto-generated method stub
	}

}