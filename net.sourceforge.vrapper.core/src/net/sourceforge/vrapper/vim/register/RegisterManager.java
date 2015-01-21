package net.sourceforge.vrapper.vim.register;

import java.util.Set;

import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchResult;
import net.sourceforge.vrapper.utils.SelectionArea;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.TextOperation;
import net.sourceforge.vrapper.vim.commands.motions.FindMotion;

/**
 * Provides access to different registers.
 *
 * @author Matthias Radig
 */
public interface RegisterManager {

    public static final String REGISTER_NAME_UNNAMED   = "\"";
    public static final String REGISTER_NAME_INSERT    = ".";
    /** System clipboard register. */
    public static final String REGISTER_NAME_CLIPBOARD = "+";
    /** "Last selection" clipboard register. */
    public static final String REGISTER_NAME_SELECTION = "*";
    public static final String REGISTER_NAME_SEARCH    = "/";
    public static final String REGISTER_SMALL_DELETE   = "-";
    public static final String REGISTER_NAME_BLACKHOLE = "_";
    public static final String REGISTER_NAME_LAST      = "@";
    public static final String REGISTER_NAME_COMMAND   = ":";
    public static final String CLIPBOARD_VALUE_UNNAMED = "unnamed";
    public static final String CLIPBOARD_VALUE_UNNAMEDPLUS = "unnamedplus";
    public static final String CLIPBOARD_VALUE_AUTOSELECT = "autoselect";
    public static final String CLIPBOARD_VALUE_AUTOSELECTPLUS = "autoselectplus";

    Set<String> getRegisterNames();
    Register getRegister(String name);
    Register getDefaultRegister();
    Register getActiveRegister();
    Register getLastEditRegister();
    void setLastNamedRegister(Register register);
    void setActiveRegister(String name);
    void setActiveRegister(Register register);
    void activateDefaultRegister();
    void activateLastEditRegister();
    Command getLastEdit();
    void setLastEdit(Command edit);
    void setLastInsertion(Command command);
    Command getLastInsertion();
    void setLastSubstitution(TextOperation operation);
    TextOperation getLastSubstitution();
    void setLastYank(RegisterContent register);
    void setLastDelete(RegisterContent register);
    void setLastFindMotion(FindMotion motion);
    FindMotion getLastFindMotion();
    Search getSearch();
    void setSearch(Search search);
    boolean isDefaultRegisterActive();
    void setLastActiveSelection(SelectionArea selectionArea);
    SelectionArea getLastActiveSelectionArea();
    public void setCurrentWorkingDirectory(String cwd);
    public String getCurrentWorkingDirectory();
    public void setLastCommand(String macroString);
    /**
     * Return information about the last search match.
     * @return either an instance of SearchResult (possibly one for which `isFound` returns false!)
     *  or <code>null</code> if no search query has been executed yet.
     */
    public SearchResult getLastSearchResult();
    void setLastSearchResult(SearchResult result);
}
