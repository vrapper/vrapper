package net.sourceforge.vrapper.eclipse.ui;

import net.sourceforge.vrapper.eclipse.interceptor.InputInterceptor;
import net.sourceforge.vrapper.eclipse.interceptor.VimInputInterceptorFactory;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.CommandLineUI;
import net.sourceforge.vrapper.vim.EditorAdaptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
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
    private final StyledText parent;
    private StyledText commandLineText;
    private EclipseCommandLineUI commandLineUI;
    private InputInterceptor inputInterceptor;

    public CommandLineUIFactory(StyledText parentText) {
        parent = parentText;

        StyledText widget = new StyledText(parent, SWT.ON_TOP | SWT.V_SCROLL);
        try {
            Method method = widget.getClass().getMethod("setAlwaysShowScrollBars", Boolean.TYPE);
            method.invoke(widget, false);
        } catch (SecurityException e) {
            VrapperLog.info("No permissions to use reflection on StyledText widget. " + e);
        } catch (IllegalArgumentException e) {
            VrapperLog.info("Wrong method signature for StyledText method. " + e);
        } catch (IllegalAccessException e) {
            VrapperLog.info("No access to StyledText method. " + e);
        } catch (InvocationTargetException e) {
            VrapperLog.error("Unexpected error while setting always show scrollbars property", e);
        } catch (NoSuchMethodException e) {
            // Expected for Eclipse 3.6 or 3.7.
        }
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
        
        parent.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                e.display.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        if (commandLineUI == null || ! commandLineUI.isOpen()) {
                            return;
                        }
                        parent.redraw();
                        parent.update();
                        commandLineUI.updateUISize();
                    }
                });
            }
        });
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
                if (commandLineUI != null) {
                    commandLineUI.dispose();
                }
            }
        });
    }

    /**
     * Listens to the paint events of the editor Vrapper has hooked into.
     * This allows us to reposition our command line so that it matches the editor.
     */
    class TextEditorPaintListener implements PaintListener {
        private int horScroll = 0;
        private int verScroll = 0;
        
        public void paintControl(PaintEvent e) {
            if (commandLineUI == null || ! commandLineUI.isOpen()) {
                return;
            }
            final StyledText parent = (StyledText) e.widget;
            e.gc.setForeground(parent.getForeground());
            e.gc.setBackground(parent.getBackground());
            int newHScroll = 0;
            int newVScroll = 0;
            int bottom = parent.getBounds().height;
            if (parent.getHorizontalBar() != null) {
                bottom -= parent.getHorizontalBar().getSize().y;
                newHScroll = parent.getHorizontalBar().getSelection();
            }
            int right = parent.getBounds().width;
            if (parent.getVerticalBar() != null) {
                right -= parent.getVerticalBar().getSize().x;
                newVScroll = parent.getVerticalBar().getSelection();
            }
            commandLineUI.setMaxHeight(parent.getBounds().height / 2);
            commandLineUI.setWidth(right - 1);
            commandLineUI.setBottom(bottom);
            Point size = commandLineText.getSize();
            if ((horScroll == newHScroll) && (verScroll == newVScroll)) {
                // Fix location
                commandLineText.setLocation(0, bottom - size.y);
                // Paint command line if there is no scrollbar or if the scrollbar is the same.
                // Run this outside the paint listener to let super class run its magic.
                e.display.asyncExec(new Runnable() {
                    
                    @Override
                    public void run() {
                        // Make sure any outstanding redraw calls on the parent are executed FIRST.
                        parent.update();
                        commandLineText.redraw();
                        // Immediately force our command line to be painted after calling redraw.
                        commandLineText.update();
                    }
                });
            } else {
                // The scrollbar offset changed. Retrigger this paint listener to draw the
                // command line in its correct spot, but do it outside this paint listener.
                e.display.asyncExec(new Runnable() {
                    
                    @Override
                    public void run() {
                        // Force complete redraw in case listener was called for partial redraw.
                        parent.redraw();
                        parent.update();
                    }
                });
                horScroll = newHScroll;
                verScroll = newVScroll;
            }
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

    public CommandLineUI createCommandLineUI(EditorAdaptor editorAdaptor) {
        if (inputInterceptor == null) {
            inputInterceptor = VimInputInterceptorFactory.INSTANCE.createInterceptor(editorAdaptor);
            commandLineText.addVerifyKeyListener(inputInterceptor);
        }
        if (commandLineUI == null) {
            commandLineUI = new EclipseCommandLineUI(commandLineText, editorAdaptor);
        }
        return commandLineUI;
    }
}
