package net.sourceforge.vrapper.eclipse.ui;

import java.util.Locale;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;

/**
 * Custom CLabel which shows the Vrapper mode in the status bar. Custom logic is present to attempt
 * fitting the mode text in a limited bounding box.
 * <p>
 * Note that The name of this class is meant to be the same as its parent so that the CSS selectors
 * in Eclipse 4.x theming will fire. We can't used WidgetElement.setCSSClass or IStyleEngine as that
 * introduces an unwanted Eclipse 4.x dependency.
 */
// [NOTE] According to https://bugs.eclipse.org/bugs/show_bug.cgi?id=391675 this widget is allowed
//        to be overridden, no matter that old versions would have the @nooverride attribute.
class CLabel extends org.eclipse.swt.custom.CLabel {

    private String modeName;

    public CLabel(Composite parent, int style) {
        super(parent, style);
    }

    @Override
    protected String shortenText(GC gc, String t, int width) {
        // Drop the "--" at the ends if the current text is too long.
        String shorter = modeName;
        // If set to e.g. "(insert) VISUAL LINE", make it even shorter.
        if (gc.textExtent(shorter).x > width
                && shorter.toUpperCase(Locale.ENGLISH).contains("VISUAL")) {
            shorter = shorter.replace("VISUAL", "VIS");
        }
        return shorter;
    }

    public void setText(String modeName) {
        this.modeName = modeName;
        super.setText("-- " + modeName + " --");
    }
}