package net.sourceforge.vrapper.eclipse.interceptor;

import net.sourceforge.vrapper.eclipse.activator.VrapperPlugin;
import net.sourceforge.vrapper.eclipse.platform.EclipseCursorAndSelection;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.vim.DefaultEditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.modes.AbstractVisualMode;
import net.sourceforge.vrapper.vim.modes.CommandBasedMode;
import net.sourceforge.vrapper.vim.modes.EditorMode;
import net.sourceforge.vrapper.vim.modes.InsertMode;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.TempVisualMode;
import net.sourceforge.vrapper.vim.modes.TemporaryMode;
import net.sourceforge.vrapper.vim.modes.VisualMode;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

public class SelectionVisualHandler implements ISelectionChangedListener {

    private DefaultEditorAdaptor editorAdaptor;
    private EclipseCursorAndSelection selectionService;
    private ITextViewer textViewer;
    private int selectionResetOffset = -1;

    public SelectionVisualHandler(DefaultEditorAdaptor editorAdaptor,
            EclipseCursorAndSelection selectionService, ITextViewer viewer) {
        this.editorAdaptor = editorAdaptor;
        this.textViewer = viewer;
        this.selectionService = selectionService;
    }

    public void selectionChanged(SelectionChangedEvent event) {
        if (!VrapperPlugin.isVrapperEnabled() || !(event.getSelection() instanceof TextSelection)
                || selectionService.isSelectionInProgress()) {
            return;
        }

        TextSelection selection = (TextSelection) event.getSelection();
        // selection.isEmpty() is false even if length == 0, don't use it
        if (selection.getLength() == 0) {
            try {
                int offset = selection.getOffset();
                IRegion lineInfo = textViewer.getDocument().getLineInformationOfOffset(offset);
                // Checks if cursor is just before line end because Normalmode will move it.
                if (lineInfo.getOffset() + lineInfo.getLength() == offset) {
                    selectionResetOffset = offset;
                } else {
                    selectionResetOffset = -1;
                }
            } catch (BadLocationException e) {
                VrapperLog.error("Received bad selection offset in selectionchange handler", e);
            }
            EditorMode currentMode = editorAdaptor.getMode(editorAdaptor.getCurrentModeName());
            // User cleared selection or moved caret with mouse in a temporary mode.
            if(currentMode instanceof TemporaryMode) {
                editorAdaptor.changeModeSafely(InsertMode.NAME);
            } else if(currentMode instanceof AbstractVisualMode){
                editorAdaptor.changeModeSafely(NormalMode.NAME);
            // Cursor can be after the line if an Eclipse operation cleared the selection, e.g. undo
            } else if (currentMode instanceof CommandBasedMode) {
                CommandBasedMode commandMode = (CommandBasedMode) currentMode;
                commandMode.placeCursor();
            }
        } else if ( ! VrapperPlugin.isMouseDown()
                || !editorAdaptor.getConfiguration().get(Options.VISUAL_MOUSE)) {
            return;
        // Detect if a reverse selection got its last character chopped off.
        } else if (selectionResetOffset != -1
                && (selection.getOffset() + selection.getLength() + 1) == selectionResetOffset) {
            textViewer.setSelectedRange(selectionResetOffset, - (selection.getLength() + 1));
            selectionResetOffset = -1;
        } else if (selection.getLength() != 0) {
            // Fix caret type
            if (editorAdaptor.getConfiguration().get(Options.SELECTION).equals("inclusive")) {
                CaretType type = CaretType.LEFT_SHIFTED_RECTANGULAR;
                if (editorAdaptor.getSelection().isReversed()) {
                    type = CaretType.RECTANGULAR;
                }
                editorAdaptor.getCursorService().setCaret(type);
            }
            if(NormalMode.NAME.equals(editorAdaptor.getCurrentModeName())) {
                editorAdaptor.changeModeSafely(VisualMode.NAME, AbstractVisualMode.KEEP_SELECTION_HINT);
            }
            else if (InsertMode.NAME.equals(editorAdaptor.getCurrentModeName())) {
                editorAdaptor.changeModeSafely(TempVisualMode.NAME,
                        AbstractVisualMode.KEEP_SELECTION_HINT, InsertMode.DONT_MOVE_CURSOR);
            }
        }
    }
}
