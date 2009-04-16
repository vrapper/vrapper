package net.sourceforge.vrapper.vim.commands.motions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;

// TODO: can we use underlying eclipse to do extra-smart stuff
// like matching XML tags, LaTeX \begin{paragraph}\end{paragraph},

/**
 * Moves to matching paretheses.
 *
 * @author Matthias Radig
 */
public class ParenthesesMove extends AbstractModelSideMotion {
    private static final Map<String, ParenthesesPair> PARENTHESES;

    static {
        Map<String, ParenthesesPair> op = new HashMap<String, ParenthesesPair>();
        op.put("(", new ParenthesesPair("(", ")", false));
        op.put("{", new ParenthesesPair("{", "}", false));
        op.put("[", new ParenthesesPair("[", "]", false));
        op.put(")", new ParenthesesPair("(", ")", true ));
        op.put("}", new ParenthesesPair("{", "}", true ));
        op.put("]", new ParenthesesPair("[", "]", true ));
        PARENTHESES = Collections.unmodifiableMap(op);
    }

	@Override
	protected int destination(int offset, TextContent content, int count) {
		LineInformation info = content.getLineInformationOfOffset(offset);
        int index = offset;
        ParenthesesPair pair = null;
        for(index=offset; index<info.getEndOffset(); index++) {
            String c = content.getText(index, 1);
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
                limit = content.getLineInformation(content.getNumberOfLines()-1).getEndOffset();
                indexModifier = 1;
            }
            while (index != limit) {
                index += indexModifier;
                String c = content.getText(index, 1);
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
        return offset;
	}

	@Override
	public BorderPolicy borderPolicy() {
		return BorderPolicy.INCLUSIVE;
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
