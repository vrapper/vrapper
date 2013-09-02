package net.sourceforge.vrapper.vim.commands;

/**
 * Use a NormalMode text object to define a line range operation.
 */
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;
import net.sourceforge.vrapper.vim.modes.InitialContentsHint;
import net.sourceforge.vrapper.vim.modes.commandline.CommandLineMode;

public class NormalLineRangeOperation extends SimpleTextOperation {
    
    public static final NormalLineRangeOperation INSTANCE = new NormalLineRangeOperation();
    
    private NormalLineRangeOperation() {
        //no-op
    }

    @Override
    public TextOperation repetition() {
        return null;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor, TextRange region, ContentType contentType) throws CommandExecutionException {
        String endDef;
        if(region.getRightBound().getModelOffset() == editorAdaptor.getModelContent().getTextLength()) {
            endDef = "$";
        }
        else {
            TextContent model = editorAdaptor.getModelContent();
            int startLine = model.getLineInformationOfOffset(region.getLeftBound().getModelOffset()).getNumber();
            int endLine = model.getLineInformationOfOffset(region.getRightBound().getModelOffset()).getNumber();
            if(contentType == ContentType.LINES) {
                endLine--;
            }

            endDef = ".";
            if(endLine > startLine) {
                endDef += "+" + (endLine - startLine);
            }
        }
        
        editorAdaptor.setPosition(region.getLeftBound(), StickyColumnPolicy.NEVER);
        editorAdaptor.changeModeSafely(CommandLineMode.NAME, new InitialContentsHint(".," + endDef + "!"));
    }

}
