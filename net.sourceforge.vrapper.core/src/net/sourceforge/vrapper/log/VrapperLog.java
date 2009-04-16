package net.sourceforge.vrapper.log;



public class VrapperLog {

	private static Log implementation = null;

	public static void setImplementation(Log instance) {
		VrapperLog.implementation = instance;
	}

	private static synchronized Log getImplementation() {
		if (implementation == null)
			implementation = new SystemStreamsLog();
		return implementation;
	}

	public static void error(String msg) {
		getImplementation().error(msg, null);
	}

	public static void error(String msg, Throwable exception) {
		getImplementation().error(msg, exception);
	}

	public static void info(String msg) {
		getImplementation().info(msg);
	}

}
