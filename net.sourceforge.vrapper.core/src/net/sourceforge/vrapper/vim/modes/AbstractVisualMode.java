package net.sourceforge.vrapper.vim.modes;

import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.convertKeyStroke;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafCtrlBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.CountingState;
import net.sourceforge.vrapper.keymap.vim.RegisterState;
import net.sourceforge.vrapper.keymap.vim.VisualMotionState;
import net.sourceforge.vrapper.keymap.vim.VisualTextObjectState;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.VimConstants;
import net.sourceforge.vrapper.vim.commands.CenterLineCommand;
import net.sourceforge.vrapper.vim.commands.ChangeModeCommand;
import net.sourceforge.vrapper.vim.commands.ChangeOperation;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.DeleteOperation;
import net.sourceforge.vrapper.vim.commands.JoinVisualLinesCommand;
import net.sourceforge.vrapper.vim.commands.LeaveVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.PasteOperation;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.SelectionBasedTextOperationCommand;
import net.sourceforge.vrapper.vim.commands.SetMarkCommand;
import net.sourceforge.vrapper.vim.commands.SwapCaseCommand;
import net.sourceforge.vrapper.vim.commands.YankOperation;

public abstract class AbstractVisualMode extends CommandBasedMode {

    public static final String NAME = "all visual modes";
    public static final String KEYMAP_NAME = "Visual Mode Keymap";

    public static final ModeSwitchHint FIX_SELECTION_HINT = new ModeSwitchHint() { };
    public static final ModeSwitchHint KEEP_SELECTION_HINT = new ModeSwitchHint() { };
    public static final ModeSwitchHint RECALL_SELECTION_HINT = new ModeSwitchHint() { };
    public static final ModeSwitchHint MOVE_CURSOR_HINT = new ModeSwitchHint() { };

    private Selection lastSelection;

    public AbstractVisualMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected KeyMapResolver buildKeyMapResolver() {
        State<String> state = union(
                state(
                    leafBind('z', KeyMapResolver.NO_KEYMAP)),
                getKeyMapsForMotions(),
                editorAdaptor.getPlatformSpecificStateProvider().getKeyMaps(VisualMode.NAME));
        final State<String> countEater = new CountConsumingState<String>(state);
        State<String> registerKeymapState = new RegisterKeymapState(KEYMAP_NAME, countEater);
        return new KeyMapResolver(registerKeymapState, KEYMAP_NAME);
    }

    @Override
    protected void placeCursor() {
    //        if (!isEnabled) {
    //            Position leftSidePosition = editorAdaptor.getSelection().getLeftBound();
    //            editorAdaptor.setPosition(leftSidePosition, false);
    //        }
        }

    public void enterMode(ModeSwitchHint... hints) throws CommandExecutionException {
        boolean fixSelection = false;
        boolean keepSelection = false;
        boolean recallSelection = false;
        for (ModeSwitchHint hint: hints) {
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
        }
        if (recallSelection) {
        	editorAdaptor.setSelection(lastSelection);
        } else if (!keepSelection) {
            editorAdaptor.setSelection(null);
        }
        if (fixSelection && editorAdaptor.getSelection() != null) {
            fixSelection();
        }
        super.enterMode(hints);
    }
    
    @Override
    public void leaveMode(ModeSwitchHint... hints)
    		throws CommandExecutionException {
    	lastSelection = editorAdaptor.getSelection();
    	super.leaveMode(hints);
    }

    protected abstract void fixSelection();

    @Override
    @SuppressWarnings("unchecked")
    protected State<Command> buildInitialState() {
        Command leaveVisual = LeaveVisualModeCommand.INSTANCE;
        Command yank   = new SelectionBasedTextOperationCommand(YankOperation.INSTANCE);
        Command delete = new SelectionBasedTextOperationCommand(DeleteOperation.INSTANCE);
        Command paste  = new SelectionBasedTextOperationCommand(PasteOperation.INSTANCE);
        Command change = new SelectionBasedTextOperationCommand.DontChangeMode(ChangeOperation.INSTANCE);
        Command swapCase = SwapCaseCommand.VISUAL_INSTANCE;
        Command commandLineMode = new ChangeModeCommand(CommandLineMode.NAME);
        Command centerLine = CenterLineCommand.CENTER;
        Command centerBottomLine = CenterLineCommand.BOTTOM;
        Command centerTopLine = CenterLineCommand.TOP;
        Command joinLines = JoinVisualLinesCommand.INSTANCE;
        Command joinLinesDumbWay = JoinVisualLinesCommand.DUMB_INSTANCE;
        State<Command> visualMotions = getVisualMotionState();
        State<Command> visualTextObjects = VisualTextObjectState.INSTANCE;
        State<Command> initialState = RegisterState.wrap(CountingState.wrap(union(
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
                transitionBind('g',
                        leafBind('J', joinLinesDumbWay)),
                transitionBind('z',
                        leafBind('z', centerLine),
                        leafBind('.', centerLine),
                        leafBind('-', centerBottomLine),
                        leafBind('b', centerBottomLine),
                        leafBind('t', centerTopLine),
                        leafBind(SpecialKey.RETURN, centerTopLine)
                ),
                transitionBind('m',
                        convertKeyStroke(
                                SetMarkCommand.KEYSTROKE_CONVERTER,
                                VimConstants.PRINTABLE_KEYSTROKES))
        ), visualMotions, visualTextObjects
        )));
        return initialState;
    }

    protected abstract VisualMotionState getVisualMotionState();

}