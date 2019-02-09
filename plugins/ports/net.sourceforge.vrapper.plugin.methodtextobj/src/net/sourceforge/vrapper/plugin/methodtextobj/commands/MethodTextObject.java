package net.sourceforge.vrapper.plugin.methodtextobj.commands;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.AbstractTextObject;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.motions.MethodDeclarationMotion;

public class MethodTextObject extends AbstractTextObject {

    public static final MethodTextObject INNER = new MethodTextObject(false);
    public static final MethodTextObject OUTER = new MethodTextObject(true);

    private boolean outer;

    private MethodTextObject(boolean outer) {
        this.outer = outer;
    }

    @Override
    public TextRange getRegion(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        Position currentOffset = editorAdaptor.getPosition();
        Position start = MethodDeclarationMotion.PREV_START.destination(editorAdaptor, currentOffset);
        Position end = MethodDeclarationMotion.NEXT_END.destination(editorAdaptor, currentOffset);
        //if start == end, no method was found
        if(start.getModelOffset() < end.getModelOffset()) {
            TextContent model = editorAdaptor.getModelContent();
            LineInformation lineStart = model.getLineInformationOfOffset(start.getModelOffset());
            LineInformation lineEnd = model.getLineInformationOfOffset(end.getModelOffset());
            if(outer) {
                //go line-wise for outer (hopefully this will include the method declaration)
                start = editorAdaptor.getCursorService().newPositionForModelOffset(lineStart.getBeginOffset());
                end = editorAdaptor.getCursorService().newPositionForModelOffset(lineEnd.getEndOffset());
            }
            else {
                //inner text ranges in Vim don't start on a newline
                //or end with only whitespace.  Fix Position if either
                //of those cases are true here.
                CursorService cursor = editorAdaptor.getCursorService();
                start = VimUtils.fixLeftDelimiter(model, cursor, start);
                end = VimUtils.fixRightDelimiter(model, cursor, end);
            }
        }
        return new StartEndTextRange(start, end);
    }

    public ContentType getContentType(Configuration configuration) {
        return ContentType.TEXT;
    }

}