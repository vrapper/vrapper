package net.sourceforge.vrapper.eclipse.ui;

import net.sourceforge.vrapper.platform.CommandLineUI;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.register.Register;
import net.sourceforge.vrapper.vim.register.RegisterContent;
import net.sourceforge.vrapper.vim.register.RegisterManager;
import net.sourceforge.vrapper.vim.register.StringRegisterContent;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.services.IDisposable;

class EclipseCommandLineUI implements CommandLineUI, IDisposable, CaretListener, SelectionListener {

    private StyledText commandLineText;
    private Register clipboard;
    private String prompt;
    private int contentsOffset;
    private Caret defaultCaret;
    private Caret endCharCaret;
    private Menu contextMenu;
    private MenuItem cutItem;
    private MenuItem copyItem;
    private MenuItem pasteItem;
    private MenuItem selectAllItem;
    
    /**
     * Signals that a "register mode marker" was inserted at the start of the selection if set.
     */
    private Point registerModeSelection;
    /** Read-only mode active. This disables destructive context menu actions. */
    private boolean readOnly;

    public EclipseCommandLineUI(final StyledText commandLineText, final EditorAdaptor editorAdaptor) {
        clipboard = editorAdaptor.getRegisterManager()
                .getRegister(RegisterManager.REGISTER_NAME_CLIPBOARD);
        this.commandLineText = commandLineText;
        commandLineText.addCaretListener(this);
        commandLineText.addSelectionListener(this);
        
        this.defaultCaret = commandLineText.getCaret();
        this.endCharCaret = CaretUtils.createCaret(CaretType.RECTANGULAR, commandLineText);
        commandLineText.setCaret(endCharCaret);
        
        contextMenu = new Menu(commandLineText);
        cutItem = new MenuItem(contextMenu, SWT.DEFAULT);
        cutItem.setText("Cut");
        cutItem.setEnabled(false);
        copyItem = new MenuItem(contextMenu, SWT.DEFAULT);
        copyItem.setText("Copy\tCtrl-Y  ");
        copyItem.setEnabled(false);
        pasteItem = new MenuItem(contextMenu, SWT.DEFAULT);
        pasteItem.setText("Paste\tCtrl-R +");
        selectAllItem = new MenuItem(contextMenu, SWT.DEFAULT);
        selectAllItem.setText("Select All");
        
        commandLineText.setMenu(contextMenu);
        
        cutItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                copySelectionToClipboard();
                erase();
            }
        });
        copyItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                copySelectionToClipboard();
            }
        });
        pasteItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                RegisterContent content = clipboard.getContent();
                if (content.getPayloadType() == ContentType.TEXT) {
                    type(content.getText());
                }
            }
        });
        selectAllItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                commandLineText.setSelection(contentsOffset, commandLineText.getCharCount());
            }
        });
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
    
    public void copySelectionToClipboard() {
        clipSelection();
        String selection = commandLineText.getSelectionText();
        if (selection.isEmpty()) {
            selection = getContents();
        }
        clipboard.setContent(new StringRegisterContent(ContentType.TEXT, selection));
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
        //If the caret didn't move, no CaretEvent gets sent. Update manually.
        updateCaret();
    }

    @Override
    public void close() {
        commandLineText.setVisible(false);
        commandLineText.setEditable(true);
        registerModeSelection = null;
        prompt = "";
        contentsOffset = 0;
        resetContents("");
        
        readOnly = false;
        
        copyItem.setEnabled(false);
        cutItem.setEnabled(false);
        pasteItem.setEnabled(true);
    }

    @Override
    public void setMode(CommandLineMode mode) {
        if (mode == CommandLineMode.DEFAULT) {
            commandLineText.setEditable(true);
            if (registerModeSelection != null) {
                commandLineText.replaceTextRange(registerModeSelection.x, 1, "");
                commandLineText.setSelection(registerModeSelection);
                registerModeSelection = null;
            } else {
                //Reset any selection made in a readonly mode, otherwise we need to check if
                // cut/copy needs to be enabled.
                commandLineText.setSelection(commandLineText.getCaretOffset());
            }
            readOnly = false;
        } else if (mode == CommandLineMode.REGISTER) {
            Point sel = commandLineText.getSelection();
            int leftOffset = Math.min(sel.x, sel.y);
            commandLineText.replaceTextRange(leftOffset, 0, "\"");
            registerModeSelection = sel;
            readOnly = true;
        } else if (mode == CommandLineMode.MORE) {
            commandLineText.setEditable(false);
            readOnly = true;
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
                commandLineText.replaceTextRange(startOffset - 1, 1, "");
                //Caret listener is called before content is updated, update manually
                updateCaret();
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
        cutItem.dispose();
        copyItem.dispose();
        pasteItem.dispose();
        selectAllItem.dispose();
        contextMenu.dispose();
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

    @Override
    public void widgetSelected(SelectionEvent e) {
        clipSelection();
        int selectionLen = Math.abs(e.y - e.x);
        cutItem.setEnabled( ! readOnly && selectionLen > 0);
        copyItem.setEnabled(selectionLen > 0);
        pasteItem.setEnabled( ! readOnly);
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
    }
}
