package net.sourceforge.vrapper.vim.modes;

import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafCtrlBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;

import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.CountingState;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.ChangeToSearchModeCommand;
import net.sourceforge.vrapper.vim.commands.ChangeToVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.Command;
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
    public void fixCaret() {
        String selectionType = editorAdaptor.getConfiguration().get(Options.SELECTION);
        CaretType type;
        if (Selection.INCLUSIVE.equals(selectionType)) {
                type = CaretType.RECTANGULAR;
        } else if (Selection.EXCLUSIVE.equals(selectionType)) {
            type = CaretType.VERTICAL_BAR;
        } else {
            type = CaretType.RECTANGULAR;
        }
        editorAdaptor.getCursorService().setCaret(type);
    }

    @Override
    protected State<Command> buildInitialState() {
        Command doSearchCommand = new VisualMotionCommand(SearchResultMotion.REPEAT);
        State<Command> characterwiseSpecific = state(
                leafBind('o', (Command) SwapSelectionSidesCommand.INSTANCE),
                leafBind('V', (Command) new ChangeToVisualModeCommand(LinewiseVisualMode.NAME)),
                leafCtrlBind('v', (Command) new ChangeToVisualModeCommand(BlockwiseVisualMode.NAME)),
                leafCtrlBind('q', (Command) new ChangeToVisualModeCommand(BlockwiseVisualMode.NAME)),
                leafBind('v', (Command) LeaveVisualModeCommand.INSTANCE)
                );
        State<Command> searchSpecific = CountingState.wrap(state(
                leafBind('/',  (Command) new ChangeToSearchModeCommand(false, doSearchCommand, true)),
                leafBind('?',  (Command) new ChangeToSearchModeCommand(true, doSearchCommand, true))
                ));
        return union(getPlatformSpecificState(NAME), characterwiseSpecific, searchSpecific, super.buildInitialState());
    }

    @Override
    protected Selection fixSelection(Selection selection) {
        Position start = selection.getFrom();
        Position end = selection.getTo();
        TextRange range;
        if (Selection.INCLUSIVE.equals(editorAdaptor.getConfiguration().get(Options.SELECTION))) {
            range = StartEndTextRange.inclusive(editorAdaptor.getCursorService(), start, end);
        } else {
            range = new StartEndTextRange(start, end);
        }
        return new SimpleSelection(start, end, range);
    }

}
