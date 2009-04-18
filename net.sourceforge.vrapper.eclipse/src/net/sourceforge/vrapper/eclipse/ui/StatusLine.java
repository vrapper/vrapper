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

    private int horScroll = 0;
    private int verScroll = 0;
    private String content = "";
    private final StyledText parent;
    private Rectangle currentRect;

    public StatusLine(StyledText textWidget) {
        this.parent = textWidget;
        parent.addPaintListener(this);
    }

    public void paintControl(PaintEvent e) {
        if ("".equals(content.trim())) {
            return;
        }
        StyledText parent = (StyledText) e.widget;
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
//            Color color = e.gc.getForeground();
//            e.gc.setForeground(new Color(e.display, 0, 0, 0));
            e.gc.fillRectangle(rect);
            e.gc.drawRectangle(rect);
//            e.gc.setForeground(color);
            e.gc.drawString(content, 5, bottom - height + offset);
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
