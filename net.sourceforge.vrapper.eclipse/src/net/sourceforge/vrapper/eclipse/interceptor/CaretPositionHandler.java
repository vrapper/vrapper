package net.sourceforge.vrapper.eclipse.interceptor;

import net.sourceforge.vrapper.eclipse.activator.VrapperPlugin;
import net.sourceforge.vrapper.eclipse.platform.OffsetConverter;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.vim.DefaultEditorAdaptor;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.CommandBasedMode;
import net.sourceforge.vrapper.vim.modes.EditorMode;
import net.sourceforge.vrapper.vim.modes.NormalMode;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

/**
 * Detects when a mouse click results in a caret move and optionally fixes the
 * cursor position.
 */
public class CaretPositionHandler implements CaretListener, MouseListener {
    private boolean caretMoved;
    private ITextViewer textViewer;
    private DefaultEditorAdaptor editorAdaptor;
    private ITextViewerExtension5 converter;

    public CaretPositionHandler(DefaultEditorAdaptor editorAdaptor,
            ITextViewer textViewer) {
        this.editorAdaptor = editorAdaptor;
        this.textViewer = textViewer;
        this.converter = OffsetConverter.create(textViewer);
    }

    @Override
    public void mouseDoubleClick(MouseEvent e) {
    }

    @Override
    public void mouseDown(MouseEvent e) {
    }

    @Override
    public void mouseUp(MouseEvent e) {
        int selectionLength = textViewer.getSelectedRange().y;
        VrapperLog.info("Mouse up, selection length: " + selectionLength);
        if (!VrapperPlugin.isVrapperEnabled() || !caretMoved
                || selectionLength > 0) {
            // Always reset this flag.
            caretMoved = false;
            return;
        }
        // First disable redraw so that the user doesn't see the cursor move
        // twice.
        textViewer.getTextWidget().setRedraw(false);

        // Moving the cursor directly in this function triggers a cascade of
        // caretMoved invokes.
        // Do this asynchronously so that the cursor position has settled.
        Display.getCurrent().asyncExec(new Runnable() {
            @Override
            public void run() {
                try {
                    EditorMode mode = getCurrentMode(editorAdaptor);
                    VrapperLog.info("Handling mouse up, caret moved.");
                    if (mode instanceof CommandBasedMode) {
                        CommandBasedMode commandMode = (CommandBasedMode) mode;
                        commandMode.placeCursor();
                    }
                } finally {
                    textViewer.getTextWidget().setRedraw(true);
                }
            }
        });
        caretMoved = false;
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
