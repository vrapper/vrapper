package net.sourceforge.vrapper.vim.commands.motions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.modes.VisualMode;

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
    
    public static final ParenthesesMove INSTANCE = new ParenthesesMove();
    private String mode;
    private Selection sel;

    @Override
    protected void setCurrentState(String mode, Selection sel) {
        this.mode = mode;
        this.sel = sel;
    }

    //default match algorithm, find match for character under cursor
    @Override
    protected int destination(int offset, TextContent content, int count) throws CommandExecutionException {
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
        if (pair == null) {
            if(CPreProcessorMove.containsPreProcessor(content, info, offset)) {
                return new CPreProcessorMove().destination(offset, content, count);
            }
            else {
                throw new CommandExecutionException("no parentheses to jump found");
            }
        }

        int match = findMatch(index, pair, content, count);
        // adjust for caret offset if the match is before the selection start
        // (see EvilCaret.java for details)
        if (VisualMode.NAME.equals(mode) && match < sel.getStart().getModelOffset()) {
            match--;
        }
        return match;
    }
    
    public static final ParenthesesMove MATCH_OPEN_PAREN = new ParenthesesMove() {
    	@Override
    	protected int destination(int offset, TextContent content, int count) throws CommandExecutionException {
    		return findMatch(offset, PARENTHESES.get(")"), content, count);
    	}
    	public BorderPolicy borderPolicy() {
    	    return BorderPolicy.EXCLUSIVE;
    	}
    };
    
    public static final ParenthesesMove MATCH_CLOSE_PAREN = new ParenthesesMove() {
    	@Override
    	protected int destination(int offset, TextContent content, int count) throws CommandExecutionException {
    		return findMatch(offset, PARENTHESES.get("("), content, count);
    	}
    	public BorderPolicy borderPolicy() {
    	    return BorderPolicy.EXCLUSIVE;
    	}
    };
    
    public static final ParenthesesMove MATCH_OPEN_CURLY = new ParenthesesMove() {
    	@Override
    	protected int destination(int offset, TextContent content, int count) throws CommandExecutionException {
    		return findMatch(offset, PARENTHESES.get("}"), content, count);
    	}
    	public BorderPolicy borderPolicy() {
    	    return BorderPolicy.EXCLUSIVE;
    	}
    };
    
    public static final ParenthesesMove MATCH_CLOSE_CURLY = new ParenthesesMove() {
    	@Override
    	protected int destination(int offset, TextContent content, int count) throws CommandExecutionException {
    		return findMatch(offset, PARENTHESES.get("{"), content, count);
    	}
    	public BorderPolicy borderPolicy() {
    	    return BorderPolicy.EXCLUSIVE;
    	}
    };
    
    private static int findMatch(int offset, ParenthesesPair pair, TextContent content, int count) {
    	int index = offset;
        int depth = count;
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
        while (index != limit && count > 0) {
            index += indexModifier;
            String c;
            try {
            	c = content.getText(index, 1);
            } catch(Exception e) {
            	return offset;
            }
            
            if (c.equals(pair.right)) {
                depth += rightModifier;
            } else if (c.equals(pair.left)) {
                depth += leftModifier;
            }
            if (depth == 0) {
                return index;
            }
        }
        return offset;
    }

    public BorderPolicy borderPolicy() {
        return BorderPolicy.INCLUSIVE;
    }

    @Override
    public boolean isJump() {
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
