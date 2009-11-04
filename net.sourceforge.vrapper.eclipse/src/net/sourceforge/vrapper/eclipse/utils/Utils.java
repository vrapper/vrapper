package net.sourceforge.vrapper.eclipse.utils;

import org.eclipse.core.runtime.IConfigurationElement;

public class Utils {

    public static IConfigurationElement onlyChild(IConfigurationElement element, String childName) {
            IConfigurationElement[] children = element.getChildren(childName);
            if (children.length != 1)
                throw new RuntimeException("element must have exactly one <" + childName + "/> child");
            return children[0];
    }

}
