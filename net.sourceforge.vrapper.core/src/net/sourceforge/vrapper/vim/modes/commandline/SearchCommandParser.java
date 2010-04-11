package net.sourceforge.vrapper.vim.modes.commandline;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchOffset;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.VimConstants;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.commands.motions.SearchResultMotion;

/**
 * Mode for input of search parameters (keyword and offset).
 *
 * @author Matthias Radig
 */
public class SearchCommandParser extends AbstractCommandParser {

    private static final Pattern AFTER_SEARCH_PATTERN = Pattern.compile("(e|b)?\\+?(-?\\d+)?");

    public SearchCommandParser(EditorAdaptor vim) {
        super(vim);
    }

    /**
     * Parses and executes a search.
     *
     * @param first contains the "command", probably '/' or '?' when searching
     */
    @Override
    public void parseAndExecute(String first, String command) {
        Search search = createSearch(first, command);
        editor.getRegisterManager().setSearch(search);
        try {
            MotionCommand.doIt(editor, SearchResultMotion.FORWARD);
        } catch (CommandExecutionException e) {
            // TODO: something useful
        }
    }

	private Search createSearch(String first, String command) {
        boolean backward = first.equals(VimConstants.BACKWARD_SEARCH_CHAR);
        StringTokenizer nizer = new StringTokenizer(command, first);
        StringBuilder sb = new StringBuilder();
        // check whether a keyword was given
        if (!command.startsWith(first)) {
            while (nizer.hasMoreTokens()) {
                String token = nizer.nextToken();
                sb.append(token);
                if (token.endsWith(VimConstants.ESCAPE_CHAR)) {
                    sb.replace(sb.length()-1, sb.length(), first);
                } else {
                    break;
                }
            }
        }
        String keyword;
        // if keyword is empty, last keyword is used
        Search oldSearch = editor.getRegisterManager().getSearch();
        boolean useLastKeyword = sb.length() == 0 && oldSearch != null;
        if (useLastKeyword) {
            keyword = oldSearch.getKeyword();
        } else {
            keyword = sb.toString();
        }
        SearchOffset searchOffset;
        if (nizer.hasMoreTokens()) {
            String afterSearch = nizer.nextToken();
            searchOffset = createSearchOffset(keyword, afterSearch);
        } else if (useLastKeyword) {
            searchOffset = oldSearch.getSearchOffset();
        } else {
            searchOffset = SearchOffset.NONE;
        }
        boolean caseSensitive = !editor.getConfiguration().get(Options.IGNORE_CASE)
            || editor.getConfiguration().get(Options.SMART_CASE)
            && StringUtils.containsUppercase(keyword);
        boolean useRegExp = editor.getConfiguration().get(Options.SEARCH_REGEX);
        Search search = new Search(keyword, backward, false, caseSensitive, searchOffset, useRegExp);
        return search;
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
