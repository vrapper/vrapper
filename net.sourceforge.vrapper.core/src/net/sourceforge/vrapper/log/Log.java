package net.sourceforge.vrapper.log;

public interface Log {
	public static final String DEBUGLOG_PROPERTY = "vrapper.debuglog";

	void info(String msg);
	void error(String msg, Throwable exception);
	void debug(String msg);
	void setDebugEnabled(boolean enabled);
}
