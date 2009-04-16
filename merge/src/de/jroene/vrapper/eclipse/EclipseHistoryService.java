package de.jroene.vrapper.eclipse;

import newpackage.glue.HistoryService;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.swt.custom.StyledText;

public class EclipseHistoryService implements IUndoManager, HistoryService {

    private final IUndoManager delegate;
    private boolean locked;
	private final StyledText textWidget;

    public EclipseHistoryService(StyledText textWidget, IUndoManager delegate) {
		this.textWidget = textWidget;
		this.delegate = delegate;
    }

    @Override
    public void lock() {
        locked = true;
    }

    @Override
    public void unlock() {
        locked = false;
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
    public void connect(ITextViewer arg0) {
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
    public void setMaximalUndoLevel(int arg0) {
        delegate.setMaximalUndoLevel(arg0);
    }

    private void deselectAll() {
    	// XXX: we acheive some degree of Vim compatibility by jumping
    	// to beginning of selection; this is hackish
    	int caretOffset = textWidget.getSelection().x;
    	textWidget.setSelection(caretOffset);
	}

}
