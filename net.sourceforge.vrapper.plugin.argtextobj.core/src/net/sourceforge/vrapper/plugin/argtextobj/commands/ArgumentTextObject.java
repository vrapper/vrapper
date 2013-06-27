package net.sourceforge.vrapper.plugin.argtextobj.commands;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.AbstractTextObject;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.motions.CountAwareMotion;

public class ArgumentTextObject extends AbstractTextObject {

    public static final ArgumentTextObject INNER = new ArgumentTextObject(true);
    public static final ArgumentTextObject OUTER = new ArgumentTextObject(false);

    private boolean inner;

    private ArgumentTextObject(boolean inner) {
        this.inner = inner;
    }

    /**
     * Helper class to find argument boundaries starting at the specified
     * position
     */
    private class ArgBoundsFinder {
        final private TextContent text;
        private int leftBound;
        private int rightBound;
        final private static String QUOTES = "\"\'";
        final private static String OPEN_PARENS = "[{(";
        final private static String CLOSE_PARENS = ")}]";

        public ArgBoundsFinder(TextContent text) {
            this.text = text;
        }

        /**
         * Finds left and right boundaries of an argument at the specified
         * position. If successful @ref getLeftBound() will point to the left
         * argument delimiter and @ref getRightBound() will point to the right
         * argument delimiter. Use @ref AdjustForInner or @ref AdjustForOuter to
         * fix the boundaries based on the type of text object.
         * @param position starting position.
         */
        public void findBoundsAt(int position) throws CommandExecutionException
        {
            leftBound = position;
            rightBound = position;
            getOutOfQuotedText();
            if (rightBound == leftBound) {
                if (isCloseParen(getCharAt(rightBound))) {
                    --leftBound;
                } else {
                    ++rightBound;
                }
            }
            int nextLeft = leftBound;
            int nextRight = rightBound;
            //
            // Try to extend bound until one of the bounds is a comma.
            // This handles cases like: fun(a, (30 + <cursor>x) * 20, c)
            //
            boolean parenthesis;
            do {
                leftBound = nextLeft;
                findLeftBound();
                nextLeft = leftBound - 1;
                rightBound = nextRight;
                findRightBound();
                nextRight = rightBound + 1;
                //
                // If reached text boundaries or there is nothing between delimiters.
                //
                if (nextLeft < 0 || nextRight >= text.getTextLength() || (rightBound - leftBound) == 1) {
                    throw new CommandExecutionException("not an argument");
                }
                parenthesis = getCharAt(leftBound) != ',' && getCharAt(rightBound) != ',';
                if (parenthesis && isIdentBackward()) {
                    // Looking at a pair of parenthesis preceded by an
                    // identifier -- single argument function call.
                    break;
                }
            } while (leftBound > 0 && rightBound < text.getTextLength() && parenthesis);
        }

        /**
         * Skip left delimiter character and any following whitespace.
         */
        public void AdjustForInner() {
            ++leftBound;
            while (leftBound < rightBound
                    && VimUtils.isWhiteSpace(Character.toString(getCharAt(leftBound)))) {
                ++leftBound;
            }
        }

        /**
         * Exclude left bound character for the first argument, include the
         * right bound character and any following whitespace.
         */
        public void AdjustForOuter() {
            if (getCharAt(leftBound) != ',') {
                ++leftBound;
                if (rightBound + 1 < text.getTextLength() && getCharAt(rightBound) == ',') {
                    ++rightBound;
                    while (rightBound + 1 < text.getTextLength()
                            && VimUtils.isWhiteSpace(Character.toString(getCharAt(rightBound)))) {
                        ++rightBound;
                    }
                }
            }
        }

        public int getLeftBound() {
            return leftBound;
        }

        public int getRightBound() {
            return rightBound;
        }

        public boolean isIdentBackward() {
            int i = leftBound - 1;
            // Skip whitespace first.
            while (i > 0 && VimUtils.isWhiteSpace(Character.toString(getCharAt(i)))) {
                --i;
            }
            final int idEnd = i;
            while (i > 0 && Character.isJavaIdentifierPart(getCharAt(i))) {
                --i;
            }
            return (idEnd - i) > 0 && Character.isJavaIdentifierStart(getCharAt(i + 1));
        }


