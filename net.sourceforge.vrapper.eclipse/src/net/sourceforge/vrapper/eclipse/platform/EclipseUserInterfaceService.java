package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.eclipse.ui.ModeContributionItem;
import net.sourceforge.vrapper.eclipse.ui.StatusLine;
import net.sourceforge.vrapper.platform.UserInterfaceService;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

public class EclipseUserInterfaceService implements UserInterfaceService {

    private static final String CONTRIBUTION_ITEM_NAME = "VimInputMode";

    private final StatusLine statusLine;
    private final IEditorPart editor;
    private final ModeContributionItem vimInputModeItem;
    
    // For unit tests
    private String lastAsciiValue = "";
    private String lastInfoValue = "";
    private String lastErrorValue = "";
    private boolean asciiSet;
    private String currentMode;

    public EclipseUserInterfaceService(IEditorPart editor, ITextViewer textViewer) {
        this.editor = editor;
        statusLine = new StatusLine(textViewer.getTextWidget());
        vimInputModeItem = getContributionItem();
        setEditorMode(VRAPPER_DISABLED);
        editor.getSite().getWorkbenchWindow().getPartService().addPartListener(new PartChangeListener());
    }

    public void setCommandLine(String content, int position) {
        statusLine.setContent(content);
        statusLine.setCaretPosition(position);
    }

    public void setEditorMode(String modeName) {
        currentMode = "-- " + modeName + " --";
        vimInputModeItem.setText(currentMode);
    }
    
    public String getCurrentEditorMode() {
        return currentMode;
    }
   
    // For :ascii command
    public void setAsciiValues(String asciiValue, int decValue, String hexValue, String octalValue) {
        String asciiValueText = "<" + asciiValue + ">  " 
                              + decValue + ",  "
                              + "Hex " + hexValue + ",  "
                              + "Octal " + octalValue;
        lastAsciiValue = asciiValueText;
        setErrorMessage(null);
        setInfoMessage(asciiValueText);
    }
   
    public String getLastAsciiValue() {
        return lastAsciiValue;
    }

    public void setErrorMessage(String content) {
        editor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(content);
        lastErrorValue = content;
    }
    

    public String getLastErrorValue() {
        return lastErrorValue;
    }
    
    public void setInfoMessage(String content) {
        editor.getEditorSite().getActionBars().getStatusLineManager().setMessage(content);
        lastInfoValue = content;
    }
    
    public String getLastInfoValue() {
        return lastInfoValue;
    }

    private ModeContributionItem getContributionItem() {
        String name = CONTRIBUTION_ITEM_NAME + editor.getEditorSite().getId();
        IStatusLineManager manager = editor.getEditorSite().getActionBars().getStatusLineManager();
        ModeContributionItem item = (ModeContributionItem) manager.find(name);
        if (item == null) {
            item = new ModeContributionItem(name);
            try {
                manager.insertBefore("ElementState", item);
            } catch (IllegalArgumentException e) {
                manager.add(item);
            }
            manager.update(true);
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
    	vimInputModeItem.setRecording(b);
    }

    public boolean isAsciiSet() {
        return asciiSet;
    }

    public void setAsciiSet(boolean asciiSet) {
        this.asciiSet = asciiSet;
    }
}