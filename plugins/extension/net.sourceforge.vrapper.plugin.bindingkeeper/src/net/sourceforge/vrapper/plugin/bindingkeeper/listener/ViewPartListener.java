package net.sourceforge.vrapper.plugin.bindingkeeper.listener;

import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.Workbench;

import net.sourceforge.vrapper.plugin.bindingkeeper.BindingKeeper;

/**
 * @author Pedro Santos
 * 
 */
public class ViewPartListener implements IPartListener, ShellListener, Runnable {

	private boolean activeWindow = true;
	private Shell editorShell;

	public boolean isInsideActiveShell() {
		return activeWindow;
	}

	/**
	 * Installs this listener
	 */
	@Override
	public void run() {
		Workbench.getInstance().getActiveWorkbenchWindow().getPartService().addPartListener(this);
		editorShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		editorShell.addShellListener(this);

	}

	// IPartListener
	@Override
	public void partActivated(IWorkbenchPart part) {
		activeWindow = true;
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

	// ShellListener
	@Override
	public void shellActivated(ShellEvent e) {
		activeWindow = true;
		BindingKeeper.getDefault().setupBindings();
	}

	@Override
	public void shellDeactivated(ShellEvent e) {
		activeWindow = false;
		BindingKeeper.getDefault().setupBindings();
	}

	@Override
	public void shellClosed(ShellEvent e) {

	}

	@Override
	public void shellDeiconified(ShellEvent e) {

	}

	@Override
	public void shellIconified(ShellEvent e) {

	}

}
