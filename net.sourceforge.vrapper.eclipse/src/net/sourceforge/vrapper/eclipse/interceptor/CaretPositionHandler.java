package net.sourceforge.vrapper.eclipse.interceptor;

import net.sourceforge.vrapper.eclipse.activator.VrapperPlugin;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.vim.DefaultEditorAdaptor;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;
import net.sourceforge.vrapper.vim.modes.EditorMode;
import net.sourceforge.vrapper.vim.modes.NormalMode;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;

/**
 * Detects when a mouse click results in a caret move and optionally fixes the
 * cursor position.
 */
public class CaretPositionHandler implements CaretListener, MouseListener {
    private boolean caretMoved;
    private ITextViewer textViewer;
    private DefaultEditorAdaptor editorAdaptor;

    public CaretPositionHandler(DefaultEditorAdaptor editorAdaptor,
            ITextViewer textViewer) {
        this.editorAdaptor = editorAdaptor;
        this.textViewer = textViewer;
    }

    @Override
    public void mouseDoubleClick(MouseEvent e) {
    }

    @Override
    public void mouseDown(MouseEvent e) {
        EditorMode mode = getCurrentMode(editorAdaptor);
        if (!VrapperPlugin.isVrapperEnabled() || !caretMoved || ! (mode instanceof NormalMode)) {
            return;
        }
        StyledText w = textViewer.getTextWidget();
        w.setRedraw(false);
        try {
            int currentOffset = w.getCaretOffset();

            // Find out what line we are in and what length it has.
            int lineNo = w.getLineAtOffset(currentOffset);
            int lineStartOffset = w.getOffsetAtLine(lineNo);
            String lineContents = w.getLine(lineNo);
            int lineEndOffset = lineStartOffset + lineContents.length();

            // Detect if caret was moved after last character. It is possible that the caret is
            // already at the right position because this mouse click might have dropped us out of
            // visual mode. In that case NormalMode.placeCursor() did its magic already.
            if (currentOffset == lineEndOffset) {
                // Change caret until the user has let go of the mouse button.
                // We can't move the caret here or we risk changing the user's selection for him.
                editorAdaptor.getCursorService().setCaret(CaretType.LEFT_SHIFTED_RECTANGULAR);
            }
        } catch (RuntimeException ex) {
            VrapperLog.error("CaretPositionHandler failed on mouse click.", ex);
        } finally {
            w.setRedraw(true);
        }
    }

    @Override
    public void mouseUp(MouseEvent e) {
        EditorMode mode = getCurrentMode(editorAdaptor);
        int selectionLength = textViewer.getSelectedRange().y;

        if (!VrapperPlugin.isVrapperEnabled() || !caretMoved
                || selectionLength > 0 || ! (mode instanceof NormalMode)) {
            // Always reset this flag.
            caretMoved = false;
            return;
        }
        // First disable redraw so that the user doesn't see the cursor move
        // twice.
        textViewer.getTextWidget().setRedraw(false);
        try {
            // Reset caret type, see mouseDown() above.
            editorAdaptor.getCursorService().setCaret(CaretType.RECTANGULAR);
            NormalMode normalMode = (NormalMode) mode;
            normalMode.placeCursor(StickyColumnPolicy.RESET_EOL);
        } finally {
            textViewer.getTextWidget().setRedraw(true);
            caretMoved = false;
        }
    }

    @Override
    public void caretMoved(CaretEvent event) {
        // We only care for the caret if moved by a mouse click.
        // The global mouse trigger is called before caret move, the one above
        // is called afterwards.
        caretMoved = VrapperPlugin.isMouseDown();
    }

    protected static EditorMode getCurrentMode(EditorAdaptor editorAdaptor) {
        return editorAdaptor.getMode(editorAdaptor.getCurrentModeName());
    }

}
