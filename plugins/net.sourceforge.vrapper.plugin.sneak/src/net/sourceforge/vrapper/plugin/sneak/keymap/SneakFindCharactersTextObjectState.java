package net.sourceforge.vrapper.plugin.sneak.keymap;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SimpleTransition;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.Transition;
import net.sourceforge.vrapper.plugin.sneak.commands.SneakTextObject;
import net.sourceforge.vrapper.plugin.sneak.modes.SneakInputMode;
import net.sourceforge.vrapper.vim.commands.TextObject;

/**
 * This special state replaces the input logic of {@link SneakInputMode} for when we can't easily
 * switch modes, e.g. when invoking an operator.
 */
public class SneakFindCharactersTextObjectState implements State<TextObject> {

    public static State<TextObject> forwards(int count) {
        return new SneakFindCharactersTextObjectState(false, count, "");
    }

    public static State<TextObject> backwards(int count) {
        return new SneakFindCharactersTextObjectState(true, count, "");
    }

    private final boolean sneakBackwards;
    private final int charactersNeeded;
    private final String characters;

    public SneakFindCharactersTextObjectState(boolean sneakBackwards, int charactersNeeded, String characters) {
        this.sneakBackwards = sneakBackwards;
        this.charactersNeeded = charactersNeeded;
        this.characters = characters;
    }

    @Override
    public Transition<TextObject> press(KeyStroke stroke) {
        if (stroke.getSpecialKey() == SpecialKey.RETURN) {
            return new SimpleTransition<TextObject>(
                    new SneakTextObject(SneakInputMode.STATEMANAGER, characters, sneakBackwards));

        // [NOTE] We might enter this else when using AltGr on Windows, ignoring user input
        } else if (stroke.getCharacter() == KeyStroke.SPECIAL_KEY || stroke.withCtrlKey()) {
            return null;
        }
        String newCharacters = characters + stroke.getCharacter();
        if (charactersNeeded <= 1) {
            return new SimpleTransition<TextObject>(
                    new SneakTextObject(SneakInputMode.STATEMANAGER, newCharacters, sneakBackwards));
        } else {
            return new SimpleTransition<TextObject>(new SneakFindCharactersTextObjectState(sneakBackwards,
                    charactersNeeded - 1, newCharacters));
        }
    }

    @Override
    public State<TextObject> union(State<TextObject> other) {
        throw new UnsupportedOperationException();
    }
}
