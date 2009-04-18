package net.sourceforge.vrapper.core.tests.utils;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Space;
import net.sourceforge.vrapper.utils.VimUtils;

/**
 * A simple {@link TextContent} implementation for unit tests.
 *
 * @author Matthias Radig
 */
public class TestTextContent implements TextContent {

    StringBuilder buffer = new StringBuilder();

    public LineInformation getLineInformation(int line) {
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
        while(index < offset) {
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

    public void replace(int index, int length, String s) {
		buffer.replace(index, index+length, s);
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

}
