package kg.totality.core.modes;

import static java.lang.Math.min;
import static kg.totality.core.commands.ConstructorWrappers.cmd;
import static kg.totality.core.commands.ConstructorWrappers.dontRepeat;
import static kg.totality.core.commands.ConstructorWrappers.edit;
import static kg.totality.core.commands.ConstructorWrappers.editText;
import static kg.totality.core.commands.ConstructorWrappers.go;
import static kg.totality.core.commands.ConstructorWrappers.javaEditText;
import static kg.totality.core.commands.ConstructorWrappers.seq;
import static kg.totality.core.keymap.StateUtils.union;
import static kg.totality.core.keymap.vim.ConstructorWrappers.KEY;
import static kg.totality.core.keymap.vim.ConstructorWrappers.leafBind;
import static kg.totality.core.keymap.vim.ConstructorWrappers.leafCtrlBind;
import static kg.totality.core.keymap.vim.ConstructorWrappers.operatorCmds;
import static kg.totality.core.keymap.vim.ConstructorWrappers.prefixedOperatorCmds;
import static kg.totality.core.keymap.vim.ConstructorWrappers.operatorCmdsWithUpperCase;
import static kg.totality.core.keymap.vim.ConstructorWrappers.state;
import static kg.totality.core.keymap.vim.ConstructorWrappers.transitionBind;
import static kg.totality.core.keymap.vim.GoThereState.motion2command;
import kg.totality.core.EditorAdaptor;
import kg.totality.core.Options;
import kg.totality.core.commands.BorderPolicy;
import kg.totality.core.commands.ChangeModeCommand;
import kg.totality.core.commands.ChangeOperation;
import kg.totality.core.commands.ChangeToInsertModeCommand;
import kg.totality.core.commands.Command;
import kg.totality.core.commands.CountIgnoringNonRepeatableCommand;
import kg.totality.core.commands.DeleteOperation;
import kg.totality.core.commands.DotCommand;
import kg.totality.core.commands.MotionPairTextObject;
import kg.totality.core.commands.MotionTextObject;
import kg.totality.core.commands.OptionDependentTextObject;
import kg.totality.core.commands.PasteAfterCommand;
import kg.totality.core.commands.PasteBeforeCommand;
import kg.totality.core.commands.RedoCommand;
import kg.totality.core.commands.StickToEOLCommand;
import kg.totality.core.commands.TextObject;
import kg.totality.core.commands.TextOperation;
import kg.totality.core.commands.TextOperationTextObjectCommand;
import kg.totality.core.commands.UndoCommand;
import kg.totality.core.commands.YankOperation;
import kg.totality.core.commands.motions.LineEndMotion;
import kg.totality.core.commands.motions.LineStartMotion;
import kg.totality.core.commands.motions.Motion;
import kg.totality.core.commands.motions.MoveLeft;
import kg.totality.core.commands.motions.MoveRight;
import kg.totality.core.commands.motions.MoveWordEndRight;
import kg.totality.core.commands.motions.MoveWordLeft;
import kg.totality.core.commands.motions.MoveWordRight;
import kg.totality.core.keymap.State;
import kg.totality.core.keymap.vim.CountingState;
import kg.totality.core.keymap.vim.GoThereState;
import kg.totality.core.keymap.vim.TextObjectState;
import kg.totality.core.utils.CaretType;
import newpackage.glue.TextContent;

public class NormalMode extends CommandBasedMode {

	public static final String NAME = "normal mode";
	public NormalMode(EditorAdaptor editorAdaptor) {
		super(editorAdaptor);
	}

