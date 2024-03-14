package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

/**
 * Insert (or remove) &lt;shiftwidth&gt; spaces at the beginning of the line. Then
 * replace every &lt;tabstop&gt; spaces with a TAB character if &lt;expandtab&gt; is
 * disabled.
 */
public class InsertShiftWidth implements TextOperation {

    public static final InsertShiftWidth INSERT = new InsertShiftWidth(true, false);
    public static final InsertShiftWidth REMOVE = new InsertShiftWidth(false, false);
    public static final InsertShiftWidth INSERT_VISUAL = new InsertShiftWidth(true, true);
    public static final InsertShiftWidth REMOVE_VISUAL = new InsertShiftWidth(false, true);

    private final boolean shiftRight;
    private final boolean visualMode;
    
    private InsertShiftWidth(boolean shiftRight, boolean visualMode) {
        this.shiftRight = shiftRight;
        this.visualMode = visualMode;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor, int count, TextObject textObject) throws CommandExecutionException {
        int tabstop = editorAdaptor.getConfiguration().get(Options.TAB_STOP);
        tabstop = Math.max(1, tabstop);
        int shiftwidth = editorAdaptor.getConfiguration().get(Options.SHIFT_WIDTH);
        shiftwidth = Math.max(1, shiftwidth);
        boolean expandtab = editorAdaptor.getConfiguration().get(Options.EXPAND_TAB);
        
        TextRange region;
        // Use count for number of widths to indent.
        // In non-visual mode, a counted operator is used to calculate the region.
        if (visualMode ) {
            region = textObject.getRegion(editorAdaptor, Counted.NO_COUNT_GIVEN);
            if (count != Counted.NO_COUNT_GIVEN) {
                shiftwidth = shiftwidth * count;
            }
        } else {
            region = textObject.getRegion(editorAdaptor, Math.abs(count));
        }

        // Fill string with number of spaces.
        String replaceTab = new String(new char[tabstop]).replace('\0', ' ');
        String replaceShiftWidth = new String(new char[shiftwidth]).replace('\0', ' ');

        TextContent model = editorAdaptor.getModelContent();
        LineInformation line;
        if(region == null) { // i_ctr-t/-d, use current line
            line = model.getLineInformationOfOffset( editorAdaptor.getPosition().getModelOffset() );
            doIt(line, model, expandtab, true, tabstop, shiftwidth, replaceTab, replaceShiftWidth);
        }
        else { // >>, <<, v_>, v_<, use region
            int startLine = calculateStartLine(model, region.getLeftBound().getModelOffset(), region.getRightBound().getModelOffset());
            if(startLine < 0) {
                //exit without moving cursor
                return; 
            }
            int endLine = calculateEndLine(model, region.getRightBound().getModelOffset());
            boolean shiftround = editorAdaptor.getConfiguration().get(Options.SHIFT_ROUND);

            editorAdaptor.getHistory().beginCompoundChange();
            int i = startLine;
            while(i < endLine) {
                line = model.getLineInformation(i);
                i++;
                if(line.getLength() == 0) {
                    // >> and << are ignored on empty lines
                    continue;
                }
                doIt(line, model, expandtab, shiftround, tabstop, shiftwidth, replaceTab, replaceShiftWidth);
            }
            editorAdaptor.getHistory().endCompoundChange();

            editorAdaptor.setPosition(
                    editorAdaptor.getCursorService().newPositionForModelOffset(
                            VimUtils.getFirstNonWhiteSpaceOffset(
                                    model, model.getLineInformation(startLine))),
                    StickyColumnPolicy.ON_CHANGE);
        }

    }
    
    private int calculateStartLine(TextContent model, int startOffset, int endOffset) {
    	int startLine;
    	//if the start is on a newline, put start on the next line
    	//(otherwise, >i{ will indent the line ending with a '{')
    	if(VimUtils.isNewLine(model.getText(startOffset, 1))) {
    		startLine = model.getLineInformationOfOffset(startOffset).getNumber() + 1;
    		
    		if(endOffset - startOffset == 1) {
    			//if one character is selected and it's a newline, ignore
    			//(<< and >> are ignored on blank lines)
    			return -1;
    		}
    	}
    	else {
    		startLine = model.getLineInformationOfOffset(startOffset).getNumber();
    	}
    	return startLine;
    }
    
    private int calculateEndLine(TextContent model, int endOffset) {
    	LineInformation endLineInformation = model.getLineInformationOfOffset(endOffset);
    	String endText = model.getText(endLineInformation.getBeginOffset(),
    			endLineInformation.getLength());
    	int endLine = endLineInformation.getNumber();
    	if (endOffset > endLineInformation.getBeginOffset() && !endText.matches("\\s*")) {
    		// if the right bound is beyond the first character of a line, include that entire line
    		// (unless everything within the bounds of this line is whitespace)
    		endLine++;
    	}
    	return endLine;
    }

    private void doIt(LineInformation line, TextContent model, boolean expandtab, boolean shiftround,
            int tabstop, int shiftwidth, String replaceTab, String replaceShiftWidth) {

        String lineStr = model.getText(line.getBeginOffset(), line.getLength());
        int whitespaceEnd = VimUtils.getFirstNonWhiteSpaceOffset(model, line) - line.getBeginOffset();

        String indent = lineStr.substring(0, whitespaceEnd);
        //expand all tab characters so we can recalculate tabstops
        indent = indent.replaceAll("\t", replaceTab);

        if(shiftRight) {
            //introduce new indent
            if( (!shiftround) || indent.length() % shiftwidth == 0) {
                indent = replaceShiftWidth + indent;
            }
            else {
                while(indent.length() % shiftwidth != 0) {
                    indent += " ";
                }
            }
        }
        else {
            //remove an indent
            if (shiftwidth > indent.length()) {
                indent = "";
            }
            else if(indent.length() >= shiftwidth && (!shiftround || indent.length() % shiftwidth == 0)) {
                indent = indent.substring(shiftwidth);
            }
            else {
                while(indent.length() % shiftwidth != 0) {
                    indent = indent.substring(1);
                }
            }
        }

        String replace = "";
        if( ! expandtab) {
            //collapse <tabstop> spaces into tab characters
            while(indent.length() >= tabstop) {
                indent = indent.substring(tabstop);
                replace += "\t";
            }
        }
        replace += indent;
        model.replace(line.getBeginOffset(), whitespaceEnd, replace);
    }

    @Override
    public TextOperation repetition() {
        return this;
    }

}
