package net.sourceforge.vrapper.utils;

import java.math.BigDecimal;
import java.util.Comparator;

/**
 * Numeric comparator for the sort command.
 * @author Brian Detweiler
 */
public class NumericStringComparator implements Comparator<String>{

	public NumericStringComparator() {
		super();
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
		BigDecimal bigDecArg1 = BigDecimal.ZERO;
		BigDecimal bigDecArg2 = BigDecimal.ZERO;

		try {
			double doubleArg1 = Double.parseDouble(str1);
			bigDecArg1 = BigDecimal.valueOf(doubleArg1);
		} catch(NumberFormatException nfe) {
			;;;
		}
		
		try {
			double doubleArg2 = Double.parseDouble(str2);
			bigDecArg2 = BigDecimal.valueOf(doubleArg2);
		} catch(NumberFormatException nfe) {
			;;;
		}

		return bigDecArg1.compareTo(bigDecArg2);
	}
}