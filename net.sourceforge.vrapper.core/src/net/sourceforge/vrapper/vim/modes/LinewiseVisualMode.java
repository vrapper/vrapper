package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.VisualMotionState;
import net.sourceforge.vrapper.keymap.vim.VisualMotionState.Motion2VMC;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;

public class LinewiseVisualMode extends AbstractVisualMode {

    public static final String NAME = "linewise visual mode";
    private static State<Command> initialState;

    public LinewiseVisualMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    public void enterMode(Object... args) {
        super.enterMode(args);
        editorAdaptor.getCursorService().setCaret(CaretType.RECTANGULAR);
    }

    @Override
    protected VisualMotionState getVisualMotionState() {
        return new VisualMotionState(Motion2VMC.LINEWISE, motions());
    }

    public String getName() {
        return NAME;
    }

    @Override
    protected State<Command> getInitialState() {
        if (initialState == null) {
            initialState = createInitialState();
        }
        return initialState;
    }

}
