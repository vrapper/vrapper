package de.jroene.vrapper.vim.token;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.jroene.vrapper.vim.LineInformation;
import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.VimEmulator;

/**
 * Moves to matching paretheses.
 *
 * @author Matthias Radig
 */
public class ParenthesesMove extends AbstractRepeatableMove {
    private static final Map<String, ParenthesesPair> PARENTHESES;

    static {
        HashMap<String, ParenthesesPair> op = new HashMap<String, ParenthesesPair>();
        op.put("(", new ParenthesesPair("(", ")", false));
        op.put("{", new ParenthesesPair("{", "}", false));
        op.put("[", new ParenthesesPair("[", "]", false));
        op.put(")", new ParenthesesPair("(", ")", true ));
        op.put("}", new ParenthesesPair("{", "}", true ));
        op.put("]", new ParenthesesPair("[", "]", true ));
        PARENTHESES = Collections.unmodifiableMap(op);
    }
    @Override
    public int calculateTarget(VimEmulator vim, int times, Token next) {
        Platform p = vim.getPlatform();
        int position = p.getPosition();
        LineInformation info = p.getLineInformation();
        int index = position;
        ParenthesesPair pair = null;
        for(index=position; index<info.getEndOffset(); index++) {
            String c = p.getText(index, 1);
            if (PARENTHESES.containsKey(c)) {
                pair = PARENTHESES.get(c);
                break;
            }
        }
        if (pair != null) {
            int depth = 1;
            int leftModifier, rightModifier, limit, indexModifier;
            if (pair.backwards) {
                leftModifier = -1;
                rightModifier = 1;
                limit = 0;
                indexModifier = -1;
            } else {
                leftModifier = 1;
                rightModifier = -1;
                limit = p.getLineInformation(p.getNumberOfLines()-1).getEndOffset();
                indexModifier = 1;
            }
            while (index != limit) {
                index += indexModifier;
                String c = p.getText(index, 1);
                if (c.equals(pair.right)) {
                    depth += rightModifier;
                } else if (c.equals(pair.left)) {
                    depth += leftModifier;
                }
                if (depth == 0) {
                    return index;
                }
            }
        }
        return position;
    }

    @Override
    public boolean isHorizontal() {
        return true;
    }

    @Override
    public boolean includesTarget() {
        return true;
    }

    private static class ParenthesesPair {
        private final String left;
        private final String right;
        private final boolean backwards;
        public ParenthesesPair(String left, String right, boolean backwards) {
            super();
            this.left = left;
            this.right = right;
            this.backwards = backwards;
        }
    }

}
