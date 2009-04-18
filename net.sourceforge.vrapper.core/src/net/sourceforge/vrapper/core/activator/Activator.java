package net.sourceforge.vrapper.core.activator;

import net.sourceforge.vrapper.log.Log;
import net.sourceforge.vrapper.log.VrapperLog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements Log {

    // The plug-in ID
    public static final String PLUGIN_ID = "net.sourceforge.vrapper.core";

    // The shared instance
    private static Activator plugin;

    /**
     * The constructor
     */
    public Activator() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        VrapperLog.setImplementation(this);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
        VrapperLog.setImplementation(null);
    }

    private static void log(int status, String msg, Throwable exception) {
        Activator instance = getDefault();
        if (instance != null) {
            instance.getLog().log(new Status(status, PLUGIN_ID, msg, exception));
        } else if (status == IStatus.ERROR) {
            System.err.println(msg);
        } else {
            System.out.println(msg);
        }

    }

    public void info(String msg) {
        log(IStatus.INFO, msg, null);
    }

    public void error(String msg, Throwable exception) {
        log(IStatus.ERROR, msg, exception);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

}
