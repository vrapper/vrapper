package net.sourceforge.vrapper.vim.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sourceforge.vrapper.utils.LineRange;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.SimpleLineRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

/**
 * Format text based on the configuration setting 'textwidth'.
 * That is, take the selected lines and ensure that no line exceeds
 * 'textwidth' characters.  If a line is shorter than 'textwidth',
 * merge it with the line below.  If a line is longer than 'textwidth',
 * split it into extra lines.
 * 
 * This follows Vim behavior as closely as possible when dealing with comments.
 * All lines starting with '//', '#', or '*' will merge with adjacent lines
 * of the same comment type.  Lines with conflicting comment characters
 * (or no leading comment characters at all) will not be merged.  Blank lines
 * are always preserved.
 * 
 * This is mapped to the 'gq<text object>' operation.
 */
public class FormatOperation extends AbstractLinewiseOperation {
	
	public static final FormatOperation INSTANCE = new FormatOperation();

	public TextOperation repetition() {
		return this;
	}

	/** Likely never called seeing how this operation is not used as an Ex command. */
	@Override
	public LineRange getDefaultRange(EditorAdaptor editorAdaptor, int count, Position currentPos)
			throws CommandExecutionException {
		return SimpleLineRange.singleLine(editorAdaptor, currentPos);
	}

	@Override
	public void execute(EditorAdaptor editorAdaptor, LineRange lineRange) throws CommandExecutionException {
		TextRange originalTextRange = lineRange.getRegion(editorAdaptor, 0);
		String text = editorAdaptor.getModelContent().getText(originalTextRange);
		String newlineChar = editorAdaptor.getConfiguration().getNewLine();
		String[] lines = text.split(newlineChar);
		int textwidth = editorAdaptor.getConfiguration().get(Options.TEXT_WIDTH);
		
		CommentedLine current;
		CommentedLine previous = null;
		List<String> formattedLines = new ArrayList<String>();
		List<String> processed;
		for(int i=0; i < lines.length; i++) {
			current = new CommentedLine(lines[i]);
			if( ! current.allowFormat()) {
				formattedLines.add(current.getFullLine());
				previous = null;
				continue;
			}
			
			//if we can merge with line above
			//(must have matching comment characters)
			if(previous != null && previous.getContinueChar().equals(current.getCommentChar())) {
				processed = formatLines(new ArrayList<String>(),
						new CommentedLine(
								previous.getPreIndent(),
								previous.getCommentChar(),
								previous.getPostIndent(),
								previous.getText()+" "+current.getText(),
								previous.getContinueChar()),
						textwidth
				);
				//remove old 'previous' text
				formattedLines.remove(formattedLines.size() - 1);
				//add all new lines (including new version of 'previous')
				formattedLines.addAll(processed);
				//prepare for next iteration
				previous = new CommentedLine(processed.get(processed.size()-1));
			}
			//we can't merge with line above, does this line need to be split?
			else {
				processed = formatLines(new ArrayList<String>(), current, textwidth);
				formattedLines.addAll(processed);
				//prepare for next iteration
				previous = new CommentedLine(processed.get(processed.size()-1));
			}
			
		}
		
		String newLines = "";
		for(String line : formattedLines) {
			newLines += line + newlineChar;
		}
		
		//swap out the old text with the formatted text
		int start = originalTextRange.getLeftBound().getModelOffset();
		int length = originalTextRange.getModelLength();
		editorAdaptor.getModelContent().replace(start, length, newLines);
		editorAdaptor.getCursorService().setPosition(originalTextRange.getStart(), StickyColumnPolicy.ON_CHANGE);
	}
	
	/**
	 * Take a long line and split it on word boundaries closest to textwidth.
	 * Note that this may result in multiple lines.  Each new line will
	 * share the same indentation and comment character as the first.
	 */
	private List<String> formatLines(List<String> formatted, CommentedLine textToFormat, int textwidth) {
		if(textToFormat.getFullLine().length() <= textwidth) {
			formatted.add(textToFormat.getFullLine());
		}
		else {
			String text = textToFormat.getText();
			//make sure we don't end up with indentation when
			//trying to find the closest whitespace character
			int adjustedTextWidth = textwidth - textToFormat.getPrefix().length();
			adjustedTextWidth = Math.max(0, adjustedTextWidth);
			int lineBreak = getLastWhitespaceBeforeWidth(text, adjustedTextWidth);
			if(lineBreak < 0) {
				//if we can't break cleanly within textwidth,
				//break as close to it as we can
				lineBreak = getFirstWhitespaceAfterWidth(text, adjustedTextWidth);
			}
			String line = text.substring(0, lineBreak);
			String remainder = text.substring(lineBreak).trim();
			formatted.add(textToFormat.getPrefix() + line);
			
			if(remainder.length() > 0) {
				CommentedLine newLine = new CommentedLine(
						textToFormat.getPreIndent(),
						textToFormat.getContinueChar(),
						textToFormat.getPostIndent(),
						remainder,
						textToFormat.getContinueChar()
				);
				//recursion!
				return formatLines(formatted, newLine, textwidth);
			}
		}
		
		return formatted;
	}
	
