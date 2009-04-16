package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.platform.ServiceProvider;

import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class EclipseServiceProvider implements ServiceProvider {

	private IHandlerService handlerService;

	public EclipseServiceProvider(AbstractTextEditor abstractTextEditor) {
		handlerService = (IHandlerService) abstractTextEditor.getSite().getService(IHandlerService.class);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getService(Class<T> serviceClass) {
		if (IHandlerService.class.equals(serviceClass))
			return (T) handlerService;
		return null;
	}

}
