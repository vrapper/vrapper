package net.sourceforge.vrapper.eclipse.ui;

import net.sourceforge.vrapper.utils.CaretType;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Caret;

public class CaretUtils {

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
	    }

	    return caret;
	}

    // XXX: this is EXTREMALLY evil :->
	private static final class EvilCaret extends Caret {
        private final int height;
        private boolean shiftLeft;

        private EvilCaret(Canvas parent, int style, int height) {
            super(parent, style);
            this.height = height;
        }

        @Override protected void checkSubclass() { }

        @Override
        public void setLocation(int x, int y) {
            if(shiftLeft) {
                x -= getSize().x;
            }
        	super.setLocation(x, y + height - getSize().y);
        }

        private void setShiftLeft(boolean shift) {
            shiftLeft = shift;
        }
    }



}
