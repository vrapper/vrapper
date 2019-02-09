package net.sourceforge.vrapper.vim.commands.motions;

import static net.sourceforge.vrapper.vim.commands.Utils.characterType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;

public class MoveWordEndLeft extends MoveLeftWithBounds {
    
    private boolean isVisualMode = false;

    public MoveWordEndLeft(boolean bailOff) {
        super(bailOff);
    }

    public MoveWordEndLeft(boolean bailOff, boolean visual) {
        super(bailOff);
        this.isVisualMode = visual;
    }

    public static final MoveWordEndLeft INSTANCE = new MoveWordEndLeft(false);
    public static final MoveWordEndLeft BAILS_OFF = new MoveWordEndLeft(true);
    public static final Motion INSTANCE_VISUAL = new MoveWordEndLeft(true, true);

    @Override
    protected boolean atBoundary(char c1, char c2) {
        return !Character.isWhitespace(c1) && characterType(c1, keywords) != characterType(c2, keywords);
    }

    public BorderPolicy borderPolicy() {
        if (isVisualMode) {
            // XXX Undocumented in Vim, 'ge' behaves as exclusive in visual mode.
            return BorderPolicy.EXCLUSIVE;
        }
        return BorderPolicy.INCLUSIVE;
    }

    @Override
    protected boolean shouldStopAtLeftBoundingChar() {
        return true;
    }

    @Override
    protected boolean stopsAtNewlines() {
        return true;
    }

}
