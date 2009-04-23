package net.sourceforge.vrapper.vim.modes;
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
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.EclipseMoveCommand;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
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

public abstract class CommandBasedMode extends AbstractMode {

    protected final State<Command> initialState;
    protected State<Command> currentState;
    private final KeyMapResolver keyMapResolver;

    public CommandBasedMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
        currentState = initialState = getInitialState();
        keyMapResolver = buildKeyMapResolver();
    }

    protected abstract State<Command> getInitialState();
    protected abstract void placeCursor();
    protected abstract KeyMapResolver buildKeyMapResolver();

    public State<Motion> motions() {
        final Motion moveLeft = new MoveLeft();
        final Motion moveRight = new MoveRight();
        final Motion moveUp = new MoveUp();
        final Motion moveDown = new MoveDown();
        final Motion findNext = new EclipseMoveCommand("org.eclipse.ui.edit.findNext", EXCLUSIVE);
        final Motion findPrevious = new EclipseMoveCommand("org.eclipse.ui.edit.findPrevious", EXCLUSIVE);
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
                leafBind('w', wordRight),
                leafBind('W', WORDRight),
                leafBind('e', wordEndRight),
                leafBind('E', WORDEndRight),
                leafBind('b', wordLeft),
                leafBind('B', WORDLeft),
                leafBind('G', go("textEnd",           LINE_WISE)),
                leafBind('n', findNext),
                leafBind('N', findPrevious),
                leafBind('0', column0),
                leafBind('$', lineEnd),
                leafBind('%', parenthesesMove),
                leafBind('^', lineStart),
                leafBind('(', javaGoTo("previous.member",   LINE_WISE)), // XXX: vim non-compatible; XXX: make java-agnostic
                leafBind(')', javaGoTo("next.member",       LINE_WISE)), // XXX: vim non-compatible; XXX: make java-agnostic
                //					leafBind(KEY("SHIFT+["), paragraphBackward), // '[' FIXME: doesn't worl
                //					leafBind(KEY("SHIFT+]"), paragraphForward),  // ']'
                transitionBind('g',
                        leafBind('g', go("textStart", LINE_WISE)),
                        leafBind('w', eclipseWordRight),
                        leafBind('b', eclipseWordLeft),
                        leafBind('e', wordEndLeft),
                        leafBind('E', WORDEndLeft)));
        return motions;
    }

    public void executeCommand(Command command) {
        try {
            if (!(command instanceof MotionCommand)) {
                editorAdaptor.getViewportService().setRepaint(false);
            }
            command.execute(editorAdaptor);
            Command repetition = command.repetition();
            if (repetition != null) {
                editorAdaptor.getRegisterManager().setLastEdit(repetition);
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
            currentState = initialState;
            keyMapResolver.reset();
        }

        Transition<Command> transition = currentState.press(keyStroke);
        keyMapResolver.press(keyStroke);
        if (transition != null) {
            Command command = transition.getValue();
            currentState = transition.getNextState();
            if (command != null) {
                executeCommand(command);
            }
        }
        if (transition == null || currentState == null) {
            currentState = initialState;
            keyMapResolver.reset();
            if (isEnabled) {
                commandDone();
            }
        }

        // FIXME: has some issues with sticky column
        placeCursor();

        return true;
    }

    public KeyMap resolveKeyMap(KeyMapProvider provider) {
        return provider.getKeyMap(keyMapResolver.getKeyMapName());
    }

    /**
     * this is a hook method which is called when command execution is done
     */
    // TODO: better name
    protected void commandDone() { }

}