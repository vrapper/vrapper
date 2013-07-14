package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.HistoryService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection.TextBlock;
import net.sourceforge.vrapper.vim.register.Register;
import net.sourceforge.vrapper.vim.register.RegisterContent;
import net.sourceforge.vrapper.vim.register.RegisterManager;
import net.sourceforge.vrapper.vim.register.StringRegisterContent;
import net.sourceforge.vrapper.vim.register.TextBlockContentBuilderRegister;

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
        	        if (VimUtils.containsNewLine(string)) {
        	            legal = false;
        	        } else {
            	        
            	        // re-position to beginning of insert
            	        final Position newStart = editorAdaptor.getPosition().addModelOffset(-string.length() + 1);
            	        editorAdaptor.setPosition(newStart, false);
        	        }
    	        }
		    }
		    
		    if (legal) {
                final TextObject blockSelection = editorAdaptor.getRegisterManager().getLastActiveSelectionArea();
                final TextRange blockRange = blockSelection.getRegion(editorAdaptor, NO_COUNT_GIVEN);
                final TextBlock textBlock = BlockWiseSelection.getTextBlock( blockRange.getStart(), blockRange.getEnd(),
                        editorAdaptor.getModelContent(), editorAdaptor.getCursorService());
    		    
                doIt(editorAdaptor, command, getCount(), textBlock);
		    }
            
            if (commitHistory) {
                final HistoryService history = editorAdaptor.getHistory();
                history.unlock("block-action");
                history.endCompoundChange();
            }
        }
        
        public static void doIt(final EditorAdaptor editorAdaptor, final TextOperation command, final int count, final TextBlock block) 
                throws CommandExecutionException {
            
            final RegisterManager registers = editorAdaptor.getRegisterManager();
            final Register lastActiveRegister = registers.getActiveRegister();
            final Register lastEditRegister = registers.getLastEditRegister();
            final CursorService cursorService = editorAdaptor.getCursorService();
            final TextContent textContent = editorAdaptor.getModelContent();
            
            TextOperation repetition = command.repetition();
            if (repetition == null) {
                repetition = command;
            }
            for (int line = block.startLine + 1; line <= block.endLine; ++line) {
                final Position runStart = cursorService.getPositionByVisualOffset(line, block.startVisualOffset);
                Position runEnd = cursorService.getPositionByVisualOffset(line, block.endVisualOffset);
                if (runEnd == null) {
                    final LineInformation lineInfo = textContent.getLineInformation(line);
                    runEnd = cursorService.newPositionForModelOffset(lineInfo.getEndOffset());
                } else {
                    runEnd = runEnd.addModelOffset(1);
                }
                if (runStart != null) {
                    editorAdaptor.setPosition(runStart, false);
                    final TextObject nextLine = new SimpleSelection(new StartEndTextRange(runStart, runEnd));

                    final RegisterContent content = lastEditRegister.getContent();
                    repetition.execute(editorAdaptor, count, nextLine);

                    lastEditRegister.setContent(content);
                    registers.setActiveRegister(lastActiveRegister); // return to default reg

                    if (repetition.repetition() != null) {
                        repetition = repetition.repetition();
                    }
                }
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
		    final TextRange blockRange = selection.getRegion(editorAdaptor, NO_COUNT_GIVEN);
		    final CursorService cursorService = editorAdaptor.getCursorService();
            final TextBlock textBlock = BlockWiseSelection.getTextBlock(blockRange.getStart(), blockRange.getEnd(),
                        editorAdaptor.getModelContent(), cursorService);
		    final Position runStart = cursorService.getPositionByVisualOffset(textBlock.startLine, textBlock.startVisualOffset);
		    Position runEnd = cursorService.getPositionByVisualOffset(textBlock.startLine, textBlock.endVisualOffset);
		    if (runEnd == null) {
		        final LineInformation lineInfo = textContent.getLineInformation(textBlock.startLine);
		        runEnd = cursorService.newPositionForModelOffset(lineInfo.getEndOffset());
		    } else {
		        runEnd = runEnd.addModelOffset(1);
		    }
		    final TextObject firstLine = new SimpleSelection(new StartEndTextRange(runStart, runEnd));
		    
		    final HistoryService history = editorAdaptor.getHistory();
		    final RegisterManager registerManager = editorAdaptor.getRegisterManager();
            Register activeRegister = registerManager.getActiveRegister();
            registerManager.setActiveRegister(new TextBlockContentBuilderRegister(activeRegister, textBlock.endVisualOffset - textBlock.startVisualOffset));
		    history.beginCompoundChange();
		    history.lock("block-action");
		    
    		command.execute(editorAdaptor, count, firstLine);
    		
    		if (changeMode) {
    		    BlockwiseRepeatCommand.doIt(editorAdaptor, command, count, textBlock);
    		    editorAdaptor.setPosition(runStart, true);
    		
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
			repetition.execute(editorAdaptor, count, editorAdaptor.getLastActiveSelectionArea());
			if (changeMode)
				LeaveVisualModeCommand.doIt(editorAdaptor);
		}

		@Override
		public CountAwareCommand repetition() {
			return this;
		}

	}



}
