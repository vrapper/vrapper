package net.sourceforge.vrapper.eclipse.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

/**
 * A PaintListener which paints a line of text at the bottom of a component.
 *
 * @author Matthias Radig
 */
public class StatusLine {

    private final static int COMMAND_CHAR_INDENT = 5;
    private int horScroll = 0;
    private int verScroll = 0;
    private String content = "";
    private final StyledText parent;
    private Rectangle currentRect;
    private int position = 0;
    private StyledText mStyledText;

    public StatusLine(StyledText textWidget) {
        this.parent = textWidget;
        mStyledText = new StyledText(parent, SWT.NONE);
        mStyledText.setFont(parent.getFont());
        mStyledText.setMargins(COMMAND_CHAR_INDENT, 3, 3, 3);
        mStyledText.setLeftMargin(COMMAND_CHAR_INDENT);
        mStyledText.setSize(5, 5);
        mStyledText.setBackground(parent.getBackground());
        mStyledText.setForeground(parent.getForeground());
        mStyledText.setWordWrap(true);
        mStyledText.setEnabled(false);
        mStyledText.setCaretOffset(2);
        mStyledText.moveAbove(parent);
        mStyledText.setVisible(false);
        mStyledText.addPaintListener(new BorderPaintListener());

        parent.addPaintListener(new TextEditorPaintListener());
        parent.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                mStyledText.dispose();
            }
        });
    }

    class TextEditorPaintListener implements PaintListener {
        
        public void paintControl(PaintEvent e) {
            if ("".equals(content.trim())) {
                mStyledText.setVisible(false);
                return;
            }
            StyledText parent = (StyledText) e.widget;
            Rectangle r = parent.getClientArea();
            e.gc.setForeground(parent.getForeground());
            e.gc.setBackground(parent.getBackground());
            int bottom = parent.getBounds().height - parent.getHorizontalBar().getSize().y;
            int right = parent.getBounds().width - parent.getVerticalBar().getSize().x;
            int height = (int) (e.gc.getFontMetrics().getHeight() * 1.5);
            int offset = (int) (height / 6.0);
            Rectangle rect = new Rectangle(0, bottom - height, right-1, height-1);
            // if the scrollbar changed, the whole component must be repaint
            if (horScroll == parent.getHorizontalBar().getSelection()
                    && verScroll == parent.getVerticalBar().getSelection()) {
                mStyledText.setText(content);
                Point size = mStyledText.computeSize(right-1, SWT.DEFAULT, true);
                mStyledText.setSize(right -1, size.y);
                mStyledText.setLocation(0, bottom - size.y);
                mStyledText.setVisible(true);
//            mStyledText.setBounds(rect);
//            mStyledText.print(e.gc);
//            e.gc.setLineWidth(1);
//            e.gc.fillRectangle(rect);
//            e.gc.drawRectangle(rect);
//            int x1 = COMMAND_CHAR_INDENT;
//            e.gc.drawString(content, x1, bottom - height + offset);
//            // draw the caret
//            int y1 = bottom - height + offset;
//            int y2 = y1 + e.gc.getFontMetrics().getHeight();
//            for (int i = 0; i < position; i++) {
//                x1 += e.gc.getAdvanceWidth(content.charAt(i));
//            }
//            
//            if(position == content.length()) {
//            	//if cursor is on last position, draw a rectangle
//            	//(matches vim behavior)
//            	e.gc.setBackground(parent.getForeground()); //black rectangle
//            	e.gc.fillRectangle(x1, y1, e.gc.getFontMetrics().getAverageCharWidth(), e.gc.getFontMetrics().getHeight());
//            }
//            else { //draw a caret between characters
//            	e.gc.drawLine(x1, y1, x1, y2);
//            }
            } else {
                parent.redraw();
                horScroll = parent.getHorizontalBar().getSelection();
                verScroll = parent.getVerticalBar().getSelection();
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
}
