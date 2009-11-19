package net.sourceforge.vrapper.vim.modes;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.convertKeyStroke;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import static net.sourceforge.vrapper.vim.commands.BorderPolicy.EXCLUSIVE;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.vrapper.keymap.EmptyState;
import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.Transition;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.KeyMapProvider;
import net.sourceforge.vrapper.platform.PlatformSpecificStateProvider;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.VimConstants;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.SwitchRegisterCommand;
import net.sourceforge.vrapper.vim.commands.VimCommandSequence;
import net.sourceforge.vrapper.vim.commands.motions.ContinueFindingMotion;
import net.sourceforge.vrapper.vim.commands.motions.FindMotion;
import net.sourceforge.vrapper.vim.commands.motions.GoToLineMotion;
import net.sourceforge.vrapper.vim.commands.motions.GoToMarkMotion;
import net.sourceforge.vrapper.vim.commands.motions.LineEndMotion;
import net.sourceforge.vrapper.vim.commands.motions.LineStartMotion;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.commands.motions.MoveBigWORDEndLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveBigWORDEndRight;
import net.sourceforge.vrapper.vim.commands.motions.MoveBigWORDLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveBigWORDRight;
import net.sourceforge.vrapper.vim.commands.motions.MoveDown;
import net.sourceforge.vrapper.vim.commands.motions.MoveDownReturn;
import net.sourceforge.vrapper.vim.commands.motions.MoveLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveRight;
import net.sourceforge.vrapper.vim.commands.motions.MoveUp;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordEndLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordEndRight;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordRight;
import net.sourceforge.vrapper.vim.commands.motions.ParenthesesMove;
import net.sourceforge.vrapper.vim.commands.motions.SearchResultMotion;
import net.sourceforge.vrapper.vim.commands.motions.ViewPortMotion;
import net.sourceforge.vrapper.vim.commands.motions.WordSearchMotion;
import net.sourceforge.vrapper.vim.register.RegisterManager;

public abstract class CommandBasedMode extends AbstractMode {

    private static State<Motion> motions;

    protected final State<Command> initialState;
    protected State<Command> currentState;
    private final KeyMapResolver keyMapResolver;
    private final StringBuilder commandBuffer;
    private static Map<String, State<Command>> initialStateCache = new HashMap<String, State<Command>>();

