package kg.totality.core;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;

import kg.totality.core.modes.EditorMode;
import kg.totality.core.modes.InsertMode;
import kg.totality.core.modes.NormalMode;
import kg.totality.core.modes.VisualMode;
import newpackage.glue.CursorService;
import newpackage.glue.Platform;
import newpackage.glue.FileService;
import newpackage.glue.HistoryService;
import newpackage.glue.SelectionService;
import newpackage.glue.ServiceProvider;
import newpackage.glue.TextContent;
import newpackage.glue.ViewportService;
import newpackage.position.Position;
import newpackage.position.TextRange;
import newpackage.vim.register.RegisterManager;

import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.ui.IWorkbenchPart;

import de.jroene.vrapper.eclipse.VrapperPlugin;
import de.jroene.vrapper.eclipse.interceptor.InputInterceptor;

public class DefaultEditorAdaptor implements EditorAdaptor, InputInterceptor {

	private EditorMode currentMode;
	private final Map<String, EditorMode> modeMap = new HashMap<String, EditorMode>();
	private final TextContent modelContent;
	private final TextContent viewContent;
	private final CursorService cursorService;
	private final SelectionService selectionService;
	private final FileService fileService;
	private final RegisterManager registerManager;
	private final ViewportService viewportService;
	private final HistoryService historyService;
	private final ServiceProvider serviceProvider;

	public DefaultEditorAdaptor(Platform editor, RegisterManager registerManager) {
		this.modelContent = editor.getModelContent();
		this.viewContent = editor.getViewContent();
		this.cursorService = editor.getCursorService();
		this.selectionService = editor.getSelectionService();
		this.historyService = editor.getHistoryService();
		this.registerManager = registerManager;
		this.serviceProvider = editor.getServiceProvider();
		viewportService = editor.getViewportService();

		fileService = editor.getFileService();
		EditorMode[] modes = {
				new NormalMode(this),
				new VisualMode(this),
				new InsertMode(this) };
		for (EditorMode mode: modes)
			modeMap.put(mode.getName(), mode);
		changeMode(NormalMode.NAME);
	}

	@Override
	public void changeMode(String modeName) {
		EditorMode newMode = modeMap.get(modeName);
		if (newMode == null) {
			VrapperPlugin.error(format("There is no mode named '%s'",  modeName));
			return;
		}
		if (currentMode != newMode) {
			if (currentMode != null)
				currentMode.leaveMode();
			currentMode = newMode;
			newMode.enterMode();
		}
		// TODO: we may set "-- MODE NAME --" message here
	}

	@Override
	public void partActivated(IWorkbenchPart part) {
		// TODO: do something useful
	}

	@Override
	public void verifyKey(VerifyEvent event) {
		if (currentMode != null)
			currentMode.getKeyListener().verifyKey(event);
	}

	@Override
	public TextContent getModelContent() {
		return modelContent;
	}

	@Override
	public TextContent getViewContent() {
		return viewContent;
	}

	@Override
	public Position getPosition() {
		return cursorService.getPosition();
	}

	@Override
	public void setPosition(Position destination, boolean updateStickyColumn) {
		cursorService.setPosition(destination, updateStickyColumn);
	}

	@Override
	public TextRange getSelection() {
		return selectionService.getSelection();
	}

	@Override
	public void setSelection(TextRange selection) {
		selectionService.setSelection(selection);
	}

	@Override
	public CursorService getCursorService() {
		return cursorService;
	}

	@Override
	public FileService getFileService() {
		return fileService;
	}

	@Override
	public ViewportService getViewportService() {
		return viewportService;
	}

	@Override
	public RegisterManager getRegisterManager() {
		return registerManager;
	}

	@Override
	public HistoryService getHistory() {
		return historyService;
	}

	@Override
	public <T> T getService(Class<T> serviceClass) {
		return serviceProvider.getService(serviceClass);
	}

}

