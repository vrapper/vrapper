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
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection;
import net.sourceforge.vrapper.vim.commands.ChangeToInsertModeCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.motions.BlockSelectionMotion;
import net.sourceforge.vrapper.vim.commands.motions.Motion;

public class BlockwiseVisualMode extends AbstractVisualMode {

    public static final String NAME = "block visual mode";
    public static final String DISPLAY_NAME = "BLOCK VISUAL";    

    public BlockwiseVisualMode(final EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public String getName() {
        return NAME;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected State<Command> buildInitialState() {
        final Motion bol = BlockSelectionMotion.COLUMN_START;
        final Motion eol = BlockSelectionMotion.COLUMN_END;
        
        final State<Command> parentState = super.buildInitialState();
        return union(parentState, state(
                leafBind('I', (Command) new ChangeToInsertModeCommand(new MotionCommand(bol))),
                leafBind('A', (Command) new ChangeToInsertModeCommand(new MotionCommand(eol)))
                ));
    }
    
    @Override
    public void enterMode(final ModeSwitchHint... args) throws CommandExecutionException {
        System.out.println("ENTER blockwise");
        final CaretType caret = CaretType.LEFT_SHIFTED_RECTANGULAR;
//        if (editorAdaptor.getConfiguration().get(Options.SELECTION).equals("exclusive"))
//            caret = CaretType.VERTICAL_BAR;
        editorAdaptor.getCursorService().setCaret(caret);
        super.enterMode(args);
    }
    
    @Override
    public void leaveMode(final ModeSwitchHint... hints)
            throws CommandExecutionException {
        System.out.println("LEAVE blockwise");
        editorAdaptor.setSelection(null);
        super.leaveMode(hints);
    }


    @Override
    protected void fixSelection() {
        System.out.println("fixSelection");
        final Selection selection = editorAdaptor.getSelection();
        Position start = selection.getStart();
        Position end = selection.getEnd();
        if (selection.isReversed())
            start = start.addModelOffset(-1);
        else
            end = end.addModelOffset(-1);
        editorAdaptor.setSelection(new BlockWiseSelection(editorAdaptor, start, end));
    }

    @Override
    protected VisualMotionState getVisualMotionState() {
        return new VisualMotionState(Motion2VMC.BLOCKWISE, motions());
    }

}
