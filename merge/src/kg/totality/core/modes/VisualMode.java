package kg.totality.core.modes;

import static kg.totality.core.commands.ConstructorWrappers.dontRepeat;
import static kg.totality.core.commands.ConstructorWrappers.editText;
import static kg.totality.core.commands.ConstructorWrappers.seq;
import static kg.totality.core.keymap.StateUtils.union;
import static kg.totality.core.keymap.vim.ConstructorWrappers.KEY;
import static kg.totality.core.keymap.vim.ConstructorWrappers.leafBind;
import static kg.totality.core.keymap.vim.ConstructorWrappers.state;
import static kg.totality.core.keymap.vim.ConstructorWrappers.transitionBind;
import kg.totality.core.EditorAdaptor;
import kg.totality.core.commands.ChangeOperation;
import kg.totality.core.commands.Command;
import kg.totality.core.commands.DeleteOperation;
import kg.totality.core.commands.LeaveVisualModeCommand;
import kg.totality.core.commands.SelectionBasedTextOperation;
import kg.totality.core.commands.SwapSelectionSidesCommand;
import kg.totality.core.commands.YankOperation;
import kg.totality.core.keymap.State;
import kg.totality.core.keymap.vim.CountingState;
import kg.totality.core.keymap.vim.VisualMotionState;
import kg.totality.core.utils.CaretType;
import newpackage.position.Position;


public class VisualMode extends CommandBasedMode {

	public static final String NAME = "visual mode";

	public VisualMode(EditorAdaptor editorAdaptor) {
		super(editorAdaptor);
	}

	@Override protected void placeCursor() {
		if (!isEnabled) {
			Position leftSidePosition = editorAdaptor.getSelection().getLeftBound();
			editorAdaptor.setPosition(leftSidePosition, false);
		}
	}

	@Override public void enterMode() {
		if (isEnabled) return;
		isEnabled = true;
		editorAdaptor.getCursorService().setCaret(CaretType.STANDARD);
	}

	@Override public void leaveMode() {
		isEnabled = false;
		editorAdaptor.setSelection(null);
	}

	@Override
	protected State<Command> getInitialState() {
		Command leaveVisual = new LeaveVisualModeCommand();
		Command swapSides = new SwapSelectionSidesCommand();
		Command yank   = dontRepeat(seq(new SelectionBasedTextOperation(new YankOperation()), leaveVisual));
		Command delete = dontRepeat(seq(new SelectionBasedTextOperation(new DeleteOperation()), leaveVisual));
		Command change = new SelectionBasedTextOperation(new ChangeOperation());
		State<Command> visualMotions = new VisualMotionState(motions());
		@SuppressWarnings("unchecked")
		State<Command> commands = CountingState.wrap(union(state(
				leafBind(KEY("CTRL+["), leaveVisual),
				leafBind(KEY("Esc"), leaveVisual),
				leafBind('v', leaveVisual),
				leafBind('y', yank),
				leafBind('s', change),
				leafBind('c', change),
				leafBind('d', delete),
				leafBind('x', delete),
				leafBind('X', delete),
				leafBind('o', swapSides),
				transitionBind('g',
						leafBind('c', seq(editText("toggle.comment"), leaveVisual)),
						leafBind('U', seq(editText("upperCase"),      leaveVisual)),
						leafBind('u', seq(editText("lowerCase"),      leaveVisual)))
		), visualMotions));
		return commands;
	}

	@Override
	public String getName() {
		return NAME;
	}

}