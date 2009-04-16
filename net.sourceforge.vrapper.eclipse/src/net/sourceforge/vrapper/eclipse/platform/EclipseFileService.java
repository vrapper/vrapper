package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.platform.FileService;

import org.eclipse.jface.text.ITextViewer;

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
