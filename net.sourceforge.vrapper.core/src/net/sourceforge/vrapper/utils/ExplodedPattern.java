package net.sourceforge.vrapper.utils;

import java.util.Iterator;
import java.util.List;

/**
 * Stores a search or replace pattern as distinct character strings to make it easier to scan for
 * flags and special characters.
 */
public class ExplodedPattern implements Iterable<String> {
    protected List<String> contents;
    protected String cachedResult;

    public ExplodedPattern(List<String> contents) {
        this.contents = contents;
    }

    public StringBuilder appendTo(StringBuilder output) {
        if (cachedResult == null) {
            for (String charStr : contents) {
                output.append(charStr);
            }
        } else {
            output.append(cachedResult);
        }
        return output;
    }

    public String toString() {
        if (cachedResult == null) {
            StringBuilder output = new StringBuilder();
            for (String charStr : contents) {
                output.append(charStr);
            }
            cachedResult = output.toString();
        }
        return cachedResult;
    }

    public boolean containsUppercase() {
        for (String charStr : contents) {
            // Control characters and flags are ignored.
            if (charStr.length() == 1 && Character.isUpperCase(charStr.charAt(0))) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(String flagOrChar) {
        return contents.contains(flagOrChar);
    }

    public boolean replace(String flagOrChar, String target) {
        boolean result = false;
        if (target == null || target.length() == 0) {
            return removeAll(flagOrChar);
        } else {
            for (int i = 0; i < contents.size(); i++) {
                if (contents.get(i).equals(flagOrChar)) {
                    contents.set(i, target);
                    result = true;
                }
            }
            cachedResult = null;
            return result;
        }
    }

    public boolean removeAll(String flagOrChar) {
        boolean result = false;
        for (int i = 0; i < contents.size(); i++) {
            if (contents.get(i).equals(flagOrChar)) {
                contents.remove(i);
                result = true;
                i--;
            }
        }
        cachedResult = null;
        return result;
    }

    @Override
    public Iterator<String> iterator() {
        return contents.iterator();
    }
}
