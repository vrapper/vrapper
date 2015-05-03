package net.sourceforge.vrapper.vim.modes;

import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionState;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafCtrlBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafState;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.CountingState;
import net.sourceforge.vrapper.keymap.vim.VisualMotionState;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.ChangeModeCommand;
import net.sourceforge.vrapper.vim.commands.ChangeToSearchModeCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.LeaveVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.SimpleSelection;
import net.sourceforge.vrapper.vim.commands.SwapSelectionSidesCommand;
import net.sourceforge.vrapper.vim.commands.VisualMotionCommand;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.commands.motions.MoveRightAcrossLines;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordEndLeft;
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
            if (editorAdaptor.getSelection().isReversed()) {
                type = CaretType.RECTANGULAR;
            } else {
                Selection selection = editorAdaptor.getSelection();
                int to = selection.getTo().getModelOffset();
                TextContent modelContent = editorAdaptor.getModelContent();
                LineInformation li = modelContent.getLineInformationOfOffset(to);
                if (to == li.getEndOffset() && to < modelContent.getTextLength()) {
                    type = CaretType.RECTANGULAR;
                } else {
                    type = CaretType.LEFT_SHIFTED_RECTANGULAR;
                }
            }
        } else if (Selection.EXCLUSIVE.equals(selectionType)) {
            type = CaretType.VERTICAL_BAR;
        } else {
            type = CaretType.RECTANGULAR;
        }
        editorAdaptor.getCursorService().setCaret(type);
    }

    @Override
    protected VisualMotionState getVisualMotionState() {
        @SuppressWarnings("unchecked")
        State<Motion> motions = union(
                leafState(' ', MoveRightAcrossLines.INSTANCE_BEHIND_CHAR),
                transitionState('g',
                        state(
                            leafBind('e', MoveWordEndLeft.INSTANCE_VISUAL),
                            leafBind('n', SearchResultMotion.NEXT_END),
                            leafBind('N', SearchResultMotion.PREVIOUS_BEGIN))),
                motions());
        return new VisualMotionState(motions);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected State<Command> buildInitialState() {
        Command doSearchCommand = new VisualMotionCommand(SearchResultMotion.REPEAT);
        State<Command> characterwiseSpecific = state(
                leafBind('o', (Command) SwapSelectionSidesCommand.INSTANCE),
                leafBind('V', (Command) new ChangeModeCommand(LinewiseVisualMode.NAME, FIX_SELECTION_HINT)),
                leafCtrlBind('v', (Command) new ChangeModeCommand(BlockwiseVisualMode.NAME, FIX_SELECTION_HINT)),
                leafCtrlBind('q', (Command) new ChangeModeCommand(BlockwiseVisualMode.NAME, FIX_SELECTION_HINT)),
                leafBind('v', (Command) LeaveVisualModeCommand.INSTANCE)
                );
        State<Command> searchSpecific = CountingState.wrap(state(
                leafBind('/',  (Command) new ChangeToSearchModeCommand(false, doSearchCommand, true)),
                leafBind('?',  (Command) new ChangeToSearchModeCommand(true, doSearchCommand, true))
                ));
        return union(getPlatformSpecificState(NAME), characterwiseSpecific, searchSpecific, super.buildInitialState());
    }

    @Override
    protected void fixSelection() {
        Selection selection = editorAdaptor.getSelection();
        Position start = selection.getFrom();
        Position end = selection.getTo();
        TextRange range;
        if (Selection.INCLUSIVE.equals(editorAdaptor.getConfiguration().get(Options.SELECTION))) {
            range = StartEndTextRange.inclusive(editorAdaptor.getCursorService(), start, end);
        } else {
            range = new StartEndTextRange(start, end);
        }
        editorAdaptor.setSelection(new SimpleSelection(start, end, range));
    }

}
