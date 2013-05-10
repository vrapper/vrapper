package net.sourceforge.vrapper.vim.commands;

import static java.lang.Character.isUpperCase;
import static java.lang.Character.toLowerCase;
import static java.lang.Character.toUpperCase;
import static java.lang.Math.min;
import net.sourceforge.vrapper.platform.HistoryService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection.Rect;

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
            final Rect rect = BlockWiseSelection.getRect(textContent, editorAdaptor.getSelection());
            final Position selectionStart = rect.getULPosition(editorAdaptor);
            
			try {
                final HistoryService history = editorAdaptor.getHistory();
                history.beginCompoundChange();
                history.lock("block-action");
            
                final int height = rect.height();
                final int width = rect.width() + 1;
                for (int i=0; i < height; i++) {
                    final Position ul = rect.getULPosition(editorAdaptor);
                    final int rowStart = ul.getModelOffset();
    				super.swapCase(textContent, rowStart, width);
                    rect.top++;
                }
                
				//move cursor to beginning of selection to match vim behavior
				editorAdaptor.getCursorService().setPosition(selectionStart, true);
				
                history.unlock("block-action");
                history.endCompoundChange();
				
				LeaveVisualModeCommand.doIt(editorAdaptor);
			} catch (final CommandExecutionException e) {
        		editorAdaptor.getUserInterfaceService().setErrorMessage(e.getMessage());
			}
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