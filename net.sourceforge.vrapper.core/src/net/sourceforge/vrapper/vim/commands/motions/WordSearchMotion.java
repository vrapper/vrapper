package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

public class WordSearchMotion extends SearchResultMotion {

    private final boolean reverse;

    public WordSearchMotion(boolean reverse) {
        super(false);
        this.reverse = reverse;
    }

    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
        editorAdaptor.getRegisterManager().setSearch(createSearch(editorAdaptor));
        return super.destination(editorAdaptor, count);
    }

    private Search createSearch(EditorAdaptor editorAdaptor) {
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
        return new Search(keyword, reverse, true, true);
    }

}
