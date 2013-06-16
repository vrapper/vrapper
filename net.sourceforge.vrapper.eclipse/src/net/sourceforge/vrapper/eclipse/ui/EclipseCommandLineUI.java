package net.sourceforge.vrapper.eclipse.ui;

import net.sourceforge.vrapper.platform.CommandLineUI;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.vim.EditorAdaptor;

import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.ui.services.IDisposable;

class EclipseCommandLineUI implements CommandLineUI, IDisposable, CaretListener {

    private StyledText commandLineText;
    private String prompt;
    private int contentsOffset;
    private Caret defaultCaret;
    private Caret endCharCaret;
    /**
     * Signals that a "register mode marker" was inserted at the start of the selection if set.
     */
    private Point registerModeSelection;

    public EclipseCommandLineUI(StyledText commandLineText, EditorAdaptor editorAdaptor) {
        this.commandLineText = commandLineText;
        commandLineText.addCaretListener(this);
        this.defaultCaret = commandLineText.getCaret();
        this.endCharCaret = CaretUtils.createCaret(CaretType.RECTANGULAR, commandLineText);
        commandLineText.setCaret(endCharCaret);
    }

    public boolean isOpen() {
        return commandLineText.isVisible();
    }

    @Override
    public void setPrompt(String newPrompt) {
        prompt = newPrompt;
        commandLineText.replaceTextRange(0, contentsOffset, newPrompt);
        contentsOffset = newPrompt.length();
        commandLineText.setCaretOffset(commandLineText.getCharCount());
    }

    @Override
    public void resetContents(String newContents) {
        commandLineText.replaceTextRange(contentsOffset,
                commandLineText.getCharCount() - contentsOffset, newContents);
        //Clear selection, set caret position to end of widget.
        commandLineText.setCaret(endCharCaret);
        commandLineText.setSelection(commandLineText.getCharCount());
    }

    @Override
    public String getContents() {
        int charCount = commandLineText.getCharCount();
        if (charCount == contentsOffset) {
            //SWT doesn't like text ranges where start == end, handle empty contents explicitly
            return "";
        } else {
            return commandLineText.getText(contentsOffset, charCount - 1);
        }
    }

    @Override
    public String getFullContents() {
        return commandLineText.getText();
    }

    @Override
    public void type(String characters) {
        clipSelection();
        int start = commandLineText.getCaretOffset();
        //Check caret position after replacing selection - caret might have been at end of selection
        if (commandLineText.getSelectionCount() > 0) {
            Point selection = commandLineText.getSelection();
            start = Math.min(selection.x, selection.y);
        }
        commandLineText.insert(characters);
        commandLineText.setCaretOffset(start + characters.length());
        //Mouse selection might cause caret to be at the same position as before, update manually
        updateCaret();
    }

    public void open() {
        commandLineText.setVisible(true);
        commandLineText.setFocus();
        commandLineText.getParent().redraw();
    }

    public int getPosition() {
        return commandLineText.getCaretOffset() - contentsOffset;
    }

    public void setPosition(int offset) {
        if (offset < 0) {
            offset = contentsOffset;
        } else if (offset + contentsOffset > commandLineText.getCharCount()) {
            offset = commandLineText.getCharCount();
        } else {
            offset += contentsOffset;
        }
        commandLineText.setCaretOffset(offset);
    }

    @Override
    public void close() {
        commandLineText.setVisible(false);
        commandLineText.setEditable(true);
        registerModeSelection = null;
        prompt = "";
        contentsOffset = 0;
        resetContents("");
    }

    @Override
    public void setMode(CommandLineMode mode) {
        if (mode == CommandLineMode.DEFAULT) {
            commandLineText.setEditable(true);
            if (registerModeSelection != null) {
                commandLineText.replaceTextRange(registerModeSelection.x, 1, "");
                commandLineText.setSelection(registerModeSelection);
                registerModeSelection = null;
            }
        } else if (mode == CommandLineMode.REGISTER) {
            Point sel = commandLineText.getSelection();
            int leftOffset = Math.min(sel.x, sel.y);
            commandLineText.replaceTextRange(leftOffset, 0, "\"");
            registerModeSelection = sel;
        } else if (mode == CommandLineMode.MORE) {
            commandLineText.setEditable(false);
        }
    }

    @Override
    public String getPrompt() {
        return prompt;
    }

    @Override
    public void addOffsetToPosition(int offset) {
        if (offset == 0) {
            return;
        }
        int newOffset = commandLineText.getCaretOffset() + offset;
        int endOffset = commandLineText.getCharCount();
        if (newOffset > endOffset) {
            newOffset = endOffset;
        } else if (newOffset < contentsOffset) {
            newOffset = contentsOffset;
        }
        commandLineText.setCaretOffset(newOffset);
    }

    @Override
    public void erase() {
        clipSelection();
        if (commandLineText.getSelectionCount() > 0) {
            commandLineText.insert("");
        } else {
            int startOffset = commandLineText.getCaretOffset();
            if (startOffset > contentsOffset) {
                commandLineText.setCaretOffset(startOffset - 1);
                commandLineText.replaceTextRange(startOffset - 1, 1, "");
            }
        }
    }

    @Override
    public void delete() {
        clipSelection();
        if (commandLineText.getSelectionCount() > 0) {
            commandLineText.insert("");
        } else {
            int startOffset = commandLineText.getCaretOffset();
            if (startOffset < commandLineText.getCharCount()) {
                commandLineText.replaceTextRange(startOffset, 1, "");
            }
        }
        //caret doesn't move if character after caret is deleted and won't trigger caretMoved
        //update manually
        updateCaret();
    }

    @Override
    public int getEndPosition() {
        return commandLineText.getCharCount() - contentsOffset;
    }

    @Override
    public void replace(int start, int end, String text) {
        int startOffset = start + contentsOffset;
        int endOffset = end + contentsOffset;
        commandLineText.replaceTextRange(startOffset, endOffset - startOffset, text);
    }

    @Override
    public void dispose() {
        endCharCaret.dispose();
    }

    @Override
    public void caretMoved(CaretEvent event) {
        updateCaret();
    }

    protected void updateCaret() {
        if (commandLineText.getCaretOffset() == commandLineText.getCharCount()) {
            commandLineText.setCaret(endCharCaret);
        } else {
            commandLineText.setCaret(defaultCaret);
        }
    }
    
    /** Makes sure that the prompt characters don't get erased when selected. */
    protected void clipSelection() {
        if (commandLineText.getSelectionCount() > 0) {
            Point sel = commandLineText.getSelection();
            int leftOffset = Math.min(sel.x, sel.y);
            int rightOffset = Math.max(sel.x, sel.y);
            if (leftOffset < contentsOffset) {
                leftOffset = contentsOffset;
            }
            if (rightOffset < contentsOffset) {
                rightOffset = contentsOffset;
            }
            commandLineText.setSelection(new Point(leftOffset, rightOffset));
        }
    }
}
