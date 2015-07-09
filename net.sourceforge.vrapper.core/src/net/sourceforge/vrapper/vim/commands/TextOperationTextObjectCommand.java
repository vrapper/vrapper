package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

public class TextOperationTextObjectCommand extends CountAwareCommand {

	private final TextOperation command;
	protected TextObject textObject;

	public TextOperationTextObjectCommand(TextOperation command, TextObject textObject) {
		this.command = command;
		this.textObject = textObject;
	}

	@Override
	public CountAwareCommand repetition() {
		TextOperation wrappedRepetition = command.repetition();
		if (wrappedRepetition != null) {
            return new TextOperationTextObjectCommand(wrappedRepetition, textObject);
        }
		return null;
	}

    @Override
    public void execute(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
        command.execute(editorAdaptor, count, textObject);

        // Reset sticky column for those few operations where it isn't done.
        CursorService cursorService = editorAdaptor.getCursorService();
        cursorService.setPosition(cursorService.getPosition(), StickyColumnPolicy.ON_CHANGE);
    }

    @Override
    public Command withCount(int count) {
        return new MultiplicableCountedCommand(count, this);
    }
}
