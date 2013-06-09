package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.eclipse.ui.CommandLineUIFactory;
import net.sourceforge.vrapper.eclipse.ui.ModeContributionItem;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.UserInterfaceService;
import net.sourceforge.vrapper.vim.ModeChangeHintReceiver;
import net.sourceforge.vrapper.vim.modes.InsertMode;

import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MGenericTile;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

public class EclipseUserInterfaceService implements UserInterfaceService {

    private static final String CONTRIBUTION_ITEM_NAME = "VimInputMode";

    private final CommandLineUIFactory statusLine;
    private final IEditorPart editor;
    private final ITextViewer textViewer;
    private final ModeContributionItem vimInputModeItem;

    private LinkedModeHandler linkedModeHandler;

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
        this.textViewer = textViewer;
        statusLine = new CommandLineUIFactory(textViewer.getTextWidget());
        vimInputModeItem = getContributionItem();
        setEditorMode(VRAPPER_DISABLED);
        editor.getSite().getWorkbenchWindow().getPartService().addPartListener(new PartChangeListener());
    }

    @Override
    public void setCommandLine(final String content, final int position) {
        statusLine.setContent(content);
        statusLine.setCaretPosition(position);
    }

    @Override
    public void setEditorMode(final String modeName) {
        currentModeName = modeName.toUpperCase();
        currentMode = "-- " + modeName + " --";
        vimInputModeItem.setText(currentMode);

        if (InsertMode.DISPLAY_NAME.equals(modeName) && linkedModeHandler != null) {
            // if there's a linked mode, we want to be notified about it
            linkedModeHandler.onCheckForLinkedMode(textViewer.getDocument());
        }
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

    public void setModeChangeHintReceiver(final ModeChangeHintReceiver editorAdaptor) {
        if (linkedModeHandler != null) {
            linkedModeHandler.unregisterListener(textViewer.getDocument());
        }
        linkedModeHandler = new LinkedModeHandler(editorAdaptor);
        linkedModeHandler.registerListener(textViewer.getDocument());
    }

    private int getElementWeight(MUIElement element) {
        int w = 10000;
        if (element.getContainerData() != null) {
            try {
                w = Integer.parseInt(element.getContainerData());
            } catch (NumberFormatException e) {
            }
        }
        return w;
    }

    @Override
    public void splitEditor(SplitDirection dir, SplitMode mode) {
        EModelService svc = (EModelService) editor.getSite().getService(EModelService.class);
        EPartService psvc = (EPartService) editor.getSite().getService(EPartService.class);
        MPart p = (MPart) editor.getSite().getService(MPart.class);
        MElementContainer<MUIElement> editorStack = p.getParent();
        MWindow window = svc.getTopLevelWindowFor(editorStack);

        if (mode == SplitMode.MOVE && editorStack.getChildren().size() < 2) {
            setErrorMessage("Editor must have at least 2 tabs");
            return;
        }

        //
        // Find a parent sash container to insert a new stack.
        //
        MUIElement neighbour = p;
        MUIElement parent = neighbour.getParent();
        while (parent != null
                && (  !(parent instanceof MPartSashContainer)
                    || (parent instanceof MArea))) {
            neighbour = parent;
            parent = neighbour.getParent();
            if (parent == null) {
                // Check if there is a placeholder for the element.
                MPlaceholder ph = svc.findPlaceholderFor(window, neighbour);
                if (ph != null) {
                    neighbour = ph;
                    parent = neighbour.getParent();
                }
            }
        }
        MPartSashContainer editorSash = (MPartSashContainer) parent;
        int neighbourIndex = editorSash.getChildren().indexOf(neighbour);
        assert editorSash != null;

        //
        // Move or clone the editor into a new Stack.
        //
        MPartStack newStack = MBasicFactory.INSTANCE.createPartStack();
        MPart newPart = null;
        if (mode == SplitMode.CLONE) {
            try {
                newPart = cloneEditor();
                newStack.getChildren().add(newPart);
                // Temporary activate the cloned editor.
                psvc.activate(p);
            } catch (PartInitException e) {
                setErrorMessage("Unable to split editor");
                VrapperLog.error("Unable to split editor", e);
                return;
            }
        } else {
            editorStack.getChildren().remove(p);
            // Activate one of the remaining tabs in the stack.
            psvc.activate((MPart)editorStack.getSelectedElement());
            newStack.getChildren().add(p);
            newPart = p;
        }

        //
        // Do we need a new sash or can we extend the existing one?
        //
        boolean isHorizontal = dir != SplitDirection.HORIZONTALLY;
        if (isHorizontal == editorSash.isHorizontal()) {
            //
            // We just need to add to the existing sash.
            //
            int totalVisWeight = 0;
            for (MUIElement child : editorSash.getChildren()) {
                if (child.isToBeRendered())
                    totalVisWeight += getElementWeight(child);
            }
            newStack.setContainerData(Integer.toString(totalVisWeight));
            editorSash.getChildren().add(neighbourIndex + 1, newStack);
        } else {
            //
            // Create a new PartSashContainer with the designed split property to
            // put in place of the neighbour.
            //
            MPartSashContainer splitSash = MBasicFactory.INSTANCE.createPartSashContainer();
            splitSash.setHorizontal(isHorizontal);
            editorSash.getChildren().remove(neighbour);
            splitSash.getChildren().add((MPartSashContainerElement) neighbour);
            splitSash.getChildren().add(newStack);

            //
            // Set 50/50 split ratio.
            //
            splitSash.setContainerData(neighbour.getContainerData());
            newStack.setContainerData("5000");
            neighbour.setContainerData("5000");

            // Add the new sash at the same location.
            editorSash.getChildren().add(neighbourIndex, splitSash);
        }

        psvc.activate(newPart, true /*requiresFocus*/);
    }

    private MPart cloneEditor() throws PartInitException {
        IWorkbenchPage page = editor.getSite().getWorkbenchWindow().getActivePage();
        IEditorPart newEditor = page.openEditor(editor.getEditorInput(),
                editor.getSite().getId(), false, IWorkbenchPage.MATCH_NONE);
        MPart newPart = (MPart) newEditor.getSite().getService(MPart.class);
        return newPart;
    }

    @SuppressWarnings("unchecked")
    private MPartStack findAdjacentStack(Where where) {
        EModelService svc = (EModelService) editor.getSite().getService(EModelService.class);
        MPart p = (MPart) editor.getSite().getService(MPart.class);
        MPartSashContainerElement editorStack = (MPartSashContainerElement) p.getParent();

        //
        // Find parent container with the desired layout.
        //
        MUIElement child = editorStack;
        MUIElement cont = editorStack.getParent();
        boolean isHorizontal = where == Where.LEFT || where == Where.RIGHT;
        while (cont instanceof MGenericTile<?>) {
            MGenericTile<MUIElement> curTile = (MGenericTile<MUIElement>) cont;
            if (curTile.isHorizontal() == isHorizontal && curTile.getChildren().size() > 1) {
                // Found right layout, calculate the index of the adjacent container
                int childIndex = curTile.getChildren().indexOf(child);
                if (where == Where.RIGHT || where == Where.DOWN) {
                    ++childIndex;
                } else {
                    --childIndex;
                }
                // Check if there is a neighbour on this level, otherwise -- go higher.
                if (childIndex >= 0 && childIndex < curTile.getChildren().size()) {
                    cont = curTile.getChildren().get(childIndex);
                    // Descend down to the selected part stack.
                    while (cont instanceof MGenericTile<?> || cont instanceof MPlaceholder) {
                        if (cont instanceof MPlaceholder) {
                            MPlaceholder mp = (MPlaceholder) cont;
                            cont = mp.getRef();
                        } else {
                            curTile = (MGenericTile<MUIElement>) cont;
                            cont = curTile.getSelectedElement();
                        }
                    }
                    if (cont instanceof MPartStack) {
                        return (MPartStack) cont;
                    } else {
                        // Can't find an adjacent part stack.
                        return null;
                    }
                }
            }
            child = cont;
            cont = cont.getParent();
            if (cont == null) {
                // Check if there is a placeholder for the element.
                MPlaceholder ph = svc.findPlaceholderFor(svc.getTopLevelWindowFor(child), child);
                if (ph != null) {
                    child = ph;
                    cont = child.getParent();
                }
            }
        }
        return null;
    }

    @Override
    public void switchEditor(Where where) {
        EPartService psvc = (EPartService) editor.getSite().getService(EPartService.class);
        MPartStack stack = findAdjacentStack(where);
        if (stack != null) {
            psvc.activate((MPart) stack.getSelectedElement(), true);
        }
    }

    @Override
    public void moveEditor(Where where, SplitMode mode) {
        EPartService psvc = (EPartService) editor.getSite().getService(EPartService.class);
        MPartStack stack = findAdjacentStack(where);
        EModelService svc = (EModelService) editor.getSite().getService(EModelService.class);
        MPart p = (MPart) editor.getSite().getService(MPart.class);
        MElementContainer<MUIElement> editorStack = p.getParent();

        if (stack == null) {
            setErrorMessage("Couldn't find a split to move into");
            return;
        }

        if (mode == SplitMode.CLONE) {
            try {
                MPart newPart = cloneEditor();
                stack.getChildren().add(newPart);
                // Temporary activate the cloned editor.
                psvc.activate(p);
                p = newPart;
            } catch (PartInitException e) {
                setErrorMessage("Unable to split editor");
                VrapperLog.error("Unable to split editor", e);
            }
        } else {
            editorStack.getChildren().remove(p);
            if (editorStack.getChildren().size() > 0) {
                // Deactivate this tab by activating preceding tab in the current stack.
                psvc.activate((MPart) editorStack.getSelectedElement());
            } else {
                // Temporary activate the split this tab is moving into
                psvc.activate((MPart) stack.getSelectedElement());
            }
            stack.getChildren().add(p);
        }

        // Activate the moved part.
        psvc.activate(p, true);
    }

}
