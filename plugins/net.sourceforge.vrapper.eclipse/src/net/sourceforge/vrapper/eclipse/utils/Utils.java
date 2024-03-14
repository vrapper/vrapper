package net.sourceforge.vrapper.eclipse.utils;

import net.sourceforge.vrapper.log.VrapperLog;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IWorkbenchPart;
import org.osgi.framework.Bundle;

public class Utils {
    /**
     * If part subclasses a class whose name is contained by element's
     * subclassAttrName attribute, then return instance of gizmoClassName,
     * otherwise return <code>null</code>.
     */
    public static Object createGizmoForElementConditionally(
            IWorkbenchPart part, String subclassAttrName,
            IConfigurationElement element, String gizmoClassName) {
        try {
//            VrapperLog.info("trying to create " + gizmoClassName + " for " + part.getTitle() + "(" + part.getClass().getName() + ")");
            String classNameToSubclass = element.getAttribute(subclassAttrName);
            if (classNameToSubclass != null) {
                // if no subclassAttrName attribute is provided, then we return gizmo anyway
                String bundleName = element.getDeclaringExtension()
                        .getContributor().getName();
                Bundle bundle = Platform.getBundle(bundleName);
                if (!bundle.loadClass(classNameToSubclass).isInstance(part))
                    return null;
            }
            return element.createExecutableExtension(gizmoClassName);
        } catch (Exception e) {
            VrapperLog.error("error in createGizmoForElementConditionally", e);
            return null;
        }
    }
}
