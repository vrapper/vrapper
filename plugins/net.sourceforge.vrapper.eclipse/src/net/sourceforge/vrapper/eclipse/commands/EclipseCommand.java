package net.sourceforge.vrapper.eclipse.commands;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.AbstractCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.LeaveVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.MultipleExecutionCommand;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.CommandException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;


public class EclipseCommand extends AbstractCommand {

    private final String action;
    private final boolean async;
    private boolean fromVisualMode;

    public EclipseCommand(String action) {
        this.action = action;
        this.async = false;
    }

    public EclipseCommand(String action, boolean async) {
        this.action = action;
        this.async = async;
    }

    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        if (fromVisualMode) {
            editorAdaptor.rememberLastActiveSelection();
        }
        doIt(action, editorAdaptor, async);

        if (fromVisualMode) {
            LeaveVisualModeCommand.doIt(editorAdaptor);
        }
    }

    public String getCommandName() {
        return action;
    }

    public Command repetition() {
        return this;
    }

    public static Display getDisplay() {
        Display display = Display.getCurrent();
        // may be null if outside the UI thread
        if (display == null) {
            display = Display.getDefault();
        }
        return display;
    }

    public static void doIt(final String action, EditorAdaptor editorAdaptor, boolean async) {
        final IHandlerService handlerService = editorAdaptor.getService(IHandlerService.class);
        final ICommandService commandService = editorAdaptor.getService(ICommandService.class);
        if (handlerService != null && commandService != null) {
            if (async) {
                //
                // Some commands misbehave if not run asynchronously.
                //
                getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        executeAction(action, handlerService, commandService);
                    }
                });
            } else {
                executeAction(action, handlerService, commandService);
            }
        } else {
            VrapperLog.error("No handler service, cannot execute: " + action);
        }
    }

    private static void executeAction(final String action,
            final IHandlerService handlerService,
            final ICommandService commandService) {
        try {
            final ParameterizedCommand command = commandService.deserialize(action);
            handlerService.executeCommand(command, null);
        } catch (CommandException e) {
            VrapperLog.error("Command not handled: " + action, e);
        }
    }

    public Command withCount(int count) {
        return new MultipleExecutionCommand(count, this);
    }

    /**
     * Mark this command as being executed in visual mode.
     * Note that this does not need to be called when running a command from command line mode, in
     * such a case the command line mode and normal mode will handle all the extra logic.
     */
    public Command fromVisualMode(boolean fromVisualMode) {
        this.fromVisualMode = fromVisualMode;
        return this;
    }

    public String toString() {
        return "eclipseCmd(" + action + ")";
    }
}
