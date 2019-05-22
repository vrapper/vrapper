package net.sourceforge.vrapper.eclipse.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.custom.ST;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import net.sourceforge.vrapper.eclipse.interceptor.InputInterceptor;
import net.sourceforge.vrapper.eclipse.interceptor.InputInterceptorManager;
import net.sourceforge.vrapper.eclipse.interceptor.UnknownEditorException;
import net.sourceforge.vrapper.eclipse.ui.EclipseCommandLineUI;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.VrapperPlatformException;
import net.sourceforge.vrapper.vim.modes.commandline.AbstractCommandLineMode;

/**
 * Handles Home / End / Page up / Page down motions.
 */
public class VrapperCommandLineMotionHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        String commandId = event.getCommand().getId();
        // Guaranteed to be present through core expression in plugin.xml
        IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
        try {
            InputInterceptor interceptor = InputInterceptorManager.INSTANCE.findActiveInterceptor(activeEditor);

            if ( ! (interceptor.getEditorAdaptor().getCurrentMode() instanceof AbstractCommandLineMode)) {
                VrapperLog.info("Command line is not shown, ignoring keypress");
                return null;
            }
            EclipseCommandLineUI commandLine = 
                    (EclipseCommandLineUI) interceptor.getEditorAdaptor().getCommandLine();

            // Detect which of the commands bound in plugin.xml was used to trigger this handler:
            if (commandId.endsWith(".lineStart")) {
                commandLine.setPosition(0);

            } else if (commandId.endsWith(".lineEnd")) {
                commandLine.setPosition(commandLine.getEndPosition());

            } else if (commandId.endsWith(".wordNext")) {
                commandLine.getWidget().invokeAction(ST.WORD_NEXT);

            } else if (commandId.endsWith(".wordPrevious")) {
                commandLine.getWidget().invokeAction(ST.WORD_PREVIOUS);

            } else if (commandId.endsWith(".paste")) {
                commandLine.getWidget().invokeAction(ST.PASTE);

            } else if (commandId.endsWith(".cut")) {
                commandLine.getWidget().invokeAction(ST.CUT);

            } else if (commandId.endsWith(".copy")) {
                commandLine.getWidget().invokeAction(ST.COPY);

            } else if (commandId.endsWith(".selectAll")) {
                commandLine.setSelection(0, commandLine.getEndPosition());
            }
            // Make sure caret remains after prompt characters
            commandLine.clipSelection();
        } catch (VrapperPlatformException e) {
            VrapperLog.error("Failed to find editor for part " + activeEditor, e);
        } catch (UnknownEditorException e) {
            VrapperLog.error("Could not find interceptor for part " + activeEditor, e);
        }
        return null;
    }
}