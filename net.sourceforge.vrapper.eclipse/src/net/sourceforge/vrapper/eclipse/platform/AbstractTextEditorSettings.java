package net.sourceforge.vrapper.eclipse.platform;

import java.lang.reflect.Method;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.UnderlyingEditorSettings;

import org.eclipse.ui.texteditor.AbstractTextEditor;

public class AbstractTextEditorSettings implements UnderlyingEditorSettings {

    private final AbstractTextEditor abstractTextEditor;

    public AbstractTextEditorSettings(AbstractTextEditor abstractTextEditor) {
        this.abstractTextEditor = abstractTextEditor;
    }

    public void setReplaceMode(boolean replace) {
        try {
            // AbstractTextEditor.enableOverwriteMode is broken - it works only for disabling overwrite mode %-/
            Method isInsertingMethod = AbstractTextEditor.class.getDeclaredMethod("isInInsertMode");
            isInsertingMethod.setAccessible(true);
            boolean isInserting = (Boolean) isInsertingMethod.invoke(abstractTextEditor);
            if (isInserting == replace) {
                Method toggleMethod = AbstractTextEditor.class.getDeclaredMethod("toggleOverwriteMode");
                toggleMethod.setAccessible(true);
                toggleMethod.invoke(abstractTextEditor);
            }
        } catch (Exception exception) {
            VrapperLog.error("error when enabling replace mode", exception);
        }
    }

}
