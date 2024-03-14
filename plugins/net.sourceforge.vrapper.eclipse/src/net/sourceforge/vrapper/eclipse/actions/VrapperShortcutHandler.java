package net.sourceforge.vrapper.eclipse.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import net.sourceforge.vrapper.eclipse.interceptor.InputInterceptor;
import net.sourceforge.vrapper.eclipse.interceptor.InputInterceptorManager;
import net.sourceforge.vrapper.eclipse.interceptor.UnknownEditorException;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.VrapperPlatformException;


public class VrapperShortcutHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if ( ! (event.getTrigger() instanceof Event)) {
            return null;
        }
        Event triggerEvent = (Event) event.getTrigger();
        if (triggerEvent.type != SWT.KeyDown) {
            return null;
        }
        VerifyEvent verifyEvent = new VerifyEvent(triggerEvent);

        // Guaranteed to be present through core expression in plugin.xml
        IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
        try {
            InputInterceptor interceptor = InputInterceptorManager.INSTANCE.findActiveInterceptor(activeEditor);
            if (interceptor == null) {
                VrapperLog.error("Could not find interceptor for part " + activeEditor);
            }
            interceptor.verifyKey(verifyEvent);
        } catch (VrapperPlatformException e) {
            VrapperLog.error("Failed to find editor for part " + activeEditor, e);
        } catch (UnknownEditorException e) {
            VrapperLog.error("Could not find interceptor for part " + activeEditor, e);
        }
        return null;
    }
}