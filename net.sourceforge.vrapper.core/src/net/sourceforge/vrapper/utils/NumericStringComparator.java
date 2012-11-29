package net.sourceforge.vrapper.utils;

import java.util.Comparator;
import java.util.regex.Pattern;

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
	}
	
	private static final String NUMERIC_FLAG = "n";
	private static final String BINARY_FLAG  = "b";
	private static final String OCTAL_FLAG   = "o";
	private static final String HEX_FLAG     = "x";
	
	/** String containing all the possible option flags */
	private static final String OPTIONS = NUMERIC_FLAG
										+ BINARY_FLAG
										+ OCTAL_FLAG
										+ HEX_FLAG;
	
	@SuppressWarnings("unused")
	private boolean numeric = false;
	private boolean binary  = false;
	private boolean octal   = false;
	private boolean hex     = false;
	
	public NumericStringComparator() {
		super();
	}
	
	public NumericStringComparator(String option) throws Exception {
		super();
    	if(option == null || !option.trim().isEmpty());
    	else if(encodeOption(option) == NumericStringOptions.NUMERIC)
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
    	else if(option.equalsIgnoreCase(OCTAL_FLAG))
    		return NumericStringOptions.OCTAL;
    	else if(option.equalsIgnoreCase(HEX_FLAG))
    		return NumericStringOptions.HEX;
    	
    	return null;
    }
    
    /**
     * Gets the first number in the string with respect to the base.
     * In decimal mode, it handles negative numbers.
     * TODO: TEST THE SHIT OUT OF THIS
     * 
     * @param str
     * @return
     */
    private double getFirstNumber(String str) {
    	try {
	    	if(binary) {
	    		try {
		    		// XXX: This was only introduced in Java 7. Will fail on earlier JDKs.
		    		return Double.parseDouble("0b" + Pattern.compile(".*([0-1]*).*").matcher(str).group());
	    		} catch (Exception e) {
		    		return Double.parseDouble(Pattern.compile(".*([0-1]*).*").matcher(str).group());
	    		}
	    	} else if(octal)
	    		return Double.parseDouble("0" + Pattern.compile(".*([0-8]*).*").matcher(str).group());
	    	else if(hex)
	    		return Double.parseDouble("0x" + Pattern.compile(".*([0-9A-Fa-f]*).*").matcher(str).group());
	    	else
	    		// Handle negative numbers
	    		return Double.parseDouble(Pattern.compile(".*([\\-][0-9]*).*").matcher(str).group());
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
		double dub1 = getFirstNumber(str1);
		double dub2 = getFirstNumber(str2);
	
		if(dub1 > dub2)
			return 1;
		else if(dub1 < dub2)
			return -1;
		
		return 0;
	}
}