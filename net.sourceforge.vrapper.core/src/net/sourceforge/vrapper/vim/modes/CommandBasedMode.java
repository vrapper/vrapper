package net.sourceforge.vrapper.vim.modes;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.convertKeyStroke;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import static net.sourceforge.vrapper.vim.commands.BorderPolicy.EXCLUSIVE;
import static net.sourceforge.vrapper.vim.commands.BorderPolicy.LINE_WISE;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.go;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.javaGoTo;
import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.Transition;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.KeyMapProvider;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.VimConstants;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.commands.SwitchRegisterCommand;
import net.sourceforge.vrapper.vim.commands.VimCommandSequence;
import net.sourceforge.vrapper.vim.commands.motions.ContinueFindingMotion;
import net.sourceforge.vrapper.vim.commands.motions.FindMotion;
import net.sourceforge.vrapper.vim.commands.motions.GoToLineMotion;
import net.sourceforge.vrapper.vim.commands.motions.LineEndMotion;
import net.sourceforge.vrapper.vim.commands.motions.LineStartMotion;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.commands.motions.MoveBigWORDEndLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveBigWORDEndRight;
import net.sourceforge.vrapper.vim.commands.motions.MoveBigWORDLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveBigWORDRight;
import net.sourceforge.vrapper.vim.commands.motions.MoveDown;
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
import net.sourceforge.vrapper.vim.commands.motions.ViewPortMotion.Type;
import net.sourceforge.vrapper.vim.register.RegisterManager;

public abstract class CommandBasedMode extends AbstractMode {

    protected final State<Command> initialState;
    protected State<Command> currentState;
    private final KeyMapResolver keyMapResolver;
    private final StringBuilder commandBuffer;

