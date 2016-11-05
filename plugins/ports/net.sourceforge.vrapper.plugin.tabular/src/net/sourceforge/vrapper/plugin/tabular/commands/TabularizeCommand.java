package net.sourceforge.vrapper.plugin.tabular.commands;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.LineRange;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.SimpleLineRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountIgnoringNonRepeatableCommand;
import net.sourceforge.vrapper.vim.commands.Counted;
import net.sourceforge.vrapper.vim.commands.LineWiseSelection;

public class TabularizeCommand extends CountIgnoringNonRepeatableCommand {

	private static final Pattern LINE_SEPARATOR = Pattern.compile("\n");

	private final Pattern columnDelimiterPattern;
	private ArrayList<FormatSpecifier> formats;
	
	public TabularizeCommand(final Pattern columnDelimiterPattern, ArrayList<FormatSpecifier> formats) {
		if (columnDelimiterPattern == null || formats == null)
			throw new IllegalArgumentException("columnDelimiterRegex and formats must not be null");
		
		if (formats.isEmpty()) {
			formats.add(FormatSpecifier.DEFAULT_FORMAT);
		}

		this.columnDelimiterPattern = columnDelimiterPattern;
		this.formats = formats;
	}
	
	@Override
	public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {

		if (shouldInferRange(editorAdaptor)) {
			LineRange targetRange = inferTargetRange(editorAdaptor);
			editorAdaptor.setSelection(new LineWiseSelection(editorAdaptor, targetRange.getFrom(), targetRange.getTo()));
		}
		
		doTabularize(editorAdaptor);
	}
	
	private boolean shouldInferRange(EditorAdaptor editorAdaptor) {
		TextRange selectedContent = editorAdaptor.getNativeSelection();
		int startOffset = selectedContent.getStart().getModelOffset();
		int endOffset = selectedContent.getEnd().getModelOffset();
		LineInformation startLine = editorAdaptor.getModelContent().getLineInformationOfOffset(startOffset);
		LineInformation endLine = editorAdaptor.getModelContent().getLineInformationOfOffset(endOffset);
		
		if (startOffset == endOffset)
			return true;
		else {
			boolean linewise = (startLine.getBeginOffset() == startOffset) && (endLine.getBeginOffset() == endOffset);
			return !linewise;
		}
	}

	private void doTabularize(EditorAdaptor editorAdaptor) throws CommandExecutionException {
		TextRange region = editorAdaptor.getSelection().getRegion(editorAdaptor, Counted.NO_COUNT_GIVEN);
		int originalStartOffset = region.getStart().getModelOffset();
		TextContent content = editorAdaptor.getModelContent();
		String textToAlign = content.getText(region);
		
		String[] lines = LINE_SEPARATOR.split(textToAlign);
		
		int totalLines = content.getNumberOfLines();
		AlignmentTable table = new AlignmentTable(totalLines);
		table.setFormatSpecifiers(formats);
		for (int rowIx = 0; rowIx < lines.length; rowIx++) {
			table.addRow();
			splitColumnsAndAddToTable(table, rowIx, lines[rowIx]);
		}
		content.replace(originalStartOffset, textToAlign.length(), table.toString()) ;
	}
	
	private void splitColumnsAndAddToTable(AlignmentTable table, int rowIx, String line) {
		Matcher columnDelimiter = columnDelimiterPattern.matcher(line);
		int endOfPreviousColumn = 0;
		while (columnDelimiter.find()) {
			String delimiter = columnDelimiter.group();
			String column = line.substring(endOfPreviousColumn, columnDelimiter.start());
			endOfPreviousColumn = columnDelimiter.end();
			table.addToRow(rowIx, column);
			table.addToRow(rowIx, delimiter);
		}

		if (endOfPreviousColumn < line.length()) {
			table.addToRow(rowIx, line.substring(endOfPreviousColumn));
		}
	}

	private LineRange inferTargetRange(EditorAdaptor editorAdaptor) {
		Position currentPos = editorAdaptor.getCursorService().getPosition();
		TextContent content = editorAdaptor.getModelContent();
		LineInformation cursorLine = content.getLineInformationOfOffset(currentPos.getModelOffset());
		int cursorLineNumber = cursorLine.getNumber();

		/* Scan lines above the cursor. Include contiguous lines that contain a column delimiter. */
		int beginLine = cursorLineNumber;
		for (int currLineNum = cursorLineNumber; currLineNum >= 0; currLineNum--) {
			LineInformation currLine = content.getLineInformation(currLineNum);
			String lineText = content.getText(currLine.getBeginOffset(), currLine.getLength());

			if (columnDelimiterPattern.matcher(lineText).find())
				beginLine = currLineNum;
			else
				break;
		}

		/* Scan lines below the cursor. Include contiguous lines that contain a column delimiter. */
		int endLine = cursorLineNumber;
		for (int currLineNum = cursorLineNumber; currLineNum < content.getNumberOfLines(); currLineNum++) {
			LineInformation currLine = content.getLineInformation(currLineNum);
			String lineText = content.getText(currLine.getBeginOffset(), currLine.getLength());

			if (columnDelimiterPattern.matcher(lineText).find())
				endLine = currLineNum;
			else
				break;
		}

		LineInformation start = content.getLineInformation(beginLine);
		LineInformation end = content.getLineInformation(endLine);
		Position startPos = editorAdaptor.getCursorService().newPositionForModelOffset(start.getBeginOffset());
		Position endPos = editorAdaptor.getCursorService().newPositionForModelOffset(end.getEndOffset());
		
		return SimpleLineRange.betweenPositions(editorAdaptor, startPos, endPos);
	}

}
