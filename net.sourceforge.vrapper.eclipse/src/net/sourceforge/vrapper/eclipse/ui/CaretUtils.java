package net.sourceforge.vrapper.eclipse.ui;

import net.sourceforge.vrapper.utils.CaretType;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Caret;

public class CaretUtils {

    /**
     * Creates a new caret of the given display type.
     * @param caretType Display type.
     * @param styledText Text widget which is the parent of the caret.
     * @return a Caret instance.
     */
    public static Caret createCaret(CaretType caretType, StyledText styledText) {
        GC gc = new GC(styledText);
        final int width = gc.getFontMetrics().getAverageCharWidth();
        final int height = gc.getFontMetrics().getHeight();
        gc.dispose();

        EvilCaret caret = new EvilCaret(styledText, SWT.NULL, height);

        switch (caretType) {
        case VERTICAL_BAR:
            caret.setSize(2, height);
            break;
        case RECTANGULAR:
            caret.setSize(width, height);
            break;
        case LEFT_SHIFTED_RECTANGULAR:
            caret.setSize(width, height);
            caret.setShiftLeft(true);
            break;
        case HALF_RECT:
            caret.setSize(width, height / 2);
            break;
        case UNDERLINE:
            caret.setSize(width, 3);
            break;
        case OVERLINE:
            caret.setSize(width, 3);
            caret.setShiftTop(false);
            break;
        }

        return caret;
    }

    // XXX: this is EXTREMALLY evil :->
    private static final class EvilCaret extends Caret {
        private final int textHeight;
        private boolean shiftLeft;
        private boolean shiftTop;

        /**
         * Set to true when a function higher up the call stack is one of the other methods below.
         * In a true multi-threaded application this should have been a ThreadLocal, but here it's
         * no issue since Eclipse only allows the singleton UI thread to manipulate widgets.
         */
        private boolean isReentrant;

        private EvilCaret(Canvas parent, int style, int textHeight) {
            super(parent, style);
            this.textHeight = textHeight;
            this.shiftTop = true;
        }

        @Override
        protected void checkSubclass() {}

        @Override
        public void setLocation(int x, int y) {
            boolean temp = isReentrant;
            try {
                if ( ! isReentrant) {
                    Point p = shift(new Point(x, y));
                    x = p.x;
                    y = p.y;
                    isReentrant = true;
                }
                super.setLocation(x, y);
            } finally {
                isReentrant = temp;
            }
        }

        @Override
        public void setLocation(Point location) {
            boolean temp = isReentrant;
            try {
                Point p = location;
                if ( ! isReentrant) {
                    p = shift(location);
                    isReentrant = true;
                }
                super.setLocation(p);
            } finally {
                isReentrant = temp;
            }
        }

        @Override
        public void setBounds(int x, int y, int width, int height) {
            boolean temp = isReentrant;
            try {
                if ( ! isReentrant) {
                    Point p = shift(new Point(x, y));
                    x = p.x;
                    y = p.y;
                    isReentrant = true;
                }
                super.setBounds(x, y, width, height);
            } finally {
                isReentrant = temp;
            }
        }

        @Override
        public void setBounds(Rectangle rect) {
            boolean temp = isReentrant;
            try {
                Point p = new Point(rect.x, rect.y);
                if ( ! isReentrant) {
                    p = shift(p);
                    isReentrant = true;
                }
                super.setBounds(new Rectangle(p.x, p.y, rect.width, rect.height));
            } finally {
                isReentrant = temp;
            }
        }

        private Point shift(Point p) {
            int x = p.x;
            int y = p.y;
            if (shiftLeft) {
                x -= getSize().x;
            }
            // Caret is placed top-left above a character but underline and half-block need to be
            // at the bottom. Fix this by offsetting with textHeight and correcting by size.
            if (shiftTop) {
                y += textHeight - getSize().y;
            }
            return new Point(x, y);
        }

        private void setShiftLeft(boolean shift) {
            shiftLeft = shift;
        }

        private void setShiftTop(boolean shift) {
            shiftTop = shift;
        }
    }
}
