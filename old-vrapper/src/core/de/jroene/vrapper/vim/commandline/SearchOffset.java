package de.jroene.vrapper.vim.commandline;

import de.jroene.vrapper.vim.LineInformation;
import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.VimUtils;

public abstract class SearchOffset {

    public static final SearchOffset NONE = new Begin(0);
    private final int offset;

    private SearchOffset(int offset) {
        this.offset = offset;
    }

    public abstract int apply(VimEmulator vim, int position);

    public abstract int unapply(VimEmulator vim, int position);

    public int getOffset() {
        return offset;
    }

    public static class Begin extends SearchOffset {

        public Begin(int offset) {
            super(offset);
        }

        @Override
        public int apply(VimEmulator vim, int position) {
            Platform p = vim.getPlatform();
            return VimUtils.calculatePositionForOffset(
                    p, position, getOffset());
        }

        @Override
        public int unapply(VimEmulator vim, int position) {
            Platform p = vim.getPlatform();
            return VimUtils.calculatePositionForOffset(
                    p, position, -getOffset());
        }

    }

    public static class End extends SearchOffset {

        public End(int offset) {
            super(offset);
        }

        @Override
        public int apply(VimEmulator vim, int position) {
            Platform p = vim.getPlatform();
            String keyword = vim.getRegisterManager().getSearch().getKeyword();
            return VimUtils.calculatePositionForOffset(
                    p, position, getOffset()+keyword.length()-1);
        }

        @Override
        public int unapply(VimEmulator vim, int position) {
            Platform p = vim.getPlatform();
            String keyword = vim.getRegisterManager().getSearch().getKeyword();
            return VimUtils.calculatePositionForOffset(
                    p, position, -getOffset()-keyword.length()+1);
        }

    }

    public static class Line extends SearchOffset {

        public Line(int offset) {
            super(offset);
        }

        @Override
        public int apply(VimEmulator vim, int position) {
            Platform p = vim.getPlatform();
            LineInformation currLine = p.getLineInformationOfOffset(position);
            int number = currLine.getNumber()+getOffset();
            number = Math.max(number, 0);
            number = Math.min(number, p.getNumberOfLines()-1);
            return p.getLineInformation(number).getBeginOffset();
        }

        @Override
        public int unapply(VimEmulator vim, int position) {
            return position;
        }
    }

}
