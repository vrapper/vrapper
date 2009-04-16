package newpackage.glue;


public interface Platform {

	TextContent getModelContent();
	TextContent getViewContent();
	CursorService getCursorService();
	SelectionService getSelectionService();
	FileService getFileService();
	ViewportService getViewportService();
	HistoryService getHistoryService();
	ServiceProvider getServiceProvider();
}
