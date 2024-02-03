package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.keymap.vim.OuterTextObject;
import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.AbstractTextObject;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.InnerTextObject;
import net.sourceforge.vrapper.vim.commands.QuoteDelimitedText;
import net.sourceforge.vrapper.vim.commands.TextObject;

/**
 * Quote text objects must be on the same line, but the
 * cursor doesn't have to be inside it.  Cursor can be
 * inside quotes or to the left of quotes.
 *     
 * If the cursor is *on* a quote then the number of quotes
 * before the cursor determines the behavior.  If there
 * are an even number of quotes before the cursor, the quote
 * on the cursor represents the left bound.  If there
 * are an odd number of quotes before the cursor, the quote
 * on the cursor represents the right bound.
 */
public class FindQuoteMotion extends AbstractModelSideMotion {
	
	private char quote;
	private boolean findLeft;
	private int startIndex = -1;
	
    public FindQuoteMotion(char quote, boolean findLeft) {
    	this.quote = quote;
    	this.findLeft = findLeft;
    }
	
    public FindQuoteMotion(char quote, int startIndex) {
    	this(quote, false);
    	this.startIndex = startIndex;
    }

	@Override
	public int destination(int offset, TextContent content, int count) throws CommandExecutionException {
		if(startIndex > -1) {
			//the left quote is after offset, we need to find
			//the next quote after *that*
			offset = startIndex;
		}
		
		LineInformation line = content.getLineInformationOfOffset(offset);
		int bol = line.getBeginOffset();
		int eol = line.getEndOffset();
		int limit = findLeft ? bol : eol;
		
		int index = getQuote(content, offset, limit, findLeft);
		if(findLeft && index == -1) {
			//there was no quote before the cursor,
			//maybe there's one after the cursor
			index = getQuote(content, offset, eol, false);
		}
		
		if(index == -1) {
			//no match found
            throw new CommandExecutionException("");
		}
		return index;
	}

	private int getQuote(TextContent content, int offset, int limit, boolean findLeft) {
		int index = offset;
        int step = findLeft ? -1 : 1;
		
		//if the cursor is *on* a quote, the number of quotes before the cursor
        //determines the behavior
		if(findLeft && isQuote(content, index)) {
			if(getNumQuotesBeforeOffset(limit, offset, content) % 2 == 0) {
				//there are an even number of quotes before the cursor
				//that means this quote starts a new balanced set
				return index;
			}
			else {
				//find the first quote before the cursor
				//(this quote will end up being the right bound)
				index--;
			}
		}
		
		while( findLeft ? index >= limit : index < limit) {
			if(isQuote(content, index)) {
				return index;
			}
			index += step;
		}
		return -1;
	}
	
	private int getNumQuotesBeforeOffset(int bol, int limit, TextContent content) {
		int index = bol;
		int numQuotes = 0;
		while(index < limit) {
			if(isQuote(content, index)) {
				numQuotes++;
			}
			index++;
		}
		return numQuotes;
	}
	
	private boolean isQuote(TextContent content, int offset) {
	    if(content.getText(offset, 1).charAt(0) == quote) {
	        if(offset == 0) {
	            return true;
	        }
	        else {
	            //skip escaped quotes
	            return content.getText(offset - 1, 1).charAt(0) != '\\';
	        }
	    }
	    return false;
	}

	public BorderPolicy borderPolicy() {
        return findLeft ? BorderPolicy.EXCLUSIVE : BorderPolicy.INCLUSIVE;
	}

    public static class FindQuoteTextObject extends AbstractTextObject {

        private TextObject inQuotes;
        private TextObject includeQuotes;
        private boolean defaultInner;

        private FindQuoteTextObject(char delimiter, boolean defaultInner) {
            QuoteDelimitedText quoteDelimitedText = new QuoteDelimitedText(delimiter);
            if (defaultInner) {
                inQuotes = new InnerTextObject(quoteDelimitedText);
            }
            includeQuotes = new OuterTextObject(quoteDelimitedText);
            this.defaultInner = defaultInner;
        }

        public static TextObject inner(char delimiter) {
            return new FindQuoteTextObject(delimiter, true);
        }

        public static TextObject outer(char delimiter) {
            return new FindQuoteTextObject(delimiter, false);
        }

        @Override
        public TextRange getRegion(EditorAdaptor editorAdaptor, int count)
                throws CommandExecutionException {
            if (count > 1 && defaultInner) {
                return includeQuotes.getRegion(editorAdaptor, count);
            } else if (defaultInner) {
                return inQuotes.getRegion(editorAdaptor, count);
            } else {
                TextRange region = includeQuotes.getRegion(editorAdaptor, count);
                //Include whitespace up to next word or EOL
                CursorService cursorService = editorAdaptor.getCursorService();
                TextContent content = editorAdaptor.getModelContent();
                int rightOffset = region.getRightBound().getModelOffset();

                LineInformation lineInfo = content.getLineInformationOfOffset(rightOffset);

                while (rightOffset < lineInfo.getEndOffset()
                        && Character.isWhitespace(content.getText(rightOffset, 1).charAt(0))) {
                    rightOffset++;
                }
                Position rightPos = cursorService.newPositionForModelOffset(rightOffset);
                region = new StartEndTextRange(region.getLeftBound(), rightPos);
                return region;
            }
        }

        @Override
        public ContentType getContentType(Configuration configuration) {
            return ContentType.TEXT;
        }

    }
}
