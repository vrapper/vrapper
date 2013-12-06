package net.sourceforge.vrapper.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.vim.SimpleKeyStroke;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.platform.SimpleConfiguration.NewLine;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.VimConstants;


/**
 * Commonly used methods.
 *
 * @author Matthias Radig
 */
// FIXME: this is just dumb port; some of those utils may be not needed any more, we may move some others elsewhere, etc.
public class VimUtils {

    public static final Pattern COMPILED_WORD_CHAR_PATTERN = Pattern.compile(VimConstants.WORD_CHAR_PATTERN);
    public static final Pattern COMPILED_PATTERN_DELIM_PATTERN = Pattern.compile(VimConstants.PATTERN_DELIM_PATTERN);

    private VimUtils() {
        // no instance
    }

    /**
     * @param s
     *            a string (of length 1).
     * @return whether s contains a single whitespace character.
     */
    public static boolean isWhiteSpace(final String s) {
        return VimConstants.WHITESPACE.contains(s);
    }

    /**
     * @param line
     *            a line in the text.
     * @return the offset where the first non-whitespace character occurs in the given line.
     */
    public static int getFirstNonWhiteSpaceOffset(final TextContent content, final LineInformation line) {
        int index = line.getBeginOffset();
        final int end = line.getEndOffset();
        while (index < end) {
            final String s = content.getText(index, 1);
            if (!isWhiteSpace(s)) {
                break;
            }
            index += 1;
        }
        return index;
    }

    /**
     * @param vim
     *            the vim emulator.
     * @param line
     *            a line in the text.
     * @return the whitespace at the begin of the given line.
     */
    public static String getIndent(final TextContent content, final LineInformation line) {
        final int offset = getFirstNonWhiteSpaceOffset(content, line);
        return content.getText(line.getBeginOffset(), offset - line.getBeginOffset());
    }

    /**
     * @param content textContent
     * @param line
     *            a line in the text.
     * @return the content of the given line, without preceeding whitespace.
     */
    public static String getWithoutIndent(final TextContent content, final LineInformation info) {
        final int offset = getFirstNonWhiteSpaceOffset(content, info);
        return content.getText(offset, info.getEndOffset() - offset);
    }
    
    public static boolean containsNewLine(final String s) {
    	for(final String newline : VimConstants.NEWLINE) {
    		if(s.contains(newline)) {
    			return true;
    		}
    	}
    	return false;
    }

    public static boolean isNewLine(final String s) {
        return VimConstants.NEWLINE.contains(s);
    }
    
    public static int startsWithNewLine(String s) {
        int nlLen = 0;
        // Find the longest new line prefix.
    	for(String newline : VimConstants.NEWLINE) {
    		if(s.startsWith(newline)) {
    			nlLen = Math.max(nlLen, newline.length());
    		}
    	}
    	return nlLen;
    }
    
    public static int endsWithNewLine(String s) {
        int nlLen = 0;
        // Find the longest new line suffix.
    	for(String newline : VimConstants.NEWLINE) {
    		if(s.endsWith(newline)) {
    			nlLen = Math.max(nlLen, newline.length());
    		}
    	}
    	return nlLen;
    }

