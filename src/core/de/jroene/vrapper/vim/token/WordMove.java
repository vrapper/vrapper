package de.jroene.vrapper.vim.token;

import java.util.regex.Pattern;

import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.VimConstants;
import de.jroene.vrapper.vim.VimEmulator;

/**
 * Different moves over words.
 *
 * @author Matthias Radig
 */
public abstract class WordMove extends AbstractRepeatableHorizontalMove {

    protected final Pattern pattern;

    WordMove(String regex) {
        super();
        pattern = Pattern.compile(regex);
    }

    protected boolean isCharacter(String s) {
        return pattern.matcher(s).find();
    }

    protected static boolean isNewLine(String s) {
        return VimConstants.NEWLINE.startsWith(s);
    }

    public boolean isWhiteSpace(String s) {
        return VimConstants.WHITESPACE.contains(s);
    }

    /**
     * Move to the begin of the next word.
     *
     * @author Matthias Radig
     */
    public static class NextBegin extends WordMove {

        public NextBegin(String regex) {
            super(regex);
        }

        @Override
        public int calculateTarget(VimEmulator vim, int times, Token next) {
            return getTarget(vim, times, false);
        }

        public int getTarget(VimEmulator vim, int times, boolean stopAtNewline) {
            Platform p = vim.getPlatform();
            int end = p.getLineInformation(p.getNumberOfLines()-1).getEndOffset();
            int index = p.getPosition();
            boolean found = false;
            int n = 0;
            while (n < times) {
                String firstChar = index < end ? p.getText(index, 1) : "";
                boolean isTerminatorWord = !(isCharacter(firstChar) || isWhiteSpace(firstChar));
                while (index < end) {
                    String s = p.getText(index, 1);
                    boolean isWhiteSpace = isWhiteSpace(s);
                    boolean isTerminator = !isWhiteSpace && !(isTerminatorWord ^ isCharacter(s));
                    if (!found && isTerminator) {
                        break;
                    } else if (!found && isWhiteSpace) {
                        if(n == times-1 && stopAtNewline && isNewLine(s)) {
                            return index;
                        }
                        found = true;
                    }
                    else if (found && !isWhiteSpace) {
                        break;
                    }
                    index += 1;
                }
                n += 1;
                found = false;
            }
            return index;
        }

        public Token createNextEndMove() {
            return new NextEnd(pattern.pattern());
        }
    }

    /**
     * Move to the next end of a word.
     *
     * @author Matthias Radig
     */
    public static class NextEnd extends WordMove {

        public NextEnd(String regex) {
            super(regex);
        }

        @Override
        public int calculateTarget(VimEmulator vim, int times, Token next) {
            Platform p = vim.getPlatform();
            int index = p.getPosition();
            int end = p.getLineInformation(p.getNumberOfLines()-1).getEndOffset();
            int n = 0;
            boolean found = false;
            while (n < times) {
                String firstChar = index < end ? p.getText(index, 1) : "";
                boolean isTerminatorWord = !(isCharacter(firstChar) || isWhiteSpace(firstChar));
                index = Math.min(index+1, end);
                while (index < end) {
                    String s = p.getText(index, 1);
                    boolean isWhiteSpace = isWhiteSpace(s);
                    if (!found && !isWhiteSpace) {
                        firstChar = p.getText(index, 1);
                        isTerminatorWord = !(isCharacter(firstChar) || isWhiteSpace(firstChar));
                    }
                    boolean isTerminator = found && (isWhiteSpace || !(isTerminatorWord ^ isCharacter(s)));
                    if (!found && !isWhiteSpace){
                        found = true;
                    } else if (found && isTerminator) {
                        index -= 1;
                        break;
                    }
                    index += 1;
                }
                found = false;
                n += 1;
            }
            return index;
        }

        @Override
        public boolean includesTarget() {
            return true;
        }
    }

    /**
     * Move to the last begin of a word.
     *
     * @author Matthias Radig
     */
    public static class LastBegin extends WordMove {

        public LastBegin(String regex) {
            super(regex);
        }

        @Override
        public int calculateTarget(VimEmulator vim, int times, Token next) {
            Platform p = vim.getPlatform();
            int index = p.getPosition();
            int n = 0;
            boolean found = false;
            int end = p.getLineInformation(p.getNumberOfLines()-1).getEndOffset();
            while (n < times) {
                String firstChar = index > 0 && index < end ? p.getText(index, 1) : "";
                boolean isTerminatorWord = !(isCharacter(firstChar) || isWhiteSpace(firstChar));
                index = Math.max(index-1, 0);
                while (index > 0) {
                    String s = p.getText(index, 1);
                    boolean isWhiteSpace = isWhiteSpace(s);
                    if (!found && !isWhiteSpace) {
                        firstChar = p.getText(index, 1);
                        isTerminatorWord = !(isCharacter(firstChar) || isWhiteSpace(firstChar));
                    }
                    boolean isTerminator = found && (isWhiteSpace || !(isTerminatorWord ^ isCharacter(s)));
                    if (!found && !isWhiteSpace){
                        found = true;
                    } else if (found && isTerminator) {
                        index += 1;
                        break;
                    }
                    index -= 1;
                }
                found = false;
                n += 1;
            }
            return index;
        }
    }
}
