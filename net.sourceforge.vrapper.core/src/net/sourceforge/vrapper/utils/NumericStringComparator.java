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
	
	private boolean binary      = false;
	private boolean octal       = false;
	private boolean hex         = false;
	private boolean usePattern  = false;
	private boolean usePatternR = false;

	private String pattern = null;
	
	public NumericStringComparator(boolean binary, boolean octal, boolean hex, String pattern, boolean patternR) throws Exception {
		super();
    		
    	//numeric is assumed, no need for a flag
    	this.binary = binary;
    	this.octal = octal;
    	this.hex = hex; 
    	
    	if(pattern != null) {
    		this.pattern = pattern;
    		usePattern = true;
    		usePatternR = patternR; 
    	}
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
	    	} else { //assume numeric ('n')
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

		if(usePattern) {
			str1 = str1.substring(usePatternR ? str1.indexOf(pattern) : str1.indexOf(pattern) + pattern.length());
			str2 = str2.substring(usePatternR ? str2.indexOf(pattern) : str2.indexOf(pattern) + pattern.length());
		}
		
		double dub1 = getFirstNumber(str1);
		double dub2 = getFirstNumber(str2);
	
		if(dub1 > dub2)
			return 1;
		else if(dub1 < dub2)
			return -1;
		
		return 0;
	}
}