package net.sourceforge.vrapper.vim.modes;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.key;
import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.platform.CommandLineUI;
import net.sourceforge.vrapper.platform.KeyMapProvider;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchOffset;
import net.sourceforge.vrapper.utils.SearchResult;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.SubstitutionDefinition;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.SimpleSelection;

/**
 * Prompt the user for each match in a substitution, activated by the 'c' flag
 * of a substitution definition (e.g., :s/foo/bar/c).
 */
public class ConfirmSubstitutionMode extends AbstractMode {

    public static final String NAME = "confirm substitution mode";
    public static final String DISPLAY_NAME = "CONFIRM SUBSTITUTION";
    private static final KeyStroke KEY_ESCAPE = key(SpecialKey.ESC);
    
    private SubstitutionDefinition subDef;
    private TextRange region;
    private CommandLineUI commandLine;
    private SearchResult nextMatch;

    public ConfirmSubstitutionMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }
    
    /**
     * ModeSwitchHint used when entering mode
     * REQUIRED for ConfirmSubstitutionMode
     */
    public static class SubstitutionConfirm implements ModeSwitchHint {
        protected SubstitutionDefinition subDef;
        protected TextRange region;

        public SubstitutionConfirm(SubstitutionDefinition subDef, TextRange region) {
            this.subDef = subDef;
            this.region = region;
        }
    }

    @Override
    public void enterMode(ModeSwitchHint... hints) throws CommandExecutionException {
        super.enterMode();
        if (hints.length > 0 && hints[0] instanceof SubstitutionConfirm) {
            SubstitutionConfirm hint = (SubstitutionConfirm) hints[0];
            subDef = hint.subDef;
            region = hint.region;
            if(region == null) {
                //if no region, start at cursor and go to end of current line
                Position start = editorAdaptor.getPosition();
                int endOffset = editorAdaptor.getModelContent().getLineInformationOfOffset(start.getModelOffset()).getEndOffset();
                Position end = editorAdaptor.getPosition().setModelOffset(endOffset);
                new StartEndTextRange(start, end);
            }
        }
        else {
            //a SubstitutionConfirm instance is required to enter this mode
            exit();
            return;
        }
        
        commandLine = editorAdaptor.getCommandLine();
        commandLine.setPrompt("replace with " + subDef.replace + " (y/n/a/q/l)?");
        commandLine.open();
        
        //XXX: The initial selection isn't displayed... why?
        findNextMatch(region.getLeftBound(), true);
    }
    
    @Override
    public void leaveMode(ModeSwitchHint... hints) throws CommandExecutionException {
        super.leaveMode(hints);
        commandLine.close();
    }
    
    private void findNextMatch(Position start, boolean doHighlight) {
        SearchOffset afterSearch = new SearchOffset.Begin(start.getModelOffset());
        Search search = new Search(subDef.find, false, false, false, afterSearch, true);
        SearchResult result = editorAdaptor.getSearchAndReplaceService().find(search, start);
        //if no match found or match is outside our range
        if(result.getStart() == null ||
                result.getRightBound().getModelOffset() > region.getRightBound().getModelOffset()) {
            exit();
            return;
        }

        if(doHighlight) {
            editorAdaptor.setSelection( new SimpleSelection(result) );
        }
        nextMatch = result;
    }

    @Override
    public boolean handleKey(KeyStroke stroke) {
        if(stroke.equals(KEY_ESCAPE)) {
            exit();
            return true;
        }

        switch(stroke.getCharacter()) {
        case 'y':
            performSubstitution();
            findNextMatch(nextMatch.getRightBound(), true);
            break;
        case 'n':
            findNextMatch(nextMatch.getRightBound(), true);
            break;
        case 'a':
            replaceAll();
            exit();
            break;
        case 'l':
            performSubstitution();
            exit();
            break;
        case 'q':
            exit();
            break;
        default:
            //ignore all other key presses
            break;
        }
        return true;
    }
    
    private void exit() {
        nextMatch = null;
        editorAdaptor.changeModeSafely(NormalMode.NAME);
    }
    
    private void replaceAll() {
        while(nextMatch != null) {
            performSubstitution();
            findNextMatch(nextMatch.getRightBound(), false);
        }
    }
    
    /**
     * XXX: This text replacement does *not* support regex!
     */
    private void performSubstitution() {
        editorAdaptor.getModelContent().replace(
                nextMatch.getLeftBound().getModelOffset(),
                nextMatch.getModelLength(),
                subDef.replace);
    }

    @Override
    public KeyMap resolveKeyMap(KeyMapProvider provider) {
        return null;
    }

}
