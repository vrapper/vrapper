package net.sourceforge.vrapper.eclipse.commands;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.AbstractCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.MultipleExecutionCommand;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.CommandException;
import org.eclipse.core.commands.common.NotDefinedException;
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

    public static void doIt(int count, String action, EditorAdaptor editorAdaptor) {
        try {
            IHandlerService handlerService = editorAdaptor.getService(IHandlerService.class);
            ICommandService commandService = editorAdaptor.getService(ICommandService.class);
            if (handlerService != null && commandService != null) {
                handlerService.executeCommand(commandService.deserialize(action), null);
            } else {
                VrapperLog.error("No handler service, cannot execute: " + action);
            }
        } catch (ExecutionException e) {
            VrapperLog.error("Error when executing command: " + action, e);
        } catch (NotDefinedException e) {
            VrapperLog.error("Command not defined: " + action, e);
        } catch (NotEnabledException e) {
            VrapperLog.error("Command not enabled: " + action, e);
        } catch (NotHandledException e) {
            VrapperLog.error("Command not handled: " + action, e);
        } catch (CommandException e) {
            VrapperLog.error("Command not handled: " + action, e);
		}
    }

    public Command withCount(int count) {
        return new MultipleExecutionCommand(count, this);
    }

}
