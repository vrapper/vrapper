package net.sourceforge.vrapper.utils;

import net.sourceforge.vrapper.platform.TextContent;

public abstract class SearchOffset {

    public static final SearchOffset NONE = new Begin(0);
    private final int offset;

    private SearchOffset(int offset) {
        this.offset = offset;
    }

    public abstract Position apply(TextContent modelContent, SearchResult result);

    public abstract Position unapply(TextContent modelContent, Position position, SearchResult result);

    public abstract boolean lineWise();

    public int getOffset() {
        return offset;
    }

    public static class Begin extends SearchOffset {

        public Begin(int offset) {
            super(offset);
        }

        @Override
        public Position apply(TextContent modelContent, SearchResult result) {
            Position start = result.getLeftBound();
            return start.setModelOffset(VimUtils.calculatePositionForOffset(
                    modelContent, start.getModelOffset(), getOffset()));
        }

        @Override
        public Position unapply(TextContent modelContent, Position position, SearchResult result) {
            return position.setModelOffset(VimUtils.calculatePositionForOffset(
                    modelContent, position.getModelOffset(), -getOffset()));
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
        public Position apply(TextContent modelContent, SearchResult result) {
            Position end = result.getRightBound();
            return end.setModelOffset(VimUtils.calculatePositionForOffset(
                    modelContent, end.getModelOffset(), getOffset()-1)); //SearchResult is exclusive
        }

        @Override
        public Position unapply(TextContent modelContent, Position position, SearchResult result) {
            return position.setModelOffset(VimUtils.calculatePositionForOffset(
                    modelContent, position.getModelOffset(), -result.getModelLength()-getOffset()+1)); //SearchResult is exclusive
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
        public Position apply(TextContent modelContent, SearchResult result) {
            TextContent tc = modelContent;
            Position start = result.getLeftBound();
            LineInformation currLine = tc.getLineInformationOfOffset(start.getModelOffset());
            int number = currLine.getNumber()+getOffset();
            number = Math.max(number, 0);
            number = Math.min(number, tc.getNumberOfLines()-1);
            return start.setModelOffset(tc.getLineInformation(number).getBeginOffset());
        }

        @Override
        public Position unapply(TextContent modelContent, Position position, SearchResult result) {
            return position;
        }

        @Override
        public boolean lineWise() {
            return true;
        }
    }

}
