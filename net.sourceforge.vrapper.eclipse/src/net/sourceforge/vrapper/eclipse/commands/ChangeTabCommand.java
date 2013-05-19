package net.sourceforge.vrapper.eclipse.commands;

import java.util.ArrayDeque;
import java.util.Deque;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.AbstractCommand;
import net.sourceforge.vrapper.vim.commands.Command;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;

/**
 * Emulating vim gt/gT tab cycling behavior.
 *
 * Not straightforward.
 */
public class ChangeTabCommand extends AbstractCommand {

    private final static int FORWARD = SWT.TRAVERSE_PAGE_NEXT;
    private final static int BACKWARD = SWT.TRAVERSE_PAGE_PREVIOUS;

    public static ChangeTabCommand NEXT_EDITOR = new ChangeTabCommand(FORWARD);
    public static ChangeTabCommand PREVIOUS_EDITOR = new ChangeTabCommand(BACKWARD);

    private int count = NO_COUNT_GIVEN;
    private int direction;

    private ChangeTabCommand(int direction) {
        this.direction = direction;
    }

    public Command withCount(int count) {
        this.count = count;
        return this;
    }

    public Command repetition() {
        return null;
    }

    public void execute(EditorAdaptor editorAdaptor) {
        // [NOTE] This code is based on org.eclipse.ui.internal.handlers.TraversePageHandler
        
        // Ideally we'd grab the StyledText widget from the current editor, but it takes some
        // refactoring to get access to it.
        // This should work as well because the currently focused editor is where vrapper runs.
        Control focusControl = Display.getCurrent().getFocusControl();
        
        Deque<Control> tabControls = new ArrayDeque<Control>();
        if (focusControl != null) {
            Control control = focusControl;
            while (control != null && ! (control instanceof Shell)) {
                if (control instanceof CTabFolder || control instanceof TabFolder) {
                    tabControls.add(control);
                }
                if (control.traverse(direction)) {
                    break;
                }
                control = control.getParent();
            }
        }
        
        // [TODO] If the current editor is a multi-page editor (e.g. XML editors), then it
        //  seems that the CTabFolder in the list is the one belonging to the editor, not the
        //  one belonging to the workspace which actually switches editors.
        // Implement old, repeated tab switching behavior until we get the right CTabFolder,
        //  and then we can finally call setSelection() as it should.
        if (count != NO_COUNT_GIVEN) {
            for (int i = 1; i < count; i++) {
                tabControls.peekLast().traverse(direction);
            }
        }
        
        // reset the count since this instance is used as a static
        // (we might call this command again in a new context)
        count = NO_COUNT_GIVEN;
    }
}
