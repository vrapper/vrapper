package net.sourceforge.vrapper.eclipse.commands;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.AbstractCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.MultipleExecutionCommand;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.CommandException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;


public class EclipseCommand extends AbstractCommand {

    private final String action;

    public EclipseCommand(String action) {
        this.action = action;
    }

    public void execute(EditorAdaptor editorAdaptor) {
        doIt(1, action, editorAdaptor);
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

    public static void doIt(int count, final String action, EditorAdaptor editorAdaptor) {
        final IHandlerService handlerService = editorAdaptor.getService(IHandlerService.class);
        final ICommandService commandService = editorAdaptor.getService(ICommandService.class);
        if (handlerService != null && commandService != null) {
            //
            // Some commands misbehave if not run asynchronously.
            //
            getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    try {
                        final ParameterizedCommand command = commandService.deserialize(action);
                        handlerService.executeCommand(command, null);
                    } catch (CommandException e) {
                        VrapperLog.error("Command not handled: " + action, e);
                    }
                }
            });
        } else {
            VrapperLog.error("No handler service, cannot execute: " + action);
        }
    }

    public Command withCount(int count) {
        return new MultipleExecutionCommand(count, this);
    }

}
