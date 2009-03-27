package de.jroene.vrapper.vim;

/**
 * Represents some input to be evaluated by the vim emulator.
 *
 * @author Matthias Radig
 */
public class VimInputEvent {

    /**
     * Special instance representing the escape key
     */
    public static final VimInputEvent ESCAPE = new VimInputEvent();
    public static final VimInputEvent ARROW_LEFT = new VimInputEvent();
    public static final VimInputEvent ARROW_RIGHT = new VimInputEvent();
    public static final VimInputEvent ARROW_DOWN = new VimInputEvent();
    public static final VimInputEvent ARROW_UP = new VimInputEvent();
    public static final VimInputEvent RETURN = new VimInputEvent();
    public static final VimInputEvent HOME = new VimInputEvent();
    public static final VimInputEvent END = new VimInputEvent();
    public static final VimInputEvent PAGE_UP = new VimInputEvent();
    public static final VimInputEvent PAGE_DOWN = new VimInputEvent();
    public static final VimInputEvent INSERT = new VimInputEvent();
    public static final VimInputEvent DELETE = new VimInputEvent();
    public static final VimInputEvent BACKSPACE = new VimInputEvent();

    /**
     * Wraps a typed character.
     *
     * @author Matthias Radig
     */
    public static class Character extends VimInputEvent {

        private final char character;

        public Character(char character) {
            super();
            this.character = character;
        }

        public char getCharacter() {
            return character;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof Character) {
                Character vie = (Character) other;
                return character == vie.character;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return character * 37 * 57;
        }
    }
}
