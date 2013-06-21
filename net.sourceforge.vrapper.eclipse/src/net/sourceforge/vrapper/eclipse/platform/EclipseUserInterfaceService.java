package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.eclipse.interceptor.LinkedModeHandler;
import net.sourceforge.vrapper.eclipse.ui.CommandLineUIFactory;
import net.sourceforge.vrapper.eclipse.ui.ModeContributionItem;
import net.sourceforge.vrapper.platform.CommandLineUI;
import net.sourceforge.vrapper.platform.UserInterfaceService;
import net.sourceforge.vrapper.vim.EditorAdaptor;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

public class EclipseUserInterfaceService implements UserInterfaceService {

    private static final String CONTRIBUTION_ITEM_NAME = "VimInputMode";

    private final CommandLineUIFactory commandLineFactory;
    private final IEditorPart editor;
    private final ModeContributionItem vimInputModeItem;

    private String lastInfoValue = "";
    private String lastErrorValue = "";

    /* When a normal-mode command is entered, the letters you type
     * are displayed in the info status bar. If your command puts
     * something in the info bar, since it is shared, we want to
     * first wipe out the command letters, and the replace it with
     * the command result. Setting infoSet to true, and storing the
     * command result in lastCommandResultValue will accomplish this.
     * See CommandBasedMode.java for the if/else logic on this. -- BRD
     */
    private boolean infoSet;
    private String lastCommandResultValue = "";

    private String currentMode;
    private String currentModeName;

    public EclipseUserInterfaceService(final IEditorPart editor, final ITextViewer textViewer) {
        this.editor = editor;
        commandLineFactory = new CommandLineUIFactory(textViewer.getTextWidget());
        vimInputModeItem = getContributionItem();
        setEditorMode(VRAPPER_DISABLED);
        editor.getSite().getWorkbenchWindow().getPartService().addPartListener(new PartChangeListener());
    }

    @Override
    public void setEditorMode(final String modeName) {
        currentModeName = modeName.toUpperCase();
        currentMode = "-- " + modeName + " --";
        vimInputModeItem.setText(currentMode);
    }

    @Override
    public String getCurrentEditorMode() {
        return currentMode;
    }

    // For :ascii command
    @Override
    public void setAsciiValues(final String asciiValue, final int decValue, final String hexValue, final String octalValue) {
        final String asciiValueText = "<" + asciiValue + ">  "
                              + decValue + ",  "
                              + "Hex " + hexValue + ",  "
                              + "Octal " + octalValue;
        lastCommandResultValue = asciiValueText;
        setErrorMessage(null);
        setInfoMessage(asciiValueText);
    }

    @Override
    public String getLastCommandResultValue() {
        return lastCommandResultValue;
    }

    @Override
    public void setLastCommandResultValue(final String lastCommandResultValue) {
        this.lastCommandResultValue = lastCommandResultValue;
    }

    @Override
    public void setErrorMessage(final String content) {
        editor.getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(content);
        lastErrorValue = content;
    }

    @Override
    public String getLastErrorValue() {
        return lastErrorValue;
    }

    @Override
    public void setInfoMessage(final String content) {
        editor.getEditorSite().getActionBars().getStatusLineManager().setMessage(content);
        lastInfoValue = content;
    }

    @Override
    public String getLastInfoValue() {
        return lastInfoValue;
    }

    private ModeContributionItem getContributionItem() {
        final String name = CONTRIBUTION_ITEM_NAME + editor.getEditorSite().getId();
        final IStatusLineManager manager = editor.getEditorSite().getActionBars().getStatusLineManager();
        ModeContributionItem item = (ModeContributionItem) manager.find(name);
        if (item == null) {
            item = new ModeContributionItem(name);
            try {
                manager.insertBefore("ElementState", item);
            } catch (final IllegalArgumentException e) {
                manager.add(item);
            }
            manager.update(true);
        }
        return item;
    }

    private final class PartChangeListener implements IPartListener {

        @Override
        public void partActivated(final IWorkbenchPart arg0) {
            if (arg0 == editor) {
                vimInputModeItem.setText(currentMode);
            }
        }

        @Override
        public void partBroughtToTop(final IWorkbenchPart arg0) { }

        @Override
        public void partClosed(final IWorkbenchPart arg0) {
            if (arg0 == editor) {
                editor.getSite().getWorkbenchWindow().getPartService().removePartListener(this);
            }
        }

        @Override
        public void partDeactivated(final IWorkbenchPart arg0) { }

        @Override
        public void partOpened(final IWorkbenchPart arg0) { }
    }

    @Override
    public void setRecording(final boolean b, final String macroName) {
        setEditorMode(currentModeName);
        vimInputModeItem.setRecording(b, macroName);
    }

    @Override
    public boolean isInfoSet() {
        return infoSet;
    }

    @Override
    public void setInfoSet(final boolean infoSet) {
        this.infoSet = infoSet;
    }

    @Override
    public CommandLineUI getCommandLineUI(EditorAdaptor editorAdaptor) {
        return commandLineFactory.createCommandLineUI(editorAdaptor);
    }
}
