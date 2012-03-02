package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchOffset;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.modes.commandline.SearchCommandParser;

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
        String keyword = grabCurrentWord(editorAdaptor);
        Search search = SearchCommandParser.createSearch(editorAdaptor, keyword, reverse, wholeWord,
                SearchOffset.NONE);
        editorAdaptor.getRegisterManager().setSearch(search);
        return super.destination(editorAdaptor, count);
    }

    private static String grabCurrentWord(EditorAdaptor editorAdaptor) {
        String keyword = "";
        TextContent p = editorAdaptor.getViewContent();
        int index = editorAdaptor.getCursorService().getPosition().getViewOffset();
        LineInformation line = p.getLineInformationOfOffset(index);
        int min = line.getBeginOffset();
        int max = line.getEndOffset();
        int first = -1;
        int last = -1;
        String s;
        boolean found = false;
        if (index < max) {
            s = p.getText(index, 1);
            if (VimUtils.isWordCharacter(s)) {
                found = true;
                first = index;
                last = index;
            }
        }
        while (index < max-1) {
            index += 1;
            s = p.getText(index, 1);
            if(VimUtils.isWordCharacter(s)) {
                last = index;
                if(!found) {
                    first = index;
                    found = true;
                }
            } else if(found) {
                break;
            }
        }
        if (found) {
            index = first;
            while (index > min) {
                index -= 1;
                s = p.getText(index, 1);
                if(VimUtils.isWordCharacter(s)) {
                    first = index;
                } else {
                    break;
                }
            }
            keyword = p.getText(first, last-first+1);
        }
        return keyword;
    }

}
