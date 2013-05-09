package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.HistoryService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection.Rect;
import net.sourceforge.vrapper.vim.register.Register;
import net.sourceforge.vrapper.vim.register.RegisterContent;
import net.sourceforge.vrapper.vim.register.StringRegisterContent;

public class SelectionBasedTextOperationCommand extends CountAwareCommand {

	public static class BlockwiseRepeatCommand implements Command {
	    
	    private final TextOperation command;
        private final int count;
        private final boolean doesInsert;
        private final boolean commitHistory;

        public BlockwiseRepeatCommand(final TextOperation command, final int count, 
                final boolean doesInsert, final boolean commitHistory) {
	        this.command = command;
	        this.count = count;
	        this.doesInsert = doesInsert;
	        this.commitHistory = commitHistory;
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
            
		    boolean legal = true;
		    if (doesInsert) {
    		    final Register lastEdit = editorAdaptor.getRegisterManager().getLastEditRegister();
    	        final RegisterContent content = lastEdit.getContent();
    	        if (!(content instanceof StringRegisterContent)) {
    	            legal = false;
    	        } else {
        
            	         
        	        final StringRegisterContent stringInsert = (StringRegisterContent) content;
        	        final String string = stringInsert.getText();
        	        if (string.indexOf('\n') > -1) {
        	            legal = false;
        	        } else {
            	        
            	        // re-position to beginning of insert
            	        final Position newStart = editorAdaptor.getPosition().addModelOffset(-string.length() + 1);
            	        editorAdaptor.setPosition(newStart, false);
        	        }
    	        }
		    }
		    
		    if (legal) {
                final TextObject blockSelection = editorAdaptor.getRegisterManager().getLastActiveSelection();
    		    final Rect rect = BlockWiseSelection.getRect(editorAdaptor, blockSelection);
    		    System.out.println("Height: " + rect.height());
    		    
                doIt(editorAdaptor, command, getCount(), rect);
		    }
            
            if (commitHistory) {
                final HistoryService history = editorAdaptor.getHistory();
                history.unlock("block-action");
                history.endCompoundChange();
            }
        }
        
        public static void doIt(final EditorAdaptor editorAdaptor, final TextOperation command, final int count, final Rect rect) 
                throws CommandExecutionException {
		    final int height = rect.height();
		    final int width = rect.width();
    		final TextOperation repetition = command.repetition();
		    for (int i=1; i < height; i++) {
		        rect.top++;
		        final Position newUl = rect.getULPosition(editorAdaptor);
		        editorAdaptor.setPosition(newUl, false);
		        final TextObject nextLine = newSelection(newUl, width);
		        repetition.execute(editorAdaptor, count, nextLine);
		        System.out.println("Execute @" + rect);
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
    		
    		    history.unlock("block-action");
        		history.endCompoundChange();
    		}
    		
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
