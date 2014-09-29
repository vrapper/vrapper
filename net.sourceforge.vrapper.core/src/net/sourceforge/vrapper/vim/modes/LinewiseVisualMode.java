package net.sourceforge.vrapper.vim.modes;

import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.seq;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.VisualMotionState;
import net.sourceforge.vrapper.keymap.vim.VisualMotionState.Motion2VMC;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.ChangeModeCommand;
import net.sourceforge.vrapper.vim.commands.ChangeToSearchModeCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.LeaveVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.LineWiseSelection;
import net.sourceforge.vrapper.vim.commands.LinewiseVisualMotionCommand;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.SwapLinewiseSelectionSidesCommand;
import net.sourceforge.vrapper.vim.commands.motions.SearchResultMotion;

public class LinewiseVisualMode extends AbstractVisualMode {

    public static final String NAME = "linewise visual mode";
    public static final String DISPLAY_NAME = "VISUAL LINE";

    public LinewiseVisualMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    public void enterMode(ModeSwitchHint... args) throws CommandExecutionException {
        CaretType caret = CaretType.RECTANGULAR;
        if (editorAdaptor.getConfiguration().get(Options.SELECTION).equals(Selection.EXCLUSIVE))
            caret = CaretType.VERTICAL_BAR;
        editorAdaptor.getCursorService().setCaret(caret);
        super.enterMode(args);
    }

    @Override
    protected VisualMotionState getVisualMotionState() {
        return new VisualMotionState(Motion2VMC.LINEWISE, motions());
    }

    public String getName() {
        return NAME;
    }

    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected State<Command> buildInitialState() {
		Command exitSearchModeCommand = seq(
				new ChangeModeCommand(LinewiseVisualMode.NAME, LinewiseVisualMode.RECALL_SELECTION_HINT),
				new LinewiseVisualMotionCommand(SearchResultMotion.FORWARD));
        State<Command> linewiseSpecific = state(
                leafBind('o', (Command) SwapLinewiseSelectionSidesCommand.INSTANCE),
                leafBind('v', (Command) new ChangeModeCommand(VisualMode.NAME, FIX_SELECTION_HINT)),
                leafBind('V', (Command) LeaveVisualModeCommand.INSTANCE),
                leafBind('/', (Command) new ChangeToSearchModeCommand(false, exitSearchModeCommand, true)),
                leafBind('?', (Command) new ChangeToSearchModeCommand(true, exitSearchModeCommand, true))
                );
        return union(getPlatformSpecificState(NAME), linewiseSpecific, super.buildInitialState());
    }

    @Override
    protected void fixSelection() {
        Selection selection = editorAdaptor.getSelection();
        if (!selection.getContentType(editorAdaptor.getConfiguration()).equals(ContentType.LINES)) {
            Position start = selection.getStart();
            Position end = selection.getEnd();
            editorAdaptor.setSelection(new LineWiseSelection(editorAdaptor, start, end));
        }
    }

}
