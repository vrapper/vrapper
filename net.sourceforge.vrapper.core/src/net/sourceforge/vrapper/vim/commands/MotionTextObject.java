package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.utils.ContentType;
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
        case EXCLUSIVE: return StartEndTextRange.exclusive(from, to);
        case INCLUSIVE: return StartEndTextRange.inclusive(from, to);
        case LINE_WISE: return StartEndTextRange.lines(editorMode, from, to);
        default:
            throw new RuntimeException("unsupported border policy: " + motion.borderPolicy());
        }
    }

    public ContentType getContentType(Configuration configuration) {
        return ContentType.fromBorderPolicy(motion.borderPolicy());
    }

}
