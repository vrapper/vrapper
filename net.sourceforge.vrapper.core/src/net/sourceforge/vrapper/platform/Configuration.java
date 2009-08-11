package net.sourceforge.vrapper.platform;

import net.sourceforge.vrapper.platform.SimpleConfiguration.NewLine;

/**
 * Holds variables which influence the behaviour of different commands.
 *
 * @author Matthias Radig
 */
public interface Configuration {

    public abstract String getNewLine();

    public abstract void setNewLine(String newLine);

    public abstract void setNewLine(NewLine newLine);

    public <T> void set(Option<T> key, T value);

    public <T> T get(Option<T> key);

    public static class Option<T> {

        private final String id;
        private final String[] alias;
        private final T defaultValue;

        private Option(String id, T defaultValue, String...alias) {
            super();
            this.id = id;
            this.defaultValue = defaultValue;
            this.alias = alias;
        }

        public static final Option<Boolean> bool(String id, boolean defaultValue, String... alias) {
            return new Option<Boolean>(id, Boolean.valueOf(defaultValue), alias);
        }

        public String getId() {
            return id;
        }

        public T getDefaultValue() {
            return defaultValue;
        }

        public String[] getAlias() {
            return alias;
        }

    }

}