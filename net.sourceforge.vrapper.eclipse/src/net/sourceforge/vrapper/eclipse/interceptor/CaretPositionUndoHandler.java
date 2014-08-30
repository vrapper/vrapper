package net.sourceforge.vrapper.eclipse.interceptor;

import static org.eclipse.core.commands.operations.OperationHistoryEvent.*;

import java.util.WeakHashMap;

import net.sourceforge.vrapper.eclipse.activator.VrapperPlugin;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.VrapperEventAdapter;
import net.sourceforge.vrapper.vim.VrapperEventListener;
import net.sourceforge.vrapper.vim.modes.EditorMode;

import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.jface.text.ITextViewer;

public class CaretPositionUndoHandler extends VrapperEventAdapter implements IOperationHistoryListener {

    protected static class VrapperState {
        public int caretOffset;
        public VrapperState(int caretOffset) {
            this.caretOffset = caretOffset;
        }
    }

    private ITextViewer textViewer;
    private EditorAdaptor editorAdaptor;
    private WeakHashMap<IUndoableOperation, VrapperState> historyInfo;
    private int previousCaretOffset;

    public CaretPositionUndoHandler(EditorAdaptor editorAdaptor, ITextViewer textViewer) {
        this.editorAdaptor = editorAdaptor;
        this.textViewer = textViewer;
        historyInfo = new WeakHashMap<IUndoableOperation, CaretPositionUndoHandler.VrapperState>();
    }

    @Override
    public void historyNotification(OperationHistoryEvent event) {
        // Check that this event wasn't triggered in another editor.
        IUndoContext testContext = new ObjectUndoContext(textViewer.getDocument());
        boolean isHandlerEnabled = editorAdaptor.getConfiguration().get(Options.UNDO_MOVES_CURSOR);
        if (VrapperPlugin.isVrapperEnabled() && isHandlerEnabled
                && event.getOperation().hasContext(testContext)) {
            switch (event.getEventType()) {
            case ABOUT_TO_EXECUTE:
                previousCaretOffset = textViewer.getSelectedRange().x;
                break;
            case ABOUT_TO_REDO:
                break;
            case ABOUT_TO_UNDO:
                break;
            case DONE:
                break;
            case OPERATION_ADDED:
                if (previousCaretOffset != -1) {
                    historyInfo.put(event.getOperation(), new VrapperState(previousCaretOffset));
                }
                previousCaretOffset = -1;
                break;
            case OPERATION_CHANGED:
                break;
            case OPERATION_NOT_OK:
                break;
            case OPERATION_REMOVED:
                break;
            case REDONE:
                break;
            case UNDONE:
                VrapperState state = historyInfo.get(event.getOperation());
                if (state != null) {
                    textViewer.setSelectedRange(state.caretOffset, 0);
                }
                break;
            default:
                    VrapperLog.info("Unknown history event type " + event.getEventType());
            }
        }
    }

    @Override
    public void commandAboutToExecute(EditorMode mode) {
        previousCaretOffset = textViewer.getSelectedRange().x;
    }

    @Override
    public void commandExecuted(EditorMode mode) {
        previousCaretOffset = -1;
    }
}
