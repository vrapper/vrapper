package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.platform.FileService;

import org.eclipse.ui.texteditor.AbstractTextEditor;

public class EclipseFileService implements FileService {

    private final AbstractTextEditor editor;

    public EclipseFileService(AbstractTextEditor editor) {
        this.editor = editor;
    }

    public boolean isEditable() {
        return editor.isEditable();
    }

    public boolean close(boolean force) {
        if (force || !editor.isDirty()) {
            editor.close(false);
            return true;
        }
        return false;
    }

    public boolean save() {
        if (editor.isDirty() && editor.isEditable()) {
            editor.doSave(null);
            return true;
        }
        return false;
    }
    
    public boolean saveAll() {
        return editor.getSite().getWorkbenchWindow().getWorkbench().saveAllEditors(false);
    }

}
