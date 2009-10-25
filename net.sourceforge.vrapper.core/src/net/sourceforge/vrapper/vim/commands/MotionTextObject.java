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
        TextContent vc = editor.getModelContent();
        LineInformation sLine = vc.getLineInformationOfOffset(from.getModelOffset());
        LineInformation eLine = vc.getLineInformationOfOffset(to.getModelOffset());
        CursorService cs = editor.getCursorService();
        if (sLine.getNumber() < eLine.getNumber()) {
            int endIndex = eLine.getNumber() < vc.getNumberOfLines()
                    ? vc.getLineInformation(eLine.getNumber()+1).getBeginOffset()
                    : eLine.getEndOffset();
            return new StartEndTextRange(
                    cs.newPositionForModelOffset(sLine.getBeginOffset()),
                    cs.newPositionForModelOffset(endIndex));
        } else {
            int startIndex = sLine.getNumber() < vc.getNumberOfLines()
                    ? vc.getLineInformation(sLine.getNumber()+1).getBeginOffset()
                    : sLine.getEndOffset();
            return new StartEndTextRange(
                    cs.newPositionForModelOffset(startIndex),
                    cs.newPositionForModelOffset(eLine.getBeginOffset()));
        }
//        TextRange range = new StartEndTextRange(from, to);
//        TextContent content = editorMode.getModelContent();
//        int start = range.getLeftBound().getModelOffset();
//        int end   = range.getRightBound().getModelOffset();
//        start = content.getLineInformationOfOffset(start).getBeginOffset();
//        end = content.getLineInformationOfOffset(end).getEndOffset() + 1;
//        end = min(end, content.getTextLength());
//        CursorService cs = editorMode.getCursorService();
//        return new StartEndTextRange(cs.newPositionForModelOffset(start), cs.newPositionForModelOffset(end));
    }

    public ContentType getContentType(Configuration configuration) {
        return ContentType.fromBorderPolicy(motion.borderPolicy());
    }

}
