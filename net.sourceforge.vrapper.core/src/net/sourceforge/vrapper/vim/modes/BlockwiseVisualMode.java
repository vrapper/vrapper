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
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.HistoryService;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.SelectionArea;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.VimConstants;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection.TextBlock;
import net.sourceforge.vrapper.vim.commands.BlockwiseYankCommand;
import net.sourceforge.vrapper.vim.commands.ChangeToInsertModeCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountAwareCommand;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.commands.ReplaceCommand;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.SwapCaseCommand;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.commands.motions.BlockSelectionMotion;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.register.Register;
import net.sourceforge.vrapper.vim.register.RegisterContent;
import net.sourceforge.vrapper.vim.register.StringRegisterContent;

public class BlockwiseVisualMode extends AbstractVisualMode {

    enum InsertModeType {
        INSERT,
        APPEND
    }

    private static class BlockwiseRepeatInsertCommand implements Command {

        final private InsertModeType mode;

        public static final Command INSERT_INSTANCE = new BlockwiseRepeatInsertCommand(InsertModeType.INSERT);
        public static final Command APPEND_INSTANCE = new BlockwiseRepeatInsertCommand(InsertModeType.APPEND);
        public static final Command REPEAT_INSERT_INSTANCE = new Repetition(InsertModeType.INSERT);
        public static final Command REPEAT_APPEND_INSTANCE = new Repetition(InsertModeType.APPEND);

        BlockwiseRepeatInsertCommand(InsertModeType mode) {
            this.mode = mode;
        }

        @Override
        public Command repetition() {
            return mode == InsertModeType.INSERT ? REPEAT_INSERT_INSTANCE : REPEAT_APPEND_INSTANCE;
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
            
            final SelectionArea sel = editorAdaptor.getRegisterManager().getLastActiveSelectionArea();
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
            final CursorService cursorService = editorAdaptor.getCursorService();
            //final Position newStart = editorAdaptor.getPosition().addModelOffset(-string.length() + 1);
            final Position newStart = cursorService.getMark(CursorService.LAST_CHANGE_START);
	        editorAdaptor.setPosition(newStart, false);
	        final TextRange region = sel.getRegion(editorAdaptor, NO_COUNT_GIVEN);
            final TextBlock block = BlockWiseSelection.getTextBlock(region.getStart(), region.getEnd(),
                    editorAdaptor.getModelContent(), cursorService);
            if (insertion != null) {
                for (int line = block.startLine + 1; line <= block.endLine; ++line) {
                    final Position newUl = cursorService.getPositionByVisualOffset(line, block.startVisualOffset);
                    if (newUl != null) {
                        editorAdaptor.setPosition(newUl, false);
                        insertion.execute(editorAdaptor);
                    }
                }
            }
	        editorAdaptor.setPosition(newStart, true);
            
            editorAdaptor.getRegisterManager().setLastEdit(repetition());
            finish(editorAdaptor);
        }
        
        private void finish(final EditorAdaptor editorAdaptor) {
            final HistoryService history = editorAdaptor.getHistory();
            history.unlock("block-action");
            history.endCompoundChange();
        }
        
    }

    static public class Repetition extends CountAwareCommand {

        final private InsertModeType mode;

        public Repetition(InsertModeType mode) {
            this.mode = mode;
        }

        @Override
        public void execute(final EditorAdaptor editorAdaptor, final int count)
                throws CommandExecutionException {
            final TextObject lastSelection = editorAdaptor.getLastActiveSelectionArea();
            if (lastSelection.getContentType(editorAdaptor.getConfiguration()) != ContentType.TEXT_RECTANGLE) {
                return;
            }
            final HistoryService history = editorAdaptor.getHistory();
            final Command insertion = editorAdaptor.getRegisterManager().getLastInsertion();
            final TextRange region = lastSelection.getRegion(editorAdaptor, count);
            final CursorService cursorService = editorAdaptor.getCursorService();
            final TextBlock block = BlockWiseSelection.getTextBlock(region.getStart(), region.getEnd(),
                    editorAdaptor.getModelContent(), cursorService);
            history.beginCompoundChange();
            history.lock("block-action");
            for (int line = block.startLine; line <= block.endLine; ++line) {
                Position newUl = cursorService.getPositionByVisualOffset(line, block.startVisualOffset);
                if (newUl != null) {
                    if (mode == InsertModeType.APPEND) {
                        newUl = newUl.addModelOffset(1);
                    }
                    editorAdaptor.setPosition(newUl, false);
                    insertion.execute(editorAdaptor);
                }
            }
            editorAdaptor.setPosition(
                    region.getStart().addModelOffset(
                            mode == InsertModeType.APPEND ? 1 : 0), true);
            history.unlock("block-action");
            history.endCompoundChange();
        }

        @Override
        public CountAwareCommand repetition() {
            return this;
        }

    }

    private static class BlockwiseChangeToInsertModeCommand extends
            ChangeToInsertModeCommand implements Command {

        final private InsertModeType mode;
        public BlockwiseChangeToInsertModeCommand(final Command command, InsertModeType mode) {
            super(command);
            this.mode = mode;
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
                              new ExecuteCommandHint.OnLeave(
                                      mode == InsertModeType.INSERT ?
                                        BlockwiseRepeatInsertCommand.INSERT_INSTANCE
                                      : BlockwiseRepeatInsertCommand.APPEND_INSTANCE),
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
                leafBind('I', (Command) new BlockwiseChangeToInsertModeCommand(new MotionCommand(bol), InsertModeType.INSERT)),
                leafBind('A', (Command) new BlockwiseChangeToInsertModeCommand(new MotionCommand(eol), InsertModeType.APPEND)),
                leafBind('~', swapCase),
                leafBind('y', (Command) BlockwiseYankCommand.INSTANCE),
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
