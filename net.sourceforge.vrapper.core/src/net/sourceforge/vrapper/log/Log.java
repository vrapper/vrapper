package net.sourceforge.vrapper.log;

public interface Log {
	void info(String msg);
	void error(String msg, Throwable exception);
}
