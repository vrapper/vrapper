package newpackage.eclipse;

import org.eclipse.jface.text.ITextViewer;

import newpackage.glue.ViewportService;

public class EclipseViewportService implements ViewportService {

	private final ITextViewer textViewer;

	public EclipseViewportService(ITextViewer textViewer) {
		this.textViewer = textViewer;
	}

	@Override
	public void setRepaint(boolean redraw) {
		textViewer.getTextWidget().setRedraw(redraw);
	}

}
