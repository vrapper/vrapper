package net.sourceforge.vrapper.vim.modes;

import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.seq;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.CountingState;
import net.sourceforge.vrapper.keymap.vim.VisualMotionState;
import net.sourceforge.vrapper.keymap.vim.VisualMotionState.Motion2VMC;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.ChangeModeCommand;
import net.sourceforge.vrapper.vim.commands.ChangeToSearchModeCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.LeaveVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.SimpleSelection;
import net.sourceforge.vrapper.vim.commands.SwapSelectionSidesCommand;
import net.sourceforge.vrapper.vim.commands.VisualMotionCommand;
import net.sourceforge.vrapper.vim.commands.motions.SearchResultMotion;


public class VisualMode extends AbstractVisualMode {

    public static final String NAME = "visual mode";
    public static final String DISPLAY_NAME = "VISUAL";    

    public VisualMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    public String getName() {
        return NAME;
    }

    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public void enterMode(ModeSwitchHint... args) throws CommandExecutionException {
        CaretType caret = CaretType.LEFT_SHIFTED_RECTANGULAR;
        if (editorAdaptor.getConfiguration().get(Options.SELECTION).equals("exclusive"))
            caret = CaretType.VERTICAL_BAR;
        editorAdaptor.getCursorService().setCaret(caret);
        super.enterMode(args);
    }

    @Override
    protected VisualMotionState getVisualMotionState() {
        return new VisualMotionState(Motion2VMC.CHARWISE, motions());
    }

    @Override
    @SuppressWarnings("unchecked")
    protected State<Command> buildInitialState() {
		Command exitSearchModeCommand = seq(
				new ChangeModeCommand(VisualMode.NAME, VisualMode.RECALL_SELECTION_HINT),
				new VisualMotionCommand(SearchResultMotion.FORWARD));
		State<Command> characterwiseSpecific = state(
                leafBind('o', (Command) SwapSelectionSidesCommand.INSTANCE),
                leafBind('V', (Command) new ChangeModeCommand(LinewiseVisualMode.NAME, FIX_SELECTION_HINT)),
                leafBind('v', (Command) LeaveVisualModeCommand.INSTANCE)
                );
		State<Command> searchSpecific = CountingState.wrap(state(
                leafBind('/',  (Command) new ChangeToSearchModeCommand(false, exitSearchModeCommand)),
                leafBind('?',  (Command) new ChangeToSearchModeCommand(true, exitSearchModeCommand))
				));
        return union(getPlatformSpecificState(NAME), characterwiseSpecific, searchSpecific, super.buildInitialState());
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
