package net.sourceforge.vrapper.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

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
    public static boolean isWhiteSpace(String s) {
        return VimConstants.WHITESPACE.contains(s);
    }

    /**
     * @param line
     *            a line in the text.
     * @return the offset where the first non-whitespace character occurs in the given line.
     */
    public static int getFirstNonWhiteSpaceOffset(TextContent content, LineInformation line) {
        int index = line.getBeginOffset();
        int end = line.getEndOffset();
        while (index < end) {
            String s = content.getText(index, 1);
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
    public static String getIndent(TextContent content, LineInformation line) {
        int offset = getFirstNonWhiteSpaceOffset(content, line);
        return content.getText(line.getBeginOffset(), offset - line.getBeginOffset());
    }

    /**
     * @param content textContent
     * @param line
     *            a line in the text.
     * @return the content of the given line, without preceeding whitespace.
     */
    public static String getWithoutIndent(TextContent content, LineInformation info) {
        int offset = getFirstNonWhiteSpaceOffset(content, info);
        return content.getText(offset, info.getEndOffset() - offset);
    }

    public static boolean isNewLine(String s) {
        return VimConstants.NEWLINE.contains(s);
    }

    public static boolean isWordCharacter(String s) {
        return VimUtils.COMPILED_WORD_CHAR_PATTERN.matcher(s).find();
    }
    
    public static boolean isPatternDelimiter(String s) {
        return VimUtils.COMPILED_PATTERN_DELIM_PATTERN.matcher(s).find();
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().equals("");
    }
    
    /**
     * @return true, if line contains only whitespace characters
     */
    public static boolean isLineBlank(TextContent content, int lineNo) {
        LineInformation line = content.getLineInformation(lineNo);
        return VimUtils.isBlank(content.getText(line.getBeginOffset(), line.getLength()));
    }
    
    /**
     * @return true, if the last character in the text buffer is newline
     */
    public static boolean endsWithEOL(EditorAdaptor editor) {
        TextContent content = editor.getModelContent();
        LineInformation line = content.getLineInformation(content.getNumberOfLines() - 1);
        return line.getNumber() > 0 && line.getLength() == 0;
    }

    /**
     * Calculates an offset position. Line breaks are not counted.
     * @param position TODO
     */
    public static int calculatePositionForOffset(TextContent p, int position, int offset) {
        LineInformation line = p.getLineInformationOfOffset(position);
        if (offset < 0) {
            int i = -offset;
            while (i > 0) {
                if(position > line.getBeginOffset()) {
                    position -=1;
                } else {
                    int nextLine = line.getNumber()-1;
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
                    int nextLine = line.getNumber()+1;
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

    public static String stripLastNewline(String text) {
        if (text.endsWith(NewLine.WINDOWS.nl)) {
            return text.substring(0, text.length()-2);
        }
        if (text.endsWith(NewLine.UNIX.nl) || text.endsWith(NewLine.MAC.nl)) {
            return text.substring(0, text.length()-1);
        }
        return text;
    }

    public static final <T> Set<T> set(T... content) {
        return Collections.unmodifiableSet(new HashSet<T>(Arrays.asList(content)));
    }

    public static SearchResult wrapAroundSearch(EditorAdaptor vim, Search search,
            Position position) {
        SearchResult result2;
        SearchAndReplaceService searcher = vim.getSearchAndReplaceService();
        SearchResult result = searcher.find(search, position);
        if (result.isFound()) {
            result2 = result;
        } else {
            // redo search from beginning / end of document
            TextContent p = vim.getModelContent();
            int index = search.isBackward() ? p.getLineInformation(p.getNumberOfLines()-1).getEndOffset()-1 : 0;
            result = searcher.find(search, position.setModelOffset(index));
            result2 = result;
        }
        return result2;
    }

}
