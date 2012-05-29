package net.sourceforge.vrapper.eclipse.ui;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Rectangle;

/**
 * A PaintListener which paints a line of text at the bottom of a component.
 *
 * @author Matthias Radig
 */
public class StatusLine implements PaintListener {

    private final static int COMMAND_CHAR_INDENT = 5;
    private int horScroll = 0;
    private int verScroll = 0;
    private String content = "";
    private final StyledText parent;
    private Rectangle currentRect;
    private int position = 0;

    public StatusLine(StyledText textWidget) {
        this.parent = textWidget;
        parent.addPaintListener(this);
    }

    public void paintControl(PaintEvent e) {
        if ("".equals(content.trim())) {
            return;
        }
        StyledText parent = (StyledText) e.widget;
        e.gc.setForeground(parent.getForeground());
        e.gc.setBackground(parent.getBackground());
        int bottom = parent.getBounds().height
        - parent.getHorizontalBar().getSize().y;
        int right = parent.getBounds().width
        - parent.getVerticalBar().getSize().x;
        int height = (int) (e.gc.getFontMetrics().getHeight() * 1.5);
        int offset = (int) (height / 6.0);
        Rectangle rect = new Rectangle(0, bottom - height, right-1, height-1);
        // if the scrollbar changed, the whole component must be repaint
        if (horScroll == parent.getHorizontalBar().getSelection()
                && verScroll == parent.getVerticalBar().getSelection()) {
            e.gc.setLineWidth(1);
            e.gc.fillRectangle(rect);
            e.gc.drawRectangle(rect);
            int x1 = COMMAND_CHAR_INDENT;
            e.gc.drawString(content, x1, bottom - height + offset);
            // draw the caret
            int y1 = bottom - height + offset;
            int y2 = bottom + offset;
            for (int i = 0; i < position; i++) {
                x1 += e.gc.getAdvanceWidth(content.charAt(i));
            }
            e.gc.drawLine(x1, y1, x1, y2);
        } else {
            parent.redraw();
            horScroll = parent.getHorizontalBar().getSelection();
            verScroll = parent.getVerticalBar().getSelection();
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
        if (currentRect != null) {
            parent.redraw(currentRect.x, currentRect.y, currentRect.width,
                    currentRect.height, true);
        } else {
            parent.redraw();
        }
    }

    /** Set the position of the caret in characters.
     *
     * @param position the position of the caret in characters.
     */
    public void setCaretPosition(int position) {
        this.position = position;
    }

    /**
     * Controls whether the status line will be painted into the parent component.
     * @param enabled whether the status line will be painted.
     */
    public void setEnabled(boolean enabled) {
        if (enabled) {
            parent.addPaintListener(this);
        } else {
            parent.removePaintListener(this);
            setContent("");
            currentRect = null;
        }
    }

}
