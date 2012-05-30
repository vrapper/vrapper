package net.sourceforge.vrapper.vim.modes.commandline;

import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.platform.Configuration.Option;
import net.sourceforge.vrapper.platform.KeyMapProvider;
import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchOffset;
import net.sourceforge.vrapper.utils.SearchResult;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.LocalConfigurationListener;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.modes.ExecuteCommandHint;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;

public class SearchMode extends AbstractCommandLineMode {

    protected class SearchConfigurationListener implements
            LocalConfigurationListener {
        
        private EditorAdaptor vim;

        public SearchConfigurationListener(EditorAdaptor vim) {
            this.vim = vim;
        }

        public <T> void optionChanged(Option<T> option, T oldValue, T newValue) {
            if (option.equals(Options.SMART_CASE)
                    || option.equals(Options.IGNORE_CASE)
                    || option.equals(Options.SEARCH_HIGHLIGHT)
                    || option.equals(Options.SEARCH_REGEX)) {
                Search lastSearch = vim.getRegisterManager().getSearch();
                // Update the search settings for the last/active search.
                if (lastSearch != null) {
                    lastSearch = SearchCommandParser.createSearch(vim, lastSearch.getKeyword(),
                            lastSearch.isBackward(), lastSearch.isWholeWord(),
                            lastSearch.getSearchOffset());
                    
                    if (Options.SEARCH_HIGHLIGHT.equals(option) && Boolean.FALSE.equals(newValue)) {
                        vim.getSearchAndReplaceService().removeHighlighting();
                        
                    // Update highlights when enabled, as search might now match more things
                    } else if (vim.getConfiguration().get(Options.SEARCH_HIGHLIGHT)) {
                        vim.getSearchAndReplaceService().highlight(lastSearch);
                    }
                }
                vim.getRegisterManager().setSearch(lastSearch);
            }
        }
    }

    public static final String NAME = "search mode";
    public static final String DISPLAY_NAME = "SEARCH";

    private boolean forward;
    private Position startPos;
    private int originalTopLine;
    private Command command;
    private SearchCommandParser searchParser;

    public SearchMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
        editorAdaptor.getConfiguration().addListener(new SearchConfigurationListener(editorAdaptor));
    }

    /**
     * @param args {@link Direction} of the search
     */
    @Override
    public void enterMode(ModeSwitchHint... args) {
        forward = args[0].equals(Direction.FORWARD);
        command = ((ExecuteCommandHint.OnLeave) args[1]).getCommand();
        startPos = editorAdaptor.getCursorService().getPosition();
        originalTopLine = editorAdaptor.getViewportService().getViewPortInformation().getTopLine();
        searchParser = new SearchCommandParser(editorAdaptor, command);
        super.enterMode(args);
    }

    @Override
    protected AbstractCommandParser createParser() {
        return searchParser;
    }

    @Override
    public boolean handleKey(KeyStroke stroke) {
        boolean incsearch = editorAdaptor.getConfiguration().get(Options.INCREMENTAL_SEARCH);
        if (incsearch &&
                (stroke.equals(AbstractCommandParser.KEY_RETURN) ||
                    stroke.equals(AbstractCommandParser.KEY_ESCAPE) ||
                       stroke.equals(AbstractCommandParser.KEY_CTRL_C))) {
                resetIncSearch();
        }
        super.handleKey(stroke);
        if (incsearch && isEnabled) {
            // isEnabled == false indicates that super method ran a search and went to normal mode.
            doIncSearch();
        }
        return true;
    }

    private void resetIncSearch() {
        editorAdaptor.getSearchAndReplaceService().removeIncSearchHighlighting();
        editorAdaptor.getCursorService().setPosition(startPos, false);
        editorAdaptor.getViewportService().setTopLine(originalTopLine);
    }

    private void doIncSearch() {
        String keyword = searchParser.getKeyWord();
        Search s = SearchCommandParser.createSearch(editorAdaptor, keyword, !forward, false, SearchOffset.NONE);
        SearchResult res = VimUtils.wrapAroundSearch(editorAdaptor, s, startPos);
        if (res.isFound()) {
            if (editorAdaptor.getConfiguration().get(Options.SEARCH_HIGHLIGHT)) {
                SearchAndReplaceService sars = editorAdaptor.getSearchAndReplaceService();
                sars.incSearchhighlight(res.getStart(), res.getModelLength());
            }
            MotionCommand.gotoAndChangeViewPort(editorAdaptor, res.getStart(), false);
        } else {
            resetIncSearch();
        }
    }

    public KeyMap resolveKeyMap(KeyMapProvider provider) {
        return null;
    }

        @Override
    protected char activationChar() {
        return forward ? '/' : '?';
    }

    public String getName() {
        return NAME;
    }

    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    public enum Direction implements ModeSwitchHint {
        FORWARD, BACKWARD;
    }


}
