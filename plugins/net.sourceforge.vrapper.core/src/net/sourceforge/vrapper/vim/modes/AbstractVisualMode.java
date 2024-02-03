package net.sourceforge.vrapper.vim.modes;

import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.changeCaret;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.convertKeyStroke;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.shiftKey;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafCtrlBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafState;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionState;

import net.sourceforge.vrapper.keymap.KeyMapInfo;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.CountConsumingKeyMapState;
import net.sourceforge.vrapper.keymap.vim.CountingState;
import net.sourceforge.vrapper.keymap.vim.RegisterState;
import net.sourceforge.vrapper.keymap.vim.VisualMotionState;
import net.sourceforge.vrapper.keymap.vim.VisualTextObjectState;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.VimConstants;
import net.sourceforge.vrapper.vim.commands.CenterLineCommand;
import net.sourceforge.vrapper.vim.commands.ChangeModeCommand;
import net.sourceforge.vrapper.vim.commands.ChangeOperation;
import net.sourceforge.vrapper.vim.commands.ChangeToVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.DeleteOperation;
import net.sourceforge.vrapper.vim.commands.FormatOperation;
import net.sourceforge.vrapper.vim.commands.InsertShiftWidth;
import net.sourceforge.vrapper.vim.commands.JoinVisualLinesCommand;
import net.sourceforge.vrapper.vim.commands.LeaveVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.PasteOperation;
import net.sourceforge.vrapper.vim.commands.PrintTextRangeInformation;
import net.sourceforge.vrapper.vim.commands.ReplaceCommand;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.SelectionBasedTextOperationCommand;
import net.sourceforge.vrapper.vim.commands.SetMarkCommand;
import net.sourceforge.vrapper.vim.commands.SwapCaseCommand;
import net.sourceforge.vrapper.vim.commands.VisualFindFileCommand;
import net.sourceforge.vrapper.vim.commands.VisualMotionCommand;
import net.sourceforge.vrapper.vim.commands.YankOperation;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.commands.motions.MoveDown;
import net.sourceforge.vrapper.vim.commands.motions.MoveLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveRight;
import net.sourceforge.vrapper.vim.commands.motions.MoveRightAcrossLines;
import net.sourceforge.vrapper.vim.commands.motions.MoveUp;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordEndLeft;
import net.sourceforge.vrapper.vim.commands.motions.SearchResultMotion;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;
import net.sourceforge.vrapper.vim.modes.commandline.CommandLineMode;

public abstract class AbstractVisualMode extends CommandBasedMode {

    public static final String NAME = "all visual modes";
    public static final String KEYMAP_NAME = "Visual Mode Keymap";

    public static final ModeSwitchHint FIX_SELECTION_HINT = new ModeSwitchHint() { };
    public static final ModeSwitchHint KEEP_SELECTION_HINT = new ModeSwitchHint() { };
    public static final ModeSwitchHint RECALL_SELECTION_HINT = new ModeSwitchHint() { };
    public static final ModeSwitchHint MOVE_CURSOR_HINT = new ModeSwitchHint() { };

