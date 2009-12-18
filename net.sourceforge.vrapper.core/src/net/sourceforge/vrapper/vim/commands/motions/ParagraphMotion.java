package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

public class ParagraphMotion extends CountAwareMotion {
    public static final ParagraphMotion FORWARD = new ParagraphMotion(true);
    public static final ParagraphMotion BACKWARD = new ParagraphMotion(false);
    public static final ParagraphMotion TO_FORWARD = new ParagraphMotion(true) {
        protected int moveMore(TextContent modelContent, int lineNo) {
            while (isLineEmpty(modelContent, lineNo))
                lineNo += step;
            return lineNo;
        };
    };
    public static final ParagraphMotion TO_BACKWARD = new ParagraphMotion(false) {
        public Position destination(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
            return super.destination(editorAdaptor, count).addModelOffset(1);
        };
    };
    
    protected final int step;

    private ParagraphMotion(boolean moveForward) {
        step = moveForward ? 1 : -1;
    }

    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
        if (count == NO_COUNT_GIVEN)
            count = 1;
        
        TextContent modelContent = editorAdaptor.getModelContent();
        LineInformation currentLine = modelContent.getLineInformationOfOffset(editorAdaptor.getPosition().getModelOffset());
        
        int lineNo = currentLine.getNumber();
        for (int i = 0; i < count; i++) {
            while (isLineEmpty(modelContent, lineNo))
                lineNo += step;
            while (isLineNonEmpty(modelContent, lineNo))
                lineNo += step;
        }
        
        lineNo = moveMore(modelContent, lineNo);
        int offset = modelContent.getLineInformation(lineNo).getBeginOffset();
        return editorAdaptor.getPosition().setModelOffset(offset);
    }

    protected int moveMore(TextContent modelContent, int lineNo) {
        return lineNo;
    }

    protected boolean isLineEmpty(TextContent content, int lineNo) {
        return doesLineEmptinessEqual(true, content, lineNo);
    }
    
    protected boolean isLineNonEmpty(TextContent content, int lineNo) {
        return doesLineEmptinessEqual(false, content, lineNo);
    }
    
    private boolean doesLineEmptinessEqual(boolean equalWhat, TextContent content, int lineNo) {
        boolean isInRange = (lineNo + step >= 0) && (lineNo + step <= content.getNumberOfLines());
        boolean isEmpty = content.getLineInformation(lineNo).getLength() == 0;
        return isInRange && (isEmpty == equalWhat);
    }

    public BorderPolicy borderPolicy() {
        return BorderPolicy.EXCLUSIVE;
    }

    public boolean updateStickyColumn() {
        return true;
    }

}
