package net.sourceforge.vrapper.eclipse.ui;

import net.sourceforge.vrapper.utils.CaretType;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Caret;

public class CaretUtils {

	public static Caret createCaret(CaretType caretType, StyledText styledText) {
	    GC gc = new GC(styledText);
        final int width = gc.getFontMetrics().getAverageCharWidth();
        final int height = gc.getFontMetrics().getHeight();
        gc.dispose();

	    Caret caret = new Caret(styledText, SWT.NULL) {
	    	// XXX: this is EXTREMALLY evil :->
	    	@Override protected void checkSubclass() { }
	    	@Override
	    	public void setLocation(int x, int y) {
	    		super.setLocation(x, y + height - getSize().y);
	    	}
	    };

	    switch (caretType) {
		case STANDARD:
			caret.setSize(2, height);
			break;
		case RECTANGULAR:
		    caret.setSize(width, height);
		    break;
		case HALF_RECT:
		    caret.setSize(width, height / 2);
			break;
		case UNDERLINE:
		    caret.setSize(width, 2);
		    break;
	    }

	    return caret;
	}



}
