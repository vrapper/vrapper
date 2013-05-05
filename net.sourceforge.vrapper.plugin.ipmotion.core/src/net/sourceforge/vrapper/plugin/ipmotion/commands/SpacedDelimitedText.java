package net.sourceforge.vrapper.plugin.ipmotion.commands;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.SimpleDelimitedText;

public class SpacedDelimitedText extends SimpleDelimitedText {
    public SpacedDelimitedText(char l, char r) {
        super(l, r);
    }
    
    @Override
    public TextRange leftDelimiter(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        TextRange result = super.leftDelimiter(editorAdaptor, count);
        int offset = result.getRightBound().getModelOffset();
        TextContent modelContent = editorAdaptor.getModelContent();
        int length = modelContent.getLineInformationOfOffset(offset).getEndOffset() - offset;
        String content = modelContent.getText(offset, length);
        int i = 0;
        while (Character.isWhitespace(content.charAt(i)))
            if (++i == length)
                return result;
        return new StartEndTextRange(result.getLeftBound(), result.getRightBound().addModelOffset(i));
    }
    
    @Override
    public TextRange rightDelimiter(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        TextRange result = super.rightDelimiter(editorAdaptor, count);
        int offset = result.getLeftBound().getModelOffset();
        TextContent modelContent = editorAdaptor.getModelContent();
        int beginOffset = modelContent.getLineInformationOfOffset(offset).getBeginOffset();
        int length = offset - beginOffset;
        String content = modelContent.getText(beginOffset, length);
        int i = length - 1;
        while (Character.isWhitespace(content.charAt(i))) {
            if (--i < 0)
                return result;
        }
        return new StartEndTextRange(result.getLeftBound().addModelOffset(i-length+1), result.getRightBound());
    }

}
