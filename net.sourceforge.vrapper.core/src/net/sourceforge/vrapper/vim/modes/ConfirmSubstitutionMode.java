package net.sourceforge.vrapper.vim.modes;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.key;
import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.platform.CommandLineUI;
import net.sourceforge.vrapper.platform.KeyMapProvider;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchOffset;
import net.sourceforge.vrapper.utils.SearchResult;
import net.sourceforge.vrapper.utils.SubstitutionDefinition;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CenterLineCommand;
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
    private int endOffset;
    private int nextLine;
    private boolean globalFlag = false;
    private CommandLineUI commandLine;
    private SearchResult lastMatch;

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
        protected int startLine;
        protected int endLine;

        public SubstitutionConfirm(SubstitutionDefinition subDef, int startLine, int endLine) {
            this.subDef = subDef;
            this.startLine = startLine;
            this.endLine = endLine;
        }
    }

    @Override
    public void enterMode(ModeSwitchHint... hints) throws CommandExecutionException {
        super.enterMode();
        if (hints.length > 0 && hints[0] instanceof SubstitutionConfirm) {
            SubstitutionConfirm hint = (SubstitutionConfirm) hints[0];
            subDef = hint.subDef;
            globalFlag = subDef.flags.contains("g");
            nextLine = hint.startLine;

            TextContent model = editorAdaptor.getModelContent();
            if(hint.endLine == model.getNumberOfLines()) {
                endOffset = model.getTextLength();
            }
            else {
                endOffset = model.getLineInformation(hint.endLine).getEndOffset();
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
        
        //XXX: Ugly hack.  This call will highlight the initial match to get
        //things started. But, I can't set a selection from within this method
        //because TextOperationTextObjectCommand will clear all selections after
        //this method returns.  So I added a horrible horrible check in that
        //class to *not* clear the selection if I've entered this mode.
        findNextMatch(true);
    }
    
    @Override
    public void leaveMode(ModeSwitchHint... hints) throws CommandExecutionException {
        super.leaveMode(hints);
        commandLine.close();
    }
    
    private void findNextMatch(boolean doHighlight) {
        int startOffset;
        if(lastMatch != null && globalFlag) {
            //next match might be on the same line
            startOffset = lastMatch.getRightBound().getModelOffset();
        }
        else {
            //start on next line
            startOffset = editorAdaptor.getModelContent().getLineInformation(nextLine).getBeginOffset();   
        }
    
        SearchOffset afterSearch = new SearchOffset.Begin(startOffset);
        Search search = new Search(subDef.find, false, false, false, afterSearch, true);
        SearchResult result = editorAdaptor.getSearchAndReplaceService().find(
                search,
                editorAdaptor.getPosition().setModelOffset(startOffset));
        //if no match found or match is outside our range
        if(result.getStart() == null || result.getRightBound().getModelOffset() > endOffset) {
            exit();
            return;
        }

        editorAdaptor.setPosition(result.getLeftBound(), false);
        if(doHighlight) {
            //force match to be visible (move scrollbars)
            //is there a better way to do this?
            CenterLineCommand.CENTER.execute(editorAdaptor);
            //highlight match
            editorAdaptor.setSelection( new SimpleSelection(result) );
        }

        //prepare for next iteration
        lastMatch = result;
        nextLine = editorAdaptor.getModelContent().getLineInformationOfOffset(
                lastMatch.getRightBound().getModelOffset()
                ).getNumber() + 1; //next line after match
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
            findNextMatch(true);
            break;
        case 'n':
            findNextMatch(true);
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
        lastMatch = null;
        editorAdaptor.setSelection(null);
        editorAdaptor.changeModeSafely(NormalMode.NAME);
    }
    
    private void replaceAll() {
        editorAdaptor.getHistory().beginCompoundChange();
        while(lastMatch != null) {
            performSubstitution();
            findNextMatch(false);
        }
        editorAdaptor.getHistory().endCompoundChange();
    }

    private void performSubstitution() {
        editorAdaptor.getSearchAndReplaceService().substitute(
                lastMatch.getLeftBound().getModelOffset(),
                subDef.find, subDef.flags, subDef.replace);
    }

    @Override
    public KeyMap resolveKeyMap(KeyMapProvider provider) {
        return null;
    }

}
