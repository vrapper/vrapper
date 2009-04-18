package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.CountAwareMotion;

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
        EclipseCommand.doIt(count, motionName, editorAdaptor);
        Position newCarretOffset = editorAdaptor.getPosition();
        editorAdaptor.setPosition(oldCarretOffset, true);
        return newCarretOffset;
    }

    public boolean updateStickyColumn() {
        return true;
    }

    public Command command() {
        return new EclipseCommand(motionName);
    }
}
