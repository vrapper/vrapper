package newpackage.eclipse;

import org.eclipse.jface.text.ITextViewer;

import newpackage.glue.FileService;

public class EclipseFileService implements FileService {

	private final ITextViewer textViewer;

	public EclipseFileService(ITextViewer textViewer) {
		this.textViewer = textViewer;
	}

	@Override
	public boolean isEditable() {
		return textViewer.isEditable();
	}

}
