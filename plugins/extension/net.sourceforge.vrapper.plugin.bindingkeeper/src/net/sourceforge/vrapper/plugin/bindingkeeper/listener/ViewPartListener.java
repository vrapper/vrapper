package net.sourceforge.vrapper.plugin.bindingkeeper.listener;

import net.sourceforge.vrapper.plugin.bindingkeeper.BindingKeeper;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @author Pedro Santos
 * 
 */
public class ViewPartListener implements IPartListener {

	@Override
	public void partActivated(IWorkbenchPart part) {
		BindingKeeper.getDefault().setupBindings();
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	@Override
	public void partClosed(IWorkbenchPart part) {

	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {

	}

	@Override
	public void partOpened(IWorkbenchPart part) {

	}

}
