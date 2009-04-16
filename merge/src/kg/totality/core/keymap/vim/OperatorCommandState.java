package kg.totality.core.keymap.vim;

import kg.totality.core.commands.Command;
import kg.totality.core.commands.SelectionBasedTextObjectCommand;
import kg.totality.core.commands.TextObject;
import kg.totality.core.commands.TextOperation;
import kg.totality.core.commands.TextOperationTextObjectCommand;
import kg.totality.core.keymap.ConvertingState;
import kg.totality.core.keymap.State;
import kg.totality.core.utils.Function;

class MakeSelectionBasedTextObjectCommand implements Function<Command, TextObject> {

	private final Command operator;

	public MakeSelectionBasedTextObjectCommand(Command operator) {
		this.operator = operator;
	}

	@Override
	public Command call(TextObject textObject) {
		if (textObject != null)
			return new SelectionBasedTextObjectCommand(operator, textObject);
		return null;
	}
}


class MakeSimpleTextObjectCommand implements Function<Command, TextObject> {

	private final TextOperation command;

	public MakeSimpleTextObjectCommand(TextOperation command) {
		this.command = command;
	}

	@Override
	public Command call(TextObject textObject) {
		return new TextOperationTextObjectCommand(command, textObject);
	}

}


public class OperatorCommandState extends ConvertingState<Command, TextObject> {

	public OperatorCommandState(Command operator, State<TextObject> wrapped) {
		super(new MakeSelectionBasedTextObjectCommand(operator), wrapped);
	}

	public OperatorCommandState(TextOperation command, State<TextObject> textObjects) {
		super(new MakeSimpleTextObjectCommand(command), textObjects);
	}
}
