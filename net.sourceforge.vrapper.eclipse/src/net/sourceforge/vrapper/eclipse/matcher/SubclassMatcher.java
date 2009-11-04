package net.sourceforge.vrapper.eclipse.matcher;

import net.sourceforge.vrapper.log.VrapperLog;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class SubclassMatcher implements ExtensionMatcher {
    
    private Class<? extends AbstractTextEditor> cls;

    @SuppressWarnings("unchecked")
    public SubclassMatcher(IConfigurationElement configurationElement) {
        String editorClassName = configurationElement.getAttribute("editor");
        try {
            cls = (Class<? extends AbstractTextEditor>) Class.forName(editorClassName);
        } catch (ClassNotFoundException e) {
            VrapperLog.error("no class", e);
            throw new RuntimeException(e);
        }
    }

    public boolean matches(AbstractTextEditor underlyingEditor) {
        return cls.isInstance(underlyingEditor);
    }

}
