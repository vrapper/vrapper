package net.sourceforge.vrapper.utils;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public abstract class SearchOffset {

    public static final SearchOffset NONE = new Begin(0);
    private final int offset;

    private SearchOffset(int offset) {
        this.offset = offset;
    }

    public abstract Position apply(EditorAdaptor vim, Position position);

    public abstract Position unapply(EditorAdaptor vim, Position position);

    public abstract boolean lineWise();

    public int getOffset() {
        return offset;
    }

    public static class Begin extends SearchOffset {

        public Begin(int offset) {
            super(offset);
        }

        @Override
        public Position apply(EditorAdaptor vim, Position position) {
            return position.setModelOffset(VimUtils.calculatePositionForOffset(
                    vim.getModelContent(), position.getModelOffset(), getOffset()));
        }

        @Override
        public Position unapply(EditorAdaptor vim, Position position) {
            return position.setModelOffset(VimUtils.calculatePositionForOffset(
                    vim.getModelContent(), position.getModelOffset(), -getOffset()));
        }

        @Override
        public boolean lineWise() {
            return false;
        }
    }

    public static class End extends SearchOffset {

        public End(int offset) {
            super(offset);
        }

        @Override
        public Position apply(EditorAdaptor vim, Position position) {
            String keyword = vim.getRegisterManager().getSearch().getKeyword();
            return position.setModelOffset(VimUtils.calculatePositionForOffset(
                    vim.getModelContent(), position.getModelOffset(), getOffset()+keyword.length()-1));
        }

        @Override
        public Position unapply(EditorAdaptor vim, Position position) {
            String keyword = vim.getRegisterManager().getSearch().getKeyword();
            return position.setModelOffset(VimUtils.calculatePositionForOffset(
                    vim.getModelContent(), position.getModelOffset(), -getOffset()-keyword.length()+1));
        }

        @Override
        public boolean lineWise() {
            return false;
        }
    }

    public static class Line extends SearchOffset {

        public Line(int offset) {
            super(offset);
        }

        @Override
        public Position apply(EditorAdaptor vim, Position position) {
            TextContent tc = vim.getModelContent();
            LineInformation currLine = tc.getLineInformationOfOffset(position.getModelOffset());
            int number = currLine.getNumber()+getOffset();
            number = Math.max(number, 0);
            number = Math.min(number, tc.getNumberOfLines()-1);
            return position.setModelOffset(tc.getLineInformation(number).getBeginOffset());
        }

        @Override
        public Position unapply(EditorAdaptor vim, Position position) {
            return position;
        }

        @Override
        public boolean lineWise() {
            return true;
        }
    }

}
