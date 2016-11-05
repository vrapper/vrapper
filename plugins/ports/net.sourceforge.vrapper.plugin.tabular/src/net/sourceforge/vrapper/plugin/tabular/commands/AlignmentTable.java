package net.sourceforge.vrapper.plugin.tabular.commands;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.vrapper.plugin.tabular.commands.FormatSpecifier.AlignmentMode;
import net.sourceforge.vrapper.utils.StringUtils;

public class AlignmentTable {

	private static final String SPACE = " ";
	
	private ArrayList<FormatSpecifier> formatSpecifiers;
	private final ArrayList<Integer> maxColumnLengths = new ArrayList<Integer>();
	private final ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
	private final int numberOfLinesInFile;
	
	public AlignmentTable(int numberOfLinesInFile) {
		this.numberOfLinesInFile = numberOfLinesInFile;
	}

	public void setFormatSpecifiers(ArrayList<FormatSpecifier> formatSpecifiers) {
		this.formatSpecifiers = formatSpecifiers;
	}
	
	public List<String> getRow(int rowIndex) {
		return data.get(rowIndex);
	}
	
	public void addRow() {
		data.add(new ArrayList<String>());
	}
	
	public void addToRow(int rowIndex, String columnValue) {
		List<String> row = data.get(rowIndex);
		int columnIx = row.size();

		String formattedValue = columnIx == 0 ? trimRight(columnValue) : columnValue.trim();
		row.add(formattedValue);
		
		/* Update the maximum column length */
		if (columnIx >= maxColumnLengths.size()) {
			maxColumnLengths.add(formattedValue.length());
			if (maxColumnLengths.size() != columnIx + 1)
				throw new IllegalStateException();
		} else if (formattedValue.length() > maxColumnLengths.get(columnIx)) {
			maxColumnLengths.set(columnIx, formattedValue.length());
		}
	}
	
	private FormatSpecifier getFormatForColumn(int columnIx) {
		int formatSpecifiersIx = columnIx % formatSpecifiers.size();
		return formatSpecifiers.get(formatSpecifiersIx);
	}

	private String pad(int rowIx, int columnIx) {
		String columnValue = data.get(rowIx).get(columnIx);
		int paddingNeeded = maxColumnLengths.get(columnIx) - columnValue.length();
		FormatSpecifier format = getFormatForColumn(columnIx);

		/* Pad column value to the maximum column length */
		String paddedValue = "";
		if (AlignmentMode.ALIGN_LEFT.equals(format.getAlignmentMode())) {
			paddedValue = columnValue + spaces(paddingNeeded);
		} else if (AlignmentMode.ALIGN_RIGHT.equals(format.getAlignmentMode())){
			paddedValue = spaces(paddingNeeded) + columnValue;
		} else if (AlignmentMode.ALIGN_CENTER.equals(format.getAlignmentMode())) {
			int spacesOnLeft = paddingNeeded / 2;
			int spacesOnRight = paddingNeeded - spacesOnLeft;
			paddedValue = spaces(spacesOnLeft) + columnValue + spaces(spacesOnRight);
		}
		
		/* Add 0 or more extra spaces to the end */
		paddedValue += spaces(format.getNumberOfExtraSpaces());
		
		return paddedValue;
	}
	
	private String spaces(int count) {
		return StringUtils.multiply(SPACE, count);
	}
	
	private String trimRight(String str) {

		int indexOfLastNonSpace = str.length() - 1;
		while (indexOfLastNonSpace >= 0) {
			char curr = str.charAt(indexOfLastNonSpace);
			if (!Character.isWhitespace(curr)) {
				break;
			}
			indexOfLastNonSpace--;
		}
		
		return str.substring(0, indexOfLastNonSpace+1);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int r = 0; r < data.size(); r++) {
			List<String> columns = data.get(r);
			for (int c = 0; c < columns.size(); c++) {
				sb.append(pad(r,c));
			}
			
			boolean isFinalLine = r == numberOfLinesInFile - 1;
			if (!isFinalLine)
				sb.append("\n");
		}
		return sb.toString();
	}

}
