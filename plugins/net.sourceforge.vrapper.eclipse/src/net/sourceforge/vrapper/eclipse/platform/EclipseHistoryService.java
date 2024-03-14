package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.eclipse.interceptor.CaretPositionUndoHandler;
import net.sourceforge.vrapper.platform.HistoryService;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.IUndoManagerExtension;

public class EclipseHistoryService implements IUndoManager, IUndoManagerExtension, HistoryService {

    private final IUndoManager delegate;
    private boolean locked;
    private String lockName = "";
    private final ITextViewer textViewer;

    public EclipseHistoryService(final ITextViewer textViewer, final IUndoManager delegate) {
        this.textViewer = textViewer;
        this.delegate = delegate;
    }
    
    /**
     * Accept any arbitrary string as a lock name.  This is to ensure
     * that a parent class can lock and unlock without a child class
     * unknowingly locking and unlocking, removing that parent's lock.
     */
    @Override
    public void lock(final String name) {
    	if(!locked) {
    		locked = true;
    		lockName = name;
    	}
    }
    
    @Override
    public void unlock(final String name) {
    	if(locked && lockName.equals(name)) {
    		locked = false;
    		lockName = "";
    	}
    }

    @Override
    public void lock() {
        lock("unnamed");
    }

    @Override
    public void unlock() {
        unlock("unnamed");
    }

    @Override
    public void beginCompoundChange() {
        if (!locked) {
            delegate.beginCompoundChange();
        }
    }

    @Override
    public void endCompoundChange() {
        if (!locked) {
            delegate.endCompoundChange();
        }
    }

    @Override
    public void connect(final ITextViewer arg0) {
        delegate.connect(arg0);
    }

    @Override
    public void disconnect() {
        delegate.disconnect();
    }

    @Override
    public void undo() {
        delegate.undo();
        deselectAll();
    }

    @Override
    public void redo() {
        delegate.redo();
        deselectAll();
    }

    @Override
    public boolean undoable() {
        return delegate.undoable();
    }

    @Override
    public boolean redoable() {
        return delegate.redoable();
    }

    @Override
    public void reset() {
        delegate.reset();
    }

    @Override
    public void setMaximalUndoLevel(final int arg0) {
        delegate.setMaximalUndoLevel(arg0);
    }

    /**
     * Clears the Eclipse selection after doing an undo / redo action, Vim only shows a caret.
     * @see CaretPositionUndoHandler See CaretPositionUndoHandler as the caret's location is handled
     *  there - this function is just a fallback.
     */
    private void deselectAll() {
        final int caretOffset = textViewer.getSelectedRange().x;
        textViewer.setSelectedRange(caretOffset, 0);
    }

	@Override
    public IUndoContext getUndoContext() {
		if (delegate instanceof IUndoManagerExtension) {
			return ((IUndoManagerExtension)delegate).getUndoContext();
		}
		return null;
	}

}
