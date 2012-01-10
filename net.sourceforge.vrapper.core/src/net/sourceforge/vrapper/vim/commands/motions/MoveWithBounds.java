package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public abstract class MoveWithBounds extends CountAwareMotion {
    protected static final int BUFFER_LEN = 32;
    
    protected abstract boolean atBoundary(char c1, char c2);
    protected abstract boolean stopsAtNewlines();
    protected abstract boolean shouldStopAtLeftBoundingChar();
    protected abstract int destination(int offset, TextContent viewContent, boolean bailOff, boolean hasMoreCounts);

    private final boolean bailOff;
    
    public MoveWithBounds(boolean bailOff) {
        this.bailOff = bailOff;
    }
    
    public boolean updateStickyColumn() {
        return true;
    }
    
    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count) {
        if (count == NO_COUNT_GIVEN)
            count = 1;

        int offset = editorAdaptor.getPosition().getModelOffset();

        for (int i = 0; i < count; i++)
            offset = destination(offset, editorAdaptor.getModelContent(), bailOff && i == 0, i != count-1);
        
        return editorAdaptor.getCursorService().newPositionForModelOffset(offset);
    }
}
    