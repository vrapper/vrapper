package net.sourceforge.vrapper.eclipse.commands;

import net.sourceforge.vrapper.eclipse.activator.VrapperPlugin;
import net.sourceforge.vrapper.eclipse.platform.EclipseCursorAndSelection;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountIgnoringNonRepeatableCommand;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.commands.motions.GoToMarkMotion;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

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

	private EditorAdaptor activateMarkEditor(EditorAdaptor editorAdaptor) throws CommandExecutionException {
	    //
	    // Check if there is an open editor associated with the mark.
	    //
	    IEditorPart editor = EclipseCursorAndSelection.getGlobalMarkEditor(id);
	    if (editor == null) {
	        //
	        // Try to open the file from the mark resource.
	        //
	        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	        IMarker marker = EclipseCursorAndSelection.getGlobalMarker(id, root);
	        if (marker != null) {

	            final String resourePath = marker.getResource().getProjectRelativePath().toString();
	            editorAdaptor.getFileService().openFile(resourePath);
	            //
	            // See if the mark is still in the editor after opening it.
	            //
	            editor = EclipseCursorAndSelection.getGlobalMarkEditor(id);
	            if (editor == null) {
	                return null;
	            }
	        } else {
	            throw new CommandExecutionException("Global mark not set");
	        }
	    }
	    //
	    // Activate the editor associated with the global mark.
	    //
	    final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	    page.activate(editor);
	    //
	    // Lookup Vrapper's EditorAdapter associated with the Eclipse editor.
	    //
	    return VrapperPlugin.getDefault().findEditor(editor);
	}

	public void execute(EditorAdaptor editorAdaptor)
			throws CommandExecutionException {
	    EditorAdaptor markEditor = editorAdaptor;
	    if (editorAdaptor.getCursorService().isGlobalMark(id)) {
	        markEditor = activateMarkEditor(editorAdaptor);
	    }
        if (markEditor == null) {
            return;
        }
        //
        // The associated editor is open and active, change current position
        // if requested.
        //
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

}
