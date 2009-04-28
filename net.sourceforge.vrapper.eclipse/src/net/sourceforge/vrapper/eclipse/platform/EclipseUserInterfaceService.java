package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.eclipse.ui.StatusLine;
import net.sourceforge.vrapper.platform.UserInterfaceService;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;

public class EclipseUserInterfaceService implements UserInterfaceService {

    private final StatusLine statusLine;
    private final IEditorPart editor;

    public EclipseUserInterfaceService(IEditorPart editor, ITextViewer textViewer) {
        this.editor = editor;
        statusLine = new StatusLine(textViewer.getTextWidget());
    }

    public void setCommandLine(String content) {
        statusLine.setContent(content);
    }

    public void setEditorMode(String modeName) {
        // TODO: integrate editor mode display from old vrapper
        setInfoMessage("-- " + modeName.toUpperCase() + " --");
    }

    public void setErrorMessage(String content) {
        editor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(content);
    }

    public void setInfoMessage(String content) {
        editor.getEditorSite().getActionBars().getStatusLineManager().setMessage(content);
    }

}
