package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.eclipse.interceptor.EclipseCommandHandler;
import net.sourceforge.vrapper.eclipse.interceptor.InputInterceptor;
import net.sourceforge.vrapper.platform.ServiceProvider;
import net.sourceforge.vrapper.platform.VrapperPlatformException;

import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class EclipseServiceProvider implements ServiceProvider {

    private final IHandlerService handlerService;
    private final ICommandService commandService;
    private InputInterceptor interceptor;

    public EclipseServiceProvider(AbstractTextEditor abstractTextEditor) {
        handlerService = (IHandlerService) abstractTextEditor.getSite().getService(IHandlerService.class);
        commandService = (ICommandService) abstractTextEditor.getSite().getService(ICommandService.class);
    }
    
    public void setInputInterceptor(InputInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> serviceClass) {
        if (IHandlerService.class.equals(serviceClass)) {
            guardService("IHandlerService", handlerService);
            return (T) handlerService;
        }
        if (ICommandService.class.equals(serviceClass)) {
            guardService("ICommandService", commandService);
            return (T) commandService;
        }
        if (EclipseCommandHandler.class.equals(serviceClass)) {
            guardService("InputInterceptor", interceptor);
            guardService("EclipseCommandHandler", interceptor.getEclipseCommandHandler());
            return (T) interceptor.getEclipseCommandHandler();
        }
        return null;
    }

    protected void guardService(String svcDescription, Object service) {
        if (service == null) {
            throw new VrapperPlatformException(svcDescription + " service was not found");
        }
    }
}
