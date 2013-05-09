package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.HistoryService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection.Rect;

public class SelectionBasedTextOperationCommand extends CountAwareCommand {

	public static class BlockwiseRepeatCommand implements Command {
	    
	    private final TextOperation command;
        private final int count;

        public BlockwiseRepeatCommand(final TextOperation command, final int count) {
	        this.command = command;
	        this.count = count;
	    }

        @Override
        public Command repetition() {
            return this;
        }

        @Override
        public Command withCount(final int count) {
            return this;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public void execute(final EditorAdaptor editorAdaptor)
                throws CommandExecutionException {
            
            final TextContent textContent = editorAdaptor.getModelContent();
            final Selection selection = editorAdaptor.getSelection();
		    final Rect rect = BlockWiseSelection.getRect(textContent, selection);
		    
            doIt(editorAdaptor, command, getCount(), rect);
        }
        
        public static void doIt(final EditorAdaptor editorAdaptor, final TextOperation command, final int count, final Rect rect) 
                throws CommandExecutionException {
		    final int height = rect.height();
		    final int width = rect.width();
    		final TextOperation repetition = command.repetition();
		    for (int i=1; i < height; i++) {
		        rect.top++;
		        final Position newUl = rect.getULPosition(editorAdaptor);
		        final TextObject nextLine = newSelection(newUl, width);
		        repetition.execute(editorAdaptor, count, nextLine);
		    }
        }

    }

    protected final TextOperation command;
	protected final boolean changeMode;

    public SelectionBasedTextOperationCommand(final TextOperation command) {
    	this(command, true);
    }

	protected SelectionBasedTextOperationCommand(final TextOperation command, final boolean leavesVisualMode) {
		this.command = command;
		this.changeMode = leavesVisualMode;
	}

	@Override
	public void execute(final EditorAdaptor editorAdaptor, final int count)
			throws CommandExecutionException {
		editorAdaptor.rememberLastActiveSelection();
		final TextContent textContent = editorAdaptor.getModelContent();
		final TextObject selection = editorAdaptor.getSelection();
		if (selection.getContentType(editorAdaptor.getConfiguration()) == ContentType.TEXT_RECTANGLE) {
		    final Rect rect = BlockWiseSelection.getRect(textContent, (Selection) selection);
		    final int width = rect.width();
		    final Position ul = rect.getULPosition(editorAdaptor);
		    final TextObject firstLine = newSelection(ul, width);
		    
		    final HistoryService history = editorAdaptor.getHistory();
		    history.beginCompoundChange();
		    history.lock("block-action");
		    
    		command.execute(editorAdaptor, count, firstLine);
    		
    		if (changeMode) {
    		    BlockwiseRepeatCommand.doIt(editorAdaptor, command, count, rect);
    		}
    		
		    history.unlock("block-action");
    		history.endCompoundChange();
    		
		} else {
    		command.execute(editorAdaptor, count, selection);
		}
		if (changeMode)
			LeaveVisualModeCommand.doIt(editorAdaptor);
	}
	
	public static TextRange newRange(final Position ul, final int width) {
	    return StartEndTextRange.inclusive(ul, ul.addModelOffset(width));
	}

	public static TextObject newSelection(final Position ul, final int width) {
	    return new SimpleSelection(newRange(ul, width));
    }

    @Override
    public CountAwareCommand repetition() {
        final TextOperation wrappedRepetition = command.repetition();
        if (wrappedRepetition != null) {
            return new Repetition(wrappedRepetition);
        }
        return null;
    }
	
	/** A variant of SelectionBasedTextOperation that doesn't change mode. */
	public static class DontChangeMode extends SelectionBasedTextOperationCommand {
		public DontChangeMode(final TextOperation command) {
			super(command, false);
		}
	}
	
	/** Repetition of SelectionBasedTextOperation */
	public class Repetition extends CountAwareCommand {
		
		private final TextOperation repetition;
		
		public Repetition(final TextOperation repeat) {
			this.repetition = repeat;
		}

		@Override
		public void execute(final EditorAdaptor editorAdaptor, final int count)
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
