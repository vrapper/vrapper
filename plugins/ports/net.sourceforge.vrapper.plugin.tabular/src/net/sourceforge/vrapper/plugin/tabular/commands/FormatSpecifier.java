package net.sourceforge.vrapper.plugin.tabular.commands;

public class FormatSpecifier {

	public static final AlignmentMode DEFAULT_ALIGNMENT = AlignmentMode.ALIGN_LEFT;
	public static final int DEFAULT_NUMBER_OF_EXTRA_SPACES = 1;
	public static final FormatSpecifier DEFAULT_FORMAT = new FormatSpecifier();
	static {
		DEFAULT_FORMAT.alignmentMode = DEFAULT_ALIGNMENT;
		DEFAULT_FORMAT.numberOfExtraSpaces = DEFAULT_NUMBER_OF_EXTRA_SPACES;
	}

	private AlignmentMode alignmentMode = AlignmentMode.ALIGN_LEFT;
	private int numberOfExtraSpaces = 1;

	public AlignmentMode getAlignmentMode() {
		return alignmentMode;
	}
	public void setAlignmentMode(AlignmentMode alignmentMode) {
		this.alignmentMode = alignmentMode;
	}
	public int getNumberOfExtraSpaces() {
		return numberOfExtraSpaces;
	}
	public void setNumberOfExtraSpaces(int numberOfExtraSpaces) {
		this.numberOfExtraSpaces = numberOfExtraSpaces;
	}
	
	@Override
	public String toString() {
		return alignmentMode.toString() + numberOfExtraSpaces;
	}

	public enum AlignmentMode { 
		ALIGN_LEFT("l"), 
		ALIGN_CENTER("c"),
		ALIGN_RIGHT("r");
		
		private String code;

		AlignmentMode(String code) {
			this.code = code;
		}

		@Override
		public String toString() {
			return code;
		}
		
		public static AlignmentMode fromChar(String alignChar) {
			for (AlignmentMode mode : values()) {
				if (mode.code.equalsIgnoreCase(alignChar)) {
					return mode;
				}
			}
			return null;
		}
	}

	
}