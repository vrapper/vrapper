package net.sourceforge.vrapper.vim.modes;

import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.VisualMotionState;
import net.sourceforge.vrapper.keymap.vim.VisualMotionState.Motion2VMC;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.ChangeModeCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.LeaveVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.SimpleSelection;
import net.sourceforge.vrapper.vim.commands.SwapSelectionSidesCommand;


public class VisualMode extends AbstractVisualMode {

    public static final String NAME = "visual mode";

    public VisualMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    public String getName() {
        return NAME;
    }

    @Override
    public void enterMode(ModeSwitchHint... args) {
        super.enterMode(args);
        CaretType caret = CaretType.LEFT_SHIFTED_RECTANGULAR;
        if (editorAdaptor.getConfiguration().get(Options.SELECTION).equals("exclusive"))
            caret = CaretType.VERTICAL_BAR;
        editorAdaptor.getCursorService().setCaret(caret);
    }
    
    @Override
    protected VisualMotionState getVisualMotionState() {
        return new VisualMotionState(Motion2VMC.CHARWISE, motions());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected State<Command> buildInitialState() {
        State<Command> characterwiseSpecific = state(
                leafBind('o', (Command) SwapSelectionSidesCommand.INSTANCE),
                leafBind('V', (Command) new ChangeModeCommand(LinewiseVisualMode.NAME, FIX_SELECTION_HINT)),
                leafBind('v', (Command) LeaveVisualModeCommand.INSTANCE)
                );
        return union(characterwiseSpecific, super.buildInitialState());
    }

    @Override
    protected void fixSelection() {
        Selection selection = editorAdaptor.getSelection();
        Position start = selection.getStart();
        Position end = selection.getEnd();
        if (selection.isReversed())
            start = start.addModelOffset(-1);
        else
            end = end.addModelOffset(-1);
        editorAdaptor.setSelection(new SimpleSelection(new StartEndTextRange(start, end)));
    }

}
