package newpackage.eclipse;

import newpackage.glue.CursorService;
import newpackage.glue.Platform;
import newpackage.glue.FileService;
import newpackage.glue.HistoryService;
import newpackage.glue.SelectionService;
import newpackage.glue.ServiceProvider;
import newpackage.glue.TextContent;
import newpackage.glue.ViewportService;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension6;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import de.jroene.vrapper.eclipse.DummyHistoryService;
import de.jroene.vrapper.eclipse.EclipseHistoryService;

public class EclipsePlatform implements Platform {

	private EclipseCursorAndSelection cursorAndSelection;
	private EclipseTextContent textContent;
	private EclipseFileService fileService;
	private EclipseViewportService viewportService;
	private HistoryService historyService;
	private EclipseServiceProvider serviceProvider;

	public EclipsePlatform(AbstractTextEditor abstractTextEditor, ITextViewer textViewer) {
		cursorAndSelection = new EclipseCursorAndSelection(textViewer);
		textContent = new EclipseTextContent(textViewer);
		fileService = new EclipseFileService(textViewer);
		viewportService = new EclipseViewportService(textViewer);
		serviceProvider = new EclipseServiceProvider(abstractTextEditor);
		if (textViewer instanceof ITextViewerExtension6) {
			IUndoManager delegate = ((ITextViewerExtension6)textViewer).getUndoManager();
			EclipseHistoryService manager = new EclipseHistoryService(textViewer.getTextWidget(), delegate);
			textViewer.setUndoManager(manager);
			this.historyService = manager;
		} else {
			this.historyService = new DummyHistoryService();
		}
	}

	@Override
	public CursorService getCursorService() {
		return cursorAndSelection;
	}

	@Override
	public TextContent getModelContent() {
		return textContent.getModelContent();
	}

	@Override
	public SelectionService getSelectionService() {
		return cursorAndSelection;
	}

	@Override
	public TextContent getViewContent() {
		return textContent.getViewContent();
	}

	@Override
	public FileService getFileService() {
		return fileService;
	}

	@Override
	public ViewportService getViewportService() {
		return viewportService;
	}

	@Override
	public HistoryService getHistoryService() {
		return historyService;
	}

	@Override
	public ServiceProvider getServiceProvider() {
		return serviceProvider;
	}

}
