package newpackage.glue;

public interface ServiceProvider {
	<T>T getService(Class<T> serviceClass);
}