        /**
         * Detects if current position is inside a quoted string and adjusts
         * left and right bounds to the boundaries of the string.
         * @note Doesn't support line continuations.
         */
        private void getOutOfQuotedText() {
            final LineInformation line = text.getLineInformationOfOffset(leftBound);
            int i = line.getBeginOffset();
            while (i <= rightBound) {
                if (isQuote(i)) {
                   final int endOfQuotedText = SkipQuotedTextForward(i, line.getEndOffset());
                   if (endOfQuotedText >= leftBound) {
                       leftBound = i - 1;
                       rightBound = endOfQuotedText + 1;
                       break;
                   } else {
                       i = endOfQuotedText;
                   }
                }
                ++i;
            }
        }

        private void findRightBound() {
            int insideParens = 0;
            while (rightBound + 1 < text.getTextLength()) {
                final int ch = getCharAt(rightBound);
                if (ch == ',' && insideParens == 0) {
                    break;
                }
                if (isOpenParen(ch)) {
                    ++insideParens;
                } else {
                    if (isCloseParen(ch)) {
                        if (insideParens == 0) {
                            break;
                        } else {
                            --insideParens;
                        }
                    } else {
                        if (isQuoteChar(ch)) {
                            rightBound = SkipQuotedTextForward(rightBound, text.getTextLength());
                        }
                    }

                }
                ++rightBound;
            }
        }

        private boolean isCloseParen(final int ch) {
            return CLOSE_PARENS.indexOf(ch) != -1;
        }

        private boolean isOpenParen(final int ch) {
            return OPEN_PARENS.indexOf(ch) != -1;
        }

        private void findLeftBound() {
            int insideParens = 0;
            while (leftBound > 0) {
                final int ch = getCharAt(leftBound);
                if (ch == ',' && insideParens == 0) {
                    break;
                }
                if (isCloseParen(ch)) {
                    ++insideParens;
                } else {
                    if (isOpenParen(ch)) {
                        if (insideParens == 0) {
                            break;
                        } else {
                            --insideParens;
                        }
                    } else {
                        if (isQuoteChar(ch)) {
                            leftBound = SkipQuotedTextBackward(leftBound, 0);
                        }
                    }

                }
                --leftBound;
            }
        }

        private boolean isQuote(final int i) {
            return QUOTES.indexOf(getCharAt(i)) != -1;
        }

        private boolean isQuoteChar(final int ch) {
            return QUOTES.indexOf(ch) != -1;
        }

        private char getCharAt(int modelOffset) {
            assert modelOffset < text.getTextLength();
            return text.getText(modelOffset, 1).charAt(0);
        }

        private int SkipQuotedTextForward(final int start, final int end) {
            assert start < end;
            final char quoteChar = getCharAt(start);
            boolean backSlash = false;
            int i = start + 1;

            while (i < end) {
                final char ch = getCharAt(i);
                if (ch == quoteChar && !backSlash) {
                    // Found matching quote and it's not escaped.
                    break;
                } else {
                    if (ch == '\\') {
                        backSlash = !backSlash;
                    } else {
                        backSlash = false;
                    }
                }
                ++i;
            }
            return i;
        }

        private int SkipQuotedTextBackward(final int start, final int end) {
            assert start > end;
            final char quoteChar = getCharAt(start);
            int i = start - 1;

            while (i > end) {
                final char ch = getCharAt(i);
                final char prevChar = getCharAt(i - 1);
                // NOTE: doesn't handle cases like \\"str", but they make no
                //       sense anyway.
                if (ch == quoteChar && prevChar != '\\') {
                    // Found matching quote and it's not escaped.
                    break;
                }
                --i;
            }
            return i;
        }
    };

    public TextRange getRegion(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
        final ArgBoundsFinder finder = new ArgBoundsFinder(editorAdaptor.getModelContent());
        final CursorService cursorService = editorAdaptor.getCursorService();
        if (count == CountAwareMotion.NO_COUNT_GIVEN) {
            count = 1;
        }
        int start = cursorService.getPosition().getModelOffset();
        int left = 0;
        for (int i = 0; i < count; ++i) {
            finder.findBoundsAt(start);
            if (inner && (i == 0 || i == count - 1)) {
                finder.AdjustForInner();
            } else {
                finder.AdjustForOuter();
            }
            if (i == 0) {
                left = finder.getLeftBound();
            }
            start = finder.getRightBound();
        }
        return new StartEndTextRange(
                cursorService.newPositionForModelOffset(left),
                cursorService.newPositionForModelOffset(finder.getRightBound()));
    }

    public ContentType getContentType(Configuration configuration) {
        return ContentType.TEXT;
    }

}