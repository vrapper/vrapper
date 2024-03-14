package net.sourceforge.vrapper.log;

public class SystemStreamsLog implements Log {

    private boolean debugEnabled = Boolean.parseBoolean(System.getProperty(DEBUGLOG_PROPERTY));

    public SystemStreamsLog(boolean enableDebug) {
        debugEnabled = enableDebug;
    }

    public void error(String msg, Throwable exception) {
        System.err.println(msg);
        if (exception != null) {
            exception.printStackTrace();
        }
    }

    public void info(String msg) {
        System.out.println(msg);
    }

    public void debug(String msg) {
        if (debugEnabled) {
            System.out.println(msg);
        }
    }

    @Override
    public void setDebugEnabled(boolean enabled) {
        debugEnabled = enabled;
    }

    @Override
    public boolean isDebugEnabled() {
        return debugEnabled;
    }
}
