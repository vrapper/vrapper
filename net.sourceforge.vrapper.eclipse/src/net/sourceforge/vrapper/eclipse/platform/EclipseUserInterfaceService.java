package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.eclipse.ui.StatusLine;
import net.sourceforge.vrapper.platform.UserInterfaceService;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.StatusLineContributionItem;

public class EclipseUserInterfaceService implements UserInterfaceService {

    private static final String CONTRIBUTION_ITEM_NAME = "VimInputMode";
    private static final String RECORDING_MESSAGE = "recording ";
    private static final int MESSAGE_WIDTH = 20;

    private final StatusLine statusLine;
    private final IEditorPart editor;
    private final StatusLineContributionItem vimInputModeItem;
    private String currentMode;
    private boolean isRecording = false;

    public EclipseUserInterfaceService(IEditorPart editor, ITextViewer textViewer) {
        this.editor = editor;
        statusLine = new StatusLine(textViewer.getTextWidget());
        vimInputModeItem = getContributionItem();
        editor.getSite().getWorkbenchWindow().getPartService().addPartListener(new PartChangeListener());
    }

    public void setCommandLine(String content) {
        statusLine.setContent(content);
    }

    public void setEditorMode(String modeName) {
        currentMode = "-- " + modeName.toUpperCase() + " --";
        vimInputModeItem.setText(currentMode);
    }

    public void setErrorMessage(String content) {
        editor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(content);
    }

    public void setInfoMessage(String content) {
    	if(isRecording) {
    		content = RECORDING_MESSAGE + content;
    	}
        editor.getEditorSite().getActionBars().getStatusLineManager().setMessage(content);
    }

    private StatusLineContributionItem getContributionItem() {
        String name = CONTRIBUTION_ITEM_NAME+editor.getEditorSite().getId();
        IStatusLineManager manager = editor.getEditorSite().getActionBars().getStatusLineManager();
        StatusLineContributionItem item = (StatusLineContributionItem) manager.find(name);
        if (item == null) {
            item = new StatusLineContributionItem(name, true, MESSAGE_WIDTH);
            try {
                manager.insertBefore("ElementState", item);
            } catch (IllegalArgumentException e) {
                manager.add(item);
            }
        }
        return item;
    }

    private final class PartChangeListener implements IPartListener {

        public void partActivated(IWorkbenchPart arg0) {
            if (arg0 == editor) {
                vimInputModeItem.setText(currentMode);
            }
        }

        public void partBroughtToTop(IWorkbenchPart arg0) { }

        public void partClosed(IWorkbenchPart arg0) {
            if (arg0 == editor) {
                editor.getSite().getWorkbenchWindow().getPartService().removePartListener(this);
            }
        }

        public void partDeactivated(IWorkbenchPart arg0) { }

        public void partOpened(IWorkbenchPart arg0) { }
    }

    public void setRecording(boolean b) {
    	isRecording = b;
    }

}
