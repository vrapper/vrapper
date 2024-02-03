package net.sourceforge.vrapper.platform;

public interface ServiceProvider {
	<T>T getService(Class<T> serviceClass);
}
