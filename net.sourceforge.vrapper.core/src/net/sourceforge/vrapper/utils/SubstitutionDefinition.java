package net.sourceforge.vrapper.utils;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.sourceforge.vrapper.vim.register.RegisterManager;

/**
 * Parse a substitution definition (:s/foo/bar/g) to make its parts more
 * accessible.  This is a generic class to keep all string parsing in one
 * location.
 */
public class SubstitutionDefinition {
    public String find;
    public String replace;
    public String flags;
    
    public SubstitutionDefinition(String def, RegisterManager registers) throws IllegalArgumentException {
        String lastSearch = registers.getRegister("/").getContent().getText();
		//whatever character is after 's' is our delimiter
		String delim = "" + def.charAt( def.indexOf('s') + 1);
		//split on the delimiter, unless that delimiter is escaped with a backslash (:s/\/\///)
		String[] fields = def.split("(?<!\\\\)"+delim, -1);
		find = "";
		replace = "";
		flags = "";
		//'s' or '%s' = fields[0]
		if(fields.length > 1) {
			find = fields[1];
			if(find.length() == 0) {
				//if no pattern defined, use last search
			    find = lastSearch;
			}
		}
		if(fields.length > 2) {
			replace = fields[2];

			//Vim uses \r to represent a newline but Eclipse interprets that as a literal
			//carriage-return.  Eclipse uses \R as a platform-independent newline
			//replace "\r" with "\R", only if it is preceded by zero or an even number of backslashes
			replace = replace.replaceAll("((^|[^\\\\])(\\\\\\\\)*)(\\\\r)", "$1\\\\R");

			//replace "\t" with the tab character "\t", only if it is preceded by zero or an even number of backslashes
			//(otherwise it gets interpreted as \\t, which is two characters)
			replace = replace.replaceAll("((^|[^\\\\])(\\\\\\\\)*)(\\\\t)", "$1\t");

			//  '\=@x' means 'insert register x'
			//but you can't include *anything* else in the replace string
			if(replace.matches("^\\\\=@.$")) {
			    replace = registers.getRegister(replace.substring(replace.length()-1)).getContent().getText();
			}
		}
		if(fields.length > 3) {
			flags = fields[3];
		}
		
		if(find.length() == 0) {
		    throw new IllegalArgumentException("No search string defined");
		}
		
        //special case to override global 'ignorecase' property (see :help \c)
        if(find.contains("\\c")) {
        	int index = find.indexOf("\\c");
        	flags += "i";
        	//replaceAll doesn't like \\c, so cut out the characters this way
        	find = find.substring(0, index) + find.substring(index+2);
        }
        if(find.contains("\\C")) {
        	int index = find.indexOf("\\C");
        	flags += "I";
        	find = find.substring(0, index) + find.substring(index+2);
        }
		
		//before attempting substitution, is this regex even valid?
		try {
		    Pattern.compile(find);
		}
		catch(PatternSyntaxException e) {
		    throw new IllegalArgumentException(e.getDescription());
		}
    }
}
