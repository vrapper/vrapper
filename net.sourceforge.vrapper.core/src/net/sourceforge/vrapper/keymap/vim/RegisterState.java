package net.sourceforge.vrapper.keymap.vim;

import java.util.Collection;
import java.util.HashSet;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SimpleTransition;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.Transition;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.SwitchRegisterCommand;

public class RegisterState implements State<Command> {

    private final State<Command> wrappedState;
    private final RegisterSelectState selectState;

    public RegisterState(State<Command> wrappedState) {
        super();
        this.wrappedState = wrappedState;
        selectState = new RegisterSelectState();
    }

    public Transition<Command> press(KeyStroke key) {
        if ('"' == key.getCharacter() && (key.getModifiers() & KeyStroke.CTRL) == 0) {
            return new SimpleTransition<Command>(selectState);
        }
        return wrappedState.press(key);
    }

    public Iterable<KeyStroke> supportedKeys() {
        Collection<KeyStroke> result = new HashSet<KeyStroke>();
        result.add(new SimpleKeyStroke(0, '"'));
        for (KeyStroke key : wrappedState.supportedKeys()) {
            result.add(key);
        }
        return result;
    }

    public State<Command> union(State<Command> other) {
        return new RegisterState(wrappedState.union(other));
    }

    public class RegisterSelectState implements State<Command> {

        public Transition<Command> press(KeyStroke key) {
            return new SimpleTransition<Command>(
                    new SwitchRegisterCommand(key.getCharacter()),
                    RegisterState.this);

        }

        public Iterable<KeyStroke> supportedKeys() {
            throw new UnsupportedOperationException("not implemented");
        }

        public State<Command> union(State<Command> other) {
            throw new UnsupportedOperationException("not implemented");
        }

    }
}
