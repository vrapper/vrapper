package net.sourceforge.vrapper.eclipse.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.keys.IBindingService;

import net.sourceforge.vrapper.eclipse.activator.VrapperPlugin;
import net.sourceforge.vrapper.eclipse.interceptor.InputInterceptor;
import net.sourceforge.vrapper.eclipse.interceptor.UnknownEditorException;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.VrapperPlatformException;

public class ActivateEditorAndTypeHandler extends AbstractHandler {

    /**
     * Set to true when a function higher up the call stack is already executing events.
     * In a true multi-threaded application this should have been a ThreadLocal, but here it's
     * no issue since Eclipse only allows the singleton UI thread to manipulate widgets.
     */
    private static volatile boolean isReentrant;
    private IBindingService bindingService;

    public ActivateEditorAndTypeHandler() {
        super();
        if (bindingService == null) {
            bindingService = (IBindingService) PlatformUI.getWorkbench().getService(IBindingService.class); 
        }
    }

    @Override
    public void dispose() {
        bindingService = null;
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
        IWorkbenchPage page = window.getActivePage();
        if (page != null) {
            IEditorPart part = HandlerUtil.getActiveEditor(event);
            if (part != null) {
                page.activate(part);

                // Now focus editor component inside if we can find a Vrapper instance for it
                try {
                    InputInterceptor interceptor = VrapperPlugin.getDefault().findActiveInterceptor(part);
                    ISourceViewer viewer = interceptor.getPlatform().getUnderlyingSourceViewer();

                    StyledText textWidget = viewer.getTextWidget();
                    textWidget.setFocus();

                    // Find the key event used to trigger this handler (if any), send to editor
                    Object trigger = event.getTrigger();
                    if (trigger != null && trigger instanceof Event
                            && ((Event)trigger).type == SWT.KeyDown) {
                        Event clone = cloneEvent((Event)trigger);
                        clone.widget = textWidget;
                        clone.doit = true;

                        invokeKeyEventNonReentrant(clone, textWidget);
                    }

                } catch (IllegalThreadStateException e) {
                    VrapperLog.info("Handler was being executed reentrant, stopping");
                } catch (VrapperPlatformException e) {
                    VrapperLog.error("No editor to focus, encountered platformexception", e);
                } catch (UnknownEditorException e) {
                    VrapperLog.debug("No editor to focus on active page");
                }
            }
        }
        return null; 
    }

    /**
     * Sends a KeyDown event back to the text widget after temporarily disabling Eclipse's key
     * binding to avoid arriving in the current handler again.
     * Just to be safe we check whether this function is entered twice and in that case bail out
     * with an exception.
     */
    private void invokeKeyEventNonReentrant(Event event, StyledText textWidget) {
        boolean tempReentrantState = isReentrant;
        boolean tempKeyFilterState = bindingService.isKeyFilterEnabled();
        try {
            if (isReentrant) {
                throw new IllegalThreadStateException("Event is being handled twice");
            } else {
                isReentrant = true;
                bindingService.setKeyFilterEnabled(false);
                textWidget.notifyListeners(event.type, event);
            }
        } finally {
            bindingService.setKeyFilterEnabled(tempKeyFilterState);
            isReentrant = tempReentrantState;
        }
    }

    private Event cloneEvent(Event source) {
        Event clone = new Event();
        clone.button = source.button;
        clone.character = source.character;
        clone.count = source.count;
        clone.data = source.data;
        clone.detail = source.detail;
        clone.display = source.display;
        clone.doit = source.doit;
        clone.end = source.end;
        clone.gc = source.gc;
        clone.height = source.height;
        clone.index = source.index;
        clone.item = source.item;
        clone.keyCode = source.keyCode;
        clone.keyLocation = source.keyLocation;
        clone.start = source.start;
        clone.stateMask = source.stateMask;
        clone.text = source.text;
        clone.time = source.time;
        clone.type = source.type;
        clone.widget = source.widget;
        clone.width = source.width;
        clone.x = source.x;
        clone.y = source.y;
        return clone;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