    public CommandBasedMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
        currentState = initialState = getInitialState();
        keyMapResolver = buildKeyMapResolver();
        commandBuffer = new StringBuilder();
    }

    protected abstract void placeCursor();
    protected abstract State<Command> buildInitialState();
    protected abstract KeyMapResolver buildKeyMapResolver();

    public State<Command> getInitialState() {
        String key = getName();
        PlatformSpecificStateProvider platformSpecificStateProvider = editorAdaptor.getPlatformSpecificStateProvider();
        if (platformSpecificStateProvider != null)
            key += " for " + platformSpecificStateProvider.getName();
        if (!initialStateCache.containsKey(key))
            initialStateCache.put(key, buildInitialState());
        return initialStateCache.get(key);
    }

    @SuppressWarnings("unchecked")
    public static State<Motion> motions() {
        if (motions == null) {
            final Motion moveLeft = MoveLeft.INSTANCE;
            final Motion moveRight = MoveRight.INSTANCE;
            final Motion moveUp = MoveUp.INSTANCE;
            final Motion moveDown = MoveDown.INSTANCE;
            final Motion moveDownReturn = MoveDownReturn.INSTANCE;
            // final Motion findNext = new
            // EclipseMoveCommand("org.eclipse.ui.edit.findNext", EXCLUSIVE);
            // final Motion findPrevious = new
            // EclipseMoveCommand("org.eclipse.ui.edit.findPrevious",
            // EXCLUSIVE);
            final Motion findNext = SearchResultMotion.FORWARD;
            final Motion findPrevious = SearchResultMotion.BACKWARD;
            final Motion findWordNext = WordSearchMotion.FORWARD;
            final Motion findWordPrevious = WordSearchMotion.BACKWARD;
            final Motion wordRight = MoveWordRight.INSTANCE;
            final Motion WORDRight = MoveBigWORDRight.INSTANCE;
            final Motion wordLeft = MoveWordLeft.INSTANCE;
            final Motion WORDLeft = MoveBigWORDLeft.INSTANCE;
            final Motion wordEndRight = MoveWordEndRight.INSTANCE;
            final Motion WORDEndRight = MoveBigWORDEndRight.INSTANCE;
            final Motion wordEndLeft = MoveWordEndLeft.INSTANCE;
            final Motion WORDEndLeft = MoveBigWORDEndLeft.INSTANCE;
            // TODO: move this to eclipse module
            // final Motion eclipseWordRight = go("wordNext", EXCLUSIVE);
            // final Motion eclipseWordLeft = go("wordPrevious", EXCLUSIVE);
            final Motion lineStart = LineStartMotion.NON_WHITESPACE;
            final Motion column0 = LineStartMotion.COLUMN0;
            final Motion lineEnd = new LineEndMotion(EXCLUSIVE); // NOTE: it's
                                                                 // not
                                                                 // INCLUSIVE;
                                                                 // bug in Vim
                                                                 // documentation
            final Motion parenthesesMove = new ParenthesesMove();
            final Motion findForward = ContinueFindingMotion.NORMAL;
            final Motion findBackward = ContinueFindingMotion.REVERSE;

            final Motion highMove = ViewPortMotion.HIGH;
            final Motion middleMove = ViewPortMotion.MIDDLE;
            final Motion lowMove = ViewPortMotion.LOW;

            motions = state(leafBind('h', moveLeft), leafBind('j', moveDown),
                    leafBind(SpecialKey.RETURN, moveDownReturn), leafBind('k',
                            moveUp), leafBind('l', moveRight), leafBind(' ',
                            moveRight), leafBind(SpecialKey.ARROW_LEFT,
                            moveLeft),
                    leafBind(SpecialKey.ARROW_DOWN, moveDown), leafBind(
                            SpecialKey.ARROW_UP, moveUp), leafBind(
                            SpecialKey.ARROW_RIGHT, moveRight), leafBind(';',
                            findForward), leafBind(',', findBackward),
                    transitionBind('t', convertKeyStroke(FindMotion
                            .keyConverter(false, false),
                            VimConstants.PRINTABLE_KEYSTROKES)),
                    transitionBind('T', convertKeyStroke(FindMotion
                            .keyConverter(false, true),
                            VimConstants.PRINTABLE_KEYSTROKES)),
                    transitionBind('f', convertKeyStroke(FindMotion
                            .keyConverter(true, false),
                            VimConstants.PRINTABLE_KEYSTROKES)),
                    transitionBind('F', convertKeyStroke(FindMotion
                            .keyConverter(true, true),
                            VimConstants.PRINTABLE_KEYSTROKES)),
                    transitionBind('\'', convertKeyStroke(
                            GoToMarkMotion.LINEWISE_CONVERTER,
                            VimConstants.PRINTABLE_KEYSTROKES)),
                    transitionBind('`', convertKeyStroke(
                            GoToMarkMotion.CHARWISE_CONVERTER,
                            VimConstants.PRINTABLE_KEYSTROKES)), leafBind('w',
                            wordRight), leafBind('W', WORDRight),
                    leafBind('e', wordEndRight),
                    leafBind('E', WORDEndRight),
                    leafBind('b', wordLeft),
                    leafBind('B', WORDLeft),
                    leafBind('G', GoToLineMotion.LAST_LINE), // XXX: counts
                    leafBind('H', highMove), leafBind('M', middleMove),
                    leafBind('L', lowMove), leafBind('n', findNext), leafBind(
                            'N', findPrevious), leafBind('*', findWordNext),
                    leafBind('#', findWordPrevious), leafBind('0', column0),
                    leafBind('$', lineEnd), leafBind('%', parenthesesMove),
                    leafBind('^', lineStart),
                    // leafBind('(', javaGoTo("previous.member", LINE_WISE)), //
                    // XXX: vim non-compatible; XXX: make java-agnostic
                    // leafBind(')', javaGoTo("next.member", LINE_WISE)), //
                    // XXX: vim non-compatible; XXX: make java-agnostic
                    // leafBind(KEY("SHIFT+["), paragraphBackward), // '['
                    // FIXME: doesn't worl
                    // leafBind(KEY("SHIFT+]"), paragraphForward), // ']'
                    transitionBind('g',
                            leafBind('g', GoToLineMotion.FIRST_LINE),
                            // leafBind('w', eclipseWordRight),
                            // leafBind('b', eclipseWordLeft),
                            leafBind('e', wordEndLeft), leafBind('E',
                                    WORDEndLeft)));
        }
        return motions;
    }

    public void executeCommand(Command command)
            throws CommandExecutionException {
        command.execute(editorAdaptor);
        Command repetition = command.repetition();
        if (repetition != null) {
            RegisterManager registerManager = editorAdaptor
                    .getRegisterManager();
            if (!registerManager.isDefaultRegisterActive()) {
                repetition = new VimCommandSequence(new SwitchRegisterCommand(
                        registerManager.getActiveRegister()), repetition);
            }
            registerManager.setLastEdit(repetition);
            editorAdaptor.getCursorService().setMark(
                    CursorService.LAST_EDIT_MARK, editorAdaptor.getPosition());
        }
    }

    public boolean handleKey(KeyStroke keyStroke) {
        if (editorAdaptor == null) {
            return false;
        }

        if (currentState == null) {
            VrapperLog
                    .error("current state was null - this shouldn't have happened!");
            reset();
        }

        Transition<Command> transition = currentState.press(keyStroke);
        keyMapResolver.press(keyStroke);
        commandBuffer.append(keyStroke.getCharacter());
        if (transition != null) {
            Command command = transition.getValue();
            currentState = transition.getNextState();
            if (command != null) {
                try {
                    executeCommand(command);
                } catch (CommandExecutionException e) {
                    editorAdaptor.getUserInterfaceService().setErrorMessage(
                            e.getMessage());
                }
            }
        }
        if (transition == null || currentState == null) {
            reset();
            if (isEnabled) {
                commandDone();
            }
        }

        editorAdaptor.getUserInterfaceService().setInfoMessage(
                commandBuffer.toString());

        // FIXME: has some issues with sticky column
        placeCursor();

        return true;
    }

    private void reset() {
        currentState = initialState;
        keyMapResolver.reset();
    }

    public KeyMap resolveKeyMap(KeyMapProvider provider) {
        return provider.getKeyMap(keyMapResolver.getKeyMapName());
    }

    public void leaveMode(ModeSwitchHint... hints) {
        resetCommandBuffer();
    }

    /**
     * this is a hook method which is called when command execution is done
     */
    // TODO: better name
    protected void commandDone() {
        resetCommandBuffer();
    }

    private void resetCommandBuffer() {
        commandBuffer.delete(0, commandBuffer.length());
        editorAdaptor.getUserInterfaceService().setInfoMessage("");
    }

    @SuppressWarnings("unchecked")
    protected State<String> getKeyMapsForMotions() {
        return state(leafBind('f', KeyMapResolver.NO_KEYMAP), leafBind('t',
                KeyMapResolver.NO_KEYMAP), leafBind('T',
                KeyMapResolver.NO_KEYMAP), leafBind('F',
                KeyMapResolver.NO_KEYMAP));
    }

    protected State<Command> getPlatformSpecificState(String mode) {
        State<Command> platformSpecificState = editorAdaptor
                .getPlatformSpecificStateProvider().getState(mode);
        if (platformSpecificState == null) {
            platformSpecificState = EmptyState.getInstance();
        }
        return platformSpecificState;
    }

}