	private int getLastWhitespaceBeforeWidth(String text, int width) {
		for(int i=width; i > 0; i--) {
			if(Character.isWhitespace(text.charAt(i))) {
				return i;
			}
		}
		//no whitespace available
		return -1;
	}
	
	private int getFirstWhitespaceAfterWidth(String text, int width) {
		for(int i=width; i < text.length(); i++) {
			if(Character.isWhitespace(text.charAt(i))) {
				return i;
			}
		}
		//no whitespace available
		return text.length();
	}
	
	/**
	 * Take a line of text and find the following pieces:
	 * - indent prior to comment character
	 * - comment character 
	 * - indent after comment character
	 * - line contents
	 */
	private class CommentedLine {
	
		public final Set<String> SINGLE_LINE_COMMENTS = VimUtils.set("//", "#", "*");
		public static final String MULTI_LINE_START = "/*";
		public static final String MULTI_LINE_END = "*/";
	
		private boolean allowFormat = true;
		private String fullLine = "";
		private String preIndent = "";
		private String commentChar = "";
		private String continueChar = "";
		private String postIndent = "";
		private String text = "";

		public boolean allowFormat()     { return allowFormat;   }
		public String  getFullLine()     { return fullLine;      }
		public String  getPreIndent()    { return preIndent;     }
		public String  getCommentChar()  { return commentChar;   }
		public String  getContinueChar() { return continueChar;  }
		public String  getPostIndent()   { return postIndent;    }
		public String  getPrefix()       { return preIndent + commentChar + postIndent; }
		public String  getText()         { return text;          }
		
		/**
		 * This constructor is used when we're modifying the text
		 * of an existing CommentedLine.  No need to re-parse
		 * all the pieces.
		 */
		public CommentedLine(String preIndent, String commentChar, String postIndent,
				String text, String continueChar) {
			this.preIndent = preIndent;
			this.commentChar = commentChar;
			this.postIndent = postIndent;
			this.text = text;
			this.continueChar = continueChar;
			this.fullLine = preIndent + commentChar + postIndent + text;
		}
		
		public CommentedLine(String line) {
			fullLine = line;
			
			//grab indent (if any)
			int indentOffset = getFirstNonWhiteSpaceOffset(line);
			if(indentOffset > -1) {
				preIndent = line.substring(0, indentOffset);
				text = line.substring(indentOffset);
			}
			else {
				preIndent = "";
				text = line;
			}
			
			if(text.matches("\\s*") || text.startsWith(MULTI_LINE_END)) {
				//blank lines and end of multi-line comments never format
				allowFormat = false;
			}
			else if(text.startsWith(MULTI_LINE_START)) {
				commentChar = MULTI_LINE_START;
				continueChar = "*";
				//chop off comment char
				text = text.substring(MULTI_LINE_START.length());
			}
			else {
				for(String comment : SINGLE_LINE_COMMENTS) {
					if(text.startsWith(comment)) {
						commentChar = comment;
						continueChar = comment;
						//chop off comment char
						text = text.substring(commentChar.length());
						break;
					}
				}
			}
			
			//get text after comment char
			int postCommentOffset = getFirstNonWhiteSpaceOffset(text);
			if(postCommentOffset > -1) {
				postIndent = text.substring(0, postCommentOffset);
				text = text.substring(postCommentOffset);
			}
			else {
				//empty lines with comment char aren't formatted
				allowFormat = false;
			}
		}
	
		private int getFirstNonWhiteSpaceOffset(String line) {
			for(int i=0; i < line.length(); i++) {
				if(! Character.isWhitespace(line.charAt(i))) {
					return i;
				}
			}
			//all whitespace
			return -1;
		}
	}

}
