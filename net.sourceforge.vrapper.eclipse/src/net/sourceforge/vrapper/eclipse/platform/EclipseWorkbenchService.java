package net.sourceforge.vrapper.eclipse.platform;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;

import net.sourceforge.vrapper.platform.WorkbenchService;

public class EclipseWorkbenchService implements WorkbenchService {
	private IWorkbench workbench;

	public EclipseWorkbenchService(IEditorPart editor) {
		workbench = editor.getSite().getWorkbenchWindow().getWorkbench();
	}
    
    public void saveAll() {
    	workbench.saveAllEditors(false);
    }

}
