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
        while (index < end) {
            String s = p.getText(index, 1);
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
        return VimConstants.NEWLINE.contains(s);
    }

    public static boolean isWordCharacter(String s) {
        return VimUtils.COMPILED_WORD_CHAR_PATTERN.matcher(s).find();
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().equals("");
    }

    /**
     * Calculates an offset position. Line breaks are not counted.
     * @param position TODO
     */
    public static int calculatePositionForOffset(Platform p, int position, int offset) {
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

    public static int getSOLAwarePositionAtLine(VimEmulator vim, int line) {
        Platform platform = vim.getPlatform();
        if( vim.getVariables().isStartOfLine()) {
            line = Math.max(line, 0);
            line = Math.min(line, platform.getNumberOfLines()-1);
            LineInformation lineInfo = platform.getLineInformation(line);
            return getFirstNonWhiteSpaceOffset(vim, lineInfo);
        }
        return VimUtils.getPositionAtLine(vim, line);
    }

    public static int getPositionAtLine(VimEmulator vim, int number) {
        Platform p = vim.getPlatform();
        number = Math.max(number, 0);
        number = Math.min(number, p.getNumberOfLines()-1);
        LineInformation targetLine = p.getLineInformation(number);
        int horPosition = vim.getHorizontalPosition();
        horPosition = Math.min(horPosition, targetLine.getLength()-1);
        horPosition = Math.max(horPosition, 0);
        return targetLine.getBeginOffset() + horPosition;
    }

}
