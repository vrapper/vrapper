package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.LineStartMotion;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

public class LineStartCommand extends CountAwareCommand {

    public static final LineStartCommand NON_WHITESPACE = new LineStartCommand(true);
    public static final LineStartCommand COLUMN0 = new LineStartCommand(false);

    private final LineStartMotion motion;

    private LineStartCommand(boolean goToFirstNonWS) {
        if (goToFirstNonWS) {
            this.motion = LineStartMotion.NON_WHITESPACE;
        } else {
            this.motion = LineStartMotion.COLUMN0;
        }
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor, int count) {
        if (count == NO_COUNT_GIVEN) {
            count = 1;
        }
        try {
            Position position = motion.destination(editorAdaptor, count);
            final CursorService cursorService = editorAdaptor.getCursorService();
            cursorService.setPosition(position, StickyColumnPolicy.NEVER);
            cursorService.stickToBOL();
        } catch (CommandExecutionException e) {
        }
    }

    @Override
    public CountAwareCommand repetition() {
        return null;
    }

}
