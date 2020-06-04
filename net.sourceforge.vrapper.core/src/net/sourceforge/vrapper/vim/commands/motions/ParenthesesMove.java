package net.sourceforge.vrapper.vim.commands.motions;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

// TODO: can we use underlying eclipse to do extra-smart stuff
// like matching XML tags, LaTeX \begin{paragraph}\end{paragraph},

/**
 * Moves to matching paretheses.
 *
 * @author Matthias Radig
 */
public class ParenthesesMove extends AbstractModelSideMotion {
    private static final Map<String, ParenthesesPair> PARENTHESES = new HashMap<String, ParenthesesPair>();

    static {
    	//default values for "matchpairs" setting
        PARENTHESES.put("(", new ParenthesesPair("(", ")", false));
        PARENTHESES.put("{", new ParenthesesPair("{", "}", false));
        PARENTHESES.put("[", new ParenthesesPair("[", "]", false));
        PARENTHESES.put(")", new ParenthesesPair("(", ")", true ));
        PARENTHESES.put("}", new ParenthesesPair("{", "}", true ));
        PARENTHESES.put("]", new ParenthesesPair("[", "]", true ));
        //hack to support multi-char pairs
        PARENTHESES.put("/*", new ParenthesesPair("/*", "*/", false));
        PARENTHESES.put("*/", new ParenthesesPair("/*", "*/", true));
    }
    
    public static final ParenthesesMove INSTANCE = new ParenthesesMove();
    
    public void addParentheses(String open, String close) {
    	PARENTHESES.put(open, new ParenthesesPair(open, close, false));
    	PARENTHESES.put(close, new ParenthesesPair(open, close, true));
    }
    
    public void removeParentheses(String open, String close) {
    	PARENTHESES.remove(open);
    	PARENTHESES.remove(close);
    }

    //default match algorithm, find match for character under cursor
    @Override
    protected int destination(int offset, TextContent content, int count) throws CommandExecutionException {
        LineInformation line = content.getLineInformationOfOffset(offset);

        // Check for one-character pairs.
        int startIndex = offset;
        if (offset == line.getEndOffset() && line.getLength() > 0) {
            // Special case in visual mode: if the cursor is past the end of
            // line (on top of the line break), we should start looking at the
            // character before.
            startIndex--;
        }
        for (int index = startIndex; index < line.getEndOffset(); index++) {
            String c = content.getText(index, 1);
            if (PARENTHESES.containsKey(c)) {
                return findMatch(index, PARENTHESES.get(c), content, count);
            }
        }

        // Check for a block comment.
        ParenthesesPair pair = getBlockComment(offset, content);
        if (pair != null) {
            return findBlockMatch(offset, pair, content);
        }
        
        // Check for C Pre-processor conditionals.
        if (CPreProcessorMove.containsPreProcessor(content, line, offset)) {
            return new CPreProcessorMove().destination(offset, content, count);
        }

        throw new CommandExecutionException("no parentheses to jump found");
    }
    
    /**
     * In Vim, the '%' feature for block comments requires the cursor to be on
     * either the '/' or the '*' character.  So, look at the character under the
     * cursor and the character before/after the cursor to see if it matches
     * that two-character string.  This is different from other matching pairs where
     * the cursor can be anywhere before the open character on that line.
     */
    private ParenthesesPair getBlockComment(int offset, TextContent content) {
        String underCursor = content.getText(offset, 1);
        //first check, is the cursor on one of these two characters?
        if(underCursor.equals("*") || underCursor.equals("/")) {
            //make sure we don't go past file boundaries when looking before/after cursor
            int start = Math.max(0, offset - 1);
            //index is exclusive, so +2 to get character after cursor
            int end = Math.min(offset + 2, content.getTextLength());

            String multiChar = content.getText(start, end - start);
            //if cursor is on file boundary, length = 2
            //otherwise, we should have 3 characters
            if(multiChar.length() == 2 && PARENTHESES.containsKey(multiChar)) {
                return PARENTHESES.get(multiChar);
            }
            else if(multiChar.length() > 2) {
                //look at chars before and under cursor
                if(PARENTHESES.containsKey(multiChar.substring(0, 2))) {
                    return PARENTHESES.get(multiChar.substring(0, 2));
                }
                //look at chars under and after cursor
                else if(PARENTHESES.containsKey(multiChar.substring(1, 3))) {
                    return PARENTHESES.get(multiChar.substring(1, 3));
                }
            }
        }

        return null;
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
    
    /**
     * Find matching block comment open/close token.  We don't have to worry
     * about nesting levels since block comments can't be nested.
     */
    private int findBlockMatch(int offset, ParenthesesPair pair, TextContent content) {
    	int index = offset;
        int limit, indexModifier;
        String match;
        if (pair.backwards) {
            limit = 0;
            indexModifier = -1;
            match = pair.left;
        } else {
            limit = content.getTextLength() -1;
            indexModifier = 1;
            match = pair.right;
        }
        
        //trying not to assume we have a two-character comment
        //....even though we do
        int matchLength = match.length();
        while(index != limit) {
            index += indexModifier;
            String c;
            try {
                c = content.getText(index, matchLength);
            } catch(Exception e) {
                return offset;
            }
            
            if(c.equals(match)) {
                //should the cursor go to the beginning or end of the match?
                return pair.backwards ? index : index + matchLength ;
            }
        }

        return offset;
    }
    
    /**
     * Find matching open/close character.  Keep track of nesting levels
     * so we match the correct pair.
     */
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
