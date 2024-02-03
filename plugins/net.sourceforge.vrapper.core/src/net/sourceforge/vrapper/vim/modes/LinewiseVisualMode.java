package net.sourceforge.vrapper.vim.modes;

import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafCtrlBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.ChangeToSearchModeCommand;
import net.sourceforge.vrapper.vim.commands.ChangeToVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.LeaveVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.LineWiseSelection;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.SwapLinewiseSelectionSidesCommand;
import net.sourceforge.vrapper.vim.commands.VisualMotionCommand;
import net.sourceforge.vrapper.vim.commands.motions.SearchResultMotion;

public class LinewiseVisualMode extends AbstractVisualMode {

    public static final String NAME = "linewise visual mode";
    public static final String DISPLAY_NAME = "VISUAL LINE";

    public LinewiseVisualMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    public void fixCaret() {
        // NOTE: we don't mirror (RECTANGULAR <-> LEFT_SHIFTED_RECTANGULAR) caret when 'selection'
        // is non-exclusive because LEFT_SHIFTED_RECTANGULAR would be invisible on empty lines.
        
        CaretType caret = CaretType.RECTANGULAR;
        if (editorAdaptor.getConfiguration().get(Options.SELECTION).equals(Selection.EXCLUSIVE))
            caret = CaretType.VERTICAL_BAR;
        editorAdaptor.getCursorService().setCaret(caret);
    }

    public String getName() {
        return NAME;
    }

    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    protected State<Command> buildInitialState() {
        Command doSearchCommand = new VisualMotionCommand(SearchResultMotion.REPEAT);
        State<Command> linewiseSpecific = state(
                leafBind('o', (Command) SwapLinewiseSelectionSidesCommand.INSTANCE),
                leafBind('v', (Command) new ChangeToVisualModeCommand(VisualMode.NAME)),
                leafBind('V', (Command) LeaveVisualModeCommand.INSTANCE),
                leafCtrlBind('v', (Command) new ChangeToVisualModeCommand(BlockwiseVisualMode.NAME)),
                leafCtrlBind('q', (Command) new ChangeToVisualModeCommand(BlockwiseVisualMode.NAME)),
                leafBind('/', (Command) new ChangeToSearchModeCommand(false, doSearchCommand, true)),
                leafBind('?', (Command) new ChangeToSearchModeCommand(true, doSearchCommand, true))
                );
        return union(getPlatformSpecificState(NAME), linewiseSpecific, super.buildInitialState());
    }

    @Override
    protected Selection fixSelection(Selection selection) {
        if (!selection.getContentType(editorAdaptor.getConfiguration()).equals(ContentType.LINES)) {
            Position start = selection.getFrom();
            Position end = selection.getTo();
            return new LineWiseSelection(editorAdaptor, start, end);
        } else {
            return selection;
        }
    }

}