	@Override
	protected State<Command> getInitialState() {
		Command visualMode = new ChangeModeCommand(VisualMode.NAME);

		Command deselectAll = new CountIgnoringNonRepeatableCommand() {
			@Override public void execute(EditorAdaptor editorMode) {
				editorAdaptor.setPosition(editorAdaptor.getSelection().getEnd(), true);
			}
		};

		final Motion moveLeft = new MoveLeft();
		final Motion moveRight = new MoveRight();
		final Motion wordRight = new MoveWordRight();
		final Motion wordLeft = new MoveWordLeft();
		final Motion wordEndRight = new MoveWordEndRight();
		final Motion bol = new LineStartMotion(true);
		final Motion eol = new LineEndMotion(BorderPolicy.EXCLUSIVE);

		final State<Motion> motions = motions();
		final TextObject innerWord = new MotionPairTextObject(wordLeft, wordEndRight);
		final TextObject aWord = new MotionPairTextObject(wordLeft, wordRight);
		final TextObject wordForCW = new OptionDependentTextObject(Options.STUPID_CW, wordEndRight, wordRight);
		final TextObject toEol = new MotionTextObject(eol);
		final TextObject wholeLine = new MotionTextObject(new LineEndMotion(BorderPolicy.LINE_WISE));
		final TextObject toEolForY = new OptionDependentTextObject(Options.STUPID_Y, wholeLine, toEol);

		@SuppressWarnings("unchecked")
		State<TextObject> textObjects = union(
				state(
					transitionBind('i',
						leafBind('w', innerWord)),
					transitionBind('a',
								leafBind('w', aWord))),
				new TextObjectState(motions));

		@SuppressWarnings("unchecked")
		State<TextObject> textObjectsForChange = CountingState.wrap(union(state(leafBind('w', wordForCW)), textObjects));

		textObjects = CountingState.wrap(textObjects);

		TextOperation delete = new DeleteOperation();
		TextOperation change = new ChangeOperation();
		TextOperation yank   = new YankOperation();
		Command undo = new UndoCommand();
		Command redo = new RedoCommand();
		Command pasteAfter  = new PasteAfterCommand();
		Command pasteBefore = new PasteBeforeCommand();
		Command deleteNext = new TextOperationTextObjectCommand(delete, new MotionTextObject(moveRight));
		Command deletePrevious = seq(motion2command(moveLeft), deleteNext); // FIXME: should do nothing when on first character of buffer
		Command repeatLastOne = new DotCommand();
		Command stickToEOLL = new StickToEOLCommand();

		State<Command> motionCommands = new GoThereState(motions);

		@SuppressWarnings("unchecked")
		State<Command> commands = CountingState.wrap(union(
				operatorCmdsWithUpperCase('d', delete, toEol,     textObjects),
				operatorCmdsWithUpperCase('y', yank,   toEolForY, textObjects),
				operatorCmdsWithUpperCase('c', change, toEol,     textObjectsForChange),
				operatorCmds('=', seq(javaEditText("indent"), deselectAll), textObjects),
				prefixedOperatorCmds('g', 'c', seq(javaEditText("toggle.comment"), deselectAll), textObjects),
				prefixedOperatorCmds('g', 'u', seq(editText("lowerCase"), deselectAll), textObjects),
				prefixedOperatorCmds('g', 'U', seq(editText("upperCase"), deselectAll), textObjects),
				state(leafBind(KEY("SHIFT+4"), stickToEOLL)),
				motionCommands,
				state(
						leafBind('i', (Command) new ChangeToInsertModeCommand()),
						leafBind('a', (Command) new ChangeToInsertModeCommand(moveRight)),
						leafBind('I', (Command) new ChangeToInsertModeCommand(bol)),
						leafBind('A', (Command) new ChangeToInsertModeCommand(eol)),
						leafBind('o', seq(new ChangeToInsertModeCommand(), editText("smartEnter"))), // FIXME: use Vrapper's code; repetition
						leafBind('O', seq(new ChangeToInsertModeCommand(), editText("smartEnterInverse"))), // FIXME: use Vrapper's code; repetition
						leafBind('v', visualMode),
						leafBind('p', pasteAfter),
						leafBind('.', repeatLastOne),
						leafBind('P', pasteBefore),
						leafBind('J', (Command) editText("join.lines")),
						leafBind('x', deleteNext),
						leafBind('X', deletePrevious),
						leafBind('s', seq(deleteNext, new ChangeToInsertModeCommand())), // FIXME: this should be compound edit
						transitionBind('z',
								leafBind('o', dontRepeat(editText("folding.expand"))),
								leafBind('R', dontRepeat(editText("folding.expand_all"))),
								leafBind('c', dontRepeat(editText("folding.collapse"))),
								leafBind('M', dontRepeat(editText("folding.collapse_all")))),
						transitionBind('g',
								leafBind('r', javaEditText("refactor.quickMenu")),
								leafBind('R', javaEditText("rename.element")),
								leafBind('t', cmd("org.eclipse.ui.window.nextEditor")),
								leafBind('T', cmd("org.eclipse.ui.window.previousEditor"))),
						leafBind('/', dontRepeat(edit("findIncremental"))),
						leafBind('?', dontRepeat(edit("findIncrementalReverse"))),
						leafBind('u', undo),
						leafCtrlBind('r', redo),
						leafCtrlBind('b', go("goto.pageUp")),
						leafCtrlBind('f', go("goto.pageDown")),
						leafCtrlBind('y', dontRepeat(editText("scroll.lineUp"))),
						leafCtrlBind('e', dontRepeat(editText("scroll.lineDown"))),
						leafCtrlBind(']', seq(javaEditText("open.editor"), deselectAll)), // NOTE: deselect won't work in other editor
						leafCtrlBind('i', dontRepeat(cmd("org.eclipse.ui.navigate.forwardHistory"))),
						leafCtrlBind('o', dontRepeat(cmd("org.eclipse.ui.navigate.backwardHistory"))))));

		return commands;
	}

	@Override
	protected void placeCursor() {
		TextContent content = editorAdaptor.getViewContent();
		int checkFrom = editorAdaptor.getPosition().getViewOffset() - 1;
		int checkTo   = min(checkFrom + 2, content.getTextLength());
		if (isEnabled && checkFrom >= 0) {
			String around = content.getText(checkFrom, checkTo - checkFrom);
			if (!around.startsWith("\n") && (around.endsWith("\n") || around.length() == 1))
				editorAdaptor.setPosition(editorAdaptor.getPosition().addViewOffset(-1), false);
		}
	}

	@Override
	protected void commandDone() {
		super.commandDone();
		editorAdaptor.getCursorService().setCaret(CaretType.RECTANGULAR);
	}

	@Override
	public void enterMode() {
		if (isEnabled) return;
		isEnabled = true;
		placeCursor();
		editorAdaptor.getCursorService().setCaret(CaretType.RECTANGULAR);
	}

	@Override
	public void leaveMode() {
		if (!isEnabled) return;
		isEnabled = false;
	}

	@Override
	public String getName() {
		return NAME;
	}

}