    /**
     * Replaces all combinations of newline characters in a string with another string.
     */
    public static String replaceNewLines(String inputString, String replacement) {
        char[] input = inputString.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length; i++) {
            if (input[i] == '\r' && i + 1 < input.length && input[i+1] == '\n') {
                sb.append(replacement);
                i++; // skip \n
            } else if (input[i] == '\r') {
                sb.append(replacement);
            } else if (input[i] == '\n') {
                sb.append(replacement);
            } else {
                sb.append(input[i]);
            }
        }
        return sb.toString();
    }

    public static boolean isWordCharacter(final String s) {
        return VimUtils.COMPILED_WORD_CHAR_PATTERN.matcher(s).find();
    }

    public static boolean isPatternDelimiter(final String s) {
        return VimUtils.COMPILED_PATTERN_DELIM_PATTERN.matcher(s).find();
    }

    public static boolean isBlank(final String s) {
        return s == null || s.trim().equals("");
    }
    
    /**
     * @return true, if line contains only whitespace characters
     */
    public static boolean isLineBlank(final TextContent content, final int lineNo) {
        final LineInformation line = content.getLineInformation(lineNo);
        return VimUtils.isBlank(content.getText(line.getBeginOffset(), line.getLength()));
    }
    
    /**
     * @return true, if the last character in the text buffer is newline
     */
    public static boolean endsWithEOL(final EditorAdaptor editor) {
        final TextContent content = editor.getModelContent();
        final LineInformation line = content.getLineInformation(content.getNumberOfLines() - 1);
        return line.getNumber() > 0 && line.getLength() == 0;
    }
    
    public static int calculateColForPosition(final TextContent p, final Position position) {
        return calculateColForOffset(p, position.getModelOffset());
    }
    
    public static int calculateColForOffset(final TextContent p, final int modelOffset) {
        final LineInformation line = p.getLineInformationOfOffset(modelOffset);
        return modelOffset - line.getBeginOffset();
    }
    
    public static int calculateLine(final TextContent text, final int offset) {
        final LineInformation line = text.getLineInformationOfOffset(offset);
        return line.getNumber();
    }
    
    public static int calculateLine(final TextContent text, final Position position) {
        return calculateLine(text, position.getModelOffset());
    }


    /**
     * Calculates an offset position. Line breaks are not counted.
     * @param position TODO
     */
    public static int calculatePositionForOffset(final TextContent p, int position, final int offset) {
        LineInformation line = p.getLineInformationOfOffset(position);
        if (offset < 0) {
            int i = -offset;
            while (i > 0) {
                if(position > line.getBeginOffset()) {
                    position -=1;
                } else {
                    final int nextLine = line.getNumber()-1;
                    if (nextLine < 0) {
                        break;
                    }
                    line = p.getLineInformation(nextLine);
                    position = Math.max(line.getBeginOffset(), line.getEndOffset()-1);
                }
                i -= 1;
            }
        } else if (offset > 0) {
            int i = offset;
            int end = Math.max(line.getBeginOffset(), line.getEndOffset()-1);
            while (i > 0) {
                if(position < end) {
                    position +=1;
                } else {
                    final int nextLine = line.getNumber()+1;
                    if (nextLine > p.getNumberOfLines()-1) {
                        break;
                    }
                    line = p.getLineInformation(nextLine);
                    end = Math.max(line.getBeginOffset(), line.getEndOffset()-1);
                    position = line.getBeginOffset();
                }
                i -= 1;
            }

        }
        return position;
    }

    public static String stripLastNewline(final String text) {
        if (text.endsWith(NewLine.WINDOWS.nl)) {
            return text.substring(0, text.length()-2);
        }
        if (text.endsWith(NewLine.UNIX.nl) || text.endsWith(NewLine.MAC.nl)) {
            return text.substring(0, text.length()-1);
        }
        return text;
    }

    public static final <T> Set<T> set(final T... content) {
        return Collections.unmodifiableSet(new HashSet<T>(Arrays.asList(content)));
    }

    public static SearchResult wrapAroundSearch(final EditorAdaptor vim, final Search search,
            final Position position) {
        SearchResult result2;
        final SearchAndReplaceService searcher = vim.getSearchAndReplaceService();
        SearchResult result = searcher.find(search, position);
        if (result.isFound()) {
            result2 = result;
        } else {
            // redo search from beginning / end of document
            final TextContent p = vim.getModelContent();
            final int index = search.isBackward() ? p.getLineInformation(p.getNumberOfLines()-1).getEndOffset()-1 : 0;
            result = searcher.find(search, position.setModelOffset(index));
            result2 = result;
        }
        return result2;
    }
    
    /**
     * Vim doesn't start a delimited range on a newline or end a range on an
     * empty line (try 'vi{' while within a function for proof).
     */
    public static Position fixLeftDelimiter(TextContent model, CursorService cursor, Position delim) {
        //check if the character after delimiter is a newline
        if(isNewLine(model.getText(delim.getModelOffset() + 1, 1))) {
            //start after newline
            LineInformation line = model.getLineInformationOfOffset(delim.getModelOffset());
            LineInformation nextLine = model.getLineInformation(line.getNumber() + 1);
            delim = cursor.newPositionForModelOffset(nextLine.getBeginOffset());
        }
        else {
            delim = delim.addModelOffset(1);
        }
        return delim;
    }
    public static Position fixRightDelimiter(TextContent model, CursorService cursor, Position delim) {
        int delimIndex = delim.getModelOffset();
        LineInformation line = model.getLineInformationOfOffset(delimIndex);
        int lineStart = line.getBeginOffset();
        
        if(delimIndex > lineStart) {
            //is everything before the delimiter just whitespace?
            String text = model.getText(lineStart, delimIndex - lineStart);
            if(VimUtils.isBlank(text)) {
                //end on previous line
                LineInformation previousLine = model.getLineInformation(line.getNumber() -1);
                delimIndex = previousLine.getEndOffset();
                delim = cursor.newPositionForModelOffset(delimIndex);
            }
        }
        return delim;
    }

    /**
     * Tries to work around AltGr madness if we detect that AltGr was pressed.
     * If you did press Ctrl + Alt or Alt and hit this function, then check the state provider.
     * This function should be called as a fallback when remaps and default bindings failed to find
     * a match.
     * <p>
     * Examples for different Operating Systems when AltGr + Q is pressed (@ key on German keybord):
     * <ul>
     * <li>Windows SWT sends Ctrl + Alt + @.</li>
     * <li>Mac OSX SWT sends Alt + @.</li>
     * <li>Linux SWT passes just @.</li>
     * </ul>
     * @return Either a {@link KeyStroke} without modifiers or <tt>null</tt> if not applicable.
     */
    public static KeyStroke fixAltGrKey(KeyStroke key) {
        //Most-common case, bail as fast as possible
        if( ! key.withAltKey()) {
            return null;
        }

        // Turn off control and alt key bits.
        if (key.getSpecialKey() == null) {
            return new SimpleKeyStroke(key.getCharacter(), key.withShiftKey(), false, false);
        } else {
            return new SimpleKeyStroke(key.getSpecialKey(), key.withShiftKey(), false, false);
        }
    }
}
