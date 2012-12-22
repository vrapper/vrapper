package net.sourceforge.vrapper.eclipse.commands;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.Counted;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.SimpleSelection;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.commands.TextOperation;

import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

public abstract class EclipseShiftOperation implements TextOperation {

    protected final boolean left;
    
    protected EclipseShiftOperation(boolean left) {
        this.left = left;
    }

    void execute(EditorAdaptor editorAdaptor, TextRange region, int count) throws CommandExecutionException {
    	TextContent modelContent = editorAdaptor.getModelContent();
    	int startOffset = region.getLeftBound().getModelOffset();
    	int endOffset = region.getRightBound().getModelOffset();
    	String action = left ? ITextEditorActionDefinitionIds.SHIFT_LEFT : ITextEditorActionDefinitionIds.SHIFT_RIGHT;
    	
    	int startLine;
    	//if the start is on a newline, put start on the next line
    	//(otherwise, >i{ will indent the line ending with a '{')
    	if(VimUtils.isNewLine(modelContent.getText(startOffset, 1))) {
    		startLine = modelContent.getLineInformationOfOffset(startOffset).getNumber() + 1;
    		
    		if(endOffset - startOffset == 1) {
    			//if one character is selected and it's a newline, ignore
    			//(<< and >> are ignored on blank lines)
    			return;
    		}
    	}
    	else {
    		startLine = modelContent.getLineInformationOfOffset(startOffset).getNumber();
    	}
    	
    	LineInformation endLineInformation = modelContent.getLineInformationOfOffset(endOffset);
    	String endText = modelContent.getText(endLineInformation.getBeginOffset(), endOffset - endLineInformation.getBeginOffset());
    	int endLine = endLineInformation.getNumber();
    	if (endOffset > endLineInformation.getBeginOffset() && !endText.matches("\\s*")) {
    		// if the right bound is beyond the first character of a line, include that entire line
    		// (unless everything within the bounds of this line is whitespace)
    		endLine++;
    	}
    	
        try {
            editorAdaptor.getHistory().beginCompoundChange();
            editorAdaptor.getHistory().lock();
            
            for (int i = 0; i < count; i++) {
                editorAdaptor.setSelection(createSelection(editorAdaptor, startLine, endLine));
                EclipseCommand.doIt(1, action, editorAdaptor);
            }
            editorAdaptor.setPosition(
                    editorAdaptor.getCursorService().newPositionForModelOffset(
                            VimUtils.getFirstNonWhiteSpaceOffset(
                                    modelContent, modelContent.getLineInformation(startLine))), true);
        } finally {
            editorAdaptor.getHistory().unlock();
            editorAdaptor.getHistory().endCompoundChange();
        }
    }

    private Selection createSelection(EditorAdaptor editor, int startLine, int endLine) {
        TextContent modelContent = editor.getModelContent();
        Position start = editor.getCursorService().newPositionForModelOffset(modelContent.getLineInformation(startLine).getBeginOffset());
        Position end = editor.getCursorService().newPositionForModelOffset(modelContent.getLineInformation(endLine).getBeginOffset());
        return new SimpleSelection(new StartEndTextRange(start, end));
    }

    public TextOperation repetition() {
        return this;
    }

    static public class Normal extends EclipseShiftOperation {

    	public static final EclipseShiftOperation LEFT = new Normal(true);
    	public static final EclipseShiftOperation RIGHT = new Normal(false);

        private Normal(boolean left) {
            super(left);
        }

        public void execute(EditorAdaptor editorAdaptor, int count,
                TextObject textObject) throws CommandExecutionException {
            super.execute(editorAdaptor, textObject.getRegion(editorAdaptor, count), 1);
        }

    }

    static public class Visual extends EclipseShiftOperation {

    	public static final EclipseShiftOperation LEFT = new Visual(true);
    	public static final EclipseShiftOperation RIGHT = new Visual(false);
        
    	private final int count;

    	private Visual(boolean left) {
            this(left, 1);
        }

        private Visual(boolean left, int count) {
            super(left);
            this.count = count;
        }

        public void execute(EditorAdaptor editorAdaptor, int count,
                TextObject textObject) throws CommandExecutionException {
        	if (count == Counted.NO_COUNT_GIVEN)
        		count = 1;
            super.execute(editorAdaptor, textObject.getRegion(editorAdaptor, 1), count);
        }

        public int getCount() {
            return count;
        }

        @Override
        public Visual repetition() {
            return this;
        }

    }


}