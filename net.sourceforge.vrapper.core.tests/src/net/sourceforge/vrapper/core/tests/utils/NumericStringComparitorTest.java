package net.sourceforge.vrapper.core.tests.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class NumericStringComparitorTest extends VimTestCase {

	private static final String NUMERIC_FLAG     = "n";
	private static final String BINARY_FLAG      = "b";
	private static final String OCTAL_FLAG       = "o";
	private static final String HEX_FLAG 		 = "x";

    private boolean hasNumber(String str, String type) {
    	if(BINARY_FLAG.equalsIgnoreCase(type))
    		return str.contains("0") || str.contains("1");
    	if(OCTAL_FLAG.equalsIgnoreCase(type))
    		return str.contains("0") ||
    			   str.contains("1") ||
    			   str.contains("2") ||
    			   str.contains("3") ||
    			   str.contains("4") ||
    			   str.contains("5") ||
    			   str.contains("6") ||
    			   str.contains("7");
    	if(HEX_FLAG.equalsIgnoreCase(type))
    		return str.contains("0") ||
    			   str.contains("1") ||
    			   str.contains("2") ||
    			   str.contains("3") ||
    			   str.contains("4") ||
    			   str.contains("5") ||
    			   str.contains("6") ||
    			   str.contains("7") ||
    			   str.contains("8") ||
    			   str.contains("9") ||
    			   str.contains("A") ||
    			   str.contains("a") ||
    			   str.contains("B") ||
    			   str.contains("b") ||
    			   str.contains("C") ||
    			   str.contains("c") ||
    			   str.contains("D") ||
    			   str.contains("d") ||
    			   str.contains("E") ||
    			   str.contains("e") ||
    			   str.contains("F") ||
    			   str.contains("f");
    	else
    		return str.contains("0") ||
    			   str.contains("1") ||
    			   str.contains("2") ||
    			   str.contains("3") ||
    			   str.contains("4") ||
    			   str.contains("5") ||
    			   str.contains("6") ||
    			   str.contains("7") ||
    			   str.contains("8") ||
    			   str.contains("9");
    }
    
	@Test
	public void TestDecimalNumbers() throws Exception {
		
	
		String str = "abcdefg---23321cbf";
		try {
			Pattern number = Pattern.compile(".*?([-]?[0-9]+).*");
			Matcher m = number.matcher(str);
			if(m.matches()) 
				System.out.println("negative: " + m.group(1));
		} catch(Exception e) {
			System.out.println(e);
		}
	
		// Non-negative
		str = "abcdefg23321cbf\n";
		//str = "1000\n";
		try {
			Pattern number = Pattern.compile(".*?([-]?[0-9]+)(.|\n)*");
			Matcher m = number.matcher(str);
			if(m.matches()) 
				System.out.println("non-negative: " + m.group(1));
			else
				System.out.println("NO MATCH");
		} catch(Exception e) {
			System.out.println(e);
		}

		str = "abcd0110abcd00011\n";
		Pattern number = Pattern.compile(".*?((0|1)+)(.|\n)*");
    	Matcher m = number.matcher(str);
    	
		if(m.matches())  {
			System.out.println("binary: " + m.group(1));
		} else
			System.out.println("NO MATCH");
	
		// Should stop at the 8's
		str = "abcd756345632348888882aab";
		number = Pattern.compile(".*?(([0-7])+).*");
    	m = number.matcher(str);
    	
		if(m.matches())  {
			System.out.println("octal: " + m.group(1));
		} else
			System.out.println("NO MATCH");
		
		// Should stop at the 8's
		str = "jHJKabcD756345632348888882aAb";
		number = Pattern.compile(".*?(([A-Fa-f0-9])+).*");
    	m = number.matcher(str);
    	
		if(m.matches())  {
			System.out.println("hex: " + m.group(1));
		} else
			System.out.println("NO MATCH");
	}
	
}