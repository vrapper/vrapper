package net.sourceforge.vrapper.vim.commands.motions;

import java.util.Locale;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchOffset;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.modes.commandline.CommandLineHistory;
import net.sourceforge.vrapper.vim.modes.commandline.SearchCommandParser;
import net.sourceforge.vrapper.vim.modes.commandline.SearchMode;

/** Starts a new search with the word the cursor sits on. */
public class WordSearchMotion extends SearchResultMotion {

    public static final WordSearchMotion FORWARD = new WordSearchMotion(false, true);
    public static final WordSearchMotion BACKWARD = new WordSearchMotion(true, true);
    public static final WordSearchMotion LENIENT_FORWARD = new WordSearchMotion(false, false);
    public static final WordSearchMotion LENIENT_BACKWARD = new WordSearchMotion(true, false);
    private final boolean reverse;
    private final boolean wholeWord;

    public WordSearchMotion(boolean reverse, boolean wholeWord) {
        super(false);
        this.reverse = reverse;
        this.wholeWord = wholeWord;
    }

    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
        String keyword = VimUtils.getWordUnderCursor(editorAdaptor, false);
        if (editorAdaptor.getConfiguration().get(Options.IGNORE_CASE)) {
            // SearchCommandParser may use 'smartcase', but we don't want that.
            keyword = keyword.toLowerCase(Locale.ENGLISH);
        }
        Search search = SearchCommandParser.createSearch(editorAdaptor, keyword, reverse, wholeWord,
                SearchOffset.NONE);
        editorAdaptor.getRegisterManager().setSearch(search);
        //add '*' and '#' searches to command-line history for search mode
        CommandLineHistory history = CommandLineHistory.INSTANCE;
        history.setMode(SearchMode.NAME);
        if(this.wholeWord) {
            //these will be converted to '\b' before the search actually runs
            //but we want '\<' and '\>' to appear in the search history
            keyword = "\\<" + keyword + "\\>";
        }
        history.append(keyword);

        return super.destination(editorAdaptor, count);
    }

}
