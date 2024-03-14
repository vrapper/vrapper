package net.sourceforge.vrapper.plugin.splitEditor.commands;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.UserInterfaceService;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;

public class SplitEditorCommand extends AbstractWindowCommand {
    public static final AbstractWindowCommand VSPLIT = new SplitEditorCommand(SplitDirection.VERTICALLY, SplitMode.CLONE, SplitContainer.SHARED_AREA);
    public static final AbstractWindowCommand HSPLIT = new SplitEditorCommand(SplitDirection.HORIZONTALLY, SplitMode.CLONE, SplitContainer.SHARED_AREA);

    private final SplitDirection direction;
    private final SplitMode mode;
    private final SplitContainer containerMode;
    private String filename;

    public SplitEditorCommand(SplitDirection dir, SplitMode mode, SplitContainer containerMode) {
    	this(dir, mode, containerMode, null);
    }

    public SplitEditorCommand(SplitDirection dir, SplitMode mode, SplitContainer containerMode, String filename) {
        this.direction = dir;
        this.containerMode = containerMode;
        this.filename = filename;
        if (filename != null) {
            this.mode = SplitMode.MOVE;
        } else {
            this.mode = mode;
        }
    }

    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        UserInterfaceService userInterfaceService = editorAdaptor.getUserInterfaceService();
        IWorkbenchPartSite editorSite = getEditorSite();

        EModelService svc = (EModelService) editorSite.getService(EModelService.class);
        EPartService psvc = (EPartService) editorSite.getService(EPartService.class);
        MPart p = null;
        if (filename == null) {
            p = (MPart) editorSite.getService(MPart.class);
        } else {
            if (!filename.startsWith("/")) {
                if (editorAdaptor.getConfiguration().get(Options.AUTO_CHDIR)) {
                    filename = editorAdaptor.getFileService().getCurrentFilePath() + "/" + filename;
                } else {
                    filename = editorAdaptor.getRegisterManager().getCurrentWorkingDirectory() + "/" + filename;
                }
            }
            p = openFileInEditor(filename);
        }
        MElementContainer<MUIElement> editorStack = p.getParent();
        MWindow window = svc.getTopLevelWindowFor(editorStack);

        if (mode == SplitMode.MOVE && editorStack.getChildren().size() < 2) {
            userInterfaceService.setErrorMessage("Editor must have at least 2 tabs");
            return;
        }

        //
        // Find a parent sash container to insert a new stack.
        //
        MUIElement neighbour = p;
        MElementContainer<? extends MUIElement> parent = neighbour.getParent();
        while (parent != null
                && !(parent instanceof MTrimmedWindow) // Detached windows
                && (  !(parent instanceof MPartSashContainer)
                        // NOTE: MArea is an instance of MPartSashContainer
                    || (parent instanceof MArea && containerMode == SplitContainer.TOP_LEVEL))) {
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
        @SuppressWarnings("unchecked")
        final MElementContainer<MUIElement> parentContainer = (MElementContainer<MUIElement>) parent;
        final int neighbourIndex = parentContainer.getChildren().indexOf(neighbour);

        //
        // Move or clone the editor into a new Stack.
        //
        MPartStack newStack = MBasicFactory.INSTANCE.createPartStack();
        // Copy tags from the original stack to make it more like the one
        // create by Eclipse.
        newStack.getTags().addAll(editorStack.getTags());
        MPart newPart = null;
        if (mode == SplitMode.CLONE) {
            try {
                newPart = cloneEditor();
                newStack.getChildren().add(newPart);
                // Temporary activate the cloned editor.
                psvc.activate(p);
            } catch (PartInitException e) {
                userInterfaceService.setErrorMessage("Unable to split editor");
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
        // NOTE: MArea always needs a sash.
        //
        final MPartSashContainer editorSash = parent instanceof MPartSashContainer ? (MPartSashContainer) parent : null;
        boolean isHorizontal = direction != SplitDirection.HORIZONTALLY;
        if (editorSash != null && isHorizontal == editorSash.isHorizontal() && !(editorSash instanceof MArea)) {
            //
            // We just need to add to the existing sash.
            //
            int totalVisWeight = 0;
            int countVis = 0;
            for (MUIElement child : parentContainer.getChildren()) {
                if (child.isToBeRendered()) {
                    totalVisWeight += getElementWeight(child);
                    ++countVis;
                }
            }
            newStack.setContainerData(Integer.toString(totalVisWeight / countVis));
            parentContainer.getChildren().add(neighbourIndex + 1, newStack);
        } else {
            //
            // Create a new PartSashContainer with the designed split property to
            // put in place of the neighbour.
            //
            MPartSashContainer splitSash = MBasicFactory.INSTANCE.createPartSashContainer();
            splitSash.setHorizontal(isHorizontal);
            parentContainer.getChildren().remove(neighbour);
            splitSash.getChildren().add((MPartSashContainerElement) neighbour);
            splitSash.getChildren().add(newStack);

            //
            // Set 50/50 split ratio.
            //
            splitSash.setContainerData(neighbour.getContainerData());
            newStack.setContainerData("5000");
            neighbour.setContainerData("5000");

            // Add the new sash at the same location.
            parentContainer.getChildren().add(neighbourIndex, (MUIElement)splitSash);
        }

        psvc.activate(newPart, true /*requiresFocus*/);
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
}
