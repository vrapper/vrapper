package net.sourceforge.vrapper.eclipse.commands;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.motions.CountAwareMotion;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

public class EclipseMoveCommand extends CountAwareMotion {

    private final String motionName;
    private final BorderPolicy borderPolicy;

    public EclipseMoveCommand(String motionName, BorderPolicy borderPolicy) {
        this.motionName = motionName;
        this.borderPolicy = borderPolicy;
    }

    public BorderPolicy borderPolicy() {
        return borderPolicy;
    }

    @Override
    public Position destination(EditorAdaptor editorAdaptor) {
        return destination(editorAdaptor, 1);
    }

    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count) {
        Position oldCarretOffset = editorAdaptor.getPosition();
        EclipseCommand.doIt(count, motionName, editorAdaptor, false);
        Position newCarretOffset = editorAdaptor.getPosition();
        editorAdaptor.setPosition(oldCarretOffset, StickyColumnPolicy.ON_CHANGE);
        return newCarretOffset;
    }

    public StickyColumnPolicy stickyColumnPolicy() {
        return StickyColumnPolicy.ON_CHANGE;
    }

    public Command command() {
        return new EclipseCommand(motionName);
    }
}
