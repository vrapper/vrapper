package net.sourceforge.vrapper.keymap.vim;

import static java.util.Arrays.asList;
import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.vim.commands.BorderPolicy.LINE_WISE;
import net.sourceforge.vrapper.keymap.CovariantState;
import net.sourceforge.vrapper.keymap.HashMapState;
import net.sourceforge.vrapper.keymap.KeyBinding;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SimpleKeyBinding;
import net.sourceforge.vrapper.keymap.SimpleTransition;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.Transition;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.vim.commands.ChangeCaretShapeCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.MotionTextObject;
import net.sourceforge.vrapper.vim.commands.SelectionBasedTextObjectCommand;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.commands.TextOperation;
import net.sourceforge.vrapper.vim.commands.TextOperationTextObjectCommand;
import net.sourceforge.vrapper.vim.commands.motions.LineEndMotion;
import net.sourceforge.vrapper.vim.commands.motions.Motion;

/**
 * Placeholder for Java-ugliness-hiding static methods intended to be statically imported
 * @author Krzysiek Goj
 */
public class ConstructorWrappers {
	public static KeyStroke key(int modifiers, char key) {
		return new SimpleKeyStroke(modifiers, key);
	}

	public static KeyStroke key(int modifiers, SpecialKey key) {
		return new SimpleKeyStroke(modifiers, key);
	}

	private static int maybeShift(char key) {
		int modifiers = 0;
		String autoShifted = "~!@#$%^&*()_+{}:\"|<>?";
		if (Character.isUpperCase(key) || autoShifted.indexOf(key) != -1)
			modifiers |= KeyStroke.SHIFT;
		return modifiers;
	}

	public static KeyStroke key(char key) {
		return new SimpleKeyStroke(maybeShift(key), key);
	}

	public static KeyStroke ctrlKey(char key) {
		return new SimpleKeyStroke(maybeShift(key) | KeyStroke.CTRL, key);
	}

	public static KeyStroke key(SpecialKey key) {
		return new SimpleKeyStroke(0, key);
	}

	public static<T> KeyBinding<T> binding(char k, Transition<T> transition) {
		return new SimpleKeyBinding<T>(key(k), transition);
	}

	public static<T> KeyBinding<T> binding(SpecialKey k, Transition<T> transition) {
		return new SimpleKeyBinding<T>(key(k), transition);
	}

	public static<T> KeyBinding<T> binding(KeyStroke stroke, Transition<T> transition) {
		return new SimpleKeyBinding<T>(stroke, transition);
	}

	public static<T> Transition<T> leaf(T value) {
		return new SimpleTransition<T>(value);
	}

	public static<T> Transition<T> transition(State<T> state) {
		return new SimpleTransition<T>(state);
	}

	public static<T> Transition<T> transition(T value, State<T> state) {
		return new SimpleTransition<T>(value, state);
	}

	public static<T> State<T> state(KeyBinding<T>... bindings) {
		return new HashMapState<T>(asList(bindings));
	}

	public static<T> KeyBinding<T> leafBind(KeyStroke k, T value) {
		return binding(k, leaf(value));
	}

	public static<T> KeyBinding<T> leafBind(char k, T value) {
		return binding(k, leaf(value));
	}

	public static<T> KeyBinding<T> leafBind(SpecialKey k, T value) {
		return binding(k, leaf(value));
	}

	public static<T> KeyBinding<T> leafCtrlBind(char k, T value) {
		return binding(ctrlKey(k), leaf(value));
	}

	public static<T> KeyBinding<T> transitionBind(char k, State<T> state) {
		return binding(k, transition(state));
	}

	public static<T> KeyBinding<T> transitionBind(char k, T value, State<T> state) {
		return binding(k, transition(value, state));
	}

	public static<T> KeyBinding<T> transitionBind(char k, KeyBinding<T>... bindings) {
		return binding(k, transition(state(bindings)));
	}

	@SuppressWarnings("unchecked")
	public static<T> State<T> leafState(char k, T value) {
		return state(leafBind(k, value));
	}

	@SuppressWarnings("unchecked")
	public static<T> State<T> transitionState(char k, State<T> state) {
		return state(transitionBind(k, state));
	}

	public static SelectionBasedTextObjectCommand operatorMoveCmd(Command operator, Motion move) {
		return new SelectionBasedTextObjectCommand(operator, new MotionTextObject(move));
	}

	public static State<Command> counted(State<Command> wrapped) {
		return CountingState.wrap(wrapped);
	}

	@SuppressWarnings("unchecked")
	private static State<Command> operatorPendingState(char key,
			State<Command> doubleKey, State<Command> operatorCmds) {
		return state(binding(key,
				transition(new ChangeCaretShapeCommand(CaretType.HALF_RECT),
				counted(union(doubleKey, operatorCmds)))));
	}

	@SuppressWarnings("unchecked")
	public static State<Command> operatorCmdsWithUpperCase(char key, TextOperation command, TextObject eolMotion, State<TextObject> textObjects) {
		assert Character.isLowerCase(key);
		Command doToEOL = new TextOperationTextObjectCommand(command, eolMotion);
		return union(
				counted(leafState(Character.toUpperCase(key), doToEOL)),
				operatorCmds(key, command, textObjects));
	}

	public static State<Command> operatorCmds(char key, TextOperation command, State<TextObject> textObjects) {
		LineEndMotion lineEndMotion = new LineEndMotion(LINE_WISE);
		Command doLinewise = new TextOperationTextObjectCommand(command, new MotionTextObject(lineEndMotion));
		State<Command> doubleKey = leafState(key, doLinewise);
		State<Command> operatorCmds = new OperatorCommandState(command, textObjects);
		return operatorPendingState(key, doubleKey, operatorCmds);
	}

	public static State<Command> operatorCmds(char key, Command operator, State<TextObject> textObjects) {
		Command doLinewise = operatorMoveCmd(operator, new LineEndMotion(LINE_WISE));
		State<Command> doubleKey = leafState(key, doLinewise);
		State<Command> operatorCmds = new OperatorCommandState(operator, textObjects);
		return operatorPendingState(key, doubleKey, operatorCmds);
	}

	public static State<Command> prefixedOperatorCmds(char prefix, char key, Command operator, State<TextObject> textObjects) {
		Command doLinewise = operatorMoveCmd(operator, new LineEndMotion(LINE_WISE));
		@SuppressWarnings("unchecked")
		State<Command> doubleKey = state(
				leafBind(key, doLinewise), // e.g. for 'g??'
				transitionBind(prefix, leafBind(key, doLinewise))); // e.g. for 'g?g?'
		State<Command> operatorCmds = new OperatorCommandState(operator, textObjects);
		return transitionState(prefix, operatorPendingState(key, doubleKey, operatorCmds));
	}

	public static<T1, T2 extends T1> State<T1> covariant(State<T2> wrapped) {
		return new CovariantState<T1, T2>(wrapped);
	}
}