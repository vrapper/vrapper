package net.sourceforge.vrapper.keymap.vim;

import net.sourceforge.vrapper.keymap.ConvertingState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.SelectionBasedTextObjectCommand;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.commands.TextOperation;
import net.sourceforge.vrapper.vim.commands.TextOperationTextObjectCommand;

class MakeSelectionBasedTextObjectCommand implements Function<Command, TextObject> {

    private final Command operator;

    public MakeSelectionBasedTextObjectCommand(Command operator) {
        this.operator = operator;
    }

    public Command call(TextObject textObject) {
        if (textObject != null) {
            return new SelectionBasedTextObjectCommand(operator, textObject);
        }
        return null;
    }
}


class MakeSimpleTextObjectCommand implements Function<Command, TextObject> {

    private final TextOperation command;

    public MakeSimpleTextObjectCommand(TextOperation command) {
        this.command = command;
    }

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
