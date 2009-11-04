package net.sourceforge.vrapper.eclipse.matcher;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class RegexMatcher implements ExtensionMatcher {
    
    private Pattern pattern;

    public RegexMatcher(IConfigurationElement configurationElement) {
        String regexStr = configurationElement.getAttribute("regex");
        pattern = Pattern.compile(regexStr);
    }

    public boolean matches(AbstractTextEditor underlyingEditor) {
        return pattern.matcher(underlyingEditor.getClass().getName()).matches();
    }

}
