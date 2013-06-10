package net.sourceforge.vrapper.eclipse.ui;

import net.sourceforge.vrapper.eclipse.interceptor.InputInterceptor;
import net.sourceforge.vrapper.eclipse.interceptor.VimInputInterceptorFactory;
import net.sourceforge.vrapper.platform.CommandLineUI;
import net.sourceforge.vrapper.vim.EditorAdaptor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Component responsible for the management of {@link CommandLineUI} instances.
 *
 * @author Matthias Radig
 * @author Bert Jacobs
 */
public class CommandLineUIFactory {

    private final static int COMMAND_CHAR_INDENT = 5;
    private int horScroll = 0;
    private int verScroll = 0;
    private String content = "";
    private final StyledText parent;
    private StyledText commandLineText;
    private EclipseCommandLineUI commandLineUI;

    public CommandLineUIFactory(StyledText parentText) {
        parent = parentText;

        StyledText widget = new StyledText(parent, SWT.NONE);
        widget.setFont(parent.getFont());
        widget.setMargins(COMMAND_CHAR_INDENT, 3, 3, 3);
        widget.setSize(5, 5);
        widget.setBackground(parent.getBackground());
        widget.setForeground(parent.getForeground());
        widget.setWordWrap(true);
        widget.setEnabled(true);
//        widget.setCaretOffset(2);
        widget.moveAbove(parent);
        widget.setVisible(false);
        widget.addPaintListener(new BorderPaintListener());
        commandLineText = widget;
        
        parent.addPaintListener(new TextEditorPaintListener());
        parent.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (commandLineUI != null && commandLineUI.isOpen()) {
                    commandLineText.forceFocus();
                }
            }
        });
        parent.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                commandLineText.dispose();
            }
        });
    }

    /**
     * Listens to the paint events of the editor Vrapper has hooked into.
     * This allows us to reposition our command line so that it matches the editor.
     */
    class TextEditorPaintListener implements PaintListener {
        
        public void paintControl(PaintEvent e) {
            if ("".equals(content.trim())) {
                commandLineText.setVisible(false);
                return;
            }
            StyledText parent = (StyledText) e.widget;
            e.gc.setForeground(parent.getForeground());
            e.gc.setBackground(parent.getBackground());
            int bottom = parent.getBounds().height - parent.getHorizontalBar().getSize().y;
            int right = parent.getBounds().width - parent.getVerticalBar().getSize().x;
            // if the scrollbar changed, the whole component must be repaint
            if (horScroll == parent.getHorizontalBar().getSelection()
                    && verScroll == parent.getVerticalBar().getSelection()) {
                Point size = commandLineText.computeSize(right-1, SWT.DEFAULT, true);
                commandLineText.setSize(right -1, size.y);
                commandLineText.setLocation(0, bottom - size.y);
//                commandLineText.setVisible(true);
            } else {
                parent.redraw();
                horScroll = parent.getHorizontalBar().getSelection();
                verScroll = parent.getVerticalBar().getSelection();
            }
        }
    }
    
    protected static class EclipseCommandLineUI implements CommandLineUI {

        private StyledText commandLineText;
        private boolean opened;

        public EclipseCommandLineUI(StyledText commandLineText, EditorAdaptor target) {
            this.commandLineText = commandLineText;
            InputInterceptor interceptor = VimInputInterceptorFactory.INSTANCE.createInterceptor(target);
            commandLineText.addVerifyKeyListener(interceptor);
            commandLineText.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    super.keyPressed(e);
                }
            });
        }

        public boolean isOpen() {
            return opened;
        }

        @Override
        public void setPrompt(String prompt) {
            
        }

        @Override
        public void setContents(String contents) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public String getContents() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getFullContents() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void append(String characters) {
            // TODO Auto-generated method stub
            
        }

        public void open() {
            commandLineText.setVisible(true);
            commandLineText.setFocus();
            opened = true;
        }

        @Override
        public void close() {
            commandLineText.setVisible(false);
            opened = false;
        }

    }

    /** Draws a rectangle inside the client area. This is used to draw in the same color as the text
     *  because SWT.BORDER style uses a system-defined color.
     */
    class BorderPaintListener implements PaintListener {

        @Override
        public void paintControl(PaintEvent e) {
            StyledText parent = (StyledText) e.widget;
            Rectangle r = parent.getBounds();
            r = new Rectangle(0, 0, r.width - 1, r.height - 1);
            e.gc.setForeground(parent.getForeground());
            e.gc.setBackground(parent.getForeground());
            e.gc.setLineWidth(1);
            e.gc.drawRectangle(r);
        }
    }

    /**
     * @return the string that is currently displayed by this instance.
     */
    public String getContent() {
        return content;
    }

    /**
     * @param content the string this instance should display.
     */
    public void setContent(String content) {
        this.content = content;
        commandLineText.setText(content);
//        commandLineUI.setContents(content);
    }

    /** Set the position of the caret in characters.
     *
     * @param position the position of the caret in characters.
     */
    public void setCaretPosition(int position) {
//        commandLineText.setCaretOffset(position);
    }

    public CommandLineUI createCommandLineUI(EditorAdaptor editorAdaptor) {
        if (commandLineUI == null) {
            commandLineUI = new EclipseCommandLineUI(commandLineText, editorAdaptor);
        }
        commandLineUI.open();
        parent.redraw();
        return commandLineUI;
    }
}
