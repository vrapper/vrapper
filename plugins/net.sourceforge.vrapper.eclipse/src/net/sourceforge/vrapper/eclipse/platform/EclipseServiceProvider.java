package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.platform.ServiceProvider;

import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class EclipseServiceProvider implements ServiceProvider {

    private final IHandlerService handlerService;
    private final ICommandService commandService;

    public EclipseServiceProvider(AbstractTextEditor abstractTextEditor) {
        handlerService = (IHandlerService) abstractTextEditor.getSite().getService(IHandlerService.class);
        commandService = (ICommandService) abstractTextEditor.getSite().getService(ICommandService.class);
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> serviceClass) {
        if (IHandlerService.class.equals(serviceClass)) {
            return (T) handlerService;
        }
        if (ICommandService.class.equals(serviceClass)) {
            return (T) commandService;
        }
        return null;
    }

}