    public AbstractVisualMode(final EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    protected KeyMapResolver buildKeyMapResolver() {
        final State<KeyMapInfo> state = union(
                editorAdaptor.getPlatformSpecificStateProvider().getKeyMaps(VisualMode.NAME));
        final State<KeyMapInfo> countEater = new CountConsumingKeyMapState(
                                                    KEYMAP_NAME, "innercount", state);
        final State<KeyMapInfo> registerKeymapState = new RegisterKeymapState(KEYMAP_NAME, countEater);
        final State<KeyMapInfo> outerCountEater = new CountConsumingKeyMapState(
                                                    KEYMAP_NAME, "outercount", registerKeymapState);
        return new KeyMapResolver(outerCountEater, KEYMAP_NAME);
    }

    @Override
    public void placeCursor(StickyColumnPolicy stickyColumnPolicy) {
        // Don't update caret after LeaveVisualCommand gets called.
        if (super.isEnabled) {
            fixCaret();
        }
    }

    @Override
    public void enterMode(final ModeSwitchHint... hints) throws CommandExecutionException {
        boolean fixSelection = false;
        boolean keepSelection = false;
        boolean recallSelection = false;
        ExecuteCommandHint onEnterCommand = null;
        for (final ModeSwitchHint hint: hints) {
            if (hint == FIX_SELECTION_HINT) {
            	keepSelection = true;
                fixSelection = true;
            }
            if (hint == KEEP_SELECTION_HINT) {
            	keepSelection = true;
            }
            if (hint == RECALL_SELECTION_HINT) {
            	recallSelection = true;
            }
            if (hint instanceof ExecuteCommandHint) {
                onEnterCommand = (ExecuteCommandHint)hint;
            }
        }
        if (recallSelection) {
            Selection previousSel = editorAdaptor.getLastActiveSelection();
            CursorService cursorService = editorAdaptor.getCursorService();
            Position from = cursorService.getMark(CursorService.INTERNAL_LAST_SELECT_FROM_MARK);
            Position to = cursorService.getMark(CursorService.INTERNAL_LAST_SELECT_TO_MARK);
            if (previousSel == null) {
                VrapperLog.info("Previous selection was null, selection not recalled.");
            } else {
                Selection updatedSel = updateSelection(editorAdaptor, previousSel, from, to);
                //Makes sure to set the sticky column.
                editorAdaptor.setPosition(updatedSel.getTo(), StickyColumnPolicy.ON_CHANGE);
                editorAdaptor.setSelection(updatedSel);
            }
        } else if (!keepSelection) {
            editorAdaptor.setSelection(null);
        }
        Selection currentSelection = editorAdaptor.getSelection();
        if (fixSelection && currentSelection != null) {
            editorAdaptor.setSelection(fixSelection(currentSelection));
        }
        super.enterMode(hints);
        if (onEnterCommand != null) {
            try {
                super.executeCommand(onEnterCommand.getCommand());
            } catch (final CommandExecutionException e) {
                editorAdaptor.getUserInterfaceService().setErrorMessage(e.getMessage());
            }
        }
        fixCaret();
    }

    private Selection updateSelection(EditorAdaptor editorAdaptor, Selection previousSel,
            Position from, Position to) {
        // Can happen if the user deleted a piece of text containing the mark or indented.
        // Also frequently happens during tests, as the stubbed mark service doesn't keep marks.
        if (from == null && to == null) {
            VrapperLog.info("Previous selection marks are null, selection might be wrong.");
        } else if (from == null) {
            VrapperLog.info("Previous selection 'from' mark is null, selection might be wrong.");
        } else if (to == null) {
            VrapperLog.info("Previous selection 'to' mark is null, selection might be wrong.");
        }
        if (from == null) {
            from = previousSel.getFrom();
        }
        if (to == null) {
            to = previousSel.getTo();
        }
        return previousSel.reset(editorAdaptor, from, to);
    }

    @Override
    public void leaveMode(final ModeSwitchHint... hints)
    		throws CommandExecutionException {
    	super.leaveMode(hints);
    }

    /**
     * Convert the current selection to one expected by the current mode.
     * For example, if a linewise selection is visible and the mode is switched to plain visual,
     * this function will update the selection so that single characters can be selected.
     * Vice versa, when switching to linewise mode, this function will make sure to select complete
     * lines if that weren't the case before.
     * @param selection Selection from which the <code>to</code> and <code>from</code> members
     *      should be used to create the new Selection. This method can check the content type
     *      to be sure that the selection needs fixing.
     * @return Selection which is now of the right type and length.
     */
    protected abstract Selection fixSelection(Selection selection);

    /**
     * Set the caret to the right type for the current selection.
     */
    public abstract void fixCaret();

    @Override
    protected State<Command> buildInitialState() {
        final Command leaveVisual = LeaveVisualModeCommand.INSTANCE;
        final Command yank   = new SelectionBasedTextOperationCommand(YankOperation.INSTANCE);
        final Command delete = new SelectionBasedTextOperationCommand(DeleteOperation.INSTANCE);
        final Command paste  = new SelectionBasedTextOperationCommand(PasteOperation.INSTANCE);
        final Command change = new SelectionBasedTextOperationCommand.DontChangeMode(ChangeOperation.INSTANCE);
        final Command format = new SelectionBasedTextOperationCommand(FormatOperation.INSTANCE);
        final Command shiftLeft = new SelectionBasedTextOperationCommand(InsertShiftWidth.REMOVE_VISUAL);
        final Command shiftRight = new SelectionBasedTextOperationCommand(InsertShiftWidth.INSERT_VISUAL);
        final Command swapCase = SwapCaseCommand.VISUAL_INSTANCE;
        final Command commandLineMode = new ChangeModeCommand(CommandLineMode.NAME, CommandLineMode.FROM_VISUAL);
        final Command centerLine = CenterLineCommand.CENTER;
        final Command centerBottomLine = CenterLineCommand.BOTTOM;
        final Command centerTopLine = CenterLineCommand.TOP;
        final Command joinLines = JoinVisualLinesCommand.INSTANCE;
        final Command joinLinesDumbWay = JoinVisualLinesCommand.DUMB_INSTANCE;
        final Command findFile = VisualFindFileCommand.INSTANCE;
        final Command printTextRangeInformation = PrintTextRangeInformation.INSTANCE;
        final State<Command> visualMotions = getVisualMotionState();
        final State<Command> visualTextObjects = new VisualTextObjectState(
                                        editorAdaptor.getTextObjectProvider());
        final State<Command> initialState = CountingState.wrap(
          RegisterState.wrap(
            CountingState.wrap(union(
                getPlatformSpecificState(NAME),
                state(
                leafBind(SpecialKey.ESC, leaveVisual),
                leafCtrlBind('c', leaveVisual),
                leafBind('y', yank),
                leafBind('s', change),
                leafBind('c', change),
                leafBind('C', change),
                leafBind('d', delete),
                leafBind('x', delete),
                leafBind('X', delete),
                leafBind(SpecialKey.DELETE, delete),
                leafBind('p', paste),
                leafBind('P', paste),
                leafBind('~', swapCase),
                leafBind('J', joinLines),
                leafBind(':', commandLineMode),
                leafBind('>', shiftRight),
                leafBind('<', shiftLeft),
                transitionBind('g',
                        leafCtrlBind('g', printTextRangeInformation),
                        // Always switch back to visual mode to show these text objects
                        leafBind('n', (Command) new ChangeToVisualModeCommand(VisualMode.NAME,
                                new VisualMotionCommand(SearchResultMotion.NEXT_END))),
                        leafBind('N', (Command) new ChangeToVisualModeCommand(VisualMode.NAME,
                                 new VisualMotionCommand(SearchResultMotion.PREVIOUS_BEGIN))),
                        leafBind('f', findFile),
                        leafBind('J', joinLinesDumbWay),
                        leafBind('q', format)),
                transitionBind('z',
                        leafBind('z', centerLine),
                        leafBind('.', centerLine),
                        leafBind('-', centerBottomLine),
                        leafBind('b', centerBottomLine),
                        leafBind('t', centerTopLine),
                        leafBind(SpecialKey.RETURN, centerTopLine)
                ),
                transitionBind('r', changeCaret(CaretType.UNDERLINE),
                        convertKeyStroke(
                                ReplaceCommand.Visual.VISUAL_KEYSTROKE,
                                VimConstants.PRINTABLE_KEYSTROKES)),
                transitionBind('m',
                        convertKeyStroke(
                                SetMarkCommand.KEYSTROKE_CONVERTER,
                                VimConstants.PRINTABLE_KEYSTROKES))
        ), visualMotions, visualTextObjects
        ))));
        return initialState;
    }

    protected VisualMotionState getVisualMotionState() {
        State<Motion> motions = union(
                leafState(' ', MoveRightAcrossLines.INSTANCE_BEHIND_CHAR),
                // Shift + Arrows use Vrapper motions rather than SWT handling to have sticky column
                leafState(shiftKey(SpecialKey.ARROW_UP), MoveUp.INSTANCE),
                leafState(shiftKey(SpecialKey.ARROW_DOWN), MoveDown.INSTANCE),
                leafState(shiftKey(SpecialKey.ARROW_LEFT), MoveLeft.INSTANCE),
                leafState(shiftKey(SpecialKey.ARROW_RIGHT), MoveRight.INSTANCE),
                transitionState('g',
                        state(
                            leafBind('e', MoveWordEndLeft.INSTANCE_VISUAL))),
                motions());
        return new VisualMotionState(motions);
    }
}
