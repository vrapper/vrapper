package net.sourceforge.vrapper.plugin.argtextobj.commands;

import java.util.Stack;

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
    private static class ArgBoundsFinder {
        final private TextContent text;
        private int leftBound;
        private int rightBound;
        private int leftBracket;
        private int rightBracket;
        final private static String QUOTES = "\"\'";
        // NOTE: brackets must match by index and ordered by rank.
        final private static String OPEN_BRACKETS  = "[{(<";
        final private static String CLOSE_BRACKETS = "]})>";

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
                if (isCloseBracket(getCharAt(rightBound))) {
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
            boolean bothBrackets;
            do {
                leftBracket = nextLeft;
                rightBracket = nextRight;
                if (!findOuterBrackets(0, text.getTextLength() - 1)) {
                    throw new CommandExecutionException("not inside argument list");
                }
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
                bothBrackets = getCharAt(leftBound) != ',' && getCharAt(rightBound) != ',';
                if (bothBrackets && isIdentBackward()) {
                    // Looking at a pair of brackets preceded by an
                    // identifier -- single argument function call.
                    break;
                }
            } while (leftBound > 0 && rightBound < text.getTextLength() && bothBrackets);
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
                if (rightBound + 1 < rightBracket && getCharAt(rightBound) == ',') {
                    ++rightBound;
                    while (rightBound + 1 < rightBracket
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
                   final int endOfQuotedText = skipQuotedTextForward(i, line.getEndOffset());
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
            while (rightBound < rightBracket) {
                final char ch = getCharAt(rightBound);
                if (ch == ',') {
                    break;
                }
                if (isOpenBracket(ch)) {
                    rightBound = skipSexp(rightBound, rightBracket, SexpDirection.FORWARD);
                } else {
                    if (isQuoteChar(ch)) {
                        rightBound = skipQuotedTextForward(rightBound, rightBracket);
                    }
                    ++rightBound;
                }
            }
        }

        static private char matchingBracket(char ch) {
            int idx = CLOSE_BRACKETS.indexOf(ch);
            if (idx != -1) {
                return OPEN_BRACKETS.charAt(idx);
            } else {
                assert isOpenBracket(ch);
                idx = OPEN_BRACKETS.indexOf(ch);
                return CLOSE_BRACKETS.charAt(idx);
            }
        }

        static private boolean isCloseBracket(final int ch) {
            return CLOSE_BRACKETS.indexOf(ch) != -1;
        }

        static private boolean isOpenBracket(final int ch) {
            return OPEN_BRACKETS.indexOf(ch) != -1;
        }

        private void findLeftBound() {
            while (leftBound > leftBracket) {
                final char ch = getCharAt(leftBound);
                if (ch == ',') {
                    break;
                }
                if (isCloseBracket(ch)) {
                    leftBound = skipSexp(leftBound, leftBracket, SexpDirection.BACKWARD);
                } else {
                    if (isQuoteChar(ch)) {
                        leftBound = skipQuotedTextBackward(leftBound, leftBracket);
                    }
                    --leftBound;
                }
            }
        }

        private boolean isQuote(final int i) {
            return QUOTES.indexOf(getCharAt(i)) != -1;
        }

        static private boolean isQuoteChar(final int ch) {
            return QUOTES.indexOf(ch) != -1;
        }

        private char getCharAt(int modelOffset) {
            assert modelOffset < text.getTextLength();
            return text.getText(modelOffset, 1).charAt(0);
        }

        private int skipQuotedTextForward(final int start, final int end) {
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

        private int skipQuotedTextBackward(final int start, final int end) {
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

        /**
         * Interface to parametrise S-expression traversal direction.
         */
        abstract static class SexpDirection {
            abstract int delta();
            abstract boolean isOpenBracket(char ch);
            abstract boolean isCloseBracket(char ch);
            abstract int skipQuotedText(int pos, int start, int end, ArgBoundsFinder self);
            static final SexpDirection FORWARD = new SexpDirection() {
                @Override int delta() { return 1; }
                @Override boolean isOpenBracket(char ch) { return ArgBoundsFinder.isOpenBracket(ch); }
                @Override boolean isCloseBracket(char ch) { return ArgBoundsFinder.isCloseBracket(ch); }
                @Override  int skipQuotedText(int pos, int start, int end, ArgBoundsFinder self) {
                    return self.skipQuotedTextForward(pos, end);
                }
            };
            static final SexpDirection BACKWARD = new SexpDirection() {
                @Override int delta() { return -1; }
                @Override boolean isOpenBracket(char ch) { return ArgBoundsFinder.isCloseBracket(ch); }
                @Override boolean isCloseBracket(char ch) { return ArgBoundsFinder.isOpenBracket(ch); }
                @Override  int skipQuotedText(int pos, int start, int end, ArgBoundsFinder self) {
                    return self.skipQuotedTextBackward(pos, start);
                }
            };
        }

        /**
         * Skip over S-expression considering priorities when unbalanced.
         * @param start position of the starting bracket.
         * @param end maximum position
         * @param dir direction instance
         * @return position after S-expression or next to the start position if 
         *         unbalanced.
         */
        private int skipSexp(final int start, final int end, SexpDirection dir) {
            char lastChar = getCharAt(start);
            assert dir.isOpenBracket(lastChar);
            Stack<Character> bracketStack = new Stack<Character>();
            bracketStack.push(lastChar);
            int i = start + dir.delta();
            while (!bracketStack.empty() && i != end) {
                final char ch = getCharAt(i);
                if (dir.isOpenBracket(ch)) {
                    bracketStack.push(ch);
                } else {
                    if (dir.isCloseBracket(ch)) {
                        if (bracketStack.lastElement() == matchingBracket(ch)) {
                            bracketStack.pop();
                        } else {
                            // Unbalanced brackets -- check ranking.
                            if (getBracketPrio(ch) < getBracketPrio(bracketStack.lastElement())) {
                                // (<...) ->  (...)
                                bracketStack.pop();
                                // Retry the same character again for cases like (...<<...).
                                continue;
                            } else {
                                // Ignore lower-priority closing brackets.
                                // (...> ->  (....
                            }
                        }
                    } else {
                        if (isQuoteChar(ch)) {
                            i = dir.skipQuotedText(i, start, end, this);
                        }
                    }
                }
                lastChar = ch;
                i += dir.delta();
            }
            if (bracketStack.empty()) {
                return i;
            } else {
                return start + dir.delta();
            }
        }

        /**
         * @return rank of a bracket.
         */
        static int getBracketPrio(char ch) {
            return Math.max(OPEN_BRACKETS.indexOf(ch), CLOSE_BRACKETS.indexOf(ch));
        }

        /**
         * Find a pair of brackets surrounding (leftBracket..rightBracket) block.
         * @param start minimum position to look for
         * @param end maximum position
         * @return true if found
         */
        public boolean findOuterBrackets(final int start, final int end) {
            boolean hasNewBracket = findPrevOpenBracket(start) && findNextCloseBracket(end);
            while (hasNewBracket) {
                final int leftPrio = getBracketPrio(getCharAt(leftBracket));
                final int rightPrio = getBracketPrio(getCharAt(rightBracket));
                if (leftPrio == rightPrio) {
                    // matching brackets
                    return true;
                } else {
                    if (leftPrio < rightPrio) {
                        if (rightBracket + 1 < end) {
                            ++rightBracket;
                            hasNewBracket = findNextCloseBracket(end);
                        } else {
                            hasNewBracket = false;
                        }
                    } else {
                        if (leftBracket > 1) {
                            --leftBracket;
                            hasNewBracket = findPrevOpenBracket(start);
                        } else {
                            hasNewBracket = false;
                        }
                    }
                }
            }
            return false;
        }

        /**
         * Finds unmatched open bracket starting at @a leftBracket.
         * @param start minimum position.
         * @return true if found
         */
        private boolean findPrevOpenBracket(final int start) {
            char ch;
            while (!isOpenBracket(ch = getCharAt(leftBracket))) {
                if (isCloseBracket(ch)) {
                    leftBracket = skipSexp(leftBracket, start, SexpDirection.BACKWARD);
                } else {
                    if (isQuoteChar(ch)) {
                        leftBracket = skipQuotedTextBackward(leftBracket, start);
                    } else {
                        if (leftBracket == start) {
                            return false;
                        }
                    }
                    --leftBracket;
                }
            }
            return true;
        }

        /**
         * Finds unmatched close bracket starting at @a rightBracket.
         * @param end maximum position.
         * @return true if found
         */
        private boolean findNextCloseBracket(final int end) {
            char ch;
            while (!isCloseBracket(ch = getCharAt(rightBracket))) {
                if (isOpenBracket(ch)) {
                    rightBracket = skipSexp(rightBracket, end, SexpDirection.FORWARD);
                } else {
                    if (isQuoteChar(ch)) {
                        rightBracket = skipQuotedTextForward(rightBracket, end);
                    } else {
                        if (rightBracket + 1 == end) {
                            return false;
                        }
                    }
                    ++rightBracket;
                }
            }
            return true;
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