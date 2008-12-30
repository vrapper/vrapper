package de.jroene.vrapper.vim.token;

import java.util.Set;

import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.VimConstants;
import de.jroene.vrapper.vim.VimEmulator;

/**
 * Different moves over words.
 *
 * @author Matthias Radig
 */
public abstract class WordMove extends AbstractRepeatableHorizontalMove {

    final Set<String> terminators;

    WordMove(Set<String> terminators) {
        super();
        this.terminators = terminators;
    }

    protected boolean isTerminator(String s) {
        return terminators.contains(s);
    }

    protected static boolean isNewLine(String s) {
        return VimConstants.NEWLINE.equals(s);
    }

    /**
     * Move to the begin of the next word.
     *
     * @author Matthias Radig
     */
    public static class NextBegin extends WordMove {

        public NextBegin(Set<String> terminators) {
            super(terminators);
        }

        @Override
        public int calculateTarget(VimEmulator vim, int times, Token next) {
            return getTarget(vim, times, false);
        }

        public int getTarget(VimEmulator vim, int times, boolean stopAtNewline) {
            Platform p = vim.getPlatform();
            int end = p.getLineInformation(p.getNumberOfLines()-1).getEndOffset();
            int index = Math.min(p.getPosition()+1, end);
            boolean found = false;
            int n = 0;
            while (n < times) {
                while (index < end) {
                    String s = p.getText(index, 1);
                    boolean isTerminator = isTerminator(s);
                    if (!found && isTerminator) {
                        if(n == times-1 && stopAtNewline && isNewLine(s)) {
                            return index;
                        }
                        found = true;
                    } else if (found && !isTerminator) {
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
            return new NextEnd(terminators);
        }
    }

    /**
     * Move to the next end of a word.
     *
     * @author Matthias Radig
     */
    public static class NextEnd extends WordMove {

        public NextEnd(Set<String> terminators) {
            super(terminators);
        }

        @Override
        public int calculateTarget(VimEmulator vim, int times, Token next) {
            Platform p = vim.getPlatform();
            int index = p.getPosition();
            int end = p.getLineInformation(p.getNumberOfLines()-1).getEndOffset();
            int n = 0;
            boolean found = false;
            while (n < times) {
                while (index < end) {
                    index += 1;
                    String s = p.getText(index, 1);
                    boolean isTerminator = isTerminator(s);
                    if (!found && !isTerminator) {
                        found = true;
                    } else if (found && isTerminator) {
                        index -= 1;
                        break;
                    }
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

        public LastBegin(Set<String> terminators) {
            super(terminators);
        }

        @Override
        public int calculateTarget(VimEmulator vim, int times, Token next) {
            Platform p = vim.getPlatform();
            int index = p.getPosition();
            int n = 0;
            boolean found = false;
            while (n < times) {
                while (index > 0) {
                    index -= 1;
                    String s = p.getText(index, 1);
                    boolean isTerminator = isTerminator(s);
                    if (!found && !isTerminator) {
                        found = true;
                    } else if (found && isTerminator) {
                        index += 1;
                        break;
                    }
                }
                found = false;
                n += 1;
            }
            return index;
        }
    }
}
