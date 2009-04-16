package de.jroene.vrapper.vim.commandline;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.jroene.vrapper.vim.Search;
import de.jroene.vrapper.vim.VimConstants;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.VimUtils;
import de.jroene.vrapper.vim.action.SearchMove;
import de.jroene.vrapper.vim.token.Token;

/**
 * Mode for input of search parameters (keyword and offset).
 *
 * @author Matthias Radig
 */
public class SearchMode extends AbstractCommandMode {

    private static final Pattern AFTER_SEARCH_PATTERN = Pattern.compile("(e|b)?\\+?(-?\\d+)?");

    public SearchMode(VimEmulator vim) {
        super(vim);
    }

    /**
     * Parses and executes a search.
     * 
     * @param first contains the "command", probably '/' or '?' when searching
     * @param command contains the string to search for
     */
    @Override
    public Token parseAndExecute(String first, String command) {
        Search search = createSearch(first, command);
        vim.getRegisterManager().setSearch(search);
        return new SearchMove(false);
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
        Search oldSearch = vim.getRegisterManager().getSearch();
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
        Search search = new Search(keyword, backward, false, searchOffset);
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
