package net.sourceforge.vrapper.plugin.linetextobj.commands;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.AbstractTextObject;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.InnerLineTextObject;
import net.sourceforge.vrapper.vim.commands.TextObject;

public class LineTextObject extends AbstractTextObject {

    public static final TextObject INNER = new InnerLineTextObject();
    public static final TextObject OUTER = new LineTextObject();

    private LineTextObject() {
    }

    public ContentType getContentType(Configuration configuration) {
        return ContentType.TEXT;
    }

    @Override
    public TextRange getRegion(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
        CursorService cs = editorAdaptor.getCursorService();
        int offset = cs.getPosition().getModelOffset();
        LineInformation line = editorAdaptor.getModelContent().getLineInformationOfOffset(offset);
        Position left = cs.newPositionForModelOffset(line.getBeginOffset());
        Position right = cs.newPositionForModelOffset(line.getEndOffset());
        return new StartEndTextRange(left, right);
    }

}