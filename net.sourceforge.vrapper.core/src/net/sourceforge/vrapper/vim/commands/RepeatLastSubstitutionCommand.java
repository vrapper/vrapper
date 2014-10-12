package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class RepeatLastSubstitutionCommand extends CountAwareCommand {
	
	public static final RepeatLastSubstitutionCommand CURRENT_LINE_ONLY = new RepeatLastSubstitutionCommand(true);
	public static final RepeatLastSubstitutionCommand GLOBALLY = new RepeatLastSubstitutionCommand(false);
	
	private boolean currentLineOnly;
	
	private RepeatLastSubstitutionCommand(boolean currentLineOnly) {
		this.currentLineOnly = currentLineOnly;
	}

	@Override
	public void execute(EditorAdaptor editorAdaptor, int count)
			throws CommandExecutionException {
		TextOperation substitution = editorAdaptor.getRegisterManager().getLastSubstitution();
		if(substitution == null) {
			//no-op
			return;
		}
		
		Command command;
		if(currentLineOnly) {
			//null TextRange is a special case for "current line"
			command = new TextOperationTextObjectCommand(
				substitution, new DummyTextObject(null)
			);
		}
		else {
    		Position start = editorAdaptor.getCursorService().newPositionForModelOffset( 0 );
    		Position end = editorAdaptor.getCursorService().newPositionForModelOffset( editorAdaptor.getModelContent().getTextLength() );
    		command = new TextOperationTextObjectCommand(
				substitution, new LineWiseSelection(editorAdaptor, start, end)
    		);
		}
		
		command.execute(editorAdaptor);
	}

	@Override
	public CountAwareCommand repetition() {
		return this;
	}

}
