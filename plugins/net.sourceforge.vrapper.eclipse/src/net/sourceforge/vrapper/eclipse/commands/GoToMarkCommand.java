package net.sourceforge.vrapper.eclipse.commands;

import net.sourceforge.vrapper.eclipse.activator.VrapperPlugin;
import net.sourceforge.vrapper.eclipse.interceptor.InputInterceptor;
import net.sourceforge.vrapper.eclipse.interceptor.UnknownEditorException;
import net.sourceforge.vrapper.eclipse.platform.EclipseCursorAndSelection;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.VrapperPlatformException;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountIgnoringNonRepeatableCommand;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.commands.motions.GoToMarkMotion;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Go to marks in the visible or background editor(s). Hard to implement in vrapper core due to the
 * cross-editor aspect.
 */
public class GoToMarkCommand extends CountIgnoringNonRepeatableCommand {

    public enum Mode {
        LINEWISE,
        CHARWISE,
        EDITOR
    }

    public static final Function<Command, KeyStroke> LINEWISE_CONVERTER = new Function<Command, KeyStroke>() {
        public Command call(KeyStroke arg) {
            return new GoToMarkCommand(Mode.LINEWISE, String.valueOf(arg.getCharacter()));
        }
    };

    public static final Function<Command, KeyStroke> CHARWISE_CONVERTER = new Function<Command, KeyStroke>() {
        public Command call(KeyStroke arg) {
            return new GoToMarkCommand(Mode.CHARWISE, String.valueOf(arg.getCharacter()));
        }
    };

    public static final Function<Command, KeyStroke> EDITOR_CONVERTER = new Function<Command, KeyStroke>() {
        public Command call(KeyStroke arg) {
            return new GoToMarkCommand(Mode.EDITOR, String.valueOf(arg.getCharacter()));
        }
    };

    private final String id;
    private final Mode mode;

    private GoToMarkCommand(Mode mode, String id) {
        if (mode == Mode.EDITOR) {
            this.id = id.toUpperCase();
        } else {
            this.id = id;
        }
        this.mode =  mode;
    }

    public void execute(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        EditorAdaptor markEditor = editorAdaptor;

        if (editorAdaptor.getCursorService().isGlobalMark(id)) {
            markEditor = activateOrOpenEditorForMark(editorAdaptor);
        }
        if (markEditor == null) {
            return;
        }
        // The associated editor is open and active, change current position if requested.
        switch (mode) {
        case LINEWISE:
            new MotionCommand(new GoToMarkMotion(true, id)).execute(markEditor);
            break;
        case CHARWISE:
            new MotionCommand(new GoToMarkMotion(false, id)).execute(markEditor);
            break;
        case EDITOR:
            // Don't change current position.
            break;
        }
    }

    private EditorAdaptor activateOrOpenEditorForMark(EditorAdaptor editorAdaptor) throws CommandExecutionException {

        final IWorkbench workbench = PlatformUI.getWorkbench();
        final IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();

        // Try to open the file from the marker resource
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IMarker marker = EclipseCursorAndSelection.getGlobalMarker(id, root);
        if (marker == null) {
            throw new CommandExecutionException("Global mark not set");
        }

        IResource markedResource = marker.getResource();
        IFile markedFile = (IFile) markedResource.getAdapter(IFile.class);
        if (markedFile == null) {
            // Marked resource is not a file?!?
            throw new CommandExecutionException("Global mark " + id + " is in file of unknown type");
        }

        // Check if there is an open editor associated with the mark.
        IEditorPart editor = findEditorForFile(page, markedFile);

        if (editor == null) {
            editor = openEditorForMarker(workbench, page, markedFile);
        }

        // Activate the editor associated with the global mark.
        page.activate(editor);

        // Lookup Vrapper's EditorAdapter associated with the Eclipse editor
        InputInterceptor interceptor = null;
        try {
            interceptor = VrapperPlugin.getDefault().findActiveInterceptor(editor);
        } catch (VrapperPlatformException e) {
            VrapperLog.error("Failed to activate editor for mark " + id + ".", e);
        } catch (UnknownEditorException e) {
            VrapperLog.info("Failed to activate editor for mark " + id + ". Error: " + e);
        }

        if (interceptor == null) {
            // Vrapper can no longer be used in this kind of editor while it could in the past?
            return null;
        } else {
            return interceptor.getEditorAdaptor();
        }
    }

    private IEditorPart findEditorForFile(IWorkbenchPage page, IFile markedFile) {
        IEditorInput needleInput = new FileEditorInput(markedFile);
        IEditorReference[] references = page.findEditors(needleInput, null, IWorkbenchPage.MATCH_INPUT);
        if (references.length > 0) {
            // take the first reference
            return references[0].getEditor(true);
        }
        return null;
    }

    private IEditorPart openEditorForMarker(final IWorkbench workbench, final IWorkbenchPage page,
            IFile markedFile) throws CommandExecutionException {
        IEditorPart editor;

        IEditorInput editorInput = new FileEditorInput(markedFile);
        String fileName = markedFile.getName();
        IEditorDescriptor desc = workbench.getEditorRegistry().getDefaultEditor(fileName);
        try {
            if (desc == null) {
                // use default because file doesn't have file extension, Eclipse can't guess its type
                editor = page.openEditor(editorInput, "org.eclipse.ui.DefaultTextEditor");
            } else {
                editor = page.openEditor(editorInput, desc.getId());
            }
        } catch (PartInitException e) {
            VrapperLog.error("Failed to open file " + markedFile.getFullPath(), e);
            throw new CommandExecutionException("Could not open editor for "
                    + markedFile.getProjectRelativePath());
        }
        return editor;
    }
}
