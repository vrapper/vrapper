package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.Motion;

public class MotionPairTextObject extends AbstractTextObject {

    private final Motion toBeginning;
    private final Motion toEnd;
    private final boolean countToBeginning;

    public MotionPairTextObject(Motion toBeginning, Motion toEnd) {
        this.toBeginning = toBeginning;
        this.toEnd = toEnd;
        this.countToBeginning = false;
    }

    protected MotionPairTextObject(Motion toBeginning, Motion toEnd, boolean countToBeginning) {
        this.toBeginning = toBeginning;
        this.toEnd = toEnd;
        this.countToBeginning = countToBeginning;
    }

    public TextRange getRegion(EditorAdaptor editorMode, int count) throws CommandExecutionException {
        Motion leftMotion = countToBeginning ? toBeginning.withCount(count) : toBeginning;
        Position from = leftMotion.destination(editorMode);
        Position to = toEnd.withCount(count).destination(editorMode);
        if (toEnd.borderPolicy() == BorderPolicy.INCLUSIVE) {
            to = to.addModelOffset(1);
        }
        return new StartEndTextRange(from, to);
    }

    public ContentType getContentType(Configuration configuration) {
        return ContentType.TEXT;
    }

}
