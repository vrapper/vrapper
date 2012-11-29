package net.sourceforge.vrapper.utils;

import java.util.Comparator;

/**
 * Ignore Case comparator for :sort command
 * @author Brian Detweiler
 */
public class IgnoreCaseStringComparator implements Comparator<String>{

	public IgnoreCaseStringComparator() {
		super();
	}
	
	public int compare(String str1, String str2) {
		return str1.compareToIgnoreCase(str2);
	}
}