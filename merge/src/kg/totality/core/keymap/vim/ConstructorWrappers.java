package kg.totality.core.keymap.vim;

import static java.lang.Character.isUpperCase;
import static java.lang.Character.toUpperCase;
import static java.util.Arrays.asList;
import static kg.totality.core.commands.BorderPolicy.LINE_WISE;
import static kg.totality.core.keymap.StateUtils.union;
import kg.totality.core.commands.ChangeCaretShapeCommand;
import kg.totality.core.commands.Command;
import kg.totality.core.commands.MotionTextObject;
import kg.totality.core.commands.SelectionBasedTextObjectCommand;
import kg.totality.core.commands.TextObject;
import kg.totality.core.commands.TextOperation;
import kg.totality.core.commands.TextOperationTextObjectCommand;
import kg.totality.core.commands.motions.LineEndMotion;
import kg.totality.core.commands.motions.Motion;
import kg.totality.core.keymap.CovariantState;
import kg.totality.core.keymap.HashMapState;
import kg.totality.core.keymap.KeyBinding;
import kg.totality.core.keymap.KeyStroke;
import kg.totality.core.keymap.SWTKeyStroke;
import kg.totality.core.keymap.SimpleKeyBinding;
import kg.totality.core.keymap.SimpleTransition;
import kg.totality.core.keymap.State;
import kg.totality.core.keymap.Transition;
import kg.totality.core.utils.CaretType;

import org.eclipse.swt.events.KeyEvent;

/**
 * Placeholder for Java-ugliness-hiding static methods intended to be statically imported
 * @author Krzysiek Goj
 */
public class ConstructorWrappers {
	// method name is uppercase, because it's argument is in SWT format (uppercase letters), not Vim one
	public static KeyStroke KEY(String key) {
		return new SWTKeyStroke(key);
	}

	private static String maybeShifted(char key) {
		return (isUpperCase(key) ? "SHIFT+" : "") + toUpperCase(key);
	}

	public static SWTKeyStroke key(char key) {
		return new SWTKeyStroke(maybeShifted(key));
	}


	public static SWTKeyStroke ctrlKey(char key) {
		return new SWTKeyStroke("CTRL+" + maybeShifted(key));
	}

	public static SWTKeyStroke key(KeyEvent event) {
		return new SWTKeyStroke(event);
	}

	public static<T> KeyBinding<T> binding(char k, Transition<T> transition) {
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