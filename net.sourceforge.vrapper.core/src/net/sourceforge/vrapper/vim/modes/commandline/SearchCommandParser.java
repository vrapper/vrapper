package net.sourceforge.vrapper.vim.modes.commandline;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchOffset;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.VimConstants;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.modes.AbstractVisualMode;
import net.sourceforge.vrapper.vim.modes.ExecuteCommandHint;

/**
 * Mode for input of search parameters (keyword and offset).
 *
 * @author Matthias Radig
 */
public class SearchCommandParser extends AbstractCommandParser {

    private static final Pattern AFTER_SEARCH_PATTERN = Pattern.compile("(e|b)?\\+?(-?\\d+)?");
    private Command commandToExecute;

    public SearchCommandParser(EditorAdaptor vim, Command commandToExecute) {
        super(vim);
        this.commandToExecute = commandToExecute;
    }
    
    /**
     * Parses and executes a search.
     *
     * @param first contains the "command", probably '/' or '?' when searching
     */
    @Override
    public Command parseAndExecute(String first, String command) {
        Search search = parseSearchCommand(first, command);
        editor.getRegisterManager().setSearch(search);
        editor.setLastSearchResult(null);
        editor.getSearchAndReplaceService().removeHighlighting();
        return commandToExecute;
    }

    @Override
    protected void handleExit(Command parsedCommand) {
        if (parsedCommand == null) {
            editor.changeModeSafely(editor.getLastModeName(),
                    // Recall selection if returning to one of the visual modes
                    AbstractVisualMode.RECALL_SELECTION_HINT);
        } else {
            editor.changeModeSafely(editor.getLastModeName(),
                    new ExecuteCommandHint.OnEnter(parsedCommand),
                    // Recall selection if returning to one of the visual modes
                    AbstractVisualMode.RECALL_SELECTION_HINT);
        }
    }

    /** Create a {@link Search} instance based on the given parameters and configuration.*/
    public static Search createSearch(EditorAdaptor editor, String keyword, boolean backward, SearchOffset offset) {
        boolean caseSensitive = ! editor.getConfiguration().get(Options.IGNORE_CASE)
            || (editor.getConfiguration().get(Options.SMART_CASE)
                && StringUtils.containsUppercase(keyword));
        
        //special case to override global 'ignorecase' property (see :help \c)
        if(keyword.contains("\\c")) {
        	int index = keyword.indexOf("\\c");
        	caseSensitive = false;
        	//replaceAll doesn't like \\c, so cut out the characters this way
        	keyword = keyword.substring(0, index) + keyword.substring(index+2);
        }
        if(keyword.contains("\\C")) {
        	int index = keyword.indexOf("\\C");
        	caseSensitive = true;
        	keyword = keyword.substring(0, index) + keyword.substring(index+2);
        }
        boolean useRegExp = editor.getConfiguration().get(Options.SEARCH_REGEX);
        if (offset == null) {
            // Sanity checking. Passing null is bad style though.
            offset = SearchOffset.NONE;
        }
        return new Search(keyword, backward, caseSensitive, offset, useRegExp);
    }

	private Search parseSearchCommand(String first, String command) {
        boolean backward = first.equals(VimConstants.BACKWARD_SEARCH_CHAR);
        // split on the delimiter, unless that delimiter is escaped with a backslash (/foo\/bar/e+2)
        // backwards search delimiter '?' needs extra escaping in this split
        // because it is a reserved character in regex.
        String[] fields = command.split("(?<!\\\\)"+(backward ? '\\' + first : first));
        String keyword = fields[0].replace("\\"+first, first);
        Search lastSearch = editor.getRegisterManager().getSearch();
        // if keyword is empty, last keyword is used
        boolean useLastKeyword = keyword.length() == 0 && lastSearch != null;
        if (useLastKeyword) {
            keyword = lastSearch.getKeyword();
        }
        SearchOffset searchOffset;
        if (fields.length > 1) {
            searchOffset = createSearchOffset(keyword, fields[1]);
        } else if (useLastKeyword) {
            searchOffset = lastSearch.getSearchOffset();
        } else {
            searchOffset = SearchOffset.NONE;
        }
        Search search = createSearch(editor, keyword, backward, searchOffset);
        return search;
    }

	/** Parses the (partial) buffer and returns only the keyword part. */
	public String getKeyWord() {
        String first = commandLine.getPrompt();
        String command = commandLine.getContents();
        return command.split("(?<!\\\\)\\"+first)[0];
	}

    private SearchOffset createSearchOffset(String keyword, String afterSearch) {
        Matcher m = AFTER_SEARCH_PATTERN.matcher(afterSearch);
        String group;
        if(!m.find() || VimUtils.isBlank(afterSearch)) {
            return null;
        }
        group = m.group(2);
        int offset = VimUtils.isBlank(group) ? 0 : Integer.parseInt(group);
        group = m.group(1);
        if (VimUtils.isBlank(group)) {
            return new SearchOffset.Line(offset);
        }
        boolean jumpToEnd = group.equals("e");
        if (jumpToEnd) {
            return new SearchOffset.End(offset);
        }
        if (offset != 0) {
            return new SearchOffset.Begin(offset);
        }
        return SearchOffset.NONE;
    }

}
