package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.Motion;

public class MotionTextObject extends AbstractTextObject {

    private final Motion motion;

    public MotionTextObject(Motion move) {
        this.motion = move;
    }

    public TextRange getRegion(EditorAdaptor editorMode, int count) throws CommandExecutionException {
        Position from = editorMode.getPosition();
        Position to = motion.withCount(count).destination(editorMode);
        return applyBorderPolicy(editorMode, from, to);
    };

    private TextRange applyBorderPolicy(EditorAdaptor editorMode, Position from, Position to) {
        switch (motion.borderPolicy()) {
        case EXCLUSIVE: return new StartEndTextRange(from, to);
        case INCLUSIVE: return new StartEndTextRange(from, to.addModelOffset(1));
        case LINE_WISE: return lines(editorMode, from, to);
        default:
            throw new RuntimeException("unsupported border policy: " + motion.borderPolicy());
        }
    }

    private static TextRange lines(EditorAdaptor editor, Position from, Position to) {
        TextContent txt = editor.getModelContent();
        LineInformation sLine = txt.getLineInformationOfOffset(from.getModelOffset());
        LineInformation eLine = txt.getLineInformationOfOffset(to.getModelOffset());
        if (sLine.getNumber() > eLine.getNumber())
            return lines(editor, to, from);
        CursorService cs = editor.getCursorService();
        int startIndex = sLine.getBeginOffset();
        int endIndex = txt.getLineInformation(eLine.getNumber()+1).getBeginOffset();
        if (eLine.getNumber() == txt.getNumberOfLines())
            endIndex = txt.getTextLength();
        return new StartEndTextRange(
                cs.newPositionForModelOffset(startIndex),
                cs.newPositionForModelOffset(endIndex));
    }

    public ContentType getContentType(Configuration configuration) {
        return ContentType.fromBorderPolicy(motion.borderPolicy());
    }

}
