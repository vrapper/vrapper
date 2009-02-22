package de.jroene.vrapper.vim.action;


import de.jroene.vrapper.vim.LineInformation;
import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.Search;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.VimUtils;

public class WordSearchAction extends AbstractSearchAction {

    public WordSearchAction(boolean reverse) {
        super(reverse);
    }

    @Override
    protected Search getSearch(VimEmulator vim) {
        String keyword = "";
        Platform p = vim.getPlatform();
        int index = p.getPosition();
        LineInformation line = p.getLineInformation();
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
        return new Search(keyword, false);
    }

}
