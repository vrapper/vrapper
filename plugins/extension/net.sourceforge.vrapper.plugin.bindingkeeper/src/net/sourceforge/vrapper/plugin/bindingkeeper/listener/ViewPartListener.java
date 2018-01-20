package net.sourceforge.vrapper.plugin.bindingkeeper.listener;

import static org.eclipse.ui.IWorkbenchCommandConstants.WINDOW_PREFERENCES;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.WorkbenchWindow;

import net.sourceforge.vrapper.plugin.bindingkeeper.BindingKeeper;

/**
 * @author Pedro Santos
 * 
 */
public class ViewPartListener implements Runnable {

	private boolean activeShell = true;

	/**
	 * @return if the shell containing text editors is active
	 */
	public boolean isInsideActiveShell() {
		return activeShell;
	}

	/*
	 * During 'Keys preference page' initialization, it's important that there are
	 * no key bindings change made by this plugin. Unfortunately it's impossible to
	 * detect 'Keys preference page' initialization at the right time due two
	 * Eclipse bugs. Given this barrier, this plugin tries it best to detect that
	 * the preference page is about to be opened .
	 * 
	 * FIRST BUG: Eclipse doesn't use the 'Preferences' shell to initialize Keys
	 * preference page, so it's impossible to register a listener remove any key
	 * bindings changed by this pluging before the keys preference page is loaded.
	 * 
	 * Context: just before the 'Keys preference page' is about to be opened by it
	 * PreferenceElement action registered in the QuickAccessContents, the shell
	 * 'Quick Access' will quickly be activated due a mouse click on its menu.
	 * 
	 * Workaround: To cleanup pluging key bindings changes when some shell, other
	 * than 'Preferences' is active. If by some reason the 'Workbench' shell got
	 * quickly deactivated and activated in sequence, Eclipse is possibly
	 * initializing 'Preferences shell' UI due a quick focus gained by 'Workbench'
	 * shell when a quick access item offering an preference page is clicked.
	 */
	private long lastDeactivated;// tracks shell activation time
	/*
	 * SECOND BUG: Eclipse didn't assigned an ID for OpenPreferencsAction (as
	 * annotated in its code, line 50), so there's no way to access this action and
	 * to listen that 'Keys preference page' is about to be initialized
	 * (ExternalActionManager.IExecuteCallback would do the job).
	 * 
	 * Workaround: to wrap OpenPreferencesAction and to add a execution listener
	 * responsible to signalize that the preferences dialog is being opened.
	 */
	private boolean showingPreferences;

	/**
	 * @return signalizes that preferences page is opened in a dialog
	 */
	public boolean isShowingPreferences() {
		return showingPreferences;
	}

	/**
	 * Installs this listener
	 */
	@Override
	public void run() {
		// workbench listeners
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

		activeWorkbenchWindow.getPartService().addPartListener(new ViewListener());
		activeWorkbenchWindow.getShell().addShellListener(new EditorShellListener());

		// install the window preferences command listener
		ICommandService service = activeWorkbenchWindow.getService(ICommandService.class);
		service.getCommand(WINDOW_PREFERENCES).addExecutionListener(new WindowPreferencesListener());

		// wrap the Preferences menu item and installs its listener
		MenuManager menuManager = ((WorkbenchWindow) activeWorkbenchWindow).getMenuManager();
		IMenuManager windowMenu = menuManager.findMenuUsingPath("window");
		ActionContributionItem preferencesItem = (ActionContributionItem) menuManager
				.findUsingPath("window/preferences");
		ActionContributionItem wrapped = new ActionContributionItem(new ActionWrapper(preferencesItem.getAction()));
		wrapped.setVisible(preferencesItem.isVisible());
		windowMenu.remove(preferencesItem);
		windowMenu.add(wrapped);
		((ActionWrapper) wrapped.getAction()).setExecutionListener(new WindowPreferencesListener());
	}

	class WindowPreferencesListener implements IExecutionListener {

		@Override
		public void preExecute(String commandId, ExecutionEvent event) {
			showingPreferences = true;
			BindingKeeper.getDefault().setupBindings();
		}

		@Override
		public void notHandled(String commandId, NotHandledException exception) {
			showingPreferences = false;
		}

		@Override
		public void postExecuteFailure(String commandId, ExecutionException exception) {
			showingPreferences = false;
		}

		@Override
		public void postExecuteSuccess(String commandId, Object returnValue) {
			showingPreferences = false;
		}

	}

	class ViewListener implements IPartListener {

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

	class EditorShellListener extends ShellAdapter {

		@Override
		public void shellActivated(ShellEvent e) {
			activeShell = true;
			long downtime = System.currentTimeMillis() - lastDeactivated;

			boolean delay = downtime < 2000;
			if (delay)
				// delay the possible erroneous removal of user key bindings
				// since the key preference can be about to open
				BindingKeeper.getDefault().setupAfterDelay();
			else
				BindingKeeper.getDefault().setupBindings();
		}

		@Override
		public void shellDeactivated(ShellEvent e) {
			activeShell = false;
			lastDeactivated = System.currentTimeMillis();
			BindingKeeper.getDefault().setupBindings();
		}
	}

}
