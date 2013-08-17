package net.sourceforge.vrapper.vim.commands;

import static java.lang.Character.isUpperCase;
import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;
import static java.lang.Math.min;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.HistoryService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection.TextBlock;

public class SwapCaseCommand extends AbstractModelSideCommand {

    public static final SwapCaseCommand INSTANCE = new SwapCaseCommand();
    public static final SwapCaseCommand VISUAL_INSTANCE = new SwapCaseCommand() {
    	//Rather than swapping the case at the current cursor position <count> times,
    	//swap the case of the currently selected text.
    	@Override
    	public void execute(final EditorAdaptor editorAdaptor, final int count) {
    		editorAdaptor.rememberLastActiveSelection();
    		final TextObject selection = editorAdaptor.getSelection();
			try {
				final TextRange range = selection.getRegion(editorAdaptor, NO_COUNT_GIVEN);
				super.swapCase(editorAdaptor.getModelContent(), range.getLeftBound().getModelOffset(), range.getModelLength());
				//move cursor to beginning of selection to match vim behavior
				editorAdaptor.getCursorService().setPosition(range.getLeftBound(), true);
				LeaveVisualModeCommand.doIt(editorAdaptor);
			} catch (final CommandExecutionException e) {
        		editorAdaptor.getUserInterfaceService().setErrorMessage(e.getMessage());
			}
    	}
    	
    };
    public static final SwapCaseCommand VISUALBLOCK_INSTANCE = new SwapCaseCommand() {
    	//Rather than swapping the case at the current cursor position <count> times,
    	//swap the case of the currently selected text.
    	@Override
    	public void execute(final EditorAdaptor editorAdaptor, final int count) {
    		editorAdaptor.rememberLastActiveSelection();
            final TextContent textContent = editorAdaptor.getModelContent();
            final Selection selection = editorAdaptor.getSelection();
            final Position selectionStart = selection.getFrom();
            final Position selectionEnd = selection.getTo();
            final CursorService cursorService = editorAdaptor.getCursorService();
            
			try {
                final HistoryService history = editorAdaptor.getHistory();
                history.beginCompoundChange();
                history.lock("block-action");
            
                TextBlock textBlock = BlockWiseSelection.getTextBlock(selectionStart, selectionEnd, textContent, cursorService);
                for (int line = textBlock.startLine; line <= textBlock.endLine; ++line) {
                    final Position start = cursorService.getPositionByVisualOffset(line, textBlock.startVisualOffset);
                    if (start == null) {
                        // no characters at the visual offset, skip this line
                        continue;
                    }
                    final int startOfs = start.getModelOffset();
                    final Position end = cursorService.getPositionByVisualOffset(line, textBlock.endVisualOffset);
                    int endOfs;
                    if (end == null) {
                        // the line is shorter that the end offset
                        endOfs = textContent.getLineInformation(line).getEndOffset();
                    } else {
                        endOfs = end.addModelOffset(1).getModelOffset();
                    }
    				super.swapCase(textContent, startOfs, endOfs - startOfs);
                }
                
				//move cursor to beginning of selection to match vim behavior
                cursorService.setPosition(cursorService.getPositionByVisualOffset(textBlock.startLine, textBlock.startVisualOffset), true);
				
                history.unlock("block-action");
                history.endCompoundChange();
				
				LeaveVisualModeCommand.doIt(editorAdaptor);
			} catch (final CommandExecutionException e) {
        		editorAdaptor.getUserInterfaceService().setErrorMessage(e.getMessage());
			}
    	}
    	
    };
    public static final SimpleTextOperation TEXT_OBJECT_INSTANCE = new SimpleTextOperation() {
        @Override
        public TextOperation repetition() {
            return this;
        }
        
        @Override
        public void execute(EditorAdaptor editorAdaptor, TextRange region, ContentType contentType) throws CommandExecutionException {
            swapCase(editorAdaptor.getModelContent(), region.getLeftBound().getModelOffset(), region.getModelLength());
        }
    };

    private SwapCaseCommand() { /* NOP */ }

    @Override
    protected int execute(final TextContent content, final int offset, final int count) {
        final LineInformation line = content.getLineInformationOfOffset(offset);
        final int end = min(offset + count, line.getEndOffset());
        final int length = end - offset;
        swapCase(content, offset, length);
        return end;
    }

    public static void swapCase(final TextContent content, final int start, final int length) {
        final String text = content.getText(start, length);
        final StringBuilder s = new StringBuilder();
        for(int i = 0; i < length; i++) {
            char c = text.charAt(i);
            c = isUpperCase(c) ? toLowerCase(c) : toUpperCase(c);
            s.append(c);
        }
        content.replace(start, length, s.toString());
    }

}