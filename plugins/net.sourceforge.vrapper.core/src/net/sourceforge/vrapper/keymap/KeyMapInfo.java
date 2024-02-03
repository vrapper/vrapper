package net.sourceforge.vrapper.keymap;

/**
 * The KeyMapInfo class tells which keymap should be activated and what group of keys were used to
 * activate it.
 */
public class KeyMapInfo {

    private final String keyMapName;
    private final String keyGroup;

    public KeyMapInfo(String keyMapName, String keyGroup) {
        if (keyMapName == null) throw new NullPointerException("keyMapName was null!");
        if (keyGroup == null) throw new NullPointerException("keyGroup was null!");

        this.keyMapName = keyMapName;
        this.keyGroup = keyGroup;
    }

    /** Currently active keymap. Cannot be null. */
    public String getKeyMapName() {
        return keyMapName;
    }

    /**
     * Get the active key group, used for groups of which the length can not be predicted.
     * Should never be null, and the value should not matter - only equality.
     * <p>
     * Currently only used for the CountConsumingState to know if we encountered a second digit.
     */
    public Object getKeyGroupId() {
        return keyGroup;
    }
    
    public String toString() {
        return "KMI[map=" + keyMapName + ",group=" + keyGroup + "]";
    }
}
