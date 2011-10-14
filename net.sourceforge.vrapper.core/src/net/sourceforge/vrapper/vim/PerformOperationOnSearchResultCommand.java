package net.sourceforge.vrapper.vim;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountAwareCommand;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.commands.TextOperation;
import net.sourceforge.vrapper.vim.commands.motions.SearchResultMotion;
import net.sourceforge.vrapper.vim.modes.commandline.SearchMode.Direction;

public class PerformOperationOnSearchResultCommand extends CountAwareCommand {
    private TextOperation operation;
    private Direction searchDirection;
    
    public PerformOperationOnSearchResultCommand(TextOperation operation, Direction searchDirection) {
        this.operation = operation;
        this.searchDirection = searchDirection;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
        SearchResultMotion search = (searchDirection.equals(Direction.FORWARD)) ? SearchResultMotion.FORWARD : SearchResultMotion.BACKWARD;
        Position end = search.destination(editorAdaptor, count);
        Position current = editorAdaptor.getPosition();
        TextObject searchResult = null; //somehow create a TextObject from 'current' to 'end'
        operation.execute(editorAdaptor, count, searchResult);
    }

    @Override
    public CountAwareCommand repetition() {
        return null;
    }

}
