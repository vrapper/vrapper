package net.sourceforge.vrapper.vim.modes;

import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.changeCaret;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.convertKeyStroke;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.VisualMotionState;
import net.sourceforge.vrapper.keymap.vim.VisualMotionState.Motion2VMC;
import net.sourceforge.vrapper.platform.HistoryService;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.PositionlessSelection;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.VimConstants;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection.Rect;
import net.sourceforge.vrapper.vim.commands.ChangeToInsertModeCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.commands.ReplaceCommand;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.SwapCaseCommand;
import net.sourceforge.vrapper.vim.commands.motions.BlockSelectionMotion;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.register.Register;
import net.sourceforge.vrapper.vim.register.RegisterContent;
import net.sourceforge.vrapper.vim.register.StringRegisterContent;

public class BlockwiseVisualMode extends AbstractVisualMode {
    
    private static class BlockwiseRepeatInsertCommand implements Command {

        public static final Command INSTANCE = new BlockwiseRepeatInsertCommand();

        @Override
        public Command repetition() {
            return this;
        }

        @Override
        public Command withCount(final int count) {
            return this;
        }

        @Override
        public int getCount() {
            return NO_COUNT_GIVEN;
        }

        @Override
        public void execute(final EditorAdaptor editorAdaptor)
                throws CommandExecutionException {
            
            final PositionlessSelection sel = editorAdaptor.getRegisterManager().getLastActiveSelection();
	        final Command insertion = editorAdaptor.getRegisterManager().getLastInsertion();
	        final Register lastEdit = editorAdaptor.getRegisterManager().getLastEditRegister();
	        final RegisterContent content = lastEdit.getContent();
	        if (!(content instanceof StringRegisterContent)) {
	            finish(editorAdaptor);
	            return;
	        }
	        
	        final StringRegisterContent stringInsert = (StringRegisterContent) content;
	        final String string = stringInsert.getText();
	        if (VimUtils.containsNewLine(string)) {
	            finish(editorAdaptor);
	            return; // don't repeat if there's a linebreak
	        }
	        
	        // re-position to beginning of insert
	        final Position newStart = editorAdaptor.getPosition().addModelOffset(-string.length() + 1);
	        editorAdaptor.setPosition(newStart, false);
            final Rect rect = BlockWiseSelection.getRect(editorAdaptor, sel);
            
	        if (insertion != null) {
    		    final int height = rect.height();
    		    for (int i=1; i < height; i++) {
    		        rect.top++;
    		        final Position newUl = rect.getULPosition(editorAdaptor);
    		        editorAdaptor.setPosition(newUl, false);
    		        insertion.execute(editorAdaptor);
    		    }
	        }
            
            finish(editorAdaptor);
        }
        
        private void finish(final EditorAdaptor editorAdaptor) {
            final HistoryService history = editorAdaptor.getHistory();
            history.unlock("block-action");
            history.endCompoundChange();
        }
        
    }

    private static class BlockwiseChangeToInsertModeCommand extends
            ChangeToInsertModeCommand implements Command {

        public BlockwiseChangeToInsertModeCommand(final Command command) {
            super(command);
        }

        @Override
        public void execute(final EditorAdaptor editorAdaptor, final int count)
                throws CommandExecutionException {
            
            if (command == null)
                throw new CommandExecutionException("Illegal state; command must not be null!");
            
            editorAdaptor.rememberLastActiveSelection();
    
		    final HistoryService history = editorAdaptor.getHistory();
		    history.beginCompoundChange();
		    history.lock("block-action");
      		editorAdaptor.changeMode(InsertMode.NAME, new ExecuteCommandHint.OnEnter(command),
                              new ExecuteCommandHint.OnLeave(BlockwiseRepeatInsertCommand.INSTANCE),
                              new WithCountHint(count));
        }
    }

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
        
        final Command swapCase = SwapCaseCommand.VISUALBLOCK_INSTANCE;
        
        final State<Command> parentState = super.buildInitialState();
        return union(state(
                leafBind('I', (Command) new BlockwiseChangeToInsertModeCommand(new MotionCommand(bol))),
                leafBind('A', (Command) new BlockwiseChangeToInsertModeCommand(new MotionCommand(eol))),
                leafBind('~', swapCase),
                transitionBind('r', changeCaret(CaretType.UNDERLINE),
                        convertKeyStroke(
                                ReplaceCommand.VisualBlock.VISUALBLOCK_KEYSTROKE,
                                VimConstants.PRINTABLE_KEYSTROKES_WITH_NL))
                ), parentState);
    }
    
    @Override
    public void enterMode(final ModeSwitchHint... args) throws CommandExecutionException {
        final CaretType caret = CaretType.LEFT_SHIFTED_RECTANGULAR;
//        if (editorAdaptor.getConfiguration().get(Options.SELECTION).equals("exclusive"))
//            caret = CaretType.VERTICAL_BAR;
        editorAdaptor.getCursorService().setCaret(caret);
        super.enterMode(args);
    }
    
    @Override
    public void leaveMode(final ModeSwitchHint... hints)
            throws CommandExecutionException {
        editorAdaptor.setSelection(null);
        super.leaveMode(hints);
    }


    @Override
    protected void fixSelection() {
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
