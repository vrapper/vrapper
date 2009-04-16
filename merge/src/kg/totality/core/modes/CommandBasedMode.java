package kg.totality.core.modes;
import static kg.totality.core.commands.BorderPolicy.EXCLUSIVE;
import static kg.totality.core.commands.BorderPolicy.LINE_WISE;
import static kg.totality.core.commands.ConstructorWrappers.go;
import static kg.totality.core.commands.ConstructorWrappers.javaGoTo;
import static kg.totality.core.keymap.vim.ConstructorWrappers.KEY;
import static kg.totality.core.keymap.vim.ConstructorWrappers.key;
import static kg.totality.core.keymap.vim.ConstructorWrappers.leafBind;
import static kg.totality.core.keymap.vim.ConstructorWrappers.state;
import static kg.totality.core.keymap.vim.ConstructorWrappers.transitionBind;
import kg.totality.core.EditorAdaptor;
import kg.totality.core.commands.Command;
import kg.totality.core.commands.EclipseMoveCommand;
import kg.totality.core.commands.MotionCommand;
import kg.totality.core.commands.motions.LineEndMotion;
import kg.totality.core.commands.motions.LineStartMotion;
import kg.totality.core.commands.motions.Motion;
import kg.totality.core.commands.motions.MoveDown;
import kg.totality.core.commands.motions.MoveLeft;
import kg.totality.core.commands.motions.MoveRight;
import kg.totality.core.commands.motions.MoveUp;
import kg.totality.core.commands.motions.MoveWORDEndLeft;
import kg.totality.core.commands.motions.MoveWORDEndRight;
import kg.totality.core.commands.motions.MoveWORDLeft;
import kg.totality.core.commands.motions.MoveWORDRight;
import kg.totality.core.commands.motions.MoveWordEndLeft;
import kg.totality.core.commands.motions.MoveWordEndRight;
import kg.totality.core.commands.motions.MoveWordLeft;
import kg.totality.core.commands.motions.MoveWordRight;
import kg.totality.core.commands.motions.ParenthesesMove;
import kg.totality.core.keymap.SWTKeyStroke;
import kg.totality.core.keymap.State;
import kg.totality.core.keymap.Transition;

import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;

import de.jroene.vrapper.eclipse.VrapperPlugin;

public abstract class CommandBasedMode implements EditorMode, VerifyKeyListener {

	protected final State<Command> initialState;
	protected State<Command> currentState;
	protected boolean isEnabled = false;
	protected final EditorAdaptor editorAdaptor;

	public CommandBasedMode(EditorAdaptor editorAdaptor) {
		this.editorAdaptor = editorAdaptor;
		currentState = initialState = getInitialState();
	}

	protected abstract State<Command> getInitialState();
	protected abstract void placeCursor();

	public State<Motion> motions() {
			final Motion moveLeft = new MoveLeft();
			final Motion moveRight = new MoveRight();
			final Motion moveUp = new MoveUp();
			final Motion moveDown = new MoveDown();
			final Motion findNext = new EclipseMoveCommand("org.eclipse.ui.edit.findNext", EXCLUSIVE);
			final Motion findPrevious = new EclipseMoveCommand("org.eclipse.ui.edit.findPrevious", EXCLUSIVE);
			final Motion wordRight = new MoveWordRight();
			final Motion WORDRight = new MoveWORDRight();
			final Motion wordLeft = new MoveWordLeft();
			final Motion WORDLeft = new MoveWORDLeft();
			final Motion wordEndRight = new MoveWordEndRight();
			final Motion WORDEndRight = new MoveWORDEndRight();
			final Motion wordEndLeft = new MoveWordEndLeft();
			final Motion WORDEndLeft = new MoveWORDEndLeft();
			final Motion eclipseWordRight = go("wordNext", EXCLUSIVE);
			final Motion eclipseWordLeft  = go("wordPrevious", EXCLUSIVE);
			final Motion lineStart = new LineStartMotion(true);
			final Motion column0 = new LineStartMotion(false);
	   		final Motion lineEnd = new LineEndMotion(EXCLUSIVE); // NOTE: it's not INCLUSIVE; bug in Vim documentation
			final Motion parenthesesMove = new ParenthesesMove();

			@SuppressWarnings("unchecked")
			State<Motion> motions = state(
					leafBind('h', moveLeft),
					leafBind('j', moveDown),
					leafBind('k', moveUp),
					leafBind('l', moveRight),
					leafBind(KEY("ARROW_LEFT"),  moveLeft),
					leafBind(KEY("ARROW_DOWN"),  moveDown),
					leafBind(KEY("ARROW_UP"),    moveUp),
					leafBind(KEY("ARROW_RIGHT"), moveRight),
					leafBind('w', wordRight),
					leafBind('W', WORDRight),
					leafBind('e', wordEndRight),
					leafBind('E', WORDEndRight),
					leafBind('b', wordLeft),
					leafBind('B', WORDLeft),
					leafBind('G', go("textEnd",           LINE_WISE)),
					leafBind('n', findNext),
					leafBind('N', findPrevious),
					leafBind('0', column0),
					leafBind(KEY("SHIFT+4"), lineEnd),                    // '$'
					leafBind(KEY("SHIFT+5"), parenthesesMove),            // '%'
					leafBind(KEY("SHIFT+6"), lineStart),                  // '^'
					leafBind(KEY("SHIFT+9"), javaGoTo("previous.member",   LINE_WISE)), // '(' XXX: vim non-compatible; XXX: make java-agnostic
					leafBind(KEY("SHIFT+0"), javaGoTo("next.member",       LINE_WISE)), // ')' XXX: vim non-compatible; XXX: make java-agnostic
//					leafBind(KEY("SHIFT+["), paragraphBackward), // '[' FIXME: doesn't worl
//					leafBind(KEY("SHIFT+]"), paragraphForward),  // ']'
					transitionBind('g',
							leafBind('g', go("textStart", LINE_WISE)),
							leafBind('w', eclipseWordRight),
							leafBind('b', eclipseWordLeft),
							leafBind('e', wordEndLeft),
							leafBind('E', WORDEndLeft)));
			return motions;
		}

	public void executeCommand(Command command) {
		try {
			if (!(command instanceof MotionCommand))
				editorAdaptor.getViewportService().setRepaint(false);
			command.execute(editorAdaptor);
			Command repetition = command.repetition();
			if (repetition != null) {
				editorAdaptor.getRegisterManager().setLastEdit(repetition);
			}
		} finally {
			editorAdaptor.getViewportService().setRepaint(true);
		}
	}


	@Override
	public void verifyKey(VerifyEvent event) {
		if (!event.doit || editorAdaptor == null)
			return;

		SWTKeyStroke keyStroke = key(event);

		if (!keyStroke.isComplete())
			return;

		if (currentState == null) {
			VrapperPlugin.error("current state was null - this shouldn't have happened!");
			currentState = initialState;
		}

		Transition<Command> transition = currentState.press(keyStroke);
		if (transition != null) {
			Command command = transition.getValue();
			currentState = transition.getNextState();
			if (command != null)
				executeCommand(command);
		}
		if (transition == null || currentState == null) {
			currentState = initialState;
			if (isEnabled)
				commandDone();
		}

		// FIXME: has some issues with sticky column
		placeCursor();

		event.doit = false;
	}

	/**
	 * this is a hook method which is called when command execution is done
	 */
	// TODO: better name
	protected void commandDone() { }

	@Override
	public VerifyKeyListener getKeyListener() {
		return this;
	}

}