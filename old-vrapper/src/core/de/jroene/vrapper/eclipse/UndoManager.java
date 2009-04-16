package de.jroene.vrapper.eclipse;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IUndoManager;

public class UndoManager implements IUndoManager {

    private final IUndoManager delegate;
    private boolean locked;

    public UndoManager(IUndoManager delegate) {
        super();
        this.delegate = delegate;
    }

    public void lock() {
        locked = true;
    }

    public void unlock() {
        locked = false;
    }

    public void beginCompoundChange() {
        if (!locked) {
            delegate.beginCompoundChange();
        }
    }

    public void endCompoundChange() {
        if (!locked) {
            delegate.endCompoundChange();
        }
    }

    public void connect(ITextViewer arg0) {
        delegate.connect(arg0);
    }

    public void disconnect() {
        delegate.disconnect();
    }

    public void redo() {
        delegate.redo();
    }

    public boolean redoable() {
        return delegate.redoable();
    }

    public void reset() {
        delegate.reset();
    }

    public void setMaximalUndoLevel(int arg0) {
        delegate.setMaximalUndoLevel(arg0);
    }

    public void undo() {
        delegate.undo();
    }

    public boolean undoable() {
        return delegate.undoable();
    }

    /**
     * Dummy implementation to be used if no delegate is present
     *
     * @author Matthias Radig
     */
    static class Dummy extends UndoManager {

        public Dummy() {
            super(null);
        }

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

    }

}
