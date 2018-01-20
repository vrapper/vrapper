package net.sourceforge.vrapper.plugin.bindingkeeper.listener;

import static org.eclipse.ui.IWorkbenchCommandConstants.WINDOW_PREFERENCES;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.quickaccess.SearchField;

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
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		MWindow mWindow = ((WorkbenchWindow) activeWorkbenchWindow).getModel();
		EModelService modelService = mWindow.getContext().get(EModelService.class);
		MToolControl searchField = (MToolControl) modelService.find("SearchField", mWindow); //$NON-NLS-1$
		SearchField field = (SearchField) searchField.getObject();

		// workbench listeners
		field.getQuickAccessSearchText().addFocusListener(new SearchFieldFocusListener());
		activeWorkbenchWindow.getPartService().addPartListener(new ViewListener());
		activeWorkbenchWindow.getShell().addShellListener(new EditorShellListener());

		// install the window preferences command listener
		ICommandService service = activeWorkbenchWindow.getService(ICommandService.class);
		service.getCommand(WINDOW_PREFERENCES).addExecutionListener(new WindowPreferencesListener());

		/*
		 * Problem: Eclipse didn't assigned an ID for OpenPreferencsAction (as annotated
		 * in its code, line 50), so there's no way to access this action and to listen
		 * that 'Keys preference page' is about to be initialized
		 * (ExternalActionManager.IExecuteCallback would do the job).
		 * 
		 * Workaround: to wrap OpenPreferencesAction and to add a execution listener
		 * responsible to signalize that the preferences dialog is being opened.
		 */
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

	class SearchFieldFocusListener implements FocusListener {

		@Override
		public void focusLost(FocusEvent e) {
			showingPreferences = false;
			BindingKeeper.getDefault().setupBindings();
		}

		@Override
		public void focusGained(FocusEvent e) {
			showingPreferences = true;
			BindingKeeper.getDefault().setupBindings();
		}
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
			BindingKeeper.getDefault().setupBindings();
		}

		@Override
		public void shellDeactivated(ShellEvent e) {
			activeShell = false;
			BindingKeeper.getDefault().setupBindings();
		}
	}

}
