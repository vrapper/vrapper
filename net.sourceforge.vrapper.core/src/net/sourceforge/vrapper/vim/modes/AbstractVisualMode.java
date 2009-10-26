package net.sourceforge.vrapper.vim.modes;

import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.convertKeyStroke;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.dontRepeat;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.seq;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.CountingState;
import net.sourceforge.vrapper.keymap.vim.RegisterState;
import net.sourceforge.vrapper.keymap.vim.VisualMotionState;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.VimConstants;
import net.sourceforge.vrapper.vim.commands.CenterLineCommand;
import net.sourceforge.vrapper.vim.commands.ChangeModeCommand;
import net.sourceforge.vrapper.vim.commands.ChangeOperation;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.DeleteOperation;
import net.sourceforge.vrapper.vim.commands.LeaveVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.SelectionBasedTextOperation;
import net.sourceforge.vrapper.vim.commands.SetMarkCommand;
import net.sourceforge.vrapper.vim.commands.YankOperation;

public abstract class AbstractVisualMode extends CommandBasedMode {

    public static final String KEYMAP_NAME = "Visual Mode Keymap";
    public static final ModeSwitchHint FIX_SELECTION_HINT = new ModeSwitchHint() { };

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
        final State<String> countEater = new CountConsumingState(state);
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

    public void enterMode(ModeSwitchHint... args) {
        if (isEnabled) {
            return;
        }
        isEnabled = true;
        boolean shouldDeselect = true;
        for (ModeSwitchHint hint: args)
            if (hint == FIX_SELECTION_HINT)
                shouldDeselect = false;
        if (shouldDeselect)
            editorAdaptor.setSelection(null);
        else if (editorAdaptor.getSelection() != null)
            fixSelection();
    }

    protected abstract void fixSelection();

    @Override
    public void leaveMode() {
        isEnabled = false;
    }

    @SuppressWarnings("unchecked")
    protected State<Command> createInitialState() {
        Command leaveVisual = LeaveVisualModeCommand.INSTANCE;
        Command yank   = dontRepeat(seq(new SelectionBasedTextOperation(YankOperation.INSTANCE), leaveVisual));
        Command delete = dontRepeat(seq(new SelectionBasedTextOperation(DeleteOperation.INSTANCE), leaveVisual));
        Command change = new SelectionBasedTextOperation(ChangeOperation.INSTANCE);
        Command commandLineMode = new ChangeModeCommand(CommandLineMode.NAME);
        Command centerLine = CenterLineCommand.INSTANCE;
        State<Command> visualMotions = getVisualMotionState();
        State<Command> initialState = new RegisterState(CountingState.wrap(union(state(
                leafBind(SpecialKey.ESC, leaveVisual),
                leafBind('y', yank),
                leafBind('s', change),
                leafBind('c', change),
                leafBind('d', delete),
                leafBind('x', delete),
                leafBind('X', delete),
                leafBind(':', commandLineMode),
                transitionBind('z',
                        leafBind('z', centerLine)),
                transitionBind('m',
                        convertKeyStroke(
                                SetMarkCommand.KEYSTROKE_CONVERTER,
                                VimConstants.PRINTABLE_KEYSTROKES))
        ), visualMotions,
        getPlatformSpecificState(VisualMode.NAME))));
        return initialState;
    }

    protected abstract VisualMotionState getVisualMotionState();

}