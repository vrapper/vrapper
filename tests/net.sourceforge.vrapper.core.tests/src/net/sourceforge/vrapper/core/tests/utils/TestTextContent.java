package net.sourceforge.vrapper.core.tests.utils;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Space;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

/**
 * A simple {@link TextContent} implementation for unit tests.
 *
 * @author Matthias Radig
 */
public class TestTextContent implements TextContent {

    StringBuilder buffer = new StringBuilder();
	private final CursorService cursorService;

    public TestTextContent(CursorService cursorService) {
		this.cursorService = cursorService;
	}

	public LineInformation getLineInformation(int line) {
	    if (line >= getNumberOfLines()) {
	        throw new RuntimeException("Line is out of range");
	    }
	    
        int index = 0;
        int currLine = 0;
        while(currLine < line && index < buffer.length()) {
            while (index < buffer.length()) {
                String c = buffer.substring(index, index+1);
                index += 1;
                if(VimUtils.isNewLine(c)) {
                    if (index < buffer.length()) {
                        c = buffer.substring(index-1, index+1);
                        if(VimUtils.isNewLine(c)) {
                            index += 1;
                        }
                    }
                    break;
                }
            }
            currLine += 1;
        }
        int startIndex = index;
        while(index < buffer.length()) {
            String c = buffer.substring(index, index+1);
            if(VimUtils.isNewLine(c)) {
                break;
            }
            index += 1;
        }
        int endIndex = index;
        return new LineInformation(line, startIndex, endIndex-startIndex);
    }

    public LineInformation getLineInformationOfOffset(int offset) {
        int index = 0;
        int currLine = 0;
        while(index < offset && index < buffer.length()) {
            String c = buffer.substring(index, index+1);
            if(VimUtils.isNewLine(c)) {
                if (index < offset-1) {
                    c = buffer.substring(index, index+2);
                    if(VimUtils.isNewLine(c)) {
                        index += 1;
                    }
                }
                currLine += 1;
            }
            index += 1;
        }
        return getLineInformation(currLine);
    }

    public int getNumberOfLines() {
        int lines = 1;
        for (int i = 0; i < buffer.length(); i++) {
            String c = buffer.substring(i, i+1);
            if(VimUtils.isNewLine(c)) {
                if (i > 1 && i < buffer.length()) {
                    c = buffer.substring(i-1, i+1);
                    if(VimUtils.isNewLine(c)) {
                        i += 1;
                    }
                }
                lines += 1;
            }
        }
        return lines;
    }

    public String getText(int index, int length) {
    	return buffer.substring(index, index + length);
    }

    public String getText(TextRange range) {
        return getText(range.getLeftBound().getModelOffset(), range.getModelLength());
    }

    public void replace(int index, int length, String s) {
		buffer.replace(index, index+length, s);
		cursorService.setPosition(new DumbPosition(index + s.length()), StickyColumnPolicy.NEVER);
    }

	public Space getSpace() {
		return Space.MODEL; // it doesn't matter
	}

	public int getTextLength() {
		return buffer.length();
	}

	public void setText(String content) {
		buffer.setLength(0);
		buffer.append(content);
	}

	public String getText() {
		return buffer.toString();
	}

    public void smartInsert(int index, String s) {
        replace(index, 0, s);
    }

    public void smartInsert(String s) {
        smartInsert(cursorService.getPosition().getModelOffset(), s);
    }

    public String toString() {
        return "TestTextContent(" + buffer + ")";
    }

}
