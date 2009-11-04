package net.sourceforge.vrapper.eclipse.matcher;

import org.eclipse.ui.texteditor.AbstractTextEditor;

public interface ExtensionMatcher {

    boolean matches(AbstractTextEditor underlyingEditor);

}
