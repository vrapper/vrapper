package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;

public class SelectionBasedTextOperationCommand extends CountAwareCommand {

	protected final TextOperation command;
	protected final boolean changeMode;

    public SelectionBasedTextOperationCommand(TextOperation command) {
    	this(command, true);
    }

	protected SelectionBasedTextOperationCommand(TextOperation command, boolean leavesVisualMode) {
		this.command = command;
		this.changeMode = leavesVisualMode;
	}

	@Override
	public void execute(EditorAdaptor editorAdaptor, int count)
			throws CommandExecutionException {
		editorAdaptor.rememberLastActiveSelection();
		TextObject selection = editorAdaptor.getSelection();
		command.execute(editorAdaptor, count, selection);
		if (changeMode)
			LeaveVisualModeCommand.doIt(editorAdaptor);
	}

	public CountAwareCommand repetition() {
        TextOperation wrappedRepetition = command.repetition();
        if (wrappedRepetition != null) {
            return new Repetition(wrappedRepetition);
        }
        return null;
    }
	
	/** A variant of SelectionBasedTextOperation that doesn't change mode. */
	public static class DontChangeMode extends SelectionBasedTextOperationCommand {
		public DontChangeMode(TextOperation command) {
			super(command, false);
		}
	}
	
	/** Repetition of SelectionBasedTextOperation */
	public class Repetition extends CountAwareCommand {
		
		private TextOperation repetition;
		
		public Repetition(TextOperation repeat) {
			this.repetition = repeat;
		}

		@Override
		public void execute(EditorAdaptor editorAdaptor, int count)
				throws CommandExecutionException {
			repetition.execute(editorAdaptor, count, editorAdaptor.getLastActiveSelection());
			if (changeMode)
				LeaveVisualModeCommand.doIt(editorAdaptor);
		}

		@Override
		public CountAwareCommand repetition() {
			return this;
		}

	}



}
