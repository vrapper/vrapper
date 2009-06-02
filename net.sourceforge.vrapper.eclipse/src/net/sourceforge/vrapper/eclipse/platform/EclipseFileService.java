package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.platform.FileService;

import org.eclipse.ui.texteditor.ITextEditor;

public class EclipseFileService implements FileService {

    private final ITextEditor editor;

    public EclipseFileService(ITextEditor editor) {
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

}
