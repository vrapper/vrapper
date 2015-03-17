package net.sourceforge.vrapper.vim.commands;

import java.util.Arrays;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;

/**
 * Insert (or remove) &lt;shiftwidth&gt; spaces inside of a blockwise selection. Then
 * replace every &lt;tabstop&gt; spaces with a TAB character if &lt;expandtab&gt; is
 * disabled.
 */
public class BlockwiseInsertShiftWidth implements TextOperation {

	public static final TextOperation INSERT = new BlockwiseInsertShiftWidth(true);
	public static final TextOperation REMOVE = new BlockwiseInsertShiftWidth(false);

	private final boolean shiftRight;
	
	private BlockwiseInsertShiftWidth(boolean shiftRight) {
		this.shiftRight = shiftRight;
	}

	@Override
	public void execute(EditorAdaptor editorAdaptor, int count, TextObject textObject) throws CommandExecutionException {
		int tabstop = editorAdaptor.getConfiguration().get(Options.TAB_STOP);
		tabstop = Math.max(1, tabstop);
		int shiftwidth = editorAdaptor.getConfiguration().get(Options.SHIFT_WIDTH);
		shiftwidth = Math.max(1, shiftwidth);
		boolean expandtab = editorAdaptor.getConfiguration().get(Options.EXPAND_TAB);
//		// Not used for blockwise shift?
//		boolean shiftround = editorAdaptor.getConfiguration().get(Options.SHIFT_ROUND);
		
		if (count != Counted.NO_COUNT_GIVEN) {
			shiftwidth = count * shiftwidth;
		}
		// Fill string with number of spaces.
		String replaceTab = new String(new char[tabstop]).replace('\0', ' ');
		String replaceShiftWidth = new String(new char[shiftwidth]).replace('\0', ' ');

		TextRange region = textObject.getRegion(editorAdaptor, Counted.NO_COUNT_GIVEN);
		TextContent model = editorAdaptor.getModelContent();
		LineInformation line = model.getLineInformationOfOffset(
				region.getLeftBound().getModelOffset());

		// Sanity check - each TextObject passed in should be just a single line part.
		if (line.getEndOffset() < region.getRightBound().getModelOffset()) {
			VrapperLog.error("Received incorrect shiftwidth segment! Start is at "
					+ region.getLeftBound() + " with " + line + " but end is at "
					+ region.getRightBound());
			throw new CommandExecutionException("Failed to shift block, bad line found.");
		} else if (region.getModelLength() < 1) {
			VrapperLog.error("Received incorrect shiftwidth segment! Start is at "
					+ region.getLeftBound() + " and segment length is 0!");
			throw new CommandExecutionException("Failed to shift block, bad line found.");
		}

		doIt(editorAdaptor, model, region, line, tabstop, shiftwidth, expandtab, replaceTab,
				replaceShiftWidth);
	}

	private void doIt(EditorAdaptor editorAdaptor, TextContent model, TextRange region,
			LineInformation line, int tabstop, int shiftwidth,
			boolean expandtab, String replaceTab, String replaceShiftWidth) throws CommandExecutionException {
		String contents = model.getText(line.getBeginOffset(), line.getLength());
		int leftOff = region.getLeftBound().getModelOffset() - line.getBeginOffset();
		int beginIndent = leftOff;
		int endIndent = leftOff;
		while (endIndent < contents.length()
				&& VimUtils.isWhiteSpace(contents.substring(endIndent, endIndent + 1))) {
			endIndent++;
		}
		int[] visualOffsets = calculateVisualOffsets(contents, endIndent, tabstop);

		int indentLength;
		if (shiftRight) {
			// Check if there is indentation on the left, it might need tab expansion / coalescing.
			while (beginIndent > 0
					&& VimUtils.isWhiteSpace(contents.substring(beginIndent -1, beginIndent))) {
				beginIndent--;
			}
			indentLength = endIndent - beginIndent;
			StringBuilder indent;
			indent = expandTabs(contents, beginIndent, endIndent, leftOff, visualOffsets);

			indent.append(replaceShiftWidth);

			if ( ! expandtab) {
				coalesceTabs(contents, indent, tabstop, beginIndent, visualOffsets);
			}
			model.replace(line.getBeginOffset() + beginIndent, indentLength, indent.toString());

		} else if (VimUtils.isWhiteSpace(contents.substring(leftOff, leftOff + 1))) {
			StringBuilder replace = new StringBuilder();
			indentLength = endIndent - beginIndent;

			// Special case: Vim doesn't reformat the indentation here, it only chops off whitespace
			// Gobble spaces and tabs until we collected enough or hit the left block boundary.
			int beginReplace = endIndent;
			while (beginReplace > leftOff
					&& VimUtils.isWhiteSpace(contents.substring(beginReplace - 1, beginReplace))
					&& replace.length() < shiftwidth) {
				beginReplace--;
				if (contents.charAt(beginReplace) == '\t') {
					// Determine the number of spaces this tab was taking up
					int tabsize = visualOffsets[beginReplace + 1] - visualOffsets[beginReplace];
					for (int i = 0; i < tabsize; i++) {
						replace.append(' ');
					}
				} else if (contents.charAt(beginReplace) == ' ') {
					replace.append(' ');
				} else {
					// This shouldn't happen...
					throw new CommandExecutionException("Found weird indentation at " + line
							+ ", contents: [" + contents + "].");
				}
			}
			// Chop off spaces
			if (replace.length() > shiftwidth) {
				replace.setLength(replace.length() - shiftwidth);
			} else {
				replace.setLength(0);
			}

			int replacedChars = endIndent - beginReplace;
			model.replace(line.getBeginOffset() + beginReplace, replacedChars, replace.toString());
		} // else if there is no whitespace at the start of the block, nothing needs to be done!
	}

