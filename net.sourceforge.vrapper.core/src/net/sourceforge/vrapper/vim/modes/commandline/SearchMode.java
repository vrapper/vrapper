package net.sourceforge.vrapper.vim.modes.commandline;

import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.KeyMapProvider;
import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchResult;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.modes.AbstractCommandLineMode;
import net.sourceforge.vrapper.vim.modes.ExecuteCommandHint;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;

public class SearchMode extends AbstractCommandLineMode {

    public static final String NAME = "search mode";

    private boolean forward;
    private Position startPos;
    private int originalTopLine;
    private Command command;

    public SearchMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
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
        super.enterMode(args);
    }

    @Override
    protected AbstractCommandParser createParser() {
        return new SearchCommandParser(editorAdaptor, command);
    }

    @Override
    public boolean handleKey(KeyStroke stroke) {
        boolean incsearch = editorAdaptor.getConfiguration().get(Options.INCREMENTAL_SEARCH);
        if (incsearch &&
                (stroke.equals(AbstractCommandParser.KEY_RETURN) ||
                    stroke.equals(AbstractCommandParser.KEY_ESCAPE))) {
                resetIncSearch();
        }
        super.handleKey(stroke);
        if (incsearch && isEnabled) {
            // isEnabled is false when input was finished by pressing enter
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
        String keyword = ((SearchCommandParser) parser).getKeyWord();
        Configuration config = editorAdaptor.getConfiguration();
        boolean caseSensitive = !config.get(Options.IGNORE_CASE)
            || config.get(Options.SMART_CASE)
            && StringUtils.containsUppercase(keyword);
        boolean useRegExp = config.get(Options.SEARCH_REGEX);
        Search s = new Search(keyword, !forward, false, caseSensitive, null, useRegExp);
        SearchResult res = VimUtils.wrapAroundSearch(editorAdaptor, s, startPos);
        if (res.isFound()) {
            if (config.get(Options.SEARCH_HIGHLIGHT)) {
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

    public enum Direction implements ModeSwitchHint {
        FORWARD, BACKWARD;
    }


}
