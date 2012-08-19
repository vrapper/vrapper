package net.sourceforge.vrapper.vim.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;

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
public class FormatOperation implements TextOperation {
	
	public static final FormatOperation INSTANCE = new FormatOperation();
	
	public static final Set<String> SINGLE_LINE_COMMENTS = VimUtils.set("//", "#", "*");
	public static final String MULTI_LINE_START = "/*";
	public static final String MULTI_LINE_END = "*/";

	public TextOperation repetition() {
		return this;
	}

	public void execute(EditorAdaptor editorAdaptor, int count, TextObject textObject) throws CommandExecutionException {
		String text = editorAdaptor.getModelContent().getText(textObject.getRegion(editorAdaptor, count));
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
			if(previous != null && previous.getCommentChar().equals(current.getCommentChar())) {
				processed = formatLines(new ArrayList<String>(), previous.getFullLine()+" "+current.getText(),
						previous.getIndent(), previous.getCommentChar(), textwidth
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
				processed = formatLines(new ArrayList<String>(), current.getFullLine(),
						current.getIndent(), current.getCommentChar(), textwidth
				);
				formattedLines.addAll(processed);
				//prepare for next iteration
				previous = current;
			}
			
		}
		
		String newLines = "";
		for(String line : formattedLines) {
			newLines += line + newlineChar;
		}
		
		TextRange region = textObject.getRegion(editorAdaptor, count);
		int start = region.getLeftBound().getModelOffset();
		int length = region.getModelLength();
		editorAdaptor.getModelContent().replace(start, length, newLines);
	}
	
	/**
	 * Take a long line and split it on word boundaries closest to textwidth.
	 * Note that this may result in multiple lines.  Each new line will
	 * share the same indentation and comment character as the first.
	 */
	private List<String> formatLines(List<String> formatted, String textToFormat,
			String indent, String commentChar, int textwidth) {
		if(textToFormat.length() <= textwidth) {
			formatted.add(textToFormat);
		}
		else {
			int lineBreak = getLastWhitespaceBeforeWidth(textToFormat, textwidth);
			if(lineBreak < 0) {
				//no whitespace, break on non-word boundary
				lineBreak = Math.min(textToFormat.length(), textwidth);
			}
			String line = textToFormat.substring(0, lineBreak);
			formatted.add(line);
			
			String newText = indent + commentChar + textToFormat.substring(lineBreak);
			if(newText.length() > 0) {
				//recursion!
				return formatLines(formatted, newText, indent, commentChar, textwidth);
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
	
	private class CommentedLine {
		private boolean allowFormat = true;
		private String commentChar = "";
		private String indent = "";
		private String fullLine = "";
		private String text = "";

		public boolean allowFormat()    { return allowFormat; }
		public String  getCommentChar() { return commentChar; }
		public String  getIndent()      { return indent;      }
		public String  getFullLine()    { return fullLine;    }
		public String  getText()        { return text;        }
		
		public CommentedLine(String line) {
			fullLine = line;
			
			//grab indent (if any)
			int indentOffset = getFirstNonWhiteSpaceOffset(line);
			if(indentOffset > -1) {
				indent = line.substring(0, indentOffset);
				text = line.substring(indentOffset);
			}
			else {
				indent = "";
				text = line;
			}
			
			if(text.matches("\\s*") || text.startsWith(MULTI_LINE_START) || text.startsWith(MULTI_LINE_END)) {
				//blank lines and start and end of multi-line comments never format
				allowFormat = false;
			}
			else {
				for(String comment : SINGLE_LINE_COMMENTS) {
					if(text.startsWith(comment)) {
						commentChar = comment;
						break;
					}
				}
			}
			
			//get text after comment char
			int nonCommentOffset = getFirstNonWhiteSpaceOffset( text.substring(commentChar.length()) );
			if(nonCommentOffset > -1) {
				text = text.substring(commentChar.length() + nonCommentOffset);
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
