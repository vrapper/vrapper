package net.sourceforge.vrapper.utils;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.platform.SimpleConfiguration;

/**
 * Numeric comparator for the sort command.
 * @author Brian Detweiler
 */
public class NumericStringComparator implements Comparator<String>{

	private static enum NumericStringOptions {
	    NUMERIC,
	    BINARY,
	    OCTAL,
	    HEX,
	    USE_PATTERN,
	    USE_PATTERN_R
	}
	
	private static final String NUMERIC_FLAG   = "n";
	private static final String BINARY_FLAG    = "b";
	private static final String OCTAL_FLAG     = "o";
	private static final String HEX_FLAG       = "x";
	private static final String USE_PATTERN    = "p";
	private static final String USE_PATTERN_R  = "r";
	
	/** String containing all the possible option flags */
	private static final String OPTIONS = NUMERIC_FLAG
										+ BINARY_FLAG
										+ OCTAL_FLAG
										+ HEX_FLAG
										+ USE_PATTERN
										+ USE_PATTERN_R;
	
	@SuppressWarnings("unused")
	private boolean numeric     = false;
	private boolean binary   	= false;
	private boolean octal	    = false;
	private boolean hex   		= false;
	private boolean usePattern  = false;
	private boolean usePatternR = false;

	private String pattern 	    = "";
	
	public NumericStringComparator() {
		super();
	}
	
	public NumericStringComparator(String option) throws Exception {
		super();
    	if(option == null || option.trim().isEmpty())
    		return;
    		
    	if(encodeOption(option) == NumericStringOptions.NUMERIC)
    		numeric = true;
    	else if(encodeOption(option) == NumericStringOptions.BINARY)
    		binary = true;
    	else if(encodeOption(option) == NumericStringOptions.OCTAL)
    		octal = true;
    	else if(encodeOption(option) == NumericStringOptions.HEX)
    		hex = true; 
    	else
    		throw new Exception("Invalid argument: " + option);
	}
	
	public NumericStringComparator(String option, String pattern) throws Exception {
		super();
		
		try {
			Pattern.compile(pattern);
		} catch(Exception e) {
			throw new Exception("Invalid pattern " + pattern);
		}
		
		setOption(option);
		setUsePattern(true); 
		setPattern(pattern);
	}
	
	public NumericStringComparator(String option, String pattern, String patternR) throws Exception {
		super();
		
		try {
			Pattern.compile(pattern);
		} catch(Exception e) {
			throw new Exception("Invalid pattern " + pattern);
		}
		
		setOption(option);
		usePatternR = true; 
		setPattern(pattern); 
	}
	
	private void setOption(String option) throws Exception {
		
    	if(option == null || option.trim().isEmpty())
    		return;
    	
    	if(encodeOption(option) == NumericStringOptions.NUMERIC)
    		numeric = true;
    	else if(encodeOption(option) == NumericStringOptions.BINARY)
    		binary = true;
    	else if(encodeOption(option) == NumericStringOptions.OCTAL)
    		octal = true;
    	else if(encodeOption(option) == NumericStringOptions.HEX)
    		hex = true; 
    	else
    		throw new Exception("Invalid argument: " + option);
	}
   
    private NumericStringOptions encodeOption(String option) {
    	if(option == null || "".equalsIgnoreCase(option) || !OPTIONS.contains(option))
    		return null;
    
    	if(option.equalsIgnoreCase(NUMERIC_FLAG))
    		return NumericStringOptions.NUMERIC;
    	else if(option.equalsIgnoreCase(BINARY_FLAG))
    		return NumericStringOptions.BINARY;
    	else if(option.equalsIgnoreCase(OCTAL_FLAG))
    		return NumericStringOptions.OCTAL;
    	else if(option.equalsIgnoreCase(HEX_FLAG))
    		return NumericStringOptions.HEX;
    	else if(option.equalsIgnoreCase(USE_PATTERN))
    		return NumericStringOptions.USE_PATTERN;
    	else if(option.equalsIgnoreCase(USE_PATTERN_R))
    		return NumericStringOptions.USE_PATTERN_R;
    	
    	return null;
    }
    
    /**
     * Gets the first number in the string with respect to the base.
     * In decimal mode, it handles negative numbers.
     * 
     * NOTE: Special note on Java Regular Expressions - I had a hell
     * 		 of a time with this because I had never worked this deeply
     * 		 with Java regex's before. Look up Reluctant quantifiers vs. 
     * 		 Possessive quantifiers before you start bashing your head against
     * 		 the wall. Also note that the . character means "any" character
     * 		 EXCEPT the new line. 
     * 
     * @param str
     * @return
     */
    private double getFirstNumber(String str) {
    	SimpleConfiguration config = new SimpleConfiguration();
    	String newLine = config.getNewLine();
    	
    	try {
	    	if(binary) {
				Pattern number = Pattern.compile(".*?((0|1)+)(.|" + newLine + ")*");
		    	Matcher m = number.matcher(str);
		    	
				if(m.matches())
					return Double.parseDouble(m.group(1));
	    	
				throw new Exception("No binary string found.");
	    	} else if(octal) {
	    		Pattern number = Pattern.compile(".*?(([0-7])+)(.|" + newLine + ")*");
	    		Matcher m = number.matcher(str);
				if(m.matches())
					return Double.parseDouble("0" + m.group(1));
				
				throw new Exception("No octal string found.");
	    	}
	    	else if(hex) {
		    	Pattern number = Pattern.compile(".*?(([A-Fa-f0-9])+)(.|" + newLine + ")*");
		    	Matcher m = number.matcher(str);
				if(m.matches())
					return new BigInteger(m.group(1), 16).doubleValue();
				
				throw new Exception("No hex string found.");
	    	} else {
	    		// Checks for both negative and non-negative numbers
	    		Pattern number = Pattern.compile(".*?([-]?[0-9]+)(.|" + newLine + ")*");
				Matcher m = number.matcher(str);
				if(m.matches()) 
					return Double.parseDouble(m.group(1));
				
				throw new Exception("No numeric string found.");
	    	}
    	} catch(Exception e) {
    		return 0;
    	}
    }
    
	/**
	 * Numbers can come in a variety of formats.
	 * Since we just want to know which is bigger (if it parses at all),
	 * BigDecimal should suffice for just about any number.
	 * 
	 * TODO: It would be handy if we could parse numbers bigger than a double.
	 * 		 This would require a separate method that takes a string and returns
	 * 		 a BigDecimal. Note that Vim does not do this, it just won't sort 
	 * 		 anything bigger than a double. I think we could do better!
	 */
	public int compare(String str1, String str2) {

		if(usePatternR) {
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher("(" + str1 + ")");
			str1 = m.group(1);
			m = p.matcher(str2);
			str2 = m.group(1);
		}
		
		if(usePattern) {
			Pattern p = Pattern.compile(pattern);
			Matcher m = p.matcher(str1);
			str1 = m.replaceFirst("");
			m = p.matcher(str2);
			str2 = m.replaceFirst("");
		}
		
		double dub1 = getFirstNumber(str1);
		double dub2 = getFirstNumber(str2);
	
		if(dub1 > dub2)
			return 1;
		else if(dub1 < dub2)
			return -1;
		
		return 0;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public boolean isUsePattern() {
		return usePattern;
	}

	public void setUsePattern(boolean usePattern) {
		this.usePattern = usePattern;
	}
}