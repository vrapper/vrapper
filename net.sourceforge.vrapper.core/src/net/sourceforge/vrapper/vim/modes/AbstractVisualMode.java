package net.sourceforge.vrapper.vim.modes;

import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.changeCaret;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.convertKeyStroke;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafCtrlBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
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
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.VimConstants;
import net.sourceforge.vrapper.vim.commands.CenterLineCommand;
import net.sourceforge.vrapper.vim.commands.ChangeModeCommand;
import net.sourceforge.vrapper.vim.commands.ChangeOperation;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.DeleteOperation;
import net.sourceforge.vrapper.vim.commands.FormatOperation;
import net.sourceforge.vrapper.vim.commands.InsertShiftWidth;
import net.sourceforge.vrapper.vim.commands.JoinVisualLinesCommand;
import net.sourceforge.vrapper.vim.commands.LeaveVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.PasteOperation;
import net.sourceforge.vrapper.vim.commands.ReplaceCommand;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.SelectionBasedTextOperationCommand;
import net.sourceforge.vrapper.vim.commands.SetMarkCommand;
import net.sourceforge.vrapper.vim.commands.SwapCaseCommand;
import net.sourceforge.vrapper.vim.commands.VisualFindFileCommand;
import net.sourceforge.vrapper.vim.commands.YankOperation;
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
    @SuppressWarnings("unchecked")
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
//        if (!isEnabled) {
//            Position leftSidePosition = editorAdaptor.getSelection().getLeftBound();
//            editorAdaptor.setPosition(leftSidePosition, false);
//        }
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
            Position start = cursorService.getMark(CursorService.LAST_SELECTION_START_MARK);
            Position end = cursorService.getMark(CursorService.LAST_SELECTION_END_MARK);
            if (previousSel == null) {
                VrapperLog.info("Previous selection was null, selection not recalled.");
            } else {
                Selection updatedSel = updateSelection(previousSel, start, end);
                editorAdaptor.setSelection(updatedSel);
            }
        } else if (!keepSelection) {
            editorAdaptor.setSelection(null);
        }
        if (fixSelection && editorAdaptor.getSelection() != null) {
            fixSelection();
        }
        super.enterMode(hints);
        if (onEnterCommand != null) {
            editorAdaptor.changeModeSafely(NormalMode.NAME, onEnterCommand);
        }
    }

    private Selection updateSelection(Selection previousSel, Position start,
            Position end) {
        if (previousSel.isReversed()
                && editorAdaptor.getConfiguration().get(Options.SELECTION)
                    .equals(Selection.INCLUSIVE)) {
            editorAdaptor.getCursorService().setCaret(CaretType.RECTANGULAR);
        }
        // Can happen if the user deleted a piece of text containing the mark or indented.
        // Also frequently happens during tests, as the stubbed mark service doesn't keep marks.
        if (start == null && end == null) {
            VrapperLog.info("Previous selection marks are null, selection might be wrong.");
        } else if (start == null) {
            VrapperLog.info("Previous selection start mark is null, selection might be wrong.");
        } else if (end == null) {
            VrapperLog.info("Previous selection end mark is null, selection might be wrong.");
        }
        if (start == null) {
            start = previousSel.getStartMark(editorAdaptor);
        }
        if (end == null) {
            end = previousSel.getEndMark(editorAdaptor);
        }
        return previousSel.selectMarks(editorAdaptor, start, end);
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
     */
    protected abstract void fixSelection();

    @Override
    @SuppressWarnings("unchecked")
    protected State<Command> buildInitialState() {
        final Command leaveVisual = LeaveVisualModeCommand.INSTANCE;
        final Command yank   = new SelectionBasedTextOperationCommand(YankOperation.INSTANCE);
        final Command delete = new SelectionBasedTextOperationCommand(DeleteOperation.INSTANCE);
        final Command paste  = new SelectionBasedTextOperationCommand(PasteOperation.INSTANCE);
        final Command change = new SelectionBasedTextOperationCommand.DontChangeMode(ChangeOperation.INSTANCE);
        final Command format = new SelectionBasedTextOperationCommand(FormatOperation.INSTANCE);
        final Command shiftLeft = new SelectionBasedTextOperationCommand(InsertShiftWidth.REMOVE);
        final Command shiftRight = new SelectionBasedTextOperationCommand(InsertShiftWidth.INSERT);
        final Command swapCase = SwapCaseCommand.VISUAL_INSTANCE;
        final Command commandLineMode = new ChangeModeCommand(CommandLineMode.NAME, CommandLineMode.FROM_VISUAL);
        final Command centerLine = CenterLineCommand.CENTER;
        final Command centerBottomLine = CenterLineCommand.BOTTOM;
        final Command centerTopLine = CenterLineCommand.TOP;
        final Command joinLines = JoinVisualLinesCommand.INSTANCE;
        final Command joinLinesDumbWay = JoinVisualLinesCommand.DUMB_INSTANCE;
        final Command findFile = VisualFindFileCommand.INSTANCE;
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
                leafBind('d', delete),
                leafBind('x', delete),
                leafBind('X', delete),
                leafBind('p', paste),
                leafBind('P', paste),
                leafBind('~', swapCase),
                leafBind('J', joinLines),
                leafBind(':', commandLineMode),
                leafBind('>', shiftRight),
                leafBind('<', shiftLeft),
                transitionBind('g',
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
                                VimConstants.PRINTABLE_KEYSTROKES_WITH_NL)),
                transitionBind('m',
                        convertKeyStroke(
                                SetMarkCommand.KEYSTROKE_CONVERTER,
                                VimConstants.PRINTABLE_KEYSTROKES))
        ), visualMotions, visualTextObjects
        ))));
        return initialState;
    }

    protected abstract VisualMotionState getVisualMotionState();

}
