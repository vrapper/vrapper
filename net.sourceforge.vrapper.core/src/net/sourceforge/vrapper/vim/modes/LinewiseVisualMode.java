package net.sourceforge.vrapper.vim.modes;

import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.VisualMotionState;
import net.sourceforge.vrapper.keymap.vim.VisualMotionState.Motion2VMC;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.ChangeModeCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.LeaveVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.LineWiseSelection;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.SwapLinewiseSelectionSidesCommand;

public class LinewiseVisualMode extends AbstractVisualMode {

    public static final String NAME = "linewise visual mode";

    public LinewiseVisualMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    public void enterMode(ModeSwitchHint... args) {
        super.enterMode(args);
        CaretType caret = CaretType.RECTANGULAR;
        if (editorAdaptor.getConfiguration().get(Options.SELECTION).equals("exclusive"))
            caret = CaretType.VERTICAL_BAR;
        editorAdaptor.getCursorService().setCaret(caret);
    }

    @Override
    protected VisualMotionState getVisualMotionState() {
        return new VisualMotionState(Motion2VMC.LINEWISE, motions());
    }

    public String getName() {
        return NAME;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected State<Command> buildInitialState() {
        State<Command> linewiseSpecific = state(
                leafBind('o', (Command) SwapLinewiseSelectionSidesCommand.INSTANCE),
                leafBind('v', (Command) new ChangeModeCommand(VisualMode.NAME, FIX_SELECTION_HINT)),
                leafBind('V', (Command) LeaveVisualModeCommand.INSTANCE)
                );
        return union(linewiseSpecific, super.buildInitialState());
    }

    @Override
    protected void fixSelection() {
        Selection selection = editorAdaptor.getSelection();
        Position start = selection.getStart();
        Position end = selection.getEnd();
        editorAdaptor.setSelection(new LineWiseSelection(editorAdaptor, start, end));
    }

}
