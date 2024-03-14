package net.sourceforge.vrapper.utils;

import java.util.Comparator;

/**
 * Comparator for the :sort command when /pattern/ is in use.
 * Sort two lines starting at the location of the /pattern/ match.
 */
public class PatternSortComparator implements Comparator<String> {
	
	private String pattern = null;
	private boolean usePatternR;
	
	public PatternSortComparator(String pattern, boolean usePatternR) {
		this.pattern = pattern;
		this.usePatternR = usePatternR;
	}

	public int compare(String str1, String str2) {
		str1 = str1.substring(usePatternR ? str1.indexOf(pattern) : str1.indexOf(pattern) + pattern.length());
		str2 = str2.substring(usePatternR ? str2.indexOf(pattern) : str2.indexOf(pattern) + pattern.length());
		
		return str1.compareTo(str2);
	}

}
