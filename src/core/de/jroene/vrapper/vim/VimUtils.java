package de.jroene.vrapper.vim;

import java.util.regex.Pattern;

/**
 * Commonly used methods.
 * 
 * @author Matthias Radig
 */
public class VimUtils {

    public static final Pattern COMPILED_WORD_CHAR_PATTERN = Pattern.compile(VimConstants.WORD_CHAR_PATTERN);

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
     * @param vim
     *            the vim emulator.
     * @param line
     *            a line in the text.
     * @return the offset where the first non-whitespace character occurs in the
     *         given line.
     */
    public static int getFirstNonWhiteSpaceOffset(VimEmulator vim,
            LineInformation line) {
        Platform p = vim.getPlatform();
        int index = line.getBeginOffset();
        int end = line.getEndOffset();
        String s = p.getText(index, 1);
        while (isWhiteSpace(s) && index < end) {
            index += 1;
            s = p.getText(index, 1);
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
    public static String getIndent(VimEmulator vim, LineInformation line) {
        Platform p = vim.getPlatform();
        int offset = getFirstNonWhiteSpaceOffset(vim, line);
        return p.getText(line.getBeginOffset(), offset - line.getBeginOffset());
    }

    /**
     * @param vim
     *            the vim emulator.
     * @param line
     *            a line in the text.
     * @return the content of the given line, without preceeding whitespace.
     */
    public static String getWithoutIndent(VimEmulator vim, LineInformation info) {
        Platform p = vim.getPlatform();
        int offset = getFirstNonWhiteSpaceOffset(vim, info);
        return p.getText(offset, info.getEndOffset() - offset);
    }

    public static boolean isNewLine(String s) {
        return VimConstants.NEWLINE.startsWith(s);
    }

    public static boolean isWordCharacter(String s) {
        return VimUtils.COMPILED_WORD_CHAR_PATTERN.matcher(s).find();
    }

}
