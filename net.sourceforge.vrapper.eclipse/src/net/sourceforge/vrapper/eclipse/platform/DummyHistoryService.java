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

    public void beginCompoundChange() {
        // TODO Auto-generated method stub

    }

    public void connect(ITextViewer arg0) {
        // TODO Auto-generated method stub

    }

    public void disconnect() {
        // TODO Auto-generated method stub

    }

    public void endCompoundChange() {
        // TODO Auto-generated method stub

    }

    public void redo() {
        // TODO Auto-generated method stub

    }

    public boolean redoable() {
        // TODO Auto-generated method stub
        return false;
    }

    public void reset() {
        // TODO Auto-generated method stub

    }

    public void setMaximalUndoLevel(int arg0) {
        // TODO Auto-generated method stub

    }

    public void undo() {
        // TODO Auto-generated method stub

    }

    public boolean undoable() {
        // TODO Auto-generated method stub
        return false;
    }

    public void lock() {
        // TODO Auto-generated method stub
    }

    public void unlock() {
        // TODO Auto-generated method stub
    }

}