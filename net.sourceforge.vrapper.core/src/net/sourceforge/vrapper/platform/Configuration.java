package net.sourceforge.vrapper.platform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /**
     * Whether the option is actually set in this config.
     * <p> If not, {@link #get(Option)} will either fetch the global config when
     * called on the local configuration, or the {@link Option}'s default value
     * will be returned when called on the global configuration.
     */
    public <T> boolean isSet(Option<T> key);

    public static enum OptionScope {
        /** This option's value will never be shared between editors, even using <tt>set</tt>. */
        LOCAL,
        /** This option's value will always be shared, even when using <tt>setlocal</tt>. */
        GLOBAL,
        /**
         * This option's value can be shared with all editors if <tt>set</tt> is used, or a
         * local value can be set using <tt>setlocal</tt>.
         */
        DEFAULT;
    }

    public static class Option<T> {
    
    public final static String SET_DELIMITER = ",";
    public final static String SET_VALUE_ITEM = ":";

        private final String id;
        private final String[] alias;
        private final T defaultValue;
        private final List<String> allNames;
        private final Set<String> legalValues;
        private final OptionScope scope;

        private Option(String id, T defaultValue, Set<String> legalValues, String...alias) {
            this(id, OptionScope.DEFAULT, defaultValue, legalValues, alias);
        }

        private Option(String id, OptionScope scope, T defaultValue,
                Set<String> legalValues, String...alias) {
            this.id = id;
            this.defaultValue = defaultValue;
            this.legalValues = legalValues;
            this.alias = alias;
            this.allNames = new ArrayList<String>();
            allNames.add(id);
            allNames.addAll(Arrays.asList(alias));
            this.scope = scope;
        }

        public static final Option<Boolean> bool(String id, boolean defaultValue, String... alias) {
            return new Option<Boolean>(id, Boolean.valueOf(defaultValue), null, alias);
        }

        public static final Option<Boolean> localBool(String id, boolean defaultValue, String... alias) {
            return new Option<Boolean>(id, OptionScope.LOCAL, Boolean.valueOf(defaultValue), null,
                    alias);
        }

        public static final Option<Boolean> globalBool(String id, boolean defaultValue, String... alias) {
            return new Option<Boolean>(id, OptionScope.GLOBAL, Boolean.valueOf(defaultValue), null,
                    alias);
        }

        public static final Option<String> string(String id, String defaultValue, String csv, String... alias) {
            Set<String> legalValues = new HashSet<String>();
            for (String value: csv.split(", *"))
                legalValues.add(value);
            assert legalValues.contains(defaultValue);
            return new Option<String>(id, defaultValue, legalValues, alias);
        }
        
        public static final Option<String> globalString(String id, String defaultValue, String csv, String... alias) {
            Set<String> legalValues = new HashSet<String>();
            for (String value: csv.split(", *"))
                legalValues.add(value);
            assert legalValues.contains(defaultValue);
            return new Option<String>(id, OptionScope.GLOBAL, defaultValue, legalValues, alias);
        }
        
        public static final Option<Set<String>> globalStringSet(String id, String defaultValueStr, String csv, String... alias) {
            Set<String> legalValues = new HashSet<String>();
            for (String value: csv.split(", *"))
                legalValues.add(value);
            Set<String> defaultValues = new HashSet<String>();
            for (String value : defaultValueStr.split(Option.SET_DELIMITER)) {
                if (value.length() > 0) {
                    int valueDelimIndex = value.indexOf(SET_VALUE_ITEM);
                    if (valueDelimIndex != -1) {
                        defaultValues.add(value.substring(0, valueDelimIndex + 1));
                    } else {
                        defaultValues.add(value);
                    }
                }
            }
            assert legalValues.containsAll(defaultValues);
            return new Option<Set<String>>(id, OptionScope.GLOBAL, defaultValues, legalValues, alias);
        }

        public static final Option<String> globalStringNoConstraint(String id, String defaultValue, String... alias) {
            return new Option<String>(id, OptionScope.GLOBAL, defaultValue, null, alias);
        }

        public static final Option<String> stringNoConstraint(String id, String defaultValue, String... alias) {
            return new Option<String>(id, defaultValue, null, alias);
        }

        public static final Option<Integer> integer(String id, int defaultValue, String... alias) {
            return new Option<Integer>(id, defaultValue, null, alias);
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

        public Iterable<String> getAllNames() {
            return allNames;
        }

        /**
         * @return set of all legal values or <code>null</code> if there are no constraints on values of this option
         */
        public Set<String> getLegalValues() {
            return legalValues;
        }

        /** Whether the option is local, default or global scope. */
        public OptionScope getScope() {
            return scope;
        }


        @Override
        public String toString() {
            return String.format("Option(%s)", id);
        }
    }

}