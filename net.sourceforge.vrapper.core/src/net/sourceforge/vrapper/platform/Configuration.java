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

    public static class Option<T> {

        private final String id;
        private final String[] alias;
        private final T defaultValue;
        private final List<String> allNames;
        private final Set<T> legalValues;

        private Option(String id, T defaultValue, Set<T> legalValues, String...alias) {
            this.id = id;
            this.defaultValue = defaultValue;
            this.legalValues = legalValues;
            this.alias = alias;
            this.allNames = new ArrayList<String>();
            allNames.add(id);
            allNames.addAll(Arrays.asList(alias));
        }

        public static final Option<Boolean> bool(String id, boolean defaultValue, String... alias) {
            return new Option<Boolean>(id, Boolean.valueOf(defaultValue), null, alias);
        }

        public static final Option<String> string(String id, String defaultValue, String csv, String... alias) {
            Set<String> legalValues = new HashSet<String>();
            for (String value: csv.split(", *"))
                legalValues.add(value);
            assert legalValues.contains(defaultValue);
            return new Option<String>(id, defaultValue, legalValues, alias);
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
        public Set<T> getLegalValues() {
            return legalValues;
        }


        @Override
        public String toString() {
            return String.format("Option(%s)", id);
        }
    }

}