package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.platform.ViewportService;

import org.eclipse.jface.text.ITextViewer;

public class EclipseViewportService implements ViewportService {

    private final ITextViewer textViewer;

    public EclipseViewportService(ITextViewer textViewer) {
        this.textViewer = textViewer;
    }

    public void setRepaint(boolean redraw) {
        textViewer.getTextWidget().setRedraw(redraw);
    }

}