	/**
	 * Puts a number of spaces into a StringBuilder for later retrieval based on the spaces and tabs
	 * between beginIndent and endIndent.
	 */
	public StringBuilder expandTabs(String contents, int beginIndent, int endIndent,
			int leftOff, int[] visualOffsets) {
		//expand all tab characters so we can recalculate tabstops
		int nspaces = 0;
		for (int i = beginIndent; i < endIndent; i++) {
			int visualWidth = visualOffsets[i + 1] - visualOffsets[i];
			if (i < leftOff && visualWidth > 1) {
				leftOff += visualWidth - 1;
			}
			nspaces += visualWidth;
		}
		char[] spaces = new char[nspaces];
		Arrays.fill(spaces, ' ');
		return new StringBuilder().append(spaces);
	}

	/**
	 * Convert spaces into tab characters if we pass &lt;tabstop&gt; characters. Note that this is
	 * far more complex than for InsertShiftWidth, in this case we need to make sure we align to
	 * tabstops as a tab to the right of a character might be 3 characters wide in the UI.
	 */
	private void coalesceTabs(String contents, StringBuilder indent, int tabstop, int beginIndent,
			int[] visualOffsets) {
		int beginIndentVOff = visualOffsets[beginIndent];
		// Round up to next tab stop using integer arithmetic
		int nextTabstopOff = (beginIndentVOff + (tabstop - 1)) / tabstop * tabstop;

		// Check if we have enough spaces available to reach the next tab stop.
		int tabstopDiff = nextTabstopOff - beginIndentVOff;
		int index = 0;
		if (indent.length() >= tabstopDiff) {
			indent.replace(0, tabstopDiff, "\t");
			index = 1;
		}
		// Now we can be sure that our tabs are aligned. Start replacing them like we usually do.
		while ((indent.length() - index) > tabstop) {
			indent.replace(index, index + tabstop, "\t");
			index++;
		}
	}

	/**
	 * Utility method to calculate the visual offset of each character in a string, taking the
	 * variable width of a tab in account. Only public + static for testing.
	 * @return an array which is maxIndex + 1 which contains the visual offset of each character
	 * offset in the string.
	 */
	public static int[] calculateVisualOffsets(String contents, int maxIndex, int tabstop) {
		int[] result = new int[maxIndex + 1];
		int nextTabstopOff = 0;
		int visualOffset = 0;
		int i = 0;
		while (i < maxIndex && i < contents.length()) {
			result[i] = visualOffset;
			if (visualOffset % tabstop == 0) {
				nextTabstopOff += tabstop;
			}
			if (contents.charAt(i) == '\t') {
				visualOffset = nextTabstopOff;
			} else if ( ! Character.isHighSurrogate(contents.charAt(i))) {
				visualOffset++;
			}
			i++;
		}
		result[i] = visualOffset;
		return result;
	}

	@Override
	public TextOperation repetition() {
		return this;
	}
}
