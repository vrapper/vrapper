package net.sourceforge.vrapper.eclipse.matcher;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.vrapper.log.VrapperLog;

import org.eclipse.core.runtime.IConfigurationElement;

public class ExtensionMatcherFactory {

    private static Map<String, Class<? extends ExtensionMatcher>> classes = new HashMap<String, Class<? extends ExtensionMatcher>>();
    
    static {
        classes.put("smelly-regex", RegexMatcher.class);
        classes.put("subclass-of", SubclassMatcher.class);
    }

    public static ExtensionMatcher create(IConfigurationElement element) {
        try {
            VrapperLog.info("creating matcher for " + element.getName());
            Class<? extends ExtensionMatcher> cls = classes.get(element.getName());
            if (cls != null)
                return cls.getConstructor(IConfigurationElement.class).newInstance(element);
        } catch (Exception e) {
            VrapperLog.error("couldn't map ExtensionMatcher", e);
        }
        return null;
    }

}
