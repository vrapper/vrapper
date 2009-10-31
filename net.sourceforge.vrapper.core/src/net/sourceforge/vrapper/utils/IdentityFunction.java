package net.sourceforge.vrapper.utils;

public class IdentityFunction<T> implements Function<T, T> {
    
    private static IdentityFunction<?> INSTANCE = new IdentityFunction<Object>();

    public T call(T arg) {
        return arg;
    }

    @SuppressWarnings("unchecked")
    public static<T> Function<T, T> getInstance() {
        return (Function<T, T>) INSTANCE;
    }

}