    public CommandBasedMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
        currentState = initialState = getInitialState();
        keyMapResolver = buildKeyMapResolver();
        commandBuffer = new StringBuilder();
    }

    protected abstract State<Command> getInitialState();
    protected abstract void placeCursor();
    protected abstract KeyMapResolver buildKeyMapResolver();

    public State<Motion> motions() {
        final Motion moveLeft = new MoveLeft();
        final Motion moveRight = new MoveRight();
        final Motion moveUp = new MoveUp();
        final Motion moveDown = new MoveDown();
//        final Motion findNext = new EclipseMoveCommand("org.eclipse.ui.edit.findNext", EXCLUSIVE);
//        final Motion findPrevious = new EclipseMoveCommand("org.eclipse.ui.edit.findPrevious", EXCLUSIVE);
        final Motion findNext = new SearchResultMotion(false);
        final Motion findPrevious = new SearchResultMotion(true);
        final Motion findWordNext = new WordSearchMotion(false);
        final Motion findWordPrevious = new WordSearchMotion(true);
        final Motion wordRight = new MoveWordRight();
        final Motion WORDRight = new MoveBigWORDRight();
        final Motion wordLeft = new MoveWordLeft();
        final Motion WORDLeft = new MoveBigWORDLeft();
        final Motion wordEndRight = new MoveWordEndRight();
        final Motion WORDEndRight = new MoveBigWORDEndRight();
        final Motion wordEndLeft = new MoveWordEndLeft();
        final Motion WORDEndLeft = new MoveBigWORDEndLeft();
        final Motion eclipseWordRight = go("wordNext", EXCLUSIVE);
        final Motion eclipseWordLeft  = go("wordPrevious", EXCLUSIVE);
        final Motion lineStart = new LineStartMotion(true);
        final Motion column0 = new LineStartMotion(false);
        final Motion lineEnd = new LineEndMotion(EXCLUSIVE); // NOTE: it's not INCLUSIVE; bug in Vim documentation
        final Motion parenthesesMove = new ParenthesesMove();
        final Motion findForward = new ContinueFindingMotion(false);
        final Motion findBackward = new ContinueFindingMotion(true);

        final Motion highMove = new ViewPortMotion(Type.HIGH);
        final Motion middleMove = new ViewPortMotion(Type.MIDDLE);
        final Motion lowMove = new ViewPortMotion(Type.LOW);
        @SuppressWarnings("unchecked")
        State<Motion> motions = state(
                leafBind('h', moveLeft),
                leafBind('j', moveDown),
                leafBind('k', moveUp),
                leafBind('l', moveRight),
                leafBind(SpecialKey.ARROW_LEFT,  moveLeft),
                leafBind(SpecialKey.ARROW_DOWN,  moveDown),
                leafBind(SpecialKey.ARROW_UP,    moveUp),
                leafBind(SpecialKey.ARROW_RIGHT, moveRight),
                leafBind(';', findForward),
                leafBind(',', findBackward),
                transitionBind('t', convertKeyStroke(
                        FindMotion.keyConverter(false, false),
                        VimConstants.PRINTABLE_KEYSTROKES)),
                transitionBind('T', convertKeyStroke(
                        FindMotion.keyConverter(false, true),
                        VimConstants.PRINTABLE_KEYSTROKES)),
                transitionBind('f', convertKeyStroke(
                        FindMotion.keyConverter(true, false),
                        VimConstants.PRINTABLE_KEYSTROKES)),
                transitionBind('F', convertKeyStroke(
                        FindMotion.keyConverter(true, true),
                        VimConstants.PRINTABLE_KEYSTROKES)),
                leafBind('w', wordRight),
                leafBind('W', WORDRight),
                leafBind('e', wordEndRight),
                leafBind('E', WORDEndRight),
                leafBind('b', wordLeft),
                leafBind('B', WORDLeft),
                leafBind('G', GoToLineMotion.LAST_LINE), // XXX: counts
                leafBind('H', highMove),
                leafBind('M', middleMove),
                leafBind('L', lowMove),
                leafBind('n', findNext),
                leafBind('N', findPrevious),
                leafBind('*', findWordNext),
                leafBind('#', findWordPrevious),
                leafBind('0', column0),
                leafBind('$', lineEnd),
                leafBind('%', parenthesesMove),
                leafBind('^', lineStart),
                leafBind('(', javaGoTo("previous.member",   LINE_WISE)), // XXX: vim non-compatible; XXX: make java-agnostic
                leafBind(')', javaGoTo("next.member",       LINE_WISE)), // XXX: vim non-compatible; XXX: make java-agnostic
                //					leafBind(KEY("SHIFT+["), paragraphBackward), // '[' FIXME: doesn't worl
                //					leafBind(KEY("SHIFT+]"), paragraphForward),  // ']'
                transitionBind('g',
                        leafBind('g', GoToLineMotion.FIRST_LINE),
                        leafBind('w', eclipseWordRight),
                        leafBind('b', eclipseWordLeft),
                        leafBind('e', wordEndLeft),
                        leafBind('E', WORDEndLeft)));
        return motions;
    }

    public void executeCommand(Command command) throws CommandExecutionException {
        try {
            if (!(command instanceof MotionCommand)) {
                editorAdaptor.getViewportService().setRepaint(false);
            }
            command.execute(editorAdaptor);
            Command repetition = command.repetition();
            if (repetition != null) {
                RegisterManager registerManager = editorAdaptor.getRegisterManager();
                if (!registerManager.isDefaultRegisterActive()) {
                    repetition = new VimCommandSequence(
                            new SwitchRegisterCommand(
                                        registerManager.getActiveRegister()),
                            repetition);
                }
               registerManager.setLastEdit(repetition);
            }
        } finally {
            editorAdaptor.getViewportService().setRepaint(true);
        }
    }


    public boolean handleKey(KeyStroke keyStroke) {
        if (editorAdaptor == null) {
            return false;
        }

        if (currentState == null) {
            VrapperLog.error("current state was null - this shouldn't have happened!");
            reset();
        }

        Transition<Command> transition = currentState.press(keyStroke);
        keyMapResolver.press(keyStroke);
        commandBuffer.append(keyStroke.getCharacter());
        editorAdaptor.getUserInterfaceService().setInfoMessage(commandBuffer.toString());
        if (transition != null) {
            Command command = transition.getValue();
            currentState = transition.getNextState();
            if (command != null) {
                try {
                    executeCommand(command);
                } catch (CommandExecutionException e) {
                    VrapperLog.info(e.getMessage());
                }
            }
        }
        if (transition == null || currentState == null) {
            reset();
            if (isEnabled) {
                commandDone();
            }
        }

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

    /**
     * this is a hook method which is called when command execution is done
     */
    // TODO: better name
    protected void commandDone() {
        commandBuffer.delete(0, commandBuffer.length());
        editorAdaptor.getUserInterfaceService().setInfoMessage("");
    }

    @SuppressWarnings("unchecked")
    protected State<String> getKeyMapsForMotions() {
        return state(
                leafBind('f', KeyMapResolver.NO_KEYMAP),
                leafBind('t', KeyMapResolver.NO_KEYMAP),
                leafBind('T', KeyMapResolver.NO_KEYMAP),
                leafBind('F', KeyMapResolver.NO_KEYMAP));
    }

}