package kg.totality.core;

import newpackage.glue.CursorService;
import newpackage.glue.FileService;
import newpackage.glue.HistoryService;
import newpackage.glue.TextContent;
import newpackage.glue.ViewportService;
import newpackage.position.Position;
import newpackage.position.TextRange;
import newpackage.vim.register.RegisterManager;

public interface EditorAdaptor {
	void changeMode(String modeName);

	TextContent getModelContent();
	TextContent getViewContent();
	CursorService getCursorService();
	FileService getFileService();
	ViewportService getViewportService();
	HistoryService getHistory();
	RegisterManager getRegisterManager();

	Position getPosition();
	void setPosition(Position destination, boolean updateStickyColumn);
	void setSelection(TextRange range);
	TextRange getSelection();
	<T>T getService(Class<T> serviceClass);
}

