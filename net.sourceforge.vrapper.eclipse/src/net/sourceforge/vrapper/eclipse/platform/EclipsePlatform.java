package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.FileService;
import net.sourceforge.vrapper.platform.HistoryService;
import net.sourceforge.vrapper.platform.Platform;
import net.sourceforge.vrapper.platform.SelectionService;
import net.sourceforge.vrapper.platform.ServiceProvider;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.platform.UserInterfaceService;
import net.sourceforge.vrapper.platform.ViewportService;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension6;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class EclipsePlatform implements Platform {

    private final EclipseCursorAndSelection cursorAndSelection;
    private final EclipseTextContent textContent;
    private final EclipseFileService fileService;
    private final EclipseViewportService viewportService;
    private HistoryService historyService;
    private final EclipseServiceProvider serviceProvider;
    private final EclipseUserInterfaceService userInterfaceService;

    public EclipsePlatform(AbstractTextEditor abstractTextEditor, ITextViewer textViewer) {
        cursorAndSelection = new EclipseCursorAndSelection(textViewer);
        textContent = new EclipseTextContent(textViewer);
        fileService = new EclipseFileService(textViewer);
        viewportService = new EclipseViewportService(textViewer);
        serviceProvider = new EclipseServiceProvider(abstractTextEditor);
        userInterfaceService = new EclipseUserInterfaceService(abstractTextEditor, textViewer);
        if (textViewer instanceof ITextViewerExtension6) {
            IUndoManager delegate = ((ITextViewerExtension6)textViewer).getUndoManager();
            EclipseHistoryService manager = new EclipseHistoryService(textViewer.getTextWidget(), delegate);
            textViewer.setUndoManager(manager);
            this.historyService = manager;
        } else {
            this.historyService = new DummyHistoryService();
        }
    }

    public CursorService getCursorService() {
        return cursorAndSelection;
    }

    public TextContent getModelContent() {
        return textContent.getModelContent();
    }

    public SelectionService getSelectionService() {
        return cursorAndSelection;
    }

    public TextContent getViewContent() {
        return textContent.getViewContent();
    }

    public FileService getFileService() {
        return fileService;
    }

    public ViewportService getViewportService() {
        return viewportService;
    }

    public HistoryService getHistoryService() {
        return historyService;
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    public UserInterfaceService getUserInterfaceService() {
        return userInterfaceService;
    }

}
