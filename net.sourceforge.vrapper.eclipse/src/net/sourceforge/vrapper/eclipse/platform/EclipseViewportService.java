package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.platform.ViewportService;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.ViewPortInformation;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Region;

public class EclipseViewportService implements ViewportService {

    private final ITextViewer textViewer;
    private final ITextViewerExtension5 textViewer5;
    private Object lock;

    public EclipseViewportService(ITextViewer textViewer) {
        this.textViewer = textViewer;
        this.textViewer5 = textViewer instanceof ITextViewerExtension5
                         ? (ITextViewerExtension5) textViewer : null;
    }

    public void setRepaint(boolean redraw) {
        if (lock == null) {
            textViewer.getTextWidget().setRedraw(redraw);
        }
    }

    public void lockRepaint(Object lock) {
        if (this.lock == null) {
            this.lock = lock;
        }
    }

    public void unlockRepaint(Object lock) {
        if (this.lock == lock) {
            this.lock = null;
        }
    }

    public void exposeModelPosition(Position position) {
        if (textViewer5 != null) {
            textViewer5.exposeModelRange(new Region(position.getModelOffset(), 1));
        }
    }

    public ViewPortInformation getViewPortInformation() {
        return new ViewPortInformation(
                textViewer5.modelLine2WidgetLine(textViewer.getTopIndex()),
                textViewer5.modelLine2WidgetLine(textViewer.getBottomIndex()));
    }

    public void setTopLine(int line) {
        textViewer.setTopIndex(textViewer5.widgetLine2ModelLine(line));
    }

    public int modelLine2ViewLine(int line) {
        return textViewer5.modelLine2WidgetLine(line);
    }

    public int viewLine2ModelLine(int line) {
        return textViewer5.widgetLine2ModelLine(line);
    }



}
