package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.LineRange;
import net.sourceforge.vrapper.utils.SimpleLineRange;
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
		SubstitutionOperation substitution = editorAdaptor.getRegisterManager().getLastSubstitution();
		if(substitution == null) {
			//no-op
			return;
		}
		
		LineRange range;
		if(currentLineOnly) {
			range = SimpleLineRange.singleLine(editorAdaptor, editorAdaptor.getPosition());
		}
		else {
			range = SimpleLineRange.entireFile(editorAdaptor);
		}
		
		substitution.execute(editorAdaptor, range);
	}

	@Override
	public CountAwareCommand repetition() {
		return this;
	}
}
