package net.sourceforge.vrapper.log;

public class SystemStreamsLog implements Log {

	@Override
	public void error(String msg, Throwable exception) {
		System.err.println(msg);
		if (exception != null)
			exception.printStackTrace();
	}

	@Override
	public void info(String msg) {
		System.out.println(msg);
	}

}